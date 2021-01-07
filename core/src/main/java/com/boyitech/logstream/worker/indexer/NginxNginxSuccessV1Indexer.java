package com.boyitech.logstream.worker.indexer;


import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.*;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import com.boyitech.logstream.core.worker.indexer.BaseIndexerConfig;
import io.krakens.grok.api.Grok;

import java.io.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Eric

 * @date 2018/11/12 16:06
 * @Description: TODO
 */
public class NginxNginxSuccessV1Indexer extends BaseIndexer {

    private  Grok grok;
    private  ArrayList<Grok> groks;
    private  ArrayList<Grok> groks2;
    private  String patterns;
    private String[] patterns1;
    private  String[] patterns2;
    private BaseIndexerConfig config;



    public NginxNginxSuccessV1Indexer(BaseWorkerConfig config) {
        super(config);
        this.config= (BaseIndexerConfig) config;

    }

    public NginxNginxSuccessV1Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
        this.config= (BaseIndexerConfig) config;

    }

    @Override
    public boolean register() {

        patterns = "%{IPORHOST:remoteAddr} - (?<remoteUser>\\w+|-) \\[%{HTTPDATE:timeLocal}\\] \"(?<request>.+)\" (?<status>\\d+) (?<bodyBytesSent>\\d+|-) \"(?<httpReferer>.*)\" \"(?<httpUserAgent>.*)\"";
        patterns1 = new String[]{
                "((%{WORD:requestMethod} %{DATA:requestUri} (HTTP/%{NUMBER:httpVersion}))|-)",
                "((%{WORD:requestMethod} %{DATA:requestUri})|-)",
                "\\s*((%{DATA:requestUri} (HTTP/%{NUMBER:httpVersion}))|-)",
                "\\s*(%{DATA:requestUri})"
        };
        patterns2=new String[]{
                "%{NOTSPACE}%{WORD:request_file_type}",
                "%{NOTSPACE:request_file_type}",
        };
        grok = GrokUtil.getGrok(patterns);
        groks = GrokUtil.getGroks(patterns1);
        groks2=GrokUtil.getGroks(patterns2);


        return true;
    }

    @Override
    public boolean format(Event event) {

        String message = event.getMessage();

        Map<String, Object> messageMap =  GrokUtil.getMap(grok,message);
        Map<String, Object> formated = event.getFormat();
        formated.putAll(messageMap);


        if (formated.get("request") != null) {
            Map<String, Object> accessLineMap = GrokUtil.getMapByGroks(groks, (String) formated.get("request"));
            formated.putAll(accessLineMap);
        }

        if(formated.get("requestUri")!=null){
            Map<String, Object>  requestUri=  GrokUtil.getMapByGroks(groks2, String.valueOf(formated.get("requestUri")));
            formated.putAll(requestUri);
        }

        if (!config.getIpFilter().equals("null")) {
            GrokUtil.filterGeoIP(config, formated);
        } else {
            setGeoIP(formated);
        }


        //accessTime
        //格式化@timestamp
        String accessTime8601 = (String) formated.get("timeIso8601");
        if (accessTime8601 != null && accessTime8601 != "-") {
            formated.put("@timestamp", accessTime8601);
        } else {
            String accessTime = (String) formated.get("timeLocal");
            if (accessTime != null && accessTime.trim().length() != 0) {
                String time1 = GrokUtil.formTime(accessTime, "dd/MMM/yyyy:HH:mm:ss Z", "yyyy-MM-dd'T'HH:mm:ssXXX");
                formated.put("@timestamp", time1);
            }
        }
        formated.put("message", message);
        formated.put("path",event.getKey());
        //格式化Metafield
        event.setMetafieldLoglevel("1");
        MetafieldHelper.setMetafield(event,"","remoteAddr","remoteAddr",formated);
        if (event.getSource() != null) {
            //time:10:13 AM 2019/10/28配合告警
            event.setMetafieldSource(event.getSource());
        }
        if(event.getMetafieldSource()==null&&event.getClientIP()!=null)
        {
            event.setMetafieldSource(event.getClientIP());
        }
        if (formated.get("flag") == "解析失败"){
            OffsetDateTime date=OffsetDateTime.now();
            formated.put("@timestamp", String.valueOf(date));
            return false;
        }

        return true;
    }


    public static void setGeoIP(Map formated) {
        String accessIP = (String) formated.get("remoteAddr");

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
                + "\"request_file_type\":{\"type\":\"keyword\"},"
                + "\"log_source\":{\"type\":\"keyword\"},"
                + "\"message\":{\"type\":\"text\"},"
                + "\"path\":{\"type\":\"keyword\"},"
                + "\"remoteUser\":{\"type\":\"keyword\"},"
                + "\"request\":{\"type\":\"text\"},"
                + "\"requestMethod\":{\"type\":\"keyword\"},"
                + "\"requestUri\":{\"type\":\"keyword\"},"
                + "\"httpUserAgent\":{\"type\":\"keyword\"},"
                + "\"httpVersion\":{\"type\":\"keyword\"},"
                + "\"bodyBytesSent\":{\"type\":\"integer\"},"
                + "\"httpReferer\":{\"type\":\"keyword\"},"
                + "\"remoteAddr\":{\"type\":\"ip\"},"
                + "\"status\":{\"type\":\"integer\"},"
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
        return GsonHelper.fromJson(mapping);
    }

    public static String getMappingString() {
        String mapping = "{\"properties\":{"
                + "\"@timestamp\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
                + "\"received_at\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
                + "\"portedAt\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
                + "\"flag\":{\"type\":\"keyword\"},"
                + "\"request_file_type\":{\"type\":\"keyword\"},"
                + "\"log_source\":{\"type\":\"keyword\"},"
                + "\"message\":{\"type\":\"text\"},"
                + "\"path\":{\"type\":\"keyword\"},"
                + "\"remoteUser\":{\"type\":\"keyword\"},"
                + "\"request\":{\"type\":\"text\"},"
                + "\"requestMethod\":{\"type\":\"keyword\"},"
                + "\"requestUri\":{\"type\":\"keyword\"},"
                + "\"httpUserAgent\":{\"type\":\"keyword\"},"
                + "\"httpVersion\":{\"type\":\"keyword\"},"
                + "\"bodyBytesSent\":{\"type\":\"integer\"},"
                + "\"httpReferer\":{\"type\":\"keyword\"},"
                + "\"remoteAddr\":{\"type\":\"ip\"},"
                + "\"status\":{\"type\":\"integer\"},"
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
