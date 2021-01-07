package com.boyitech.logstream.worker.indexer;


import com.alibaba.fastjson.JSONObject;
import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.GrokUtil;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.IndexerTimeUtils;
import com.boyitech.logstream.core.util.MetafieldHelper;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import com.boyitech.logstream.core.worker.indexer.BaseIndexerConfig;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.Iterator;
import java.util.Map;

/**
 * @author: juzheng
 * @Title: SangforAccesscontrolAllV1Indexer
 * @date: 2019-08-05T14:24:35.707
 * @Description: 1. 此indexer文件根据indexer通用模版创建
 * 2.为完成电机学院和博世华域的深信服ac日志的解析需求；
 * 3.实际参考 博世华域的版本来解析的；
 * 4.此版本收到的日志为syslog外发的json格式的字符串，经过预处理得差不多了；
 * 5.主要解析result字段；原日志为压缩过的xml但实际过来的时候基本已被解析得差不多了
 */
public class SangforAccesscontrolAllV1Indexer extends BaseIndexer {
    private static Document doc;
    private BaseIndexerConfig config;


    public SangforAccesscontrolAllV1Indexer(BaseWorkerConfig config) {
        super(config);
        this.config= (BaseIndexerConfig) config;

    }

    public SangforAccesscontrolAllV1Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
        this.config= (BaseIndexerConfig) config;

    }

    @Override
    public boolean register() {
        //此处注册正确的正则+grok
        doc = null;
        return true;
    }

    @Override
    public boolean format(Event event) {
        //最外层，格式化日志主体
        String messageJson = event.getMessage();//此次获得的是一个json格式的字符串
       try {
           if (GrokUtil.isJSONValid(messageJson) == true) {//还是要验证一下
               Map mapType = JSONObject.parseObject(messageJson);
               Map<String, Object> format = event.getFormat();
               format.putAll(mapType);

               JSONObject pa = JSONObject.parseObject(messageJson);
               String result = pa.getString("result");
               String logType = pa.getString("type");
               if (GrokUtil.isStringHasValue(logType)) {
                   format.put("log_type", logType);
               }
               format.replace("type", "sangfor-acsql");
               if (GrokUtil.isStringHasValue(result)) {
                   try {
                       doc = DocumentHelper.parseText(result);
                       Element root = doc.getRootElement();//获得根结点
                       Iterator rootIt = root.elementIterator();//遍历根结点
                       while (rootIt.hasNext()) {
                           Element element = (Element) rootIt.next();
                           format.put(element.attribute(0).getValue(), element.getText());
                       }
                   } catch (DocumentException e) {
                       addException(e.getMessage() + " message:" + messageJson);
                       LOGGER.error(e.getMessage() + " message:" + messageJson);
                       // e.printStackTrace();
                   }
               }

               //格式化时间，样本：
               // IndexerTimeUtils.getISO8601Time2(format, "", "");

               if (!config.getIpFilter().equals("null")) {
                   GrokUtil.filterGeoIP(config, format);
               } else {
                   //使用GeoIP对src_ip、dst_ip或部分特殊字段处理，
                   GrokUtil.setGeoIP2(format, "dst_ip");
                   GrokUtil.setGeoIP2(format, "hst_ip");
               }

               //格式化Metafield ，可能需要根基实际日志格式替换部分字段名如client_ip/src_ip、dst_ip
               event.setMetafieldLoglevel("2");
               if (event.getSource() != null) {
                   format.put("log_source", event.getSource());//增加来源设备标识；
               }

               if (format.get("hst_ip") != null && format.get("dst_ip") != null) {
                   format.put("ip_addr_pair", format.get("hst_ip") + "=>" + format.get("dst_ip"));
               }
               MetafieldHelper.setMetafield(event, "hst_ip", "dst_ip", "", format);
               if (format.get("flag") == "解析失败")
                   return false;


           } else {
               LOGGER.error("message格式错误:" + messageJson);
           }
       }
       catch (Exception ex){
        LOGGER.error(ex+"message:"+messageJson);
        addException(ex+"message:"+messageJson);
       }
        return true;
    }

    @Override
    public void tearDown() {
    }

    //上传的Mapping，要在下面两处空格处加上对应的Mapping字段；
    public static Map getMapping() {
        //language=JSON
        String mapping = "{\"properties\":{\"@timestamp\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},\"received_at\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},\"portedAt\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},\"flag\":{\"type\":\"keyword\"},\"message\":{\"type\":\"keyword\"},\"@version\":{\"type\":\"keyword\"},\"app\":{\"type\":\"keyword\"},\"customer\":{\"type\":\"keyword\"},\"dev_id\":{\"type\":\"long\"},\"dst_ip\":{\"type\":\"ip\"},\"format_level\":{\"type\":\"keyword\"},\"group\":{\"type\":\"keyword\"},\"host\":{\"type\":\"keyword\"},\"hst_ip\":{\"type\":\"ip\"},\"hst_ip_bin\":{\"type\":\"keyword\"},\"ip_version\":{\"type\":\"keyword\"},\"log_class\":{\"type\":\"keyword\"},\"log_class_index\":{\"type\":\"keyword\"},\"ntime\":{\"type\":\"long\"},\"record_id\":{\"type\":\"keyword\"},\"record_time\":{\"type\":\"keyword\"},\"result\":{\"type\":\"text\"},\"seconds\":{\"type\":\"long\"},\"serv\":{\"type\":\"keyword\"},\"site\":{\"type\":\"keyword\"},\"table_tag\":{\"type\":\"keyword\"},\"tags\":{\"type\":\"keyword\"},\"timestamp\":{\"type\":\"keyword\"},\"tm_type\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"user\":{\"type\":\"keyword\"},\"usr_name\":{\"type\":\"keyword\"},\"log_type\":{\"type\":\"keyword\"},\"host_ip\":{\"type\":\"ip\"},\"host_ip_bin\":{\"type\":\"keyword\"},\"dst_ip_bin\":{\"type\":\"keyword\"},\"src_port\":{\"type\":\"keyword\"},\"serv_port\":{\"type\":\"keyword\"},\"net_action\":{\"type\":\"keyword\"},\"channel_path\":{\"type\":\"keyword\"},\"line_no\":{\"type\":\"keyword\"},\"up_flux\":{\"type\":\"keyword\"},\"down_flux\":{\"type\":\"keyword\"},\"active_user\":{\"type\":\"keyword\"},\"total_user\":{\"type\":\"keyword\"},\"identify\":{\"type\":\"keyword\"},\"site_id\":{\"type\":\"keyword\"},\"login_time\":{\"type\":\"keyword\"},\"usr_count\":{\"type\":\"keyword\"},\"http_type\":{\"type\":\"keyword\"},\"dst_ip_geoip\":{\"properties\":{\"location\":{\"type\":\"geo_point\"},\"city_name\":{\"type\":\"keyword\"},\"continent_code\":{\"type\":\"keyword\"},\"country_code2\":{\"type\":\"keyword\"},\"country_code3\":{\"type\":\"keyword\"},\"country_name\":{\"type\":\"keyword\"},\"dma_code\":{\"type\":\"keyword\"},\"ip\":{\"type\":\"ip\"},\"latitude\":{\"type\":\"float\"},\"longitude\":{\"type\":\"float\"},\"postal_code\":{\"type\":\"keyword\"},\"region_name\":{\"type\":\"keyword\"},\"timezone\":{\"type\":\"keyword\"}}},\"hst_ip_geoip\":{\"properties\":{\"location\":{\"type\":\"geo_point\"},\"city_name\":{\"type\":\"keyword\"},\"continent_code\":{\"type\":\"keyword\"},\"country_code2\":{\"type\":\"keyword\"},\"country_code3\":{\"type\":\"keyword\"},\"country_name\":{\"type\":\"keyword\"},\"dma_code\":{\"type\":\"keyword\"},\"ip\":{\"type\":\"ip\"},\"latitude\":{\"type\":\"float\"},\"longitude\":{\"type\":\"float\"},\"postal_code\":{\"type\":\"keyword\"},\"region_name\":{\"type\":\"keyword\"},\"timezone\":{\"type\":\"keyword\"}}},\"Metafield_type\":{\"type\":\"keyword\"},\"Metafield_category\":{\"type\":\"keyword\"},\"Metafield_subject\":{\"type\":\"keyword\"},\"Metafield_object\":{\"type\":\"keyword\"},\"Metafield_loglevel\":{\"type\":\"keyword\"},\"Metafield_source\":{\"type\":\"keyword\"},\"Metafield_description\":{\"type\":\"keyword\",\"fields\":{\"raw\":{\"type\":\"keyword\"}}}},\"dynamic_templates\":[{\"prefix_match\":{\"match\":\"*\",\"mapping\":{\"type\":\"keyword\"}}}]}";
        return GsonHelper.fromJson(mapping);
    }

    public static String getMappingString() {
        String mapping = "{\"properties\":{\"@timestamp\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},\"received_at\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},\"portedAt\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},\"flag\":{\"type\":\"keyword\"},\"message\":{\"type\":\"keyword\"},\"@version\":{\"type\":\"keyword\"},\"app\":{\"type\":\"keyword\"},\"customer\":{\"type\":\"keyword\"},\"dev_id\":{\"type\":\"long\"},\"dst_ip\":{\"type\":\"ip\"},\"format_level\":{\"type\":\"keyword\"},\"group\":{\"type\":\"keyword\"},\"host\":{\"type\":\"keyword\"},\"hst_ip\":{\"type\":\"ip\"},\"hst_ip_bin\":{\"type\":\"keyword\"},\"ip_version\":{\"type\":\"keyword\"},\"log_class\":{\"type\":\"keyword\"},\"log_class_index\":{\"type\":\"keyword\"},\"ntime\":{\"type\":\"long\"},\"record_id\":{\"type\":\"keyword\"},\"record_time\":{\"type\":\"keyword\"},\"result\":{\"type\":\"text\"},\"seconds\":{\"type\":\"long\"},\"serv\":{\"type\":\"keyword\"},\"site\":{\"type\":\"keyword\"},\"table_tag\":{\"type\":\"keyword\"},\"tags\":{\"type\":\"keyword\"},\"timestamp\":{\"type\":\"keyword\"},\"tm_type\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"user\":{\"type\":\"keyword\"},\"usr_name\":{\"type\":\"keyword\"},\"log_type\":{\"type\":\"keyword\"},\"host_ip\":{\"type\":\"ip\"},\"host_ip_bin\":{\"type\":\"keyword\"},\"dst_ip_bin\":{\"type\":\"keyword\"},\"src_port\":{\"type\":\"keyword\"},\"serv_port\":{\"type\":\"keyword\"},\"net_action\":{\"type\":\"keyword\"},\"channel_path\":{\"type\":\"keyword\"},\"line_no\":{\"type\":\"keyword\"},\"up_flux\":{\"type\":\"keyword\"},\"down_flux\":{\"type\":\"keyword\"},\"active_user\":{\"type\":\"keyword\"},\"total_user\":{\"type\":\"keyword\"},\"identify\":{\"type\":\"keyword\"},\"site_id\":{\"type\":\"keyword\"},\"login_time\":{\"type\":\"keyword\"},\"usr_count\":{\"type\":\"keyword\"},\"http_type\":{\"type\":\"keyword\"},\"dst_ip_geoip\":{\"properties\":{\"location\":{\"type\":\"geo_point\"},\"city_name\":{\"type\":\"keyword\"},\"continent_code\":{\"type\":\"keyword\"},\"country_code2\":{\"type\":\"keyword\"},\"country_code3\":{\"type\":\"keyword\"},\"country_name\":{\"type\":\"keyword\"},\"dma_code\":{\"type\":\"keyword\"},\"ip\":{\"type\":\"ip\"},\"latitude\":{\"type\":\"float\"},\"longitude\":{\"type\":\"float\"},\"postal_code\":{\"type\":\"keyword\"},\"region_name\":{\"type\":\"keyword\"},\"timezone\":{\"type\":\"keyword\"}}},\"hst_ip_geoip\":{\"properties\":{\"location\":{\"type\":\"geo_point\"},\"city_name\":{\"type\":\"keyword\"},\"continent_code\":{\"type\":\"keyword\"},\"country_code2\":{\"type\":\"keyword\"},\"country_code3\":{\"type\":\"keyword\"},\"country_name\":{\"type\":\"keyword\"},\"dma_code\":{\"type\":\"keyword\"},\"ip\":{\"type\":\"ip\"},\"latitude\":{\"type\":\"float\"},\"longitude\":{\"type\":\"float\"},\"postal_code\":{\"type\":\"keyword\"},\"region_name\":{\"type\":\"keyword\"},\"timezone\":{\"type\":\"keyword\"}}},\"Metafield_type\":{\"type\":\"keyword\"},\"Metafield_category\":{\"type\":\"keyword\"},\"Metafield_subject\":{\"type\":\"keyword\"},\"Metafield_object\":{\"type\":\"keyword\"},\"Metafield_loglevel\":{\"type\":\"keyword\"},\"Metafield_source\":{\"type\":\"keyword\"},\"Metafield_description\":{\"type\":\"keyword\",\"fields\":{\"raw\":{\"type\":\"keyword\"}}}},\"dynamic_templates\":[{\"prefix_match\":{\"match\":\"*\",\"mapping\":{\"type\":\"keyword\"}}}]}";
        return mapping;
    }
}