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
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * @author Eric
 * @date 2018/12/14 16:13
 * @Description: TODO
 */
public class LinuxLinuxGeneralsecureV1Indexer extends BaseIndexer {
    private String pattern1;
    private Grok grok1;

    public LinuxLinuxGeneralsecureV1Indexer(BaseWorkerConfig config) {
        super(config);
    }

    public LinuxLinuxGeneralsecureV1Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
    }

    @Override
    public boolean register() {

        pattern1 =   "(?<timestamp>%{MONTH}(\\s+)%{MONTHDAY}(\\s+)%{TIME})\\s+%{DATA:host-name}\\s+%{DATA:log_body}$";


        grok1 = GrokUtil.getGrok(pattern1);

        return true;
    }

    @Override
    public boolean format(Event event) {
        //Dec 14 16:19:42 http1 sudo:  monitor : TTY=unknown ; PWD=/home/monitor ; USER=root ; COMMAND=/bin/bash /home/monitor/logagent/process/axsh_process.sh
        String[] s = {
                "(?<timestamp>%{MONTH}(\\s+)%{MONTHDAY}(\\s+)%{TIME})\\s+%{DATA:host-name}\\s+%{DATA:log_body}$"
        };
        String message = event.getMessage();
        Map<String, Object> format = event.getFormat();
        Map<String, Object> map = GrokUtil.getMap(grok1, message);
        format.putAll(map);
//        format.put("log_type","sshd");


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

        format.put("@timestamp", timestamp);


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
                + "\"host-name\":{\"type\":\"keyword\"},"
                + "\"log_body\":{\"type\":\"keyword\"},"
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
                + "\"host-name\":{\"type\":\"keyword\"},"
                + "\"log_body\":{\"type\":\"keyword\"},"
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
