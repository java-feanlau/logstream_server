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
 * @author:juzheng
 * @Title: YamuDnsAllV1Indexer
 * @date: 2019-07-16T14:14:07.938
 * @Description: 1、此indexer文件根据indexer通用模版创建；
 * 2、为商学院dns日志进行格式化处理；
 * 3、对于可处理的可能的日志来源:
 * （1）接收syslog外发，为原始日志，有"|";
 * (2）日志经logstash filter的split插件处理，"|"皆被替换为","
 */
public class YamuDnsAllV1Indexer extends BaseIndexer {
    //此处初始化正则、grok的变量
    private ArrayList<Grok> groks1;
    private Grok grok2;
    private String[] patterns1;
    private String pattern2;
    private BaseIndexerConfig config;


    public YamuDnsAllV1Indexer(BaseWorkerConfig config) {
        super(config);
        this.config = (BaseIndexerConfig) config;

    }

    public YamuDnsAllV1Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
        this.config= (BaseIndexerConfig) config;

    }

    @Override
    public boolean register() {
        //此处注册正确的正则+grok
        patterns1 = new String[]
                {
                        "^(%{IP:src_ip})\\|(%{DATA:request_domain})\\|(%{DATA:request_time})\\|(%{DATA:A_domain})\\|(%{DATA:code})\\|(%{DATA:dnsrecord_type})\\|(%{DATA:cname})\\|(%{DATA:AAAA_domain})\\|(%{IP:dns_server})$",
                        "^(%{IP:src_ip})\\,(\\s*)(%{DATA:request_domain})\\,(\\s*)(%{DATA:request_time})\\,(\\s*)(%{DATA:A_domain})\\,(\\s*)(%{DATA:code})\\,(\\s*)(%{DATA:dnsrecord_type})\\,(\\s*)(%{DATA:cname})\\,(\\s*)(%{DATA:AAAA_domain})\\,(\\s*)(%{IP})$"
                };
        pattern2 = "%{IP:A1_ip}%{GREEDYDATA}";

        groks1 = GrokUtil.getGroks(patterns1);
        grok2 = GrokUtil.getGrok(pattern2);
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
        String A_domain = String.valueOf(format.get("A_domain"));
        if (GrokUtil.isStringHasValue(A_domain)) {
            Map<String, Object> map2 = GrokUtil.getMap(grok2, A_domain);
            format.putAll(map2);
            if (map2.get("flag") == null && map2.get("flag") != "解析失败") {
                event.setMetafieldLoglevel("2");
            }
        }

        //格式化时间，样本：20181212162923
        IndexerTimeUtils.getISO8601Time2(format, "request_time", "yyyyMMddHHmmss");

        if (!config.getIpFilter().equals("null")) {
            GrokUtil.filterGeoIP(config, format);
        } else {
            //使用GeoIP对src_ip、dst_ip或部分特殊字段处理，
            GrokUtil.setGeoIP2(format, "A1_ip");
            GrokUtil.setGeoIP2(format, "src_ip");
        }

        //格式化Metafield ，可能需要根基实际日志格式替换部分字段名如client_ip/src_ip、dst_ip
        event.setMetafieldLoglevel("2");
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
        MetafieldHelper.setMetafield(event,"src_ip","dns_server","",format);
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
                + "\"src_ip\":{\"type\":\"keyword\"},"
                + "\"request_domain\":{\"type\":\"keyword\"},"
                + "\"request_time\":{\"type\":\"keyword\"},"
                + "\"A_domain\":{\"type\":\"keyword\"},"
                + "\"code\":{\"type\":\"keyword\"},"
                + "\"dnsrecord_type\":{\"type\":\"keyword\"},"
                + "\"cname\":{\"type\":\"keyword\"},"
                + "\"AAAA_domain\":{\"type\":\"keyword\"},"
                + "\"dns_server\":{\"type\":\"keyword\"},"
                + "\"A1_ip\":{\"type\":\"keyword\"},"
                + "\"A1_ip_geoip\": {"
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
                + "\"src_ip\":{\"type\":\"keyword\"},"
                + "\"request_domain\":{\"type\":\"keyword\"},"
                + "\"request_time\":{\"type\":\"keyword\"},"
                + "\"A_domain\":{\"type\":\"keyword\"},"
                + "\"code\":{\"type\":\"keyword\"},"
                + "\"dnsrecord_type\":{\"type\":\"keyword\"},"
                + "\"cname\":{\"type\":\"keyword\"},"
                + "\"AAAA_domain\":{\"type\":\"keyword\"},"
                + "\"dns_server\":{\"type\":\"keyword\"},"
                + "\"A1_ip\":{\"type\":\"keyword\"},"
                + "\"A1_ip_geoip\": {"
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