package com.boyitech.logstream.worker.indexer;

import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.*;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import com.boyitech.logstream.core.worker.indexer.BaseIndexerConfig;
import io.krakens.grok.api.Grok;
import io.krakens.grok.api.GrokCompiler;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author juzheng
 * @Title: NginxNginxSuccessV2Indexer
 * @date 2019/12/12 3:21 PM
 * @Description: 参照FileBeat新写的NginxAccess日志的格式化
 */
public class NginxNginxSuccessV2Indexer extends BaseIndexer {
    private BaseIndexerConfig config;
    private Grok grok;
    private Grok grok1;

    private String pattern;
    private String pattern1;

    public NginxNginxSuccessV2Indexer(BaseWorkerConfig config) {
        super(config);
        this.config= (BaseIndexerConfig) config;

    }
    public NginxNginxSuccessV2Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
        this.config= (BaseIndexerConfig) config;

    }

    @Override
    public boolean register() {
        pattern="%{IPORHOST:source.ip} - %{DATA:user.name} \\[%{HTTPDATE:nginx.access.time}\\] \"%{DATA:nginx.access.info}\" %{NUMBER:http.response.status_code:long} %{NUMBER:http.response.body.bytes:long} \"%{DATA:http.request.referrer}\" \"%{DATA:user_agent.original}";
        pattern1= "%{WORD:http.request.method} %{DATA:url.original} HTTP/%{NUMBER:http.version}";
        GrokCompiler grokCompiler = GrokCompiler.newInstance();
        grokCompiler.registerDefaultPatterns();
        grokCompiler.register("IP_LIST", "%{IP}(\"?,?\\s*%{IP})*");
        grok = grokCompiler.compile(pattern, true);
        grok1 = grokCompiler.compile(pattern1, true);

        return true;
    }

    @Override
    public boolean format(Event event) {
        String message = event.getMessage();
        Map<String, Object> messageMap =  GrokUtil.getMap(grok,message);
        Map<String, Object> formated = event.getFormat();
        formated.putAll(messageMap);
        formated.put("message", message);
        formated.put("path",event.getKey());
        event.setMetafieldLoglevel("1");
        if (formated.get("nginx.access.info") != null) {
            Map<String, Object> accessLineMap = GrokUtil.getMap(grok1, (String) formated.get("nginx.access.info"));
            formated.putAll(accessLineMap);
            event.setMetafieldLoglevel("2");
        }
        if (!config.getIpFilter().equals("null")) {
            GrokUtil.filterGeoIP(config, formated);
        } else {
            setGeoIP(formated);
        }
        IndexerTimeUtils.getISO8601Time2(formated,"nginx.access.time","dd/MMM/yyyy:HH:mm:ss Z");

        MetafieldHelper.setMetafield(event,"","source.ip","source.ip",formated);
        if (event.getSource() != null) {
            event.setMetafieldSource(event.getSource());
        }
        if(event.getMetafieldSource()==null&&event.getClientIP()!=null)
        {
            event.setMetafieldSource(event.getClientIP());
        }

        if (formated.get("flag") == "解析失败"){
            event.setMetafieldLoglevel("0");
            OffsetDateTime date=OffsetDateTime.now();
            formated.put("@timestamp", String.valueOf(date));
            return false;
        }

        return true;
    }

    public static void setGeoIP(Map formated) {
        String accessIP = (String) formated.get("source.ip");
        if (accessIP != null && accessIP.trim().length() != 0) {
            try {
                Map geoIPInfo = GeoIPHelper.getInstance().getGeoIPInfo(accessIP);
                formated.put("source.ip_geoip", geoIPInfo);
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
                + "\"source.ip\":{\"type\":\"ip\"},"
                + "\"user.name\":{\"type\":\"keyword\"},"
                + "\"message\":{\"type\":\"text\"},"
                + "\"nginx.access.time\":{\"type\":\"keyword\"},"
                + "\"nginx.access.info\":{\"type\":\"keyword\"},"
                + "\"http.response.status_code\":{\"type\":\"integer\"},"
                + "\"http.response.body.bytes\":{\"type\":\"integer\"},"
                + "\"http.request.referrer\":{\"type\":\"keyword\"},"
                + "\"user_agent.original\":{\"type\":\"keyword\"},"
                + "\"http.request.method\":{\"type\":\"keyword\"},"
                + "\"url.original\":{\"type\":\"keyword\"},"
                + "\"http.version\":{\"type\":\"keyword\"},"
                + "\"source.ip_geoip\": {"
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
                + "\"source.ip\":{\"type\":\"ip\"},"
                + "\"user.name\":{\"type\":\"keyword\"},"
                + "\"message\":{\"type\":\"text\"},"
                + "\"nginx.access.time\":{\"type\":\"keyword\"},"
                + "\"nginx.access.info\":{\"type\":\"keyword\"},"
                + "\"http.response.status_code\":{\"type\":\"integer\"},"
                + "\"http.response.body.bytes\":{\"type\":\"integer\"},"
                + "\"http.request.referrer\":{\"type\":\"keyword\"},"
                + "\"user_agent.original\":{\"type\":\"keyword\"},"
                + "\"http.request.method\":{\"type\":\"keyword\"},"
                + "\"url.original\":{\"type\":\"keyword\"},"
                + "\"http.version\":{\"type\":\"keyword\"},"
                + "\"source.ip_geoip\": {"
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
