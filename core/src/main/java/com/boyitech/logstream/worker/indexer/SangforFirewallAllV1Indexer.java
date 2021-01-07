package com.boyitech.logstream.worker.indexer;


import com.alibaba.fastjson.JSONObject;
import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.GrokUtil;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.MetafieldHelper;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import com.boyitech.logstream.core.worker.indexer.BaseIndexerConfig;
import io.krakens.grok.api.Grok;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/**
 * @author juzheng

 * @date 11:08 AM
 * @Description:  深信服防火墙日志格式化，包括4种类型
 */
public class SangforFirewallAllV1Indexer extends BaseIndexer {
    private String[] patterns1;
    private ArrayList<Grok> groks1;
    private BaseIndexerConfig config;

    public SangforFirewallAllV1Indexer(BaseWorkerConfig config) {
        super(config);
        this.config = (BaseIndexerConfig) config;
    }

    public SangforFirewallAllV1Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
        this.config = (BaseIndexerConfig) config;
    }

    @Override
    public boolean register() {

        patterns1 =new String[]{
                "^日志类型:%{NOTSPACE:sangfor-log-type},\\s*源IP:%{IP:src_ip},\\s*源端口:%{NUMBER:src_port},\\s*目的IP:%{IP:dst_ip},\\s*目的端口:%{NUMBER:dst_port},\\s*风险类型:%{NOTSPACE:threat_type},\\s*严重级别:%{NOTSPACE:threat_level},\\s*协议:%{NOTSPACE:proto},\\s*URL:%{NOTSPACE:url}$",
                "^日志类型:%{NOTSPACE:sangfor-log-type},\\s*用户:%{NOTSPACE:user},\\s*源IP:%{IP:src_ip},\\s*源端口:%{NUMBER:src_port},\\s*目的IP:%{IP:dst_ip},\\s*目的端口:%{NUMBER:dst_port},\\s*应用类型:%{NOTSPACE:app_type},\\s*应用名称:%{NOTSPACE:app_name},\\s*系统动作:%{NOTSPACE:action}$",
                "^日志类型:%{NOTSPACE:sangfor-log-type},\\s*用户:%{USER:user},\\s*IP地址:(%{IP:oper_ip}),\\s*操作对象:%{NOTSPACE:oper_obj},\\s*操作类型:%{NOTSPACE:oper_type},\\s*描述:%{DATA:oper_desc}$",
                "^日志类型:%{NOTSPACE:sangfor-log-type},\\s*源IP:%{IP:src_ip},\\s*源端口:%{NUMBER:src_port},\\s*目的IP:%{IP:dst_ip},\\s*目的端口:%{NUMBER:dst_port},\\s*攻击类型:%{NOTSPACE:threat_type},\\s*严重级别:%{NOTSPACE:threat_level},\\s*系统动作:%{NOTSPACE:action},\\s*URL:%{GREEDYDATA:url}$",
                "日志类型:%{NOTSPACE:sangfor-log-type},(\\s+)应用类型:%{DATA:app_type},(\\s+)用户名/主机:%{IP:user_ip},(\\s+)上行流量\\(KB\\):%{NUMBER:uplink_traffic},(\\s+)下行流量\\(KB\\):%{NUMBER:downstream_traffic},(\\s+)总流量\\(KB\\):%{NUMBER:total_flow}"

        };
        groks1 = GrokUtil.getGroks(patterns1);

        return true;
    }

    @Override
    public boolean format(Event event) {

        String messagejson = event.getMessage();
        if (GrokUtil.isJSONValid(messagejson) == true) {
            Map mapType = JSONObject.parseObject(messagejson);
            mapType.remove("flag");
            Map<String, Object> format = event.getFormat();
            format.putAll(mapType);
            JSONObject pa = JSONObject.parseObject(messagejson);
            String message=pa.getString("message");
            Map<String, Object> map = GrokUtil.getMapByGroks(groks1, message);
            format.putAll(map);

            if (!config.getIpFilter().equals("null")) {
                GrokUtil.filterGeoIP(config, format);
            } else {
                GrokUtil.setGeoIP2(format, "src_ip");
                GrokUtil.setGeoIP2(format, "dst_ip");
                GrokUtil.setGeoIP2(format, "user_ip");
            }
            //格式化Metafield
            event.setMetafieldLoglevel("1");
            if (format.get("src_ip") != null)
                MetafieldHelper.setMetafield(event, "src_ip", "dst_ip", "", format);
            else
                MetafieldHelper.setMetafield(event, "user_ip", "", "", format);
            if (format.get("flag") == "解析失败")
                return false;
            return true;
        }
        else {
            Map<String, Object> map = GrokUtil.getMapByGroks(groks1, messagejson);
            Map<String, Object> format = event.getFormat();
            format.putAll(map);
            format.put("message", messagejson);

            if (!config.getIpFilter().equals("null")) {
                GrokUtil.filterGeoIP(config, format);
            } else {
                GrokUtil.setGeoIP2(format, "src_ip");
                GrokUtil.setGeoIP2(format, "dst_ip");
                GrokUtil.setGeoIP2(format, "user_ip");
            }

            //格式化时间
            String pattern = "yyyy-MM-dd'T'HH:mm:ssXXX";
            String format1 = org.apache.commons.lang3.time.DateFormatUtils.format(new Date(), pattern);
            format.put("@timestamp", format1);

            //格式化Metafield
            event.setMetafieldLoglevel("1");
            if (format.get("src_ip") != null)
                MetafieldHelper.setMetafield(event, "src_ip", "dst_ip", "", format);
            else
                MetafieldHelper.setMetafield(event, "user_ip", "", "", format);
            if (format.get("flag") == "解析失败")
                return false;
            return true;
        }
    }
    @Override
    public void tearDown() {

    }


    public static Map getMapping() {
        String mapping = "{\"properties\":{"
                + "\"@timestamp\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
                + "\"received_at\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
                + "\"portedAt\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
                + "\"flag\":{\"type\":\"keyword\"},"
                + "\"message\":{\"type\":\"text\"},"
                + "\"sangfor-log-type\":{\"type\":\"keyword\"},"
                + "\"src_ip\":{\"type\":\"keyword\"},"
                + "\"src_port\":{\"type\":\"keyword\"},"
                + "\"dst_ip\":{\"type\":\"keyword\"},"
                + "\"dst_port\":{\"type\":\"keyword\"},"
                + "\"threat_type\":{\"type\":\"keyword\"},"
                + "\"threat_level\":{\"type\":\"keyword\"},"
                + "\"proto\":{\"type\":\"keyword\"},"
                + "\"url\":{\"type\":\"keyword\"},"
                + "\"user\":{\"type\":\"keyword\"},"
                + "\"app_type\":{\"type\":\"keyword\"},"
                + "\"app_name\":{\"type\":\"keyword\"},"
                + "\"action\":{\"type\":\"keyword\"},"
                + "\"oper_ip\":{\"type\":\"keyword\"},"
                + "\"oper_obj\":{\"type\":\"keyword\"},"
                + "\"oper_type\":{\"type\":\"keyword\"},"
                + "\"oper_desc\":{\"type\":\"keyword\"},"
                + "\"user_ip\":{\"type\":\"ip\"},"
                + "\"uplink_traffic\":{\"type\":\"long\"},"
                + "\"downstream_traffic\":{\"type\":\"long\"},"
                + "\"total_flow\":{\"type\":\"long\"},"
                + "\"user_ip_geoip\": {"
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
                + "\"sangfor-log-type\":{\"type\":\"keyword\"},"
                + "\"src_ip\":{\"type\":\"keyword\"},"
                + "\"src_port\":{\"type\":\"keyword\"},"
                + "\"dst_ip\":{\"type\":\"keyword\"},"
                + "\"dst_port\":{\"type\":\"keyword\"},"
                + "\"threat_type\":{\"type\":\"keyword\"},"
                + "\"threat_level\":{\"type\":\"keyword\"},"
                + "\"proto\":{\"type\":\"keyword\"},"
                + "\"url\":{\"type\":\"keyword\"},"
                + "\"user\":{\"type\":\"keyword\"},"
                + "\"app_type\":{\"type\":\"keyword\"},"
                + "\"app_name\":{\"type\":\"keyword\"},"
                + "\"action\":{\"type\":\"keyword\"},"
                + "\"oper_ip\":{\"type\":\"keyword\"},"
                + "\"oper_obj\":{\"type\":\"keyword\"},"
                + "\"oper_type\":{\"type\":\"keyword\"},"
                + "\"oper_desc\":{\"type\":\"keyword\"},"
                + "\"user_ip\":{\"type\":\"ip\"},"
                + "\"uplink_traffic\":{\"type\":\"long\"},"
                + "\"downstream_traffic\":{\"type\":\"long\"},"
                + "\"total_flow\":{\"type\":\"long\"},"
                + "\"user_ip_geoip\": {"
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
