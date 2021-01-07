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
import java.util.Date;
import java.util.Map;

/**
 * @author Eric

 * @date 2018/12/14 15:18
 * @Description: TODO
 */
public class LinuxLinuxAuditV1Indexer extends BaseIndexer {

    private String[] patterns1;
    private ArrayList<Grok> groks1;

    public LinuxLinuxAuditV1Indexer(BaseWorkerConfig config) {
        super(config);
    }

    public LinuxLinuxAuditV1Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
    }

    @Override
    public boolean register() {

        patterns1 = new String[]{
                "(type=%{WORD:type}\\s+|)(msg=%{DATA:msg}:\\s+|)(pid=%{NUMBER:pid}\\s+|)" +
                        "(uid=%{NUMBER:uid}\\s+|)(auid=%{NUMBER:auid}\\s+|)(ses=%{NUMBER:ses}\\s+|)" +
                        "(subj=%{NOTSPACE:subj}\\s+|)(msg1=%{NOTSPACE:msg}\\s+|)(op=%{NOTSPACE:op}\\s+|)" +
                        "(grantors=%{NOTSPACE:grantors}\\s+|)(acct=\"%{DATA:acct}\"\\s+|)" +
                        "(exe=\"%{DATA:exe}\"\\s+|)(hostname=%{NOTSPACE:hostname}\\s+|)(addr=%{NOTSPACE:addr}\\s+|)" +
                        "(terminal=%{DATA:terminal}\\s+|)(res=%{NOTSPACE:res}|)"
        };
        groks1 = GrokUtil.getGroks(patterns1);

        return true;
    }

    @Override
    public boolean format(Event event) {
        /**
         * type=LOGIN msg=audit(1544741401.431:79109): pid=1075 uid=0 subj=system_u:system_r:crond_t:s0-s0:c0.c1023 old-auid=4294967295 auid=0 tty=(none) old-ses=4294967295 ses=11007 res=1
         * type=USER_START msg=audit(1544741401.514:79110): pid=1074 uid=0 auid=0 ses=11006 subj=system_u:system_r:crond_t:s0-s0:c0.c1023 msg='op=PAM:session_open grantors=pam_loginuid,pam_keyinit,pam_limits,pam_systemd acct="root" exe="/usr/sbin/crond" hostname=? addr=? terminal=cron res=success'
         */

        String message = event.getMessage();
        Map<String, Object> format = event.getFormat();
        Map<String, Object> map = GrokUtil.getMapByGroks(groks1, message);
        format.putAll(map);
//        format.put("path","/var/log/audit/audit.log");
//        format.put("log_type","linux-audit");

        String pattern = "yyyy-MM-dd'T'HH:mm:ssZ";
        String format1 = DateFormatUtils.format(new Date(), pattern);
        format.put("@timestamp", format1);


        //格式化Metafield
        event.setMetafieldLoglevel("1");
        // format.put("Metafield_category", "Security");
//        if (event.getLogType() != null) {
//            format.put("Metafield_description", event.getLogType());
//            format.put("Metafield_type", event.getLogType());
//        }
//        if (event.getSource() != null) {
//            event.setSource(event.getSource());
//            //format.put("Metafield_source", event.getSource());
//        }
//
//        if (format.get("src_ip") != null) {
//            event.setSource(String.valueOf(format.get("src_ip")));
//            // format.put("Metafield_object", format.get("src_ip"));
//        }
//        if (format.get("dst_ip") != null) {
//            event.setSource(String.valueOf(format.get("dst_ip")));
//            // format.put("Metafield_subject", format.get("dst_ip"));
//        } else {
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
                + "\"type\":{\"type\":\"keyword\"},"
                + "\"msg\":{\"type\":\"text\"},"
                + "\"pid\":{\"type\":\"keyword\"},"
                + "\"uid\":{\"type\":\"keyword\"},"
                + "\"auid\":{\"type\":\"keyword\"},"
                + "\"ses\":{\"type\":\"keyword\"},"
                + "\"subj\":{\"type\":\"keyword\"},"
                + "\"msg1\":{\"type\":\"keyword\"},"
                + "\"op\":{\"type\":\"keyword\"},"
                + "\"grantors\":{\"type\":\"keyword\"},"
                + "\"acct\":{\"type\":\"keyword\"},"
                + "\"exe\":{\"type\":\"keyword\"},"
                + "\"hostname\":{\"type\":\"keyword\"},"
                + "\"addr\":{\"type\":\"keyword\"},"
                + "\"terminal\":{\"type\":\"keyword\"},"
                + "\"res\":{\"type\":\"keyword\"},"
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
                + "\"type\":{\"type\":\"keyword\"},"
                + "\"msg\":{\"type\":\"text\"},"
                + "\"pid\":{\"type\":\"keyword\"},"
                + "\"uid\":{\"type\":\"keyword\"},"
                + "\"auid\":{\"type\":\"keyword\"},"
                + "\"ses\":{\"type\":\"keyword\"},"
                + "\"subj\":{\"type\":\"keyword\"},"
                + "\"msg1\":{\"type\":\"keyword\"},"
                + "\"op\":{\"type\":\"keyword\"},"
                + "\"grantors\":{\"type\":\"keyword\"},"
                + "\"acct\":{\"type\":\"keyword\"},"
                + "\"exe\":{\"type\":\"keyword\"},"
                + "\"hostname\":{\"type\":\"keyword\"},"
                + "\"addr\":{\"type\":\"keyword\"},"
                + "\"terminal\":{\"type\":\"keyword\"},"
                + "\"res\":{\"type\":\"keyword\"},"
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
