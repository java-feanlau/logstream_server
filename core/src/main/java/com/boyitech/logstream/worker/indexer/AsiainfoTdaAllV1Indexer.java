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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author juzheng
 * @date 2019/3/26 11:49 AM
 * @Description: 亚信TDA的日志格式化，第二层log-body主要切割制表符\t
 */
public class AsiainfoTdaAllV1Indexer extends BaseIndexer {
    private String[] patterns1;
    private ArrayList<Grok> groks1;
    private BaseIndexerConfig config;

    public AsiainfoTdaAllV1Indexer(BaseWorkerConfig config) {
        super(config);
        this.config = (BaseIndexerConfig) config;

    }

    public AsiainfoTdaAllV1Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
        this.config = (BaseIndexerConfig) config;

    }

    @Override
    public boolean register() {

        patterns1 = new String[]{
                "\\<(%{NUMBER:device_id})\\>(%{DATA:log_type})\\|Asiainfo security\\|(%{WORD:device_alias})\\|(%{DATA:device_version})\\|(%{DATA:tda_type})\\|dvc=(%{IP:device_ip})(\\s+)deviceMacAddress=(%{DATA:device_mac})(\\s+)(%{DATA:device_hostname})(\\s+)deviceGUID=(%{DATA:device_guid})(\\s+)ptype=(%{DATA:ptype})(\\s+)devTimeFormat=%{GREEDYDATA:devTimeFormat}\\tsev=%{NUMBER:sev}\\tmsg=%{GREEDYDATA:msg}\\tdevTime=(%{GREEDYDATA:timestamp}) GMT\\+08\\:00(\\t*)%{GREEDYDATA:log_body}",
                "\\<(%{NUMBER:device_id})\\>(%{DATA:log_type})\\|Asiainfo security\\|(%{WORD:device_alias})\\|(%{DATA:device_version})\\|(%{DATA:tda_type})\\|([\\s,\\S]+)ptype=(%{DATA:ptype})(\\s+)dvc=(%{IP:device_ip})(\\s+)deviceMacAddress=(%{DATA:device_mac})(\\s+)(%{DATA:device_hostname})(\\s+)deviceGUID=(%{DATA:device_guid})(\\s+)devTime=(?<timestamp>%{MONTH} %{MONTHDAY} %{YEAR} %{TIME})(\\s+)(\\S+)(\\s+)(%{GREEDYDATA:log_body})"
        };
        groks1 = GrokUtil.getGroks(patterns1);

        return true;
    }

    @Override
    public boolean format(Event event) {

        String message = event.getMessage();
        Map<String, Object> messageMap = GrokUtil.getMapByGroks(groks1, message);
        Map<String, Object> formated = event.getFormat();
        formated.putAll(messageMap);
        formated.put("message", message);
        if (messageMap.get("flag") == null && messageMap.get("flag") != "解析失败") {
            // event.setMetafieldLoglevel("1");
            event.setMetafieldLoglevel("1");
        }

        String log_body = String.valueOf(formated.get("log_body"));
        String[] arr;
        String[] arr2;
        String left = new String();
        String right = new String();
        if (log_body != null && log_body.length() != 0) {
            Map<String, Object> log_body_map = new HashMap<>();
            arr = log_body.split("\\t");
            boolean a = true;
            for (String s1 : arr) {
                arr2 = s1.split("=");
                if (arr2.length > 1) {
                    left = arr2[0];
                    right = arr2[1];
                    log_body_map.put("risk_" + left, right);
                }
            }
            formated.putAll(log_body_map);
        }
        //格式化时间 Jan 02 2019 07:53:42
        IndexerTimeUtils.getISO8601Time2(formated, "timestamp", "MMM dd yyyy HH:mm:ss");

        event.setMetafieldLoglevel("1");

        if (event.getSource() != null) {
            formated.put("log_source", event.getSource());
        }

        if (formated.get("risk_src") != null) {
            formated.put("src_ip", formated.get("risk_src"));
        }
        if (formated.get("risk_dst") != null) {
            formated.put("dst_ip", formated.get("risk_dst"));
        }
        if (formated.get("src_ip") != null && formated.get("dst_ip") != null) {
            formated.put("ip_addr_pair", formated.get("src_ip") + "=>" + formated.get("dst_ip"));
        }
        MetafieldHelper.setMetafield(event, "src_ip", "device_ip", "dst_ip", formated);

        //geoip解析及过滤：
        if (!config.getIpFilter().equals("null")) {
            GrokUtil.filterGeoIP(config, formated);
        } else {
            GrokUtil.setGeoIP2(formated, "src_ip");
            GrokUtil.setGeoIP2(formated, "dst_ip");
        }

        //补充日志来源ip
        if (event.getSource() != null) {
            formated.put("host", event.getSource());
        }

        //补充大的日志种类:设备名-设备
        formated.put("log_class", "asiainfo-tda");
        formated.put("format_level", event.getMetafieldLoglevel());

        if (formated.get("flag") == "解析失败")
            return false;
        return true;
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
                + "\"device_id\":{\"type\":\"keyword\"},"
                + "\"log_type\":{\"type\":\"keyword\"},"
                + "\"log_source\":{\"type\":\"keyword\"},"
                + "\"device_alias\":{\"type\":\"keyword\"},"
                + "\"device_version\":{\"type\":\"keyword\"},"
                + "\"tda_type\":{\"type\":\"keyword\"},"
                + "\"device_ip\":{\"type\":\"keyword\"},"
                + "\"device_mac\":{\"type\":\"keyword\"},"
                + "\"device_hostname\":{\"type\":\"keyword\"},"
                + "\"device_guid\":{\"type\":\"keyword\"},"
                + "\"ptype\":{\"type\":\"keyword\"},"
                + "\"devTimeFormat\":{\"type\":\"keyword\"},"
                + "\"log_body\":{\"type\":\"keyword\"},"
                + "\"timestamp\":{\"type\":\"keyword\"},"
                + "\"msg\":{\"type\":\"keyword\"},"
                + "\"format_level\":{\"type\":\"keyword\"},"
                + "\"ip_addr_pair\":{\"type\":\"keyword\"},"
                + "\"log_class\":{\"type\":\"keyword\"},"
                + "\"host\":{\"type\":\"keyword\"},"
                + "\"risk_src\": {\"type\": \"ip\" },"
                + "\"risk_dst\": {\"type\": \"ip\" },"
                + "\"risk_dvc\": {\"type\": \"ip\" },"
                + "\"risk_interestedIp\": {\"type\": \"ip\" },"
                + "\"risk_peerIp\": {\"type\": \"ip\" },"
                + "\"risk_aggregatedCnt\": {\"type\": \"integer\" },"
                + "\"risk_cnt\": {\"type\": \"integer\" },"
                + "\"risk_deviceRiskConfidenceLevel\": {\"type\": \"integer\" },"
                + "\"risk_dstPort\": {\"type\": \"integer\" },"
                + "\"risk_vLANId\": {\"type\": \"integer\" },"
                + "\"src_ip\": {\"type\": \"ip\" },"
                + "\"dst_ip\": {\"type\": \"ip\" },"
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
                + "\"Metafield_description\":{\"type\":\"keyword\", \"fields\": {\"raw\": {\"type\": \"keyword\"}}}"
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
                + "\"device_id\":{\"type\":\"keyword\"},"
                + "\"log_type\":{\"type\":\"keyword\"},"
                + "\"log_source\":{\"type\":\"keyword\"},"
                + "\"device_alias\":{\"type\":\"keyword\"},"
                + "\"device_version\":{\"type\":\"keyword\"},"
                + "\"tda_type\":{\"type\":\"keyword\"},"
                + "\"device_ip\":{\"type\":\"keyword\"},"
                + "\"device_mac\":{\"type\":\"keyword\"},"
                + "\"device_hostname\":{\"type\":\"keyword\"},"
                + "\"device_guid\":{\"type\":\"keyword\"},"
                + "\"ptype\":{\"type\":\"keyword\"},"
                + "\"devTimeFormat\":{\"type\":\"keyword\"},"
                + "\"log_body\":{\"type\":\"keyword\"},"
                + "\"timestamp\":{\"type\":\"keyword\"},"
                + "\"msg\":{\"type\":\"keyword\"},"
                + "\"format_level\":{\"type\":\"keyword\"},"
                + "\"ip_addr_pair\":{\"type\":\"keyword\"},"
                + "\"log_class\":{\"type\":\"keyword\"},"
                + "\"host\":{\"type\":\"keyword\"},"
                + "\"risk_src\": {\"type\": \"ip\" },"
                + "\"risk_dst\": {\"type\": \"ip\" },"
                + "\"risk_dvc\": {\"type\": \"ip\" },"
                + "\"risk_interestedIp\": {\"type\": \"ip\" },"
                + "\"risk_peerIp\": {\"type\": \"ip\" },"
                + "\"risk_aggregatedCnt\": {\"type\": \"integer\" },"
                + "\"risk_cnt\": {\"type\": \"integer\" },"
                + "\"risk_deviceRiskConfidenceLevel\": {\"type\": \"integer\" },"
                + "\"risk_dstPort\": {\"type\": \"integer\" },"
                + "\"risk_vLANId\": {\"type\": \"integer\" },"
                + "\"src_ip\": {\"type\": \"ip\" },"
                + "\"dst_ip\": {\"type\": \"ip\" },"
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
                + "\"Metafield_description\":{\"type\":\"keyword\", \"fields\": {\"raw\": {\"type\": \"keyword\"}}}"
                + "}"
                + "}";
        return mapping;
    }

}
