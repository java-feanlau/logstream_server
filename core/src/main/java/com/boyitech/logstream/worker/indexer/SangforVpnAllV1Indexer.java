package com.boyitech.logstream.worker.indexer;


import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.GrokUtil;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.IndexerTimeUtils;
import com.boyitech.logstream.core.util.MetafieldHelper;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import com.boyitech.logstream.core.worker.indexer.BaseIndexerConfig;
import io.krakens.grok.api.Grok;


import java.util.ArrayList;
import java.util.Map;

/**
 * @author: juzheng
 * @Title: SangforVpnAllV1Indexer
 * @date: 2019-07-26T10:27:55.197
 * @Description: 此indexer文件根据indexer通用模版创建
 */
public class SangforVpnAllV1Indexer extends BaseIndexer {
    //此处初始化正则、grok的变量
    private ArrayList<Grok> groks1;
    private Grok grok2_System;
    private Grok grok2_access_resource;
    private Grok grok2_SSL_VPN;
    private Grok grok2_other;
    private Grok grok3_access_resource_operation_messages;
    private Grok grok3_SSL_VPN_operation_messages;
    private Grok grok3_other_operation_messages;

    private String[] patterns1;
    private String pattern2_System;
    private String pattern2_access_resource;
    private String pattern2_SSL_VPN;
    private String pattern2_other;
    private String pattern3_access_resource_operation_messages;
    private String pattern3_SSL_VPN_operation_messages;
    private String pattern3_other_operation_messages;
    private BaseIndexerConfig config;


    public SangforVpnAllV1Indexer(BaseWorkerConfig config) {
        super(config);
        this.config= (BaseIndexerConfig) config;

    }

    public SangforVpnAllV1Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
        this.config= (BaseIndexerConfig) config;

    }

    @Override
    public boolean register() {
        //此处注册正确的正则+grok

        patterns1 = new String[]{
                "^\\<(%{NUMBER:id})\\>\\[(%{DATA:sslvpn_type})\\](%{GREEDYDATA:sslvpn_log_body})",
                "^\\<(%{NUMBER:id})\\>\\[(%{NOTSPACE:sslvpn_type})\\](%{GREEDYDATA:sslvpn_log_body})",
        };
        pattern2_System = "^(%{NOTSPACE:user})(\\s+)from IP (%{IP:src_ip}):(\\s+)(%{GREEDYDATA:operation_messages})";
        pattern2_access_resource = "^\\[(%{NOTSPACE:sslvpn_type_identify})\\](%{NOTSPACE:user})(\\s+)from IP (%{IP:src_ip}):(\\s+)(%{GREEDYDATA:operation_messages})";
        pattern2_SSL_VPN="^\\[(%{NOTSPACE:sslvpn_type_identify})\\](%{GREEDYDATA:operation_messages})";
        pattern2_other = "^\\[(%{NOTSPACE:sslvpn_type_identify})\\](%{GREEDYDATA:operation_messages})";
        pattern3_access_resource_operation_messages = "(%{NOTSPACE:style})(\\s+)(%{IP:dst_ip})\\:(%{NUMBER:dst_port})(\\s+)(%{GREEDYDATA:result})";
        pattern3_SSL_VPN_operation_messages="%{GREEDYDATA:operation_results}\\, access\\(proto\\=%{NUMBER:proto} %{IP:dst_ip}:%{NUMBER:dst_port}\\)";
        pattern3_other_operation_messages = "^(%{NOTSPACE:user})(\\s+)from IP (%{IP:src_ip}):(\\s+)(%{GREEDYDATA:operation_results})";


        groks1 = GrokUtil.getGroks(patterns1);
        grok2_System = GrokUtil.getGrok(pattern2_System);
        grok2_access_resource = GrokUtil.getGrok(pattern2_access_resource);
        grok2_SSL_VPN=GrokUtil.getGrok(pattern2_SSL_VPN);
        grok2_other = GrokUtil.getGrok(pattern2_other);
        grok3_access_resource_operation_messages = GrokUtil.getGrok(pattern3_access_resource_operation_messages);
        grok3_SSL_VPN_operation_messages=GrokUtil.getGrok(pattern3_SSL_VPN_operation_messages);
        grok3_other_operation_messages = GrokUtil.getGrok(pattern3_other_operation_messages);
        return true;

    }

    @Override
    public boolean format(Event event) {
        //最外层，格式化日志主体
        String message = event.getMessage();
        Map<String, Object> map = GrokUtil.getMapByGroks(groks1, message);
        Map<String, Object> format = event.getFormat();
        format.putAll(map);
        format.put("message", message);
        if (map.get("flag") == null && map.get("flag") != "解析失败") {
            event.setMetafieldLoglevel("1");
        }

        //下一层，具体字段继续格式化
        String sslvpn_type = String.valueOf(format.get("sslvpn_type"));
        String sslvpn_log_body = String.valueOf(format.get("sslvpn_log_body"));
        if (GrokUtil.isStringHasValue(sslvpn_type) && GrokUtil.isStringHasValue(sslvpn_log_body)) {
            if (sslvpn_type.equals("System")) {
                Map<String, Object> mapSystem = GrokUtil.getMap(grok2_System, sslvpn_log_body);
                format.putAll(mapSystem);
                if (map.get("flag") == null && map.get("flag") != "解析失败") {
                    event.setMetafieldLoglevel("2");
                }
            } else if (sslvpn_type.equals("access resource")) {
                Map<String, Object> mapAccessResource = GrokUtil.getMap(grok2_access_resource, sslvpn_log_body);
                format.putAll(mapAccessResource);
                if (map.get("flag") == null && map.get("flag") != "解析失败") {
                    event.setMetafieldLoglevel("2");
                    String operation_messages = String.valueOf(format.get("operation_messages"));
                    if (GrokUtil.isStringHasValue(operation_messages)) {
                        Map<String, Object> map_operation_messages = GrokUtil.getMap(grok3_access_resource_operation_messages, operation_messages);
                        format.putAll(map_operation_messages);
                        if (map.get("flag") == null && map.get("flag") != "解析失败") {
                            event.setMetafieldLoglevel("3");
                        }
                    }
                }
            } else if (sslvpn_type.equals("SSL VPN")){
                Map<String, Object> mapSSLVPN = GrokUtil.getMap(grok2_SSL_VPN, sslvpn_log_body);
                format.putAll(mapSSLVPN);
                if (map.get("flag") == null && map.get("flag") != "解析失败") {
                    event.setMetafieldLoglevel("2");
                    String operation_messages = String.valueOf(format.get("operation_messages"));
                    if (GrokUtil.isStringHasValue(operation_messages)) {
                        Map<String, Object> map_operation_messages = GrokUtil.getMap(grok3_SSL_VPN_operation_messages, operation_messages);
                        format.putAll(map_operation_messages);
                        if (map.get("flag") == null && map.get("flag") != "解析失败") {
                            event.setMetafieldLoglevel("3");
                        }
                    }
                }
            }else {
                Map<String, Object> mapSystem = GrokUtil.getMap(grok2_other, sslvpn_log_body);
                format.putAll(mapSystem);
                if (map.get("flag") == null && map.get("flag") != "解析失败") {
                    event.setMetafieldLoglevel("2");
                    String operation_messages = String.valueOf(format.get("operation_messages"));
                    if (GrokUtil.isStringHasValue(operation_messages)) {
                        Map<String, Object> map_operation_messages2 = GrokUtil.getMap(grok3_other_operation_messages, operation_messages);
                        format.putAll(map_operation_messages2);
                        if (map.get("flag") == null && map.get("flag") != "解析失败") {
                            event.setMetafieldLoglevel("3");
                        }
                    }
                }
            }

        }


        //格式化时间，样本：
        IndexerTimeUtils.getISO8601Time2(format, "", "");


        if (!config.getIpFilter().equals("null")) {
            GrokUtil.filterGeoIP(config, format);
        } else {
            //使用GeoIP对src_ip、dst_ip或部分特殊字段处理，
            GrokUtil.setGeoIP2(format, "src_ip");
            GrokUtil.setGeoIP2(format, "dst_ip");
        }

        //格式化Metafield ，可能需要根基实际日志格式替换部分字段名如client_ip/src_ip、dst_ip
        // format.put("Metafield_category","sangfor_vpn_all_v1");//满足字段要求
        event.setMetafieldLoglevel("2");
//        if (event.getMsgType() != null) {
//            format.put("Metafield_category", event.getMsgType());
//        }
//        if (event.getLogType() != null) {
//            format.put("Metafield_description", event.getLogType());
//            format.put("Metafield_type", event.getLogType());
//        }
//        if (event.getSource() != null) {
//            format.put("Metafield_source", event.getSource());
//            format.put("log_source", event.getSource());//增加来源设备标识；
//        }
//        if (format.get("src_ip") != null) {
//            format.put("Metafield_object", format.get("src_ip"));
//        }
//        if (format.get("dst_ip") != null) {
//            format.put("Metafield_subject", format.get("dst_ip"));
//        } else {
//            format.put("Metafield_subject", event.getSource());
//        }
        MetafieldHelper.setMetafield(event,"src_ip","dst_ip","",format);
        if (format.get("flag") == "解析失败")
            return false;
        return true;
    }

    @Override
    public void tearDown() {
    }

    //上传的Mapping，要在下面两处空格处加上对应的Mapping字段；
    public static Map getMapping() {
        String mapping = "{\"properties\":{"
                + "\"@timestamp\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
                + "\"received_at\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
                + "\"portedAt\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
                + "\"flag\":{\"type\":\"keyword\"},"
                + "\"message\":{\"type\":\"text\"},"
                + "\"id\":{\"type\":\"keyword\"},"
                + "\"sslvpn_type\":{\"type\":\"keyword\"},"
                + "\"sslvpn_log_body\":{\"type\":\"keyword\"},"
                + "\"user\":{\"type\":\"keyword\"},"
                + "\"src_ip\":{\"type\":\"keyword\"},"
                + "\"operation_messages\":{\"type\":\"keyword\"},"
                + "\"sslvpn_type_identify\":{\"type\":\"keyword\"},"
                + "\"style\":{\"type\":\"keyword\"},"
                + "\"dst_ip\":{\"type\":\"keyword\"},"
                + "\"dst_port\":{\"type\":\"keyword\"},"
                + "\"result\":{\"type\":\"keyword\"},"
                + "\"operation_results\":{\"type\":\"keyword\"},"
                + "\"proto\":{\"type\":\"keyword\"},"
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
                + "\"id\":{\"type\":\"keyword\"},"
                + "\"sslvpn_type\":{\"type\":\"keyword\"},"
                + "\"sslvpn_log_body\":{\"type\":\"keyword\"},"
                + "\"user\":{\"type\":\"keyword\"},"
                + "\"src_ip\":{\"type\":\"keyword\"},"
                + "\"operation_messages\":{\"type\":\"keyword\"},"
                + "\"sslvpn_type_identify\":{\"type\":\"keyword\"},"
                + "\"style\":{\"type\":\"keyword\"},"
                + "\"dst_ip\":{\"type\":\"keyword\"},"
                + "\"dst_port\":{\"type\":\"keyword\"},"
                + "\"result\":{\"type\":\"keyword\"},"
                + "\"operation_results\":{\"type\":\"keyword\"},"
                + "\"proto\":{\"type\":\"keyword\"},"
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
        return mapping;
    }
}