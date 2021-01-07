package com.boyitech.logstream.worker.indexer;

import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.GrokUtil;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.MetafieldHelper;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import io.krakens.grok.api.Grok;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;



/**
 * @author Eric

 * @date 2018/12/17 15:14
 * @Description: TODO
 */
public class LinuxLinuxGeneralmessageV1Indexer extends BaseIndexer {
    private String pattern1;
    private Grok grok1;

    public LinuxLinuxGeneralmessageV1Indexer(BaseWorkerConfig config) {
        super(config);
    }

    public LinuxLinuxGeneralmessageV1Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
    }

    @Override
    public boolean register() {

        pattern1 = "(?<timestamp>%{MONTH}(\\s+)%{MONTHDAY}(\\s+)%{TIME})\\s+%{NOTSPACE:form}\\s+%{DATA:user}:\\s+%{DATA:message}$";


        grok1 = GrokUtil.getGrok(pattern1);

        return true;
    }

    @Override
    public boolean format(Event event) {
        String message = event.getMessage();
        Map<String, Object> map = GrokUtil.getMap(grok1, message);
        Map<String, Object> format = event.getFormat();
        format.putAll(map);
//        format.put("type","linux-general-messages");
//        format.put("path","/var/log/messages");
        //格式化时间
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        String timestamp = (String) format.get("timestamp");
        if (timestamp != null && timestamp.trim().length() != 0) {
            timestamp = year + " " + timestamp;
            timestamp = GrokUtil.formTime(timestamp, "yyyy MMM dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ssXXX");
        } else {
            String pattern = "yyyy-MM-dd'T'HH:mm:ssXXX";
            timestamp = DateFormatUtils.format(new Date(), pattern);
        }


        //格式化Metafield
        event.setMetafieldLoglevel("1");
        // format.put("Metafield_category", "Security");
//        if (event.getLogType() != null) {
//            format.put("Metafield_description", event.getLogType());
//            format.put("Metafield_type", event.getLogType());
//        }
//        if (event.getSource() != null) {
//            format.put("Metafield_source", event.getSource());
//        }
//
//        if (format.get("src_ip") != null) {
//            format.put("Metafield_object", format.get("src_ip"));
//        }
//        if (format.get("dst_ip") != null) {
//            format.put("Metafield_subject", format.get("dst_ip"));
//        }else {
//            format.put("Metafield_subject", event.getSource());
//        }
        MetafieldHelper.setMetafield(event,"","","",format);
        format.put("@timestamp", timestamp);

        return false;
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
                + "\"form\":{\"type\":\"keyword\"},"
                + "\"user\":{\"type\":\"keyword\"},"
                + "\"message\":{\"type\":\"keyword\"},"
                + "\"type\":{\"type\":\"keyword\"},"
                + "\"path\":{\"type\":\"keyword\"},"
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
                + "\"form\":{\"type\":\"keyword\"},"
                + "\"user\":{\"type\":\"keyword\"},"
                + "\"message\":{\"type\":\"keyword\"},"
                + "\"type\":{\"type\":\"keyword\"},"
                + "\"path\":{\"type\":\"keyword\"},"
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
