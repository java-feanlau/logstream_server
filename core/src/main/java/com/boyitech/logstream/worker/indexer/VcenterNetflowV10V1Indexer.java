package com.boyitech.logstream.worker.indexer;


import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.GrokUtil;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.MetafieldHelper;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import com.boyitech.logstream.core.worker.indexer.BaseIndexerConfig;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Eric

 * @date 2018/12/19 16:41
 * @Description: TODO
 */
public class VcenterNetflowV10V1Indexer extends BaseIndexer {

    private final static String WRONGIP = "0.0.0.0";

    private BaseIndexerConfig config;


    public VcenterNetflowV10V1Indexer(BaseWorkerConfig config) {
        super(config);
        this.config= (BaseIndexerConfig) config;

    }

    public VcenterNetflowV10V1Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
        this.config= (BaseIndexerConfig) config;

    }

    @Override
    public boolean format(Event event) {
        String message = event.getMessage();
        Map allMap = GsonHelper.fromJson(message);
//        System.out.println(allMap);
        String srcAddr = (String) allMap.get("srcAddr");
        String dstAddr = (String) allMap.get("dstAddr");
        if(!isIPv4(srcAddr) || !isIPv4(srcAddr)){
            allMap.put("srcAddr",WRONGIP);
            allMap.put("dstAddr",WRONGIP);
            allMap.put("srcAddrIPv6",srcAddr);
            allMap.put("dstAddrIPv6",dstAddr);
        }
        Map<String, Object> format = event.getFormat();
        format.put("message", message);
        format.putAll(allMap);


        if (allMap.get("startTime") != null) {
            format.put("@timestamp", allMap.get("startTime"));
        } else if (allMap.get("endTime") != null) {
            format.put("@timestamp", allMap.get("endTime"));
        } else {
            String pattern = "yyyy-MM-dd'T'HH:mm:ssXXX";
            String format1 = DateFormatUtils.format(new Date(), pattern);
            format.put("@timestamp", format1);
        }

        if (!config.getIpFilter().equals("null")) {
            GrokUtil.filterGeoIP(config, format);
        } else {
            GrokUtil.setGeoIP2(format,"srcAddr");
            GrokUtil.setGeoIP2(format,"dstAddr");

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
//        if (format.get("srcAddr") != null) {
//            format.put("Metafield_object", format.get("srcAddr"));
//        }
//        if (format.get("dstAddr") != null) {
//            format.put("Metafield_subject", format.get("dstAddr"));
//        } else {
//            format.put("Metafield_subject", event.getSource());
        //}
        MetafieldHelper.setMetafield(event,"srcAddr","dstAddr","",format);

        if (format.get("flag") == "解析失败")
            return false;
        return true;
    }

    @Override
    public void tearDown() {

    }

    public boolean isIPv4(String addr)
    {
        if(addr.length() < 7 || addr.length() > 15 || "".equals(addr))
        {
            return false;
        }
        /**
         * 判断IP格式和范围
         */
        String rexp = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";

        Pattern pat = Pattern.compile(rexp);

        Matcher mat = pat.matcher(addr);

        boolean ipAddress = mat.find();

        return ipAddress;
    }

    public static Map getMapping() {
        String mapping = "{\"properties\":{"
                + "\"@timestamp\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
                + "\"received_at\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
                + "\"portedAt\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
                + "\"flag\":{\"type\":\"keyword\"},"
                + "\"message\":{\"type\":\"text\"},"
                + "\"direction\": {\"type\": \"integer\"},"
                + "\"dstAddr\": {\"type\": \"ip\"},"
                + "\"dstPort\": {\"type\": \"integer\"},"
                + "\"endTime\": {\"type\": \"date\"},"
                + "\"flowEndReason\": {\"type\": \"integer\"},"
                + "\"inputInt\": {\"type\": \"integer\"},"
                + "\"ipToS\": {\"type\": \"integer\"},"
                + "\"maxTTL\": {\"type\": \"integer\"},"
                + "\"octets\": {\"type\": \"integer\"},"
                + "\"outputInt\": {\"type\": \"integer\"},"
                + "\"packets\": {\"type\": \"integer\"},"
                + "\"protocol\": {\"type\": \"integer\"},"
                + "\"segmentId\": {\"type\": \"long\"},"
                + "\"setId\": {\"type\": \"integer\"},"
                + "\"srcAddr\": {\"type\": \"ip\"},"
                + "\"srcPort\": {\"type\": \"integer\"},"
                + "\"tcpFlags\": {\"type\": \"integer\"},"
                + "\"startTime\": {\"type\": \"date\"},"
                + "\"version\":{\"type\":\"keyword\"},"
                + "\"srcAddrIPv6\":{\"type\":\"keyword\"},"
                + "\"dstAddrIPv6\":{\"type\":\"keyword\"},"
                + "\"src_ip_geoip\": {"
                + "\"properties\": {"
                + "\"location\": {\"type\": \"geo_point\"},"
                + "\"city_name\": {\"type\": \"keyword\"},"
                + "\"continent_code\": {\"type\": \"keyword\"},"
                + "\"country_code2\": {\"type\": \"keyword\"},"
                + "\"country_code3\": {\"type\": \"keyword\"},"
                + "\"country_name\": {\"type\": \"keyword\"},"
                + "\"dma_code\": {\"type\": \"keyword\"},"
                + "\"ip\": {\"type\": \"ip\"},"
                + "\"latitude\": {\"type\": \"float\"},"
                + "\"longitude\": {\"type\": \"float\"},"
                + "\"postal_code\": {\"type\": \"keyword\"},"
                + "\"region_name\": {\"type\": \"keyword\"},"
                + "\"timezone\": {\"type\": \"keyword\"}"
                + "}"
                + "},"
                + "\"dst_ip_geoip\": {"
                + "\"properties\": {"
                + "\"location\": {\"type\": \"geo_point\"},"
                + "\"city_name\": {\"type\": \"keyword\"},"
                + "\"continent_code\": {\"type\": \"keyword\"},"
                + "\"country_code2\": {\"type\": \"keyword\"},"
                + "\"country_code3\": {\"type\": \"keyword\"},"
                + "\"country_name\": {\"type\": \"keyword\"},"
                + "\"dma_code\": {\"type\": \"keyword\"},"
                + "\"ip\": {\"type\": \"ip\"},"
                + "\"latitude\": {\"type\": \"float\"},"
                + "\"longitude\": {\"type\": \"float\"},"
                + "\"postal_code\": {\"type\": \"keyword\"},"
                + "\"region_name\": {\"type\": \"keyword\"},"
                + "\"timezone\": {\"type\": \"keyword\"}"
                + "}"
                + "},"
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
                + "\"direction\": {\"type\": \"integer\"},"
                + "\"dstAddr\": {\"type\": \"ip\"},"
                + "\"dstPort\": {\"type\": \"integer\"},"
                + "\"endTime\": {\"type\": \"date\"},"
                + "\"flowEndReason\": {\"type\": \"integer\"},"
                + "\"inputInt\": {\"type\": \"integer\"},"
                + "\"ipToS\": {\"type\": \"integer\"},"
                + "\"maxTTL\": {\"type\": \"integer\"},"
                + "\"octets\": {\"type\": \"integer\"},"
                + "\"outputInt\": {\"type\": \"integer\"},"
                + "\"packets\": {\"type\": \"integer\"},"
                + "\"protocol\": {\"type\": \"integer\"},"
                + "\"segmentId\": {\"type\": \"long\"},"
                + "\"setId\": {\"type\": \"integer\"},"
                + "\"srcAddr\": {\"type\": \"ip\"},"
                + "\"srcPort\": {\"type\": \"integer\"},"
                + "\"tcpFlags\": {\"type\": \"integer\"},"
                + "\"startTime\": {\"type\": \"date\"},"
                + "\"version\":{\"type\":\"keyword\"},"
                + "\"srcAddrIPv6\":{\"type\":\"keyword\"},"
                + "\"dstAddrIPv6\":{\"type\":\"keyword\"},"
                + "\"client_ip_geoip\": {"
                + "\"properties\": {"
                + "\"location\": {\"type\": \"geo_point\"},"
                + "\"city_name\": {\"type\": \"keyword\"},"
                + "\"continent_code\": {\"type\": \"keyword\"},"
                + "\"country_code2\": {\"type\": \"keyword\"},"
                + "\"country_code3\": {\"type\": \"keyword\"},"
                + "\"country_name\": {\"type\": \"keyword\"},"
                + "\"dma_code\": {\"type\": \"keyword\"},"
                + "\"ip\": {\"type\": \"ip\"},"
                + "\"latitude\": {\"type\": \"float\"},"
                + "\"longitude\": {\"type\": \"float\"},"
                + "\"postal_code\": {\"type\": \"keyword\"},"
                + "\"region_name\": {\"type\": \"keyword\"},"
                + "\"timezone\": {\"type\": \"keyword\"}"
                + "}"
                + "},"
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
