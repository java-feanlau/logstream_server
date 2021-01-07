package com.boyitech.logstream.worker.indexer;


import com.boyitech.logstream.core.cache.memory.MemoryCache;
import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.GrokUtil;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.MetafieldHelper;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import io.krakens.grok.api.Grok;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * @author juzheng

 * @date 2019/4/22 9:41 AM
 * @Description:
 */
public class MicrosoftWebiisAllV1Indexer extends BaseIndexer {
    private String pattern1;
    private Grok grok1;

    public MicrosoftWebiisAllV1Indexer(BaseWorkerConfig config) {
        super(config);
    }

    public MicrosoftWebiisAllV1Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
    }

    @Override
    public boolean register() {
        pattern1="(%{CHTIME:http_time})\\s+(%{DATA:s_ip})\\s+%{WORD:cs_method}\\s+(%{DATA:cs-uri-stem})\\s+(%{DATA:cs-uri-query})\\s+(%{NUMBER:s_port})\\s+(%{GREEDYDATA:cs-username})\\s+(%{DATA:c_ip})\\s+(%{GREEDYDATA:cs-user_agent})\\s+(%{NUMBER:sc-status})\\s+(%{NUMBER:sc-substatus})\\s+(%{NUMBER:sc-win32-status})\\s+(%{NUMBER:time-taken})";
        grok1 = GrokUtil.getGrok(pattern1);
        return true;
    }

    @Override
    public boolean format(Event event) {
        String message = event.getMessage();
        Map<String, Object> map = GrokUtil.getMap(grok1, message);
        Map<String, Object> format = event.getFormat();
        format.putAll(map);
        format.put("message", message);

        //格式化时间 2015-06-27 00:32:29
        String timestamp = (String) format.get("http_time");
        if (timestamp != null && timestamp.trim().length() != 0) {
            Date time = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            try {
                time = sdf.parse(timestamp);
            } catch (ParseException e) {
                LOGGER.error("@timestamp时间格式化出错");
            }
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
            timestamp = sdf2.format(time);
            format.put("@timestamp", timestamp);
        }

        //格式化Metafield
        event.setMetafieldLoglevel("1");
        MetafieldHelper.setMetafield(event,"","c_ip","s_ip",format);
        if (event.getSource() != null) {
            //time:10:13 AM 2019/10/28配合告警
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
                + "\"http_time\":{\"type\":\"keyword\"},"
                + "\"s_ip\":{\"type\":\"keyword\"},"
                + "\"cs_method\":{\"type\":\"keyword\"},"
                + "\"cs-uri-stem\":{\"type\":\"keyword\"},"
                + "\"cs-uri-query\":{\"type\":\"keyword\"},"
                + "\"s_port\":{\"type\":\"keyword\"},"
                + "\"cs-username\":{\"type\":\"keyword\"},"
                + "\"c_ip\":{\"type\":\"keyword\"},"
                + "\"cs-user_agent\":{\"type\":\"keyword\"},"
                + "\"sc-status\":{\"type\":\"keyword\"},"
                + "\"sc-substatus\":{\"type\":\"keyword\"},"
                + "\"sc-win32-status\":{\"type\":\"keyword\"},"
                + "\"time-taken\":{\"type\":\"keyword\"},"
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
                + "\"http_time\":{\"type\":\"keyword\"},"
                + "\"s_ip\":{\"type\":\"keyword\"},"
                + "\"cs_method\":{\"type\":\"keyword\"},"
                + "\"cs-uri-stem\":{\"type\":\"keyword\"},"
                + "\"cs-uri-query\":{\"type\":\"keyword\"},"
                + "\"s_port\":{\"type\":\"keyword\"},"
                + "\"cs-username\":{\"type\":\"keyword\"},"
                + "\"c_ip\":{\"type\":\"keyword\"},"
                + "\"cs-user_agent\":{\"type\":\"keyword\"},"
                + "\"sc-status\":{\"type\":\"keyword\"},"
                + "\"sc-substatus\":{\"type\":\"keyword\"},"
                + "\"sc-win32-status\":{\"type\":\"keyword\"},"
                + "\"time-taken\":{\"type\":\"keyword\"},"
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
