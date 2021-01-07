package com.boyitech.logstream.worker.indexer;

import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.GrokUtil;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.MetafieldHelper;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import io.krakens.grok.api.Grok;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * @author Eric
 * @Title: LinuxFtpd
 * @date 2018/12/17 9:17
 * @Description: TODO
 */
public class LinuxLinuxGeneralmailV1Indexer extends BaseIndexer {
    private String pattern1;
    private String[] patterns1;
    private Grok grok1;
    private ArrayList<Grok> groks1;

    public LinuxLinuxGeneralmailV1Indexer(BaseWorkerConfig config) {
        super(config);
    }

    public LinuxLinuxGeneralmailV1Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
    }

    @Override
    public boolean register() {

        pattern1 = "(?<timestamp>%{MONTH}(\\s+)%{MONTHDAY}(\\s+)%{TIME})\\s%{NOTSPACE:host-name}\\s+%{WORD:mail_type}\\[%{NUMBER:id}\\]:\\s%{DATA:log_body}$";
        patterns1 = new String[]{
                "%{DATA:UUID}:\\s%{DATA:to_from}=%{DATA:destination},\\s%{DATA:descption}$",
                "%{DATA:error_information}$"
        };


        grok1 = GrokUtil.getGrok(pattern1);
        groks1 = GrokUtil.getGroks(patterns1);

        return true;
    }


    @Override
    public boolean format(Event event) {

        String message = event.getMessage();
        Map<String, Object> map = GrokUtil.getMap(grok1, message);
        Map<String, Object> format = event.getFormat();
        format.putAll(map);
        format.put("message",message);
//        format.put("type","linux-extras-vsftpd");

        String log_body = (String) format.get("log_body");
        if (log_body !=null && !log_body.trim().equals("")) {
            String[] s1={
                    "%{DATA:UUID}:\\s%{DATA:to_from}=%{DATA:destination},\\s%{DATA:descption}$",
                    "%{DATA:error_information}$"
            };
            Map<String, Object> mapByMuch = GrokUtil.getMapByGroks(groks1, log_body);
            format.putAll(mapByMuch);

        }


        //格式化时间
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        String timestamp = (String) format.get("timestamp");
        if (timestamp != null && timestamp.trim().length() != 0) {
            timestamp = year + " " + timestamp;
            Date time = new Date();

            timestamp = GrokUtil.formTime(timestamp, "yyyy MMM dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ssXXX");
        } else {
            String pattern = "yyyy-MM-dd'T'HH:mm:ssZ";
            timestamp = DateFormatUtils.format(new Date(), pattern);
        }

        format.put("@timestamp", timestamp);

        //格式化Metafield
        event.setMetafieldLoglevel("1");
        // format.put("Metafield_category", "Security");
//        if (event.getLogType() != null) {
//            format.put("Metafield_description", event.getLogType());
//            format.put("Metafield_type", event.getLogType());
//        }
//        if (event.getSource() != null) {
//            event.setSource(event.getSource());
//           // format.put("Metafield_source", event.getSource());
//        }
//
//        if (format.get("src_ip") != null) {
//            event.setSource(String.valueOf(format.get("src_ip")));
//
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
                + "\"host-name\":{\"type\":\"keyword\"},"
                + "\"mail_type\":{\"type\":\"keyword\"},"
                + "\"id\":{\"type\":\"keyword\"},"
                + "\"log_body\":{\"type\":\"keyword\"},"
                + "\"type\":{\"type\":\"keyword\"},"
                + "\"UUID\":{\"type\":\"keyword\"},"
                + "\"to_from\":{\"type\":\"keyword\"},"
                + "\"destination\":{\"type\":\"keyword\"},"
                + "\"descption\":{\"type\":\"keyword\"},"
                + "\"error_information\":{\"type\":\"keyword\"},"
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
                + "\"host-name\":{\"type\":\"keyword\"},"
                + "\"mail_type\":{\"type\":\"keyword\"},"
                + "\"id\":{\"type\":\"keyword\"},"
                + "\"log_body\":{\"type\":\"keyword\"},"
                + "\"type\":{\"type\":\"keyword\"},"
                + "\"UUID\":{\"type\":\"keyword\"},"
                + "\"to_from\":{\"type\":\"keyword\"},"
                + "\"destination\":{\"type\":\"keyword\"},"
                + "\"descption\":{\"type\":\"keyword\"},"
                + "\"error_information\":{\"type\":\"keyword\"},"
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
