package com.boyitech.logstream.worker.indexer;

import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.*;
import com.boyitech.logstream.core.util.filter_rule.IpRangeRule;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import com.boyitech.logstream.core.worker.indexer.BaseIndexerConfig;
import io.krakens.grok.api.Grok;

import java.util.*;

/**
 * @author Eric
 * @Title: NginxIndexer
 * @date 2018/11/12 16:06
 * @Description: TODO
 */
public class ApacheApacheErrorV1Indexer extends BaseIndexer {

    private String[] patterns1;
    private String pattern1;
    private ArrayList<Grok> groks1;
    private Grok grok1;
    private BaseIndexerConfig config;
    public ApacheApacheErrorV1Indexer(BaseWorkerConfig config) {
        super(config);
        this.config = (BaseIndexerConfig) config;
    }

    public ApacheApacheErrorV1Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
        this.config = (BaseIndexerConfig) config;
    }

    public boolean register() {
        patterns1 = new String[]{
                "^\\[client %{IP:client_ip}\\] %{DATA:error_reason}\\: %{DATA:error_path}, referer\\: %{DATA:referer_uri}$",
                "^\\[client %{IP:client_ip}\\] %{DATA:error_reason}\\: %{DATA:error_path}$",
                "^\\[client %{IP:client_ip}\\] %{DATA:error_reason}$"
        };
        pattern1 = "^\\[\\w{3} (?<timestamp>%{MONTH} %{MONTHDAY} %{TIME} %{YEAR})\\] \\[%{NOTSPACE:log_severity}\\]\\s%{DATA:error_messages}$";

        grok1 = GrokUtil.getGrok(pattern1);
        groks1 = GrokUtil.getGroks(patterns1);

        return true;
    }

    @Override
    public boolean format(Event event) {
        String[] s = {
                "",
        };
        String message = event.getMessage();
        Map<String, Object> messageMap = GrokUtil.getMap(grok1, message);
        Map<String, Object> format = event.getFormat();
        format.put("message", message);
        format.putAll(messageMap);
        if (format.get("flag") == null && format.get("flag") != "解析失败") {
            event.setMetafieldLoglevel("1");
        }

        String log_severity = (String) format.get("log_severity");
        String error_messages = (String) format.get("error_messages");
        Map<String, Object> mapByMuch = new HashMap<>();
        if (log_severity != null && log_severity.equals("error")) {


            mapByMuch = GrokUtil.getMapByGroks(groks1, error_messages);
            format.putAll(mapByMuch);

            if (format.get("flag") == null && format.get("flag") != "解析失败") {
                event.setMetafieldLoglevel("2");
            }
        }

        if(!config.getIpFilter().equals("null")){
            GrokUtil.filterGeoIP(config,format);
        }
        else {
            setGeoIP(format);
        }

        //accessTime
        //格式化@timestamp
        IndexerTimeUtils.getISO8601Time2(format,"timestamp","MMM dd HH:mm:ss yyyy");

        format.put("path",event.getKey());
//        MetafieldHelper.setMetafield(event,"","client_ip","",format);
        event.setMetafieldObject((String) format.get("client_ip"));
        event.setMetafieldSubject((String) format.get("client_ip"));
        if (event.getSource() != null) {
            format.put("log_source",event.getSource());
            event.setMetafieldSource(event.getSource());
        }
        if(event.getMetafieldSource()==null&&event.getClientIP()!=null)
        {
            event.setMetafieldSource(event.getClientIP());
        }
        if (format.get("flag") == "解析失败")
            return false;
        return true;
    }


    public static void setGeoIP(Map formated) {
        String accessIP = (String) formated.get("client_ip");

        if (accessIP != null && accessIP.trim().length() != 0) {
            try {
                Map geoIPInfo = GeoIPHelper.getInstance().getGeoIPInfo(accessIP);
                formated.put("client_ip_geoip", geoIPInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
                + "\"path\":{\"type\":\"keyword\"},"
                + "\"log_source\":{\"type\":\"keyword\"},"
                + "\"log_severity\":{\"type\":\"keyword\"},"
                + "\"error_reason\":{\"type\":\"keyword\"},"
                + "\"error_path\":{\"type\":\"keyword\"},"
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
                + "\"client_ip\":{\"type\":\"ip\"},"
                + "\"error_messages\":{\"type\":\"keyword\"},"
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
                + "\"path\":{\"type\":\"keyword\"},"
                + "\"log_source\":{\"type\":\"keyword\"},"
                + "\"log_severity\":{\"type\":\"keyword\"},"
                + "\"error_reason\":{\"type\":\"keyword\"},"
                + "\"error_path\":{\"type\":\"keyword\"},"
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
                + "\"client_ip\":{\"type\":\"ip\"},"
                + "\"error_messages\":{\"type\":\"keyword\"},"
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
