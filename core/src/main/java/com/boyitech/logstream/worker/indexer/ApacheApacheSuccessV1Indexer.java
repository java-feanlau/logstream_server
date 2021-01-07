package com.boyitech.logstream.worker.indexer;


import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.*;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import com.boyitech.logstream.core.worker.indexer.BaseIndexerConfig;
import io.krakens.grok.api.Grok;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * @author Eric
 * @Title: NginxIndexer
 * @date 2018/11/12 16:06
 * @Description: TODO
 */
public class ApacheApacheSuccessV1Indexer extends BaseIndexer {

    private String[] patterns1;
    private ArrayList<Grok> groks1;
    private BaseIndexerConfig config;


    public ApacheApacheSuccessV1Indexer(BaseWorkerConfig config) {
        super(config);
        this.config = (BaseIndexerConfig) config;
    }

    public ApacheApacheSuccessV1Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
        this.config = (BaseIndexerConfig) config;
    }

    @Override
    public boolean register() {

        patterns1 = new String[]{
                "^%{IP:remoteAddr} - %{USER:remoteUser} \\[%{HTTPDATE:timeLocal}\\] \"((%{WORD:requestMethod} %{DATA:request} (HTTP/%{NUMBER:httpVersion}))|-)\" %{NUMBER:status} (?:%{NUMBER:bodyBytesSent}|-)",
                "^%{IP:remoteAddr} - %{USER:remoteUser} \\[%{HTTPDATE:timeLocal}\\] \"((%{WORD:requestMethod} %{DATA:request})|-)\" %{NUMBER:status} (?:%{NUMBER:bodyBytesSent}|-)",
                "^%{IP:remoteAddr} - %{USER:remoteUser} \\[%{HTTPDATE:timeLocal}\\] \"\\s*((%{DATA:request} (HTTP/%{NUMBER:httpVersion}))|-)\" %{NUMBER:status} (?:%{NUMBER:bodyBytesSent}|-)",
                "^%{IP:remoteAddr} - %{USER:remoteUser} \\[%{HTTPDATE:timeLocal}\\] \"\\s*(%{DATA:request})\" %{NUMBER:status} (?:%{NUMBER:bodyBytesSent}|-)"
        };

        groks1 = GrokUtil.getGroks(patterns1);

        return true;
    }

    @Override
    public boolean format(Event event) {

        String message = event.getMessage();
        Map<String, Object> messageMap = GrokUtil.getMapByGroks(groks1, message);
        Map<String, Object> format = event.getFormat();
        format.put("message", message);
        format.putAll(messageMap);
        if (format.get("flag") == null && format.get("flag") != "解析失败") {
            //event.setMetafieldLoglevel("1");
            event.setMetafieldLoglevel("1");
        }

        if (!config.getIpFilter().equals("null")) {
            GrokUtil.filterGeoIP(config, format);
        } else {
            setGeoIP(format);
        }

        //accessTime
        //格式化@timestamp
        IndexerTimeUtils.getISO8601Time2(format, "timeLocal", "dd/MMM/yyyy:HH:mm:ss Z");

        MetafieldHelper.setMetafield(event,"","remoteAddr","remoteAddr",format);
        format.put("path", event.getKey());
        if (event.getSource() != null) {
            format.put("log_source", event.getSource());//增加来源设备标识；
            event.setMetafieldSource(event.getSource());
        }
        if(event.getMetafieldSource()==null&&event.getClientIP()!=null)
        {
            event.setMetafieldSource(event.getClientIP());
        }
        if(format.get("request")!=null){
            format.put("request_uri",format.get("request"));
        }
        if (format.get("flag") == "解析失败")
            return false;
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
                + "\"message\":{\"type\":\"text\"},"
                + "\"path\":{\"type\":\"keyword\"},"
                + "\"log_source\":{\"type\":\"keyword\"},"
                + "\"remoteUser\":{\"type\":\"keyword\"},"
                + "\"request\":{\"type\":\"text\"},"
                + "\"request_uri\":{\"type\":\"keyword\"},"
                + "\"requestMethod\":{\"type\":\"keyword\"},"
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
                + "\"message\":{\"type\":\"text\"},"
                + "\"path\":{\"type\":\"keyword\"},"
                + "\"log_source\":{\"type\":\"keyword\"},"
                + "\"remoteUser\":{\"type\":\"keyword\"},"
                + "\"request\":{\"type\":\"text\"},"
                + "\"request_uri\":{\"type\":\"keyword\"},"
                + "\"requestMethod\":{\"type\":\"keyword\"},"
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
