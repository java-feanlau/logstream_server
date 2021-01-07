package com.boyitech.logstream.worker.indexer;


import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.GrokUtil;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.MetafieldHelper;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import io.krakens.grok.api.Grok;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/**
 * @author Eric
 * @date 2018/12/14 16:13
 * @Description: TODO
 */
public class LinuxLinuxOslogV1Indexer extends BaseIndexer {
    private String[] patterns1;
    private ArrayList<Grok> groks1;

    public LinuxLinuxOslogV1Indexer(BaseWorkerConfig config) {
        super(config);
    }

    public LinuxLinuxOslogV1Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
    }

    @Override
    public boolean register() {

        patterns1 = new String[]{
                "<%{NUMBER:id}>%{WORD:log_type}\\[%{NUMBER:process_id}\\]:\\s+%{DATA:log-body}$",
                "<%{NUMBER:id}>\\s?%{DATA:log-body}$"
        };


        groks1 = GrokUtil.getGroks(patterns1);

        return true;
    }

    @Override
    public boolean format(Event event) {
        String message = event.getMessage();
        Map<String, Object> format = event.getFormat();
        Map<String, Object> map = GrokUtil.getMapByGroks(groks1, message);
        format.putAll(map);
        format.put("log_type","sshd");

        String pattern = "yyyy-MM-dd'T'HH:mm:ssZ";
        String format1 = DateFormatUtils.format(new Date(), pattern);
        format.put("@timestamp",format1);


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
                + "\"process_id\":{\"type\":\"keyword\"},"
                + "\"log-body\":{\"type\":\"keyword\"},"
                + "\"format-level\":{\"type\":\"keyword\"},"
                + "\"log_type\":{\"type\":\"keyword\"},"
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
                + "\"process_id\":{\"type\":\"keyword\"},"
                + "\"log-body\":{\"type\":\"keyword\"},"
                + "\"format-level\":{\"type\":\"keyword\"},"
                + "\"log_type\":{\"type\":\"keyword\"},"
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
