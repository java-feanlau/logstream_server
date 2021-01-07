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
 * @date 2018/12/14 9:55
 * @Description: TODO
 */
public class LinuxLinuxGeneralcronV1Indexer extends BaseIndexer {

    private String[] patterns1;
    private ArrayList<Grok> groks1;

    public LinuxLinuxGeneralcronV1Indexer(BaseWorkerConfig config) {
        super(config);
    }

    public LinuxLinuxGeneralcronV1Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
    }

    @Override
    public boolean register() {

        patterns1 = new String[]{
                "(?<timestamp>%{MONTH}(\\s+)%{MONTHDAY}(\\s+)%{TIME})\\s+%{NOTSPACE:logsource}\\s+%{DATA:program}\\[%{NUMBER:pid}\\]:\\s\\(%{WORD:user}\\)\\s+%{WORD:action}\\s+%{DATA:des}$",
                "(?<timestamp>%{MONTH}(\\s+)%{MONTHDAY}(\\s+)%{TIME})\\s+%{NOTSPACE:logsource}\\s+%{DATA:program}\\[%{NUMBER:pid}\\]:\\s+%{DATA:action}$"
        };
        groks1 = GrokUtil.getGroks(patterns1);

        return true;
    }

    @Override
    public boolean format(Event event) {
        String message = event.getMessage();
        Map<String, Object> map = GrokUtil.getMapByGroks(groks1, message);
        Map<String, Object> formated = event.getFormat();
        formated.putAll(map);
//        formated.put("log_type","linux-general-cron");
//        formated.put("path","/var/log/cron");



        //格式化时间
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        String timestamp = (String) formated.get("timestamp");
        if (timestamp != null && timestamp.trim().length() != 0) {
            timestamp = year + " " + timestamp;
            timestamp = GrokUtil.formTime(timestamp, "yyyy MMM dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ssXXX");
        } else {
            String pattern = "yyyy-MM-dd'T'HH:mm:ssZ";
            timestamp = DateFormatUtils.format(new Date(), pattern);
        }
        formated.put("@timestamp", timestamp);


        //格式化Metafield
        event.setMetafieldLoglevel("1");
        // format.put("Metafield_category", "Security");
//        if (event.getLogType() != null) {
//            formated.put("Metafield_description", event.getLogType());
//            formated.put("Metafield_type", event.getLogType());
//        }
//        if (event.getSource() != null) {
//            event.setSource(event.getSource());
//           // formated.put("Metafield_source", event.getSource());
//        }
//
//        if (formated.get("src_ip") != null) {
//            event.setSource(String.valueOf(formated.get("src_ip")));
//            // formated.put("Metafield_object", formated.get("src_ip"));
//        }
//        if (formated.get("dst_ip") != null) {
//            event.setSource(String.valueOf(formated.get("dst_ip")));
//            // formated.put("Metafield_subject", formated.get("dst_ip"));
//        }else {
//            formated.put("Metafield_subject", event.getSource());
//        }
        MetafieldHelper.setMetafield(event,"","","",formated);

        if (formated.get("flag") == "解析失败")
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
                + "\"logsource\":{\"type\":\"keyword\"},"
                + "\"program\":{\"type\":\"keyword\"},"
                + "\"pid\":{\"type\":\"keyword\"},"
                + "\"user\":{\"type\":\"keyword\"},"
                + "\"action\":{\"type\":\"keyword\"},"
                + "\"des\":{\"type\":\"keyword\"},"
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
                + "\"logsource\":{\"type\":\"keyword\"},"
                + "\"program\":{\"type\":\"keyword\"},"
                + "\"pid\":{\"type\":\"keyword\"},"
                + "\"user\":{\"type\":\"keyword\"},"
                + "\"action\":{\"type\":\"keyword\"},"
                + "\"des\":{\"type\":\"keyword\"},"
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
