package com.boyitech.logstream.worker.indexer;


import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.MetafieldHelper;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import org.apache.commons.lang.StringEscapeUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

/**
 * @author juzheng
 * @date 2019/3/24 09:38
 * @Description: TODO
 */
public class NmapNmapAllV1Indexer extends BaseIndexer {

    private  static Document doc;
    public NmapNmapAllV1Indexer(BaseWorkerConfig config) {
        super(config);
    }

    public NmapNmapAllV1Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
    }

    @Override
    public boolean register() {
        doc=null;
        return true;
    }

    @Override
    public void tearDown() {
    }

    @Override
    public boolean format(Event event) {
        String message = event.getMessage();  //此处message返回的是一个字符串格式的xml日志；
        Map<String, Object> format = event.getFormat();
        format.put("message", message);
        if (message == null) {
            format.put("flag", "解析失败，message为空");
            return false;
        }
        if(message!=null){
            //读取xml，封装对象
            try {
                doc = DocumentHelper.parseText(message);  //message存储得是xml格式的字符串，此处将其封装成文档对象；
            } catch (DocumentException e) {
                e.printStackTrace();
            }
            Element root = doc.getRootElement();//首先获得最底层的根节点
            String version = root.attribute("version").getText();//获得根节点的version字段的值
            format.put("version",version);
            String arg = root.attribute("args").getText();//获得根节点的args字段的值
            arg=StringEscapeUtils.unescapeJava(arg);//把里面多\\反转义
            format.put("arg",arg);
            Iterator<Element> hosts = root.elementIterator("host");//获得根节点下面的host节点的元素存到一个迭代器里
            /*<根节点>
            * <host starttime="4323121">
            *     <status state="up"></status>
            *     <address></address>
            *     ...
            * </host>
            * </根节点>
            */
            while (hosts.hasNext()) {
                Element host = hosts.next();//这里就相当于一直往下读这个host迭代器的内容；
                String starttime = host.attribute("starttime").getText() + "000";//获得host节点的starttime字段值
                String endtime = host.attribute("endtime").getText() + "000";//获得host节点的endtime字段值
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");//时间格式化成ISO-8601格式
                String formatStartTime = sdf.format(new Date(Long.valueOf(starttime)));
                String formatEndTime = sdf.format(new Date(Long.valueOf(endtime)));
                format.put("formatStartTime",formatStartTime);
                format.put("formatEndTime",formatEndTime);

                //status
                Element status = host.element("status");//现在在host节点下， 获得host节点下的status节点
                String hostState = status.attribute("state").getText();//获得status节点的state字段的值；
                String reason = status.attribute("reason").getText();//获得status节点的reason字段的值
                String reason_ttl = status.attribute("reason_ttl").getText();//获得status节点的reason_ttl字段的值
                format.put("hostState",hostState);
                format.put("reason",reason);
                format.put("reason_ttl",reason_ttl);
                //address
                Iterator<Element> addresses = host.elementIterator("address");//获得根节点下的hots节点下的address节点
                String macHost = null;
                String vendor = null;
                String ipv4Host = null;
                while (addresses.hasNext()) {
                    Element address = addresses.next();
                    String addrtype = address.attribute("addrtype").getText();
                    if (addrtype.equals("ipv4")) {
                        ipv4Host = address.attribute("addr").getText();
                        format.put("ipv4Host",ipv4Host);
                    } else {
                        macHost = address.attribute("addr").getText();
                        vendor = address.attribute("vendor") != null ? address.attribute("vendor").getText() : null;
                        format.put("macHost",macHost);
                        format.put("vendor",vendor);

                    }
                }

                Element runstats = root.element("runstats");
                Element finished = runstats.element("finished");
                String elapsed = finished.attribute("elapsed").getText();
                String summary = finished.attribute("summary").getText();
                String exit = finished.attribute("exit").getText();
                format.put("elapsed",elapsed);
                format.put("exit",exit);
                format.put("summary",summary);
                //ports
                Element portsAll = host.element("ports");
                Iterator<Element> ports = portsAll.elementIterator("port");
                while (ports.hasNext()) {
                    Element port = ports.next();
                    String protocol = port.attribute("protocol").getText();
                    String portid = port.attribute("portid").getText();
                    Element state = port.element("state");
                    String portState = state.attribute("state").getText();
                    String portreason = state.attribute("reason").getText();
                    String portReasonTtl = state.attribute("reason_ttl").getText();
                    Element service = port.element("service");
                    String name = service.attribute("name").getText();
                    String method = service.attribute("method").getText();
                    String conf = service.attribute("conf").getText();
                    String product = service.attribute("product") != null ? service.attribute("product").getText() : null;
                    format.put("protocol",protocol);
                    format.put("portid",portid);
                    format.put("portState",portState);
                    format.put("portreason",portreason);
                    format.put("portReasonTtl",portReasonTtl);
                    format.put("name",name);
                    format.put("method",method);
                    format.put("conf",conf);
                    format.put("product",product);
                }
            }
        }


        //格式化Metafield
        event.setMetafieldLoglevel("1");
        // format.put("Metafield_category", "Security");
//        if (event.getLogType() != null) {
//            format.put("Metafield_description", event.getLogType());
//            format.put("Metafield_type", event.getLogType());
//        }
//        if (event.getSource() != null) {
//            format.put("Metafield_source", event.getSource());
//        }
//
//        if (format.get("ipv4Host") != null) {
//            format.put("Metafield_object", format.get("ipv4Host"));
//        }
      //  format.put("Metafield_subject", event.getSource());
        MetafieldHelper.setMetafield(event,"ipv4Host","","",format);


        if (format.get("flag") == "解析失败")
            return false;
        return true;
    }


    public static Map getMapping() {
        String mapping = "{\"properties\":{"
                + "\"@timestamp\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
                + "\"received_at\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
                + "\"portedAt\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
                + "\"flag\":{\"type\":\"keyword\"},"
                + "\"message\":{\"type\":\"text\"},"
                + "\"arg\": {\"type\": \"text\"},"
                + "\"conf\": {\"type\": \"integer\"},"
                + "\"elapsed\": {\"type\": \"float\"},"
                + "\"exit\": {\"type\": \"keyword\"},"
                + "\"formatEndTime\": {\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
                + "\"formatStartTime\": {\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
                + "\"hostState\": {\"type\": \"keyword\"},"
                + "\"ipv4Host\": {\"type\": \"ip\"},"
                + "\"macHost\": {\"type\": \"keyword\"},"
                + "\"method\": {\"type\": \"keyword\"},"
                + "\"name\": {\"type\": \"keyword\"},"
                + "\"portid\": {\"type\": \"integer\"},"
                + "\"portReasonTtl\": {\"type\": \"long\"},"
                + "\"portState\": {\"type\": \"keyword\"},"
                + "\"portreason\": {\"type\": \"keyword\"},"
                + "\"product\": {\"type\": \"keyword\"},"
                + "\"protocol\": {\"type\": \"keyword\"},"
                + "\"reason\": {\"type\": \"keyword\"},"
                + "\"reason_ttl\":{\"type\":\"keyword\"},"
                + "\"summary\":{\"type\":\"text\"},"
                + "\"vendor\":{\"type\":\"keyword\"},"
                + "\"version\":{\"type\":\"keyword\"},"
                + "\"scan_id\":{\"type\":\"keyword\"},"
                + "\"Metafield_type\":{\"type\":\"keyword\"},"
                + "\"Metafield_category\":{\"type\":\"keyword\"},"
                + "\"Metafield_subject\":{\"type\":\"keyword\"},"
                + "\"Metafield_object\":{\"type\":\"keyword\"},"
                + "\"Metafield_loglevel\":{\"type\":\"keyword\"},"
                + "\"Metafield_source\":{\"type\":\"keyword\"},"
                + "\"Metafield_description\":{\"type\":\"text\", \"fields\": {\"raw\": {\"type\": \"keyword\"}}}"
                + "}"
                + "}";
        return GsonHelper.fromJson(mapping);
    }

    public static String getMappingString() {
        String mapping = "{\"properties\":{"
                + "\"@timestamp\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
                + "\"received_at\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
                + "\"portedAt\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
                + "\"flag\":{\"type\":\"keyword\"},"
                + "\"message\":{\"type\":\"text\"},"
                + "\"arg\": {\"type\": \"text\"},"
                + "\"conf\": {\"type\": \"integer\"},"
                + "\"elapsed\": {\"type\": \"float\"},"
                + "\"exit\": {\"type\": \"keyword\"},"
                + "\"formatEndTime\": {\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
                + "\"formatStartTime\": {\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
                + "\"hostState\": {\"type\": \"keyword\"},"
                + "\"ipv4Host\": {\"type\": \"ip\"},"
                + "\"macHost\": {\"type\": \"keyword\"},"
                + "\"method\": {\"type\": \"keyword\"},"
                + "\"name\": {\"type\": \"keyword\"},"
                + "\"portid\": {\"type\": \"integer\"},"
                + "\"portReasonTtl\": {\"type\": \"long\"},"
                + "\"portState\": {\"type\": \"keyword\"},"
                + "\"portreason\": {\"type\": \"keyword\"},"
                + "\"product\": {\"type\": \"keyword\"},"
                + "\"protocol\": {\"type\": \"keyword\"},"
                + "\"reason\": {\"type\": \"keyword\"},"
                + "\"reason_ttl\":{\"type\":\"keyword\"},"
                + "\"summary\":{\"type\":\"text\"},"
                + "\"vendor\":{\"type\":\"keyword\"},"
                + "\"version\":{\"type\":\"keyword\"},"
                + "\"scan_id\":{\"type\":\"keyword\"},"
                + "\"Metafield_type\":{\"type\":\"keyword\"},"
                + "\"Metafield_category\":{\"type\":\"keyword\"},"
                + "\"Metafield_subject\":{\"type\":\"keyword\"},"
                + "\"Metafield_object\":{\"type\":\"keyword\"},"
                + "\"Metafield_loglevel\":{\"type\":\"keyword\"},"
                + "\"Metafield_source\":{\"type\":\"keyword\"},"
                + "\"Metafield_description\":{\"type\":\"text\", \"fields\": {\"raw\": {\"type\": \"keyword\"}}}"
                + "}"
                + "}";
        return mapping;
    }
}
