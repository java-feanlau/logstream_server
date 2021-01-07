package com.boyitech.logstream.worker.indexer;


import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.GeoIPHelper;
import com.boyitech.logstream.core.util.GrokUtil;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.MetafieldHelper;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import com.boyitech.logstream.core.worker.BaseWorker;
import com.boyitech.logstream.core.worker.indexer.BaseIndexerConfig;
import io.krakens.grok.api.Grok;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.function.ObjIntConsumer;

/**
 * @author Eric
 * @date 2018/11/12 16:06
 * @Description: TODO
 */
public class NginxNginxErrorV1Indexer extends BaseIndexer {



    private  ArrayList<Grok> groks1;
    private  ArrayList<Grok> groks2;
    private  ArrayList<Grok> groks3;
    private String[] patterns1;
    private String[] patterns2;
    private String[] patterns3;
    private  ArrayList<Grok> groks4;
    private String[] patterns4;
    private BaseIndexerConfig config;


    public NginxNginxErrorV1Indexer(BaseWorkerConfig config) {
        super(config);
        this.config= (BaseIndexerConfig) config;

    }

    public NginxNginxErrorV1Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
        this.config= (BaseIndexerConfig) config;

    }

    @Override
    public boolean register() {

        patterns1 = new String[]{
                "(?<timestamp>%{YEAR}/%{MONTHNUM}/%{MONTHDAY} %{TIME}) \\[%{DATA:severity}\\] (%{NUMBER:pid}#%{NUMBER}: \\*%{NUMBER}|\\*%{NUMBER}) %{GREEDYDATA:error_message_full}",
                "(?<timestamp>%{YEAR}/%{MONTHNUM}/%{MONTHDAY} %{TIME}) \\[%{DATA:severity}\\] %{GREEDYDATA:error_message_full}",
                "(?<timestamp>%{YEAR}/%{MONTHNUM}/%{MONTHDAY} %{TIME}) %{GREEDYDATA:error_message_full}"
        };
        patterns2 = new String[]{
                "((%{WORD:requestMethod} %{DATA:requestUri} (HTTP/%{NUMBER:httpVersion}))|-)",
                "((%{WORD:requestMethod} %{DATA:requestUri})|-)",
                "\\s*((%{DATA:requestUri} (HTTP/%{NUMBER:httpVersion}))|-)",
                "\\s*(%{DATA:requestUri})"
        };

        patterns3 = new String[]{
                "%{DATA}client: %{IP:client_ip},%{GREEDYDATA}",
                "%{DATA}%{IP:client_ip}%{DATA}",
                "%{GREEDYDATA}"
        };
        patterns4=new String[]{
                "%{DATA}%{IP:client_ip}%{DATA}",
                "%{GREEDYDATA}"
        };

        groks1 = GrokUtil.getGroks(patterns1);
        groks2 = GrokUtil.getGroks(patterns2);
        groks3 = GrokUtil.getGroks(patterns3);
        groks4 = GrokUtil.getGroks(patterns4);



        return true;
    }


    @Override
    public boolean format(Event event) {

        String message = event.getMessage();
        Map<String, Object> messageMap = GrokUtil.getMapByGroks(groks1, message);
        Map<String, Object> formated = event.getFormat();
        formated.put("message", message);
        formated.putAll(messageMap);


        String error_message_full = (String) formated.get("error_message_full");
        Map<String, Object> map1 = GrokUtil.getMapByGroks(groks2, error_message_full);

        String requestUri=String.valueOf(formated.get("requestUri"));
        if(GrokUtil.isStringHasValue(requestUri)) {
            Map<String, Object> map2 = GrokUtil.getMapByGroks(groks3, requestUri);
            formated.putAll(map2);
        }
        if(GrokUtil.isStringHasValue(error_message_full)){
            Map<String, Object> map3 = GrokUtil.getMapByGroks(groks4, error_message_full);
            formated.putAll(map3);
        }

        formated.putAll(map1);

        //setGeoIP(formated);
        if (!config.getIpFilter().equals("null")) {
            GrokUtil.filterGeoIP(config, formated);
        } else {
            GrokUtil.setGeoIP2(formated,"client_ip");
        }

        //accessTime
        //格式化@timestamp
        String timestamp = (String) formated.get("timestamp");
        if (timestamp != null && timestamp.trim().length() != 0) {
            Date time = new Date();
            //2018-09-09 19:15:14
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
            try {
                time = sdf.parse(timestamp);
            } catch (ParseException e) {
                BaseWorker.LOGGER.error("@timestamp时间格式化出错");
            }
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
            timestamp = sdf2.format(time);
            formated.put("@timestamp", timestamp);
        }

        formated.put("path",event.getKey());
        //格式化Metafield
        event.setMetafieldLoglevel("1");

        MetafieldHelper.setMetafield(event,"","client_ip","client_ip",formated);
        if (event.getSource() != null) {
            //time:10:13 AM 2019/10/28配合告警
            event.setMetafieldSource(event.getSource());
        }
        if(event.getMetafieldSource()==null&&event.getClientIP()!=null)
        {
            event.setMetafieldSource(event.getClientIP());
        }
        if (formated.get("flag") == "解析失败") {
            OffsetDateTime date=OffsetDateTime.now();
            formated.put("@timestamp", String.valueOf(date));
            return false;
        }
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
                + "\"log_source\":{\"type\":\"keyword\"},"
                + "\"severity\":{\"type\":\"keyword\"},"
                + "\"error_message\":{\"type\":\"keyword\"},"
                + "\"error_message_full\":{\"type\":\"keyword\"},"
                + "\"request_method\":{\"type\":\"keyword\"},"
                + "\"http_version\":{\"type\":\"keyword\"},"
                + "\"path\":{\"type\":\"keyword\"},"
                + "\"request_host\":{\"type\":\"keyword\"},"
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
                + "\"pid\":{\"type\":\"integer\"},"
                + "\"request_server\":{\"type\":\"keyword\"},"
                + "\"client_ip\":{\"type\":\"ip\"},"
                + "\"requestUri\":{\"type\":\"keyword\"},"
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
                + "\"log_source\":{\"type\":\"keyword\"},"
                + "\"severity\":{\"type\":\"keyword\"},"
                + "\"error_message\":{\"type\":\"keyword\"},"
                + "\"error_message_full\":{\"type\":\"keyword\"},"
                + "\"request_method\":{\"type\":\"keyword\"},"
                + "\"http_version\":{\"type\":\"keyword\"},"
                + "\"path\":{\"type\":\"keyword\"},"
                + "\"request_host\":{\"type\":\"keyword\"},"
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
                + "\"pid\":{\"type\":\"integer\"},"
                + "\"request_server\":{\"type\":\"keyword\"},"
                + "\"client_ip\":{\"type\":\"ip\"},"
                + "\"request_uri\":{\"type\":\"keyword\"},"
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
