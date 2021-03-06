package com.boyitech.logstream.worker.indexer;


import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.GrokUtil;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.MetafieldHelper;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import io.krakens.grok.api.Grok;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @author juzheng
 * @date 3:33 PM
 * @Description:
 */
public class PanabitPanabitAppV1Indexer extends BaseIndexer {
    private String pattern1;
    private Grok grok1;

    public PanabitPanabitAppV1Indexer(BaseWorkerConfig config) {
        super(config);
    }

    public PanabitPanabitAppV1Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
    }

    @Override
    public boolean register() {

        pattern1="<PNB%{NUMBER:id}>%{NOTSPACE:panabit-log-type}\\s*%{NUMBER:unix_time}\\s*%{NOTSPACE:dns_fqdn}\\s*%{IP:client_ip}\\s*%{IP:server_ip}$";
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

        //格式化时间
        Long timestamp = Long.valueOf((String)format.get("unix_time"))*1000L;
        Date time = new Date(Long.valueOf(timestamp));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        String unix_time=sdf.format(time);
        format.put("@timestamp", unix_time);

        //格式化Metafield
        event.setMetafieldLoglevel("1");
//        if (event.getLogType() != null) {
//            format.put("Metafield_description", event.getLogType());
//            format.put("Metafield_type", event.getLogType());
//        }
//        if (event.getSource() != null) {
//            format.put("Metafield_source", event.getSource());
//        }
//
//        if (format.get("client_ip") != null) {
//            format.put("Metafield_object", format.get("client_ip"));
//        }
//        if (format.get("server_ip") != null) {
//            format.put("Metafield_subject", format.get("server_ip"));
//        }else {
//            format.put("Metafield_subject", event.getSource());
//        }
        MetafieldHelper.setMetafield(event,"client_ip","server_ip","",format);
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
                + "\"panabit-log-type\":{\"type\":\"keyword\"},"
                + "\"unix_time\":{\"type\":\"keyword\"},"
                + "\"dns_fqdn\":{\"type\":\"keyword\"},"
                + "\"client_ip\":{\"type\":\"keyword\"},"
                + "\"server_ip\":{\"type\":\"keyword\"},"
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
                + "\"panabit-log-type\":{\"type\":\"keyword\"},"
                + "\"unix_time\":{\"type\":\"keyword\"},"
                + "\"dns_fqdn\":{\"type\":\"keyword\"},"
                + "\"client_ip\":{\"type\":\"keyword\"},"
                + "\"server_ip\":{\"type\":\"keyword\"},"
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
