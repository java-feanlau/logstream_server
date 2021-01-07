package com.boyitech.logstream.worker.indexer;


import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.GeoIPHelper;
import com.boyitech.logstream.core.util.GrokUtil;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.MetafieldHelper;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import com.boyitech.logstream.core.worker.indexer.BaseIndexerConfig;
import io.krakens.grok.api.Grok;
import io.krakens.grok.api.GrokCompiler;
import io.krakens.grok.api.Match;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Eric
 * @date 2018/11/12 16:06
 * @Description: TODO
 */
public class SquidSquidAccessV1Indexer extends BaseIndexer {
    private static Grok grok;
    private static String patterns;
    private BaseIndexerConfig config;


    public SquidSquidAccessV1Indexer(BaseWorkerConfig config) {
        super(config);
        this.config= (BaseIndexerConfig) config;


    }

    public SquidSquidAccessV1Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
        this.config= (BaseIndexerConfig) config;

    }

    @Override
    public boolean register() {

        patterns = "%{NUMBER:timestamp}\\s+%{NUMBER:response_time}\\s+%{IPORHOST:client_ip}\\s+%{DATA:request_status}/%{DATA:status_code}\\s%{NUMBER:response_bytes}\\s%{WORD:request_method}\\s%{URI:request_uri}\\s%{DATA:remote_user}|-\\s%{WORD:hierarchy_status}/%{NOTSPACE:remote_server}\\s+%{DATA:content_type}$";

        GrokCompiler grokCompiler = GrokCompiler.newInstance();
        grokCompiler.registerDefaultPatterns();
        //2018-10-24 10:32:42
        grokCompiler.register("CHTIME", "%{YEAR}-%{MONTHNUM}-%{MONTHDAY} %{HOUR}:(?:[0-5][0-9]|[0-9]):%{SECOND}");

        grok = grokCompiler.compile(patterns, true);
        return true;
    }

    @Override
    public boolean format(Event event) {
        //1541325462.372     15 172.100.50.180 TCP_MISS/200 4207 GET http://caigou.sbs.edu.cn/euqManage/login.do?returnUrl=/index.do - FIRST_UP_PARENT/caigou text/html
        String message = event.getMessage();
        Match grokMatch = grok.match(message);
        if (grokMatch.isNull()) {
            HashMap<String, Object> flgs = new HashMap<String, Object>();
            flgs.put("flag", "解析失败");
//            System.out.println("格式有误 patterns:" + patterns + " message:" + message);
            return false;
        }


        Map<String, Object> messageMap = grokMatch.capture();
        Map<String, Object> format = event.getFormat();
        format.put("message", message);
        format.putAll(messageMap);
        if (format.get("flag") == null && format.get("flag") != "解析失败") {
            event.setMetafieldLoglevel("1");
        }

        if (!config.getIpFilter().equals("null")) {
            GrokUtil.filterGeoIP(config, format);
        } else {
            setGeoIP(format);
        }

        //accessTime
        //格式化@timestamp
        String timestamp = (String) format.get("timestamp");
        if (timestamp != null && timestamp.trim().length() != 0) {
            String replace = timestamp.replace(".", "");
            Date date = new Date(Long.parseLong(replace));
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
            timestamp = sdf2.format(date);
            format.put("@timestamp", timestamp);
        }

        MetafieldHelper.setMetafield(event,"client_ip","","",format);
        if (event.getSource() != null) {
            //time:10:13 AM 2019/10/28配合告警
            event.setMetafieldSource(event.getSource());
        }
        if(event.getMetafieldSource()==null&&event.getClientIP()!=null)
        {
            event.setMetafieldSource(event.getClientIP());
        }
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
                + "\"timestamp\":{\"type\":\"keyword\"},"
                + "\"response_time\":{\"type\":\"keyword\"},"
                + "\"client_ip\":{\"type\":\"ip\"},"
                + "\"request_status\":{\"type\":\"keyword\"},"
                + "\"status_code\":{\"type\":\"keyword\"},"
                + "\"response_bytes\":{\"type\":\"integer\"},"
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
                + "\"request_method\":{\"type\":\"keyword\"},"
                + "\"request_uri\":{\"type\":\"keyword\"},"
                + "\"remote_user\":{\"type\":\"keyword\"},"
                + "\"hierarchy_status\":{\"type\":\"keyword\"},"
                + "\"remote_server\":{\"type\":\"keyword\"},"
                + "\"content_type\":{\"type\":\"keyword\"},"
                + "\"format-level\":{\"type\":\"integer\"},"
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
                + "\"timestamp\":{\"type\":\"keyword\"},"
                + "\"response_time\":{\"type\":\"keyword\"},"
                + "\"client_ip\":{\"type\":\"ip\"},"
                + "\"request_status\":{\"type\":\"keyword\"},"
                + "\"status_code\":{\"type\":\"keyword\"},"
                + "\"response_bytes\":{\"type\":\"integer\"},"
                + "\"request_method\":{\"type\":\"keyword\"},"
                + "\"request_uri\":{\"type\":\"keyword\"},"
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
                + "\"remote_user\":{\"type\":\"keyword\"},"
                + "\"hierarchy_status\":{\"type\":\"keyword\"},"
                + "\"remote_server\":{\"type\":\"keyword\"},"
                + "\"content_type\":{\"type\":\"keyword\"},"
                + "\"format-level\":{\"type\":\"integer\"},"
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
