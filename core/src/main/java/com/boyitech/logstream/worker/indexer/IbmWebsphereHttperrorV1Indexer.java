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
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * @author juzheng

 * @date 3:14 PM
 * @Description:
 */
public class IbmWebsphereHttperrorV1Indexer extends BaseIndexer {
    private String[] patterns1;
    private ArrayList<Grok> groks1;

    public IbmWebsphereHttperrorV1Indexer(BaseWorkerConfig config) {
        super(config);
    }

    public IbmWebsphereHttperrorV1Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
    }

    @Override
    public boolean register() {
        patterns1=new String[]{
                "^(\\<(%{NUMBER:id})\\>)(%{NOTSPACE:type})\\[(%{NUMBER:process_id})\\]\\:(\\s+)\\[\\w{3} (?<timestamp>%{MONTH} %{MONTHDAY} %{TIME} %{YEAR})\\] \\[(%{NOTSPACE:log_severity})\\]\\s\\[client %{IP:client_ip}\\] (%{DATA:error_reason})\\: (%{GREEDYDATA:error_path})",
                "\\[\\w{3} (?<timestamp>%{MONTH} %{MONTHDAY} %{TIME} %{YEAR})\\] \\[(%{NOTSPACE:log_severity})\\]\\s\\[client %{IP:client_ip}\\] (%{DATA:error_reason})\\: (%{DATA:error_path}), referer\\: (%{GREEDYDATA:referer_uri})"
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
        format.put("message",message);

        //格式化时间 Jun 30 23:58:52 2015
        String timestamp = (String) format.get("timestamp");
        if (timestamp != null && timestamp.trim().length() != 0) {
            Date time = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd HH:mm:ss yyyy", Locale.US);
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
//            //  format.put("Metafield_object", format.get("client_ip"));
//        }
//        if (format.get("dst_ip") != null) {
//            event.setSource(String.valueOf(format.get("dst_ip")));
//            //  format.put("Metafield_subject", format.get("dst_ip"));
//        }else {
//            format.put("Metafield_subject", event.getSource());
//        }
        MetafieldHelper.setMetafield(event,"client_ip","","",format);

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
                + "\"type\":{\"type\":\"keyword\"},"
                + "\"process_id\":{\"type\":\"keyword\"},"
                + "\"timestamp\":{\"type\":\"keyword\"},"
                + "\"log_severity\":{\"type\":\"keyword\"},"
                + "\"client_ip\":{\"type\":\"keyword\"},"
                + "\"error_reason\":{\"type\":\"keyword\"},"
                + "\"error_path\":{\"type\":\"keyword\"},"
                + "\"referer_uri\":{\"type\":\"keyword\"},"
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
                + "\"type\":{\"type\":\"keyword\"},"
                + "\"process_id\":{\"type\":\"keyword\"},"
                + "\"timestamp\":{\"type\":\"keyword\"},"
                + "\"log_severity\":{\"type\":\"keyword\"},"
                + "\"client_ip\":{\"type\":\"keyword\"},"
                + "\"error_reason\":{\"type\":\"keyword\"},"
                + "\"error_path\":{\"type\":\"keyword\"},"
                + "\"referer_uri\":{\"type\":\"keyword\"},"
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
