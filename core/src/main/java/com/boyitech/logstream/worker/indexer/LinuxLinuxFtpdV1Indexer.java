package com.boyitech.logstream.worker.indexer;

import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.GrokUtil;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.MetafieldHelper;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import io.krakens.grok.api.Grok;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * @author Eric
 * @date 2018/12/17 9:17
 * @Description: TODO
 */
public class LinuxLinuxFtpdV1Indexer extends BaseIndexer {
    private String[] patterns1;
    private ArrayList<Grok> groks1;

    public LinuxLinuxFtpdV1Indexer(BaseWorkerConfig config) {
        super(config);
    }

    public LinuxLinuxFtpdV1Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
    }

    @Override
    public boolean register() {

        patterns1 = new String[]{
                "%{DAY:day}\\s+(?<timestamp>%{MONTH}(\\s+)%{MONTHDAY}(\\s+)%{TIME}(\\s+)%{YEAR})\\s+%{NUMBER:time_consuming}\\s+%{IP:server_ip}\\s+%{NUMBER:file_size}\\s+" +
                        "%{NOTSPACE:file_name}\\s+%{WORD:send_type}\\s+%{NOTSPACE:special}\\s+%{NOTSPACE:direction}\\s+%{NOTSPACE:access_patterns}\\s+%{NOTSPACE:name}\\s+" +
                        "%{NOTSPACE:type_name}\\s+%{DATA:authentication_method}\\s+%{NOTSPACE:authenticated_userid}\\s+%{NOTSPACE:comletion_status}"
        };
        groks1 = GrokUtil.getGroks(patterns1);

        return true;
    }

    //send_type: a:ASCII传输。b:二进制文件传输
    //direction：o:FTP服务器->客户端。i：客户端->FTP服务器
    //access_patterns:a：匿名用户。g：来宾用户。r：真实用户
    //type_name:一般为FTP
    @Override
    public boolean format(Event event) {
        String message = event.getMessage();
        Map<String, Object> map = GrokUtil.getMapByGroks(groks1, message);
        Map<String, Object> format = event.getFormat();
        format.putAll(map);
        format.put("message",message);
//        format.put("type","linux-extras-vsftpd");

        //格式化时间
        String timestamp = (String) format.get("timestamp");
        if (timestamp != null && timestamp.trim().length() != 0) {
            Date time = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd HH:mm:ss yyyy", Locale.US);
            try {
                time = sdf.parse(timestamp);
            } catch (ParseException e) {
                LOGGER.error("@timestamp时间格式化出错");
            }
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
            timestamp = sdf2.format(time);
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
//            // format.put("Metafield_object", format.get("src_ip"));
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
                + "\"day\":{\"type\":\"keyword\"},"
                + "\"time_consuming\":{\"type\":\"keyword\"},"
                + "\"server_ip\":{\"type\":\"ip\"},"
                + "\"file_name\":{\"type\":\"keyword\"},"
                + "\"send_type\":{\"type\":\"keyword\"},"
                + "\"special\":{\"type\":\"keyword\"},"
                + "\"direction\":{\"type\":\"keyword\"},"
                + "\"access_patterns\":{\"type\":\"keyword\"},"
                + "\"name\":{\"type\":\"keyword\"},"
                + "\"type_name\":{\"type\":\"keyword\"},"
                + "\"authenticated_userid\":{\"type\":\"keyword\"},"
                + "\"comletion_status\":{\"type\":\"keyword\"},"
                + "\"file_name\":{\"type\":\"keyword\"},"
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
                + "\"day\":{\"type\":\"keyword\"},"
                + "\"time_consuming\":{\"type\":\"keyword\"},"
                + "\"server_ip\":{\"type\":\"ip\"},"
                + "\"file_name\":{\"type\":\"keyword\"},"
                + "\"send_type\":{\"type\":\"keyword\"},"
                + "\"special\":{\"type\":\"keyword\"},"
                + "\"direction\":{\"type\":\"keyword\"},"
                + "\"access_patterns\":{\"type\":\"keyword\"},"
                + "\"name\":{\"type\":\"keyword\"},"
                + "\"type_name\":{\"type\":\"keyword\"},"
                + "\"authenticated_userid\":{\"type\":\"keyword\"},"
                + "\"comletion_status\":{\"type\":\"keyword\"},"
                + "\"file_name\":{\"type\":\"keyword\"},"
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
