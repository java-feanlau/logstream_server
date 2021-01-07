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
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * @author juzheng
 * @date 4:11 PM
 * @Description:
 */
public class A10LoadblanceAllV1Indexer extends BaseIndexer {
    private String[] patterns1;
    private ArrayList<Grok> groks1;
    private BaseIndexerConfig config;

    public A10LoadblanceAllV1Indexer(BaseWorkerConfig config) {
        super(config);
        this.config = (BaseIndexerConfig) config;
    }

    public A10LoadblanceAllV1Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
        this.config = (BaseIndexerConfig) config;
    }

    @Override
    public boolean register() {
        patterns1 = new String[]{
                "^\\<%{NUMBER:id}\\>a10logd:(\\s)\\[%{NOTSPACE:a10-log-type}\\]\\<%{NUMBER:log_id}\\>(\\s)Local authentication failed\\(user\\:(\\s)%{NOTSPACE:user}\\)\\:%{GREEDYDATA:fail_message}",
                "^(\\<%{NUMBER:id}\\>)a10logd:(\\s)\\[%{NOTSPACE:a10-log-type}\\]\\<%{NUMBER:log_id}\\>(\\s)The user, %{NOTSPACE:user},%{GREEDYDATA:the_middle}%{IP:client_ip},%{GREEDYDATA:the_end}",
                "^(\\<%{NUMBER:id}\\>)a10logd:(\\s)\\[%{NOTSPACE:a10-log-type}\\]\\<%{NUMBER:log_id}\\>(\\s)%{CHTIME:http_time}\\s*%{IP:c_ip}\\s*%{IP:s_ip}\\s*%{INT:s_port}\\s*%{NOTSPACE:cs_method}\\s*%{NOTSPACE:cs_uri_stem}\\s*%{DATA:cs_uri_query}\\s*%{IP:n_ip}\\s*%{INT:n_port}\\s*%{INT:sc_status}\\s*%{INT:sc_bytes}\\s*%{INT:cs_bytes}\\s*%{NUMBER:final_time_taken}\\s*%{NOTSPACE:cs_useragent}\\s*%{NOTSPACE:cs_referer}$"
        };
        groks1 = GrokUtil.getGroks(patterns1);
        return true;
    }

    @Override
    public boolean format(Event event) {
        String message = event.getMessage();
        Map<String, Object> map = GrokUtil.getMapByGroks(groks1, message);
        Map<String, Object> format = event.getFormat();
        format.putAll(map);
        format.put("message", message);

        //格式化时间
        IndexerTimeUtils.getISO8601Time2(format, "http_time", "yyyy-MM-dd HH:mm:ss");

        //格式化Metafield
        //event.setMetafieldLoglevel("1");
        event.setMetafieldLoglevel("1");

        MetafieldHelper.setMetafield(event,"client_ip","","",format);
        MetafieldHelper.setMetafield(event,"c_ip","s_ip","n_ip",format);
        if (format.get("flag") == "解析失败")
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
                + "\"id\":{\"type\":\"keyword\"},"
                + "\"a10-log-type\":{\"type\":\"keyword\"},"
                + "\"log_id\":{\"type\":\"keyword\"},"
                + "\"user\":{\"type\":\"keyword\"},"
                + "\"fail_messages\":{\"type\":\"keyword\"},"
                + "\"client_ip\":{\"type\":\"keyword\"},"
                + "\"http_time\":{\"type\":\"keyword\"},"
                + "\"c_ip\":{\"type\":\"keyword\"},"
                + "\"s_ip\":{\"type\":\"keyword\"},"
                + "\"s_port\":{\"type\":\"keyword\"},"
                + "\"cs_method\":{\"type\":\"keyword\"},"
                + "\"cs_uri_stem\":{\"type\":\"keyword\"},"
                + "\"cs_uri_query\":{\"type\":\"keyword\"},"
                + "\"n_ip\":{\"type\":\"keyword\"},"
                + "\"n_port\":{\"type\":\"keyword\"},"
                + "\"sc_status\":{\"type\":\"keyword\"},"
                + "\"sc_bytes\":{\"type\":\"keyword\"},"
                + "\"cs_bytes\":{\"type\":\"keyword\"},"
                + "\"final_time_taken\":{\"type\":\"keyword\"},"
                + "\"cs_useragent\":{\"type\":\"keyword\"},"
                + "\"cs_referer\":{\"type\":\"keyword\"},"
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
                + "\"a10-log-type\":{\"type\":\"keyword\"},"
                + "\"log_id\":{\"type\":\"keyword\"},"
                + "\"user\":{\"type\":\"keyword\"},"
                + "\"fail_messages\":{\"type\":\"keyword\"},"
                + "\"client_ip\":{\"type\":\"keyword\"},"
                + "\"hittp_time\":{\"type\":\"keyword\"},"
                + "\"c_ip\":{\"type\":\"keyword\"},"
                + "\"s_ip\":{\"type\":\"keyword\"},"
                + "\"s_port\":{\"type\":\"keyword\"},"
                + "\"cs_method\":{\"type\":\"keyword\"},"
                + "\"cs_uri_stem\":{\"type\":\"keyword\"},"
                + "\"cs_uri_query\":{\"type\":\"keyword\"},"
                + "\"n_ip\":{\"type\":\"keyword\"},"
                + "\"n_port\":{\"type\":\"keyword\"},"
                + "\"sc_status\":{\"type\":\"keyword\"},"
                + "\"sc_bytes\":{\"type\":\"keyword\"},"
                + "\"cs_bytes\":{\"type\":\"keyword\"},"
                + "\"final_time_taken\":{\"type\":\"keyword\"},"
                + "\"cs_useragent\":{\"type\":\"keyword\"},"
                + "\"cs_referer\":{\"type\":\"keyword\"},"
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
