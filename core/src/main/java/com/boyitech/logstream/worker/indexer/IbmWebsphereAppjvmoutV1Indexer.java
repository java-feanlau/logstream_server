package com.boyitech.logstream.worker.indexer;


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
import java.util.Map;

/**
 * @author juzheng
 * @date 3:44 PM
 * @Description:
 */
public class IbmWebsphereAppjvmoutV1Indexer extends BaseIndexer {
    private String pattern1;
    private Grok grok1;

    public IbmWebsphereAppjvmoutV1Indexer(BaseWorkerConfig config) {
        super(config);
    }

    public IbmWebsphereAppjvmoutV1Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
    }

    @Override
    public boolean register() {

        pattern1="^\\[(?<timestamp>%{YEAR}-%{MONTHNUM}-%{MONTHDAY} %{TIME})\\:\\d{3} CST\\] (%{NOTSPACE:seq_num}) SystemOut(\\s+)(%{GREEDYDATA:system-out})";
        grok1 = GrokUtil.getGrok(pattern1);

        return true;
    }

    @Override
    public boolean format(Event event) {

        String message = event.getMessage();
        Map<String, Object> map = GrokUtil.getMap(grok1, message);
        Map<String, Object> format = event.getFormat();
        format.putAll(map);
        format.put("message",message);

        //格式化时间 15-6-19 14:01:38
        String timestamp = (String) format.get("timestamp");
        if (timestamp != null && timestamp.trim().length() != 0) {
            Date time = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
            try {
                time = sdf.parse(timestamp);
            } catch (ParseException e) {
                LOGGER.error("@timestamp时间格式化出错");
            }
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
            timestamp = sdf2.format(time);
            format.put("@timestamp", timestamp);
        }


        event.setMetafieldLoglevel("1");
//        if (event.getLogType() != null) {
//            format.put("Metafield_description", event.getLogType());
//            format.put("Metafield_type", event.getLogType());
//        }
//        if (event.getSource() != null) {
//            event.setSource(event.getSource());
//           // format.put("Metafield_source", event.getSource());
//        }
//
//        if (format.get("client_ip") != null) {
//            event.setSource(String.valueOf(format.get("client_ip")));
//            // format.put("Metafield_object", format.get("client_ip"));
//        }
//        if (format.get("dst_ip") != null) {
//            event.setSource(String.valueOf(format.get("dst_ip")));
//            //  format.put("Metafield_subject", format.get("dst_ip"));
//        }else {
//            format.put("Metafield_subject", event.getSource());
//        }
        MetafieldHelper.setMetafield(event,"","","",format);
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
                + "\"timestamp\":{\"type\":\"keyword\"},"
                + "\"seq_num\":{\"type\":\"keyword\"},"
                + "\"system-out\":{\"type\":\"keyword\"},"
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
                + "\"seq_num\":{\"type\":\"keyword\"},"
                + "\"system-out\":{\"type\":\"keyword\"},"
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
