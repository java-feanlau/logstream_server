package com.boyitech.logstream.worker.indexer;


import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.GrokUtil;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.MetafieldHelper;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import com.boyitech.logstream.core.worker.indexer.BaseIndexerConfig;
import io.krakens.grok.api.Grok;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author juzheng
 * @date 2019/3/22 9:18 AM
 * @Description:
 */
public class CheckpointFirewallAllV1Indexer extends BaseIndexer {
    private Grok grok_1;//第一层
    private ArrayList<Grok> grok_2_logbody_monitor;//当log_level=Log且action为monitor,
    private ArrayList<Grok> grok_2_logbody_allow;//当log_level=Log且action为allow
    private ArrayList<Grok> grok_2_logbody_alert; // 当log_level=Alert且无action
    private ArrayList<Grok> groks_2_logbody_drop;//当log_level=Log且action为drop
    private ArrayList<Grok> groks_2_logbody_accept;//当log_level=Log且action为accept
    //当action为其他，log_level为其他，抛出
    private String pattern_1;
    private String[] pattern_2_logbody_monitor;
    private String[] pattern_2_logbody_allow;
    private String[] pattern_2_logbody_alert;
    private String[] pattern_2_logbody_drop;
    private String[] pattern_2_logbody_accept;
    private BaseIndexerConfig config;

    public CheckpointFirewallAllV1Indexer(BaseWorkerConfig config) {
        super(config);
        this.config = (BaseIndexerConfig) config;
    }

    public CheckpointFirewallAllV1Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
        this.config = (BaseIndexerConfig) config;
    }

    @Override
    public boolean register() {

        pattern_1 = "\\<(%{NUMBER:syslog_id})\\>(\\d+)(\\s+)(%{DATA:timestamp})(\\s+)(%{IP:device_ip})(\\s+)(%{DATA:dev_name})(\\s+)-(\\s+)(%{DATA:log_level})(\\s+)\\[Fields\\@(%{DATA:filed_id})(\\s+)(Action=\\\"(%{DATA:action})\\\"(\\s+)){0,1}(%{GREEDYDATA:log_body})";
        pattern_2_logbody_monitor = new String[]{
                "UUid=\\\"(%{DATA:uuid})\\\"(\\s+)(inzone=\\\"(%{DATA:inzone})\\\"(\\s+)outzone=\\\"(%{DATA:outzone})\\\"(\\s+)){0,1}(rule=\\\"(%{DATA:rule})\\\"(\\s+)rule_uid=\\\"(%{DATA:rule_uid})\\\"(\\s+)){0,1}(service_id=\\\"(%{DATA:service_id})\\\"(\\s+)){0,1}src=\\\"(%{IP:src_ip})\\\"(\\s+)dst=\\\"(%{IP:dst_ip})\\\"(\\s+)proto=\\\"(%{NUMBER:proto})\\\"(\\s+)(xlatesrc=\\\"(%{IP:xlate_src})\\\"(\\s+)NAT_rulenum=\\\"(%{NUMBER:NAT_rulenum})\\\"(\\s+)NAT_addtnl_rulenum=\\\"(%{NUMBER:NAT_addtnl_rulenum})\\\"(\\s+)){0,1}(rule=\\\"(%{DATA:rule_sec})\\\"(\\s+)){0,1}(message_info=\\\"(%{DATA:message_info})\\\"(\\s+)){0,1}product=\\\"(%{DATA:product})\\\"(\\s+)service=\\\"(%{NUMBER:service})\\\"(\\s+)s_port=\\\"(%{NUMBER:src_port})\\\"(\\s+)(xlatesport=\\\"(%{NUMBER:xlate_sport})\\\"(\\s+)){0,1}product_family=\\\"(%{DATA:product_family})\\\"\\]",
                "UUid=\\\"(%{DATA:uuid})\\\"(\\s+)src=\\\"(%{IP:src_ip})\\\"(\\s+)dst=\\\"(%{IP:dst_ip})\\\"(\\s+)proto=\\\"(%{NUMBER:proto})\\\"(\\s+)message_info=\\\"%{DATA:message_info}\\\"(\\s+)product=\\\"(%{DATA:product})\\\"(\\s+)product_family=\\\"(%{GREEDYDATA:product_family})\\\"\\]",
                "UUid=\\\"(%{DATA:uuid})\\\"(\\s+)ICMP=\\\"%{DATA:ICMP}\\\"(\\s+)src=\\\"(%{IP:src_ip})\\\"(\\s+)dst=\\\"(%{IP:dst_ip})\\\"(\\s+)proto=\\\"(%{NUMBER:proto})\\\"(\\s+)ICMP Type=\\\"%{NUMBER:ICMP_Type}\\\"(\\s+)ICMP Code=\\\"%{NUMBER:ICMP_Code}\\\"(\\s+)message_info=\\\"%{DATA:message_info}\\\"(\\s+)product=\\\"(%{DATA:product})\\\"(\\s+)product_family=\\\"(%{GREEDYDATA:product_family})\\\"\\]",
                "%{GREEDYDATA:log_body2}"
        };
        pattern_2_logbody_allow = new String[]{
                "UUid=\\\"(%{DATA:uuid})\\\"(\\s+)src=\\\"(%{IP:src_ip})\\\"(\\s+)dst=\\\"(%{IP:dst_ip})\\\"(\\s+)proto=\\\"(%{NUMBER:proto})\\\"(\\s+)(%{GREEDYDATA:allow_log_messages})product=\\\"(%{DATA:product})\\\"(\\s+)service=\\\"(%{NUMBER:service})\\\"(\\s+)s_port=\\\"(%{NUMBER:src_port})\\\"(\\s+)product_family=\\\"(%{DATA:product_family})\\\"\\]",
                "%{GREEDYDATA:log_body2}"
        };
        pattern_2_logbody_alert =new String[]{
                "UUid=\\\"(%{DATA:uuid})\\\"(\\s+)description=\\\"%{DATA:description}\\\" reason=\\\"%{GREEDYDATA:reason}\\\" severity=\\\"%{NUMBER:severity}\\\" update status=\\\"%{WORD:update_status}\\\" product=\\\"%{DATA:product}\\\" product_family=\\\"%{DATA:product_family}\\\"\\]",
                "%{GREEDYDATA:log_body2}"
        };
        pattern_2_logbody_drop = new String[]{
                "UUid=\\\"(%{DATA:uuid})\\\"(\\s+)(inzone=\\\"(%{DATA:inzone})\\\"(\\s+)outzone=\\\"(%{DATA:outzone})\\\"(\\s+)){0,1}(rule=\\\"(%{DATA:rule})\\\"(\\s+)rule_uid=\\\"(%{DATA:rule_uid})\\\"(\\s+)){0,1}(service_id=\\\"(%{DATA:service_id})\\\"(\\s+)){0,1}src=\\\"(%{IP:src_ip})\\\"(\\s+)dst=\\\"(%{IP:dst_ip})\\\"(\\s+)proto=\\\"(%{NUMBER:proto})\\\"(\\s+)(xlatesrc=\\\"(%{IP:xlate_src})\\\"(\\s+)NAT_rulenum=\\\"(%{NUMBER:NAT_rulenum})\\\"(\\s+)NAT_addtnl_rulenum=\\\"(%{NUMBER:NAT_addtnl_rulenum})\\\"(\\s+)){0,1}(rule=\\\"(%{DATA:rule_sec})\\\"(\\s+)){0,1}(message_info=\\\"(%{DATA:message_info})\\\"(\\s+)){0,1}product=\\\"(%{DATA:product})\\\"(\\s+)service=\\\"(%{NUMBER:service})\\\"(\\s+)s_port=\\\"(%{NUMBER:src_port})\\\"(\\s+)(xlatesport=\\\"(%{NUMBER:xlate_sport})\\\"(\\s+)){0,1}product_family=\\\"(%{DATA:product_family})\\\"\\]",
                "UUid=\\\"(%{DATA:uuid})\\\"(\\s+)src=\\\"(%{IP:src_ip})\\\"(\\s+)dst=\\\"(%{IP:dst_ip})\\\"(\\s+)proto=\\\"(%{NUMBER:proto})\\\"(\\s+)scheme:=\\\"%{WORD:scheme}\\\"(\\s+)methods:=\\\"%{WORD:methods}\\\"(\\s+)peer gateway=\\\"%{IP:peer_gateway}\\\" encryption failure:=\\\"%{DATA:encryption_failure}\\\" vpn_user=\\\"%{DATA:vpn_user}\\\" session_uid=\\\"%{DATA:session_uid}\\\" fw_subproduct=\\\"%{DATA:fw_subproduct}\\\" vpn_feature_name=\\\"%{DATA:vpn_feature_name}\\\" product=\\\"(%{DATA:product})\\\" service=\\\"(%{NUMBER:service})\\\" s_port=\\\"(%{NUMBER:src_port})\\\" product_family=\\\"(%{DATA:product_family})\\\"\\]",
                "UUid=\\\"(%{DATA:uuid})\\\"(\\s+)rule=\\\"%{DATA:rule}\\\"(\\s+)rule_uid=\\\"%{DATA:rule_uid}\\\"(\\s+)ICMP=\\\"%{DATA:ICMP}\\\"(\\s+)src=\\\"%{IP:src_ip}\\\"(\\s+)dst=\\\"%{IP:dst_ip}\\\"(\\s+)proto=\\\"%{DATA:proto}\\\"(\\s+)ICMP Type=\\\"%{DATA:ICMP_Type}\\\"(\\s+)ICMP Code=\\\"%{DATA:ICMP_Code}\\\"(\\s+)product=\\\"%{DATA:product}\\\"(\\s+)product_family=\\\"%{DATA:product_family}\\\"]",
                "%{GREEDYDATA:log_body2}"
        };
        pattern_2_logbody_accept = new String[]{
                "UUid=\\\"(%{DATA:uuid})\\\"(\\s+)(inzone=\\\"(%{DATA:inzone})\\\"(\\s+)outzone=\\\"(%{DATA:outzone})\\\"(\\s+)){0,1}(rule=\\\"(%{DATA:rule})\\\"(\\s+)rule_uid=\\\"(%{DATA:rule_uid})\\\"(\\s+)){0,1}(service_id=\\\"(%{DATA:service_id})\\\"(\\s+)){0,1}src=\\\"(%{IP:src_ip})\\\"(\\s+)dst=\\\"(%{IP:dst_ip})\\\"(\\s+)proto=\\\"(%{NUMBER:proto})\\\"(\\s+)(xlatesrc=\\\"(%{IP:xlate_src})\\\"(\\s+)NAT_rulenum=\\\"(%{NUMBER:NAT_rulenum})\\\"(\\s+)NAT_addtnl_rulenum=\\\"(%{NUMBER:NAT_addtnl_rulenum})\\\"(\\s+)){0,1}(rule=\\\"(%{DATA:rule_sec})\\\"(\\s+)){0,1}(message_info=\\\"(%{DATA:message_info})\\\"(\\s+)){0,1}product=\\\"(%{DATA:product})\\\"(\\s+)service=\\\"(%{NUMBER:service})\\\"(\\s+)s_port=\\\"(%{NUMBER:src_port})\\\"(\\s+)(xlatesport=\\\"(%{NUMBER:xlate_sport})\\\"(\\s+)){0,1}product_family=\\\"(%{DATA:product_family})\\\"\\]",
                "UUid=\\\"(%{DATA:uuid})\\\"(\\s+)(rule=\\\"(%{DATA:rule})\\\"(\\s+)rule_uid=\\\"(%{DATA:rule_uid})\\\"(\\s+)){0,1}(service_id=\\\"(%{DATA:service_id})\\\"(\\s+)){0,1}ICMP=\\\"%{DATA:ICMP}\\\"(\\s+)src=\\\"(%{IP:src_ip})\\\"(\\s+)dst=\\\"(%{IP:dst_ip})\\\"(\\s+)proto=\\\"(%{NUMBER:proto})\\\"(\\s+)ICMP Type=\\\"%{NUMBER:ICMP_Type}\\\"(\\s+){0,1}ICMP Code=\\\"%{NUMBER:ICMP_Code}\\\"(\\s+){0,1}(xlatesrc=\\\"(%{IP:xlate_src})\\\"(\\s+){0,1}NAT_rulenum=\\\"(%{NUMBER:NAT_rulenum})\\\"(\\s+){0,1}NAT_addtnl_rulenum=\\\"(%{NUMBER:NAT_addtnl_rulenum})\\\"(\\s+)){0,1}product=\\\"(%{DATA:product})\\\"(\\s+)product_family=\\\"(%{DATA:product_family})\\\"\\]",
                "UUid=\\\"(%{DATA:uuid})\\\"(\\s+)(rule=\\\"(%{DATA:rule})\\\"(\\s+)rule_uid=\\\"(%{DATA:rule_uid})\\\"(\\s+)){0,1}(service_id=\\\"(%{DATA:service_id})\\\"(\\s+)){0,1}src=\\\"%{IP:src_ip}\\\"(\\s+){0,1}dst=\\\"%{IP:dst_ip}\\\"(\\s+){0,1}proto=\\\"%{NUMBER:proto}\\\"(\\s+){0,1}xlatedst=\\\"%{IP:xlate_dst}\\\"(\\s+){0,1}NAT_rulenum=\\\"%{NUMBER:NAT_rulenum}\\\"(\\s+){0,1}NAT_addtnl_rulenum=\\\"%{NUMBER:NAT_addtnl_rulenum}\\\"(\\s+){0,1}product=\\\"%{DATA:product}\\\"(\\s+){0,1}service=\\\"%{NUMBER:service}\\\"(\\s+){0,1}s_port=\\\"%{NUMBER:src_port}\\\"(\\s+){0,1}product_family=\\\"%{DATA:product_family}\\\"\\]",
                "%{GREEDYDATA:log_body2}"
        };

        grok_1 = GrokUtil.getGrok(pattern_1);
        grok_2_logbody_monitor = GrokUtil.getGroks(pattern_2_logbody_monitor);
        grok_2_logbody_allow = GrokUtil.getGroks(pattern_2_logbody_allow);
        grok_2_logbody_alert = GrokUtil.getGroks(pattern_2_logbody_alert);
        groks_2_logbody_drop = GrokUtil.getGroks(pattern_2_logbody_drop);
        groks_2_logbody_accept = GrokUtil.getGroks(pattern_2_logbody_accept);

        return true;
    }

    @Override
    public boolean format(Event event) {

        String message = event.getMessage();
        Map<String, Object> messageMap = GrokUtil.getMap(grok_1, message);
        Map<String, Object> formated = event.getFormat();
        formated.putAll(messageMap);
        formated.put("message", message);
        if (messageMap.get("flag") == null && messageMap.get("flag") != "解析失败") {
            event.setMetafieldLoglevel("1");
        }
        String log_level = String.valueOf(formated.get("log_level"));
        String log_body = String.valueOf(formated.get("log_body"));
        if (log_body != null) {
            String action = String.valueOf(formated.get("action"));
            if (log_level.equals("Log")) {
                if (action.equals("monitor")) {
                    Map<String, Object> map = GrokUtil.getMapByGroks(grok_2_logbody_monitor, log_body);
                    formated.putAll(map);
                    if (messageMap.get("flag") == null && messageMap.get("flag") != "解析失败")
                        event.setMetafieldLoglevel("2");
                }
                if (action.equals("accept")) {
                    Map<String, Object> map = GrokUtil.getMapByGroks(groks_2_logbody_accept, log_body);
                    formated.putAll(map);
                    if (messageMap.get("flag") == null && messageMap.get("flag") != "解析失败")
                        event.setMetafieldLoglevel("2");
                }
                if (action.equals("drop")) {
                    Map<String, Object> map = GrokUtil.getMapByGroks(groks_2_logbody_drop, log_body);
                    formated.putAll(map);
                    if (messageMap.get("flag") == null && messageMap.get("flag") != "解析失败")
                        event.setMetafieldLoglevel("2");
                }
                if (action.equals("allow")) {
                    Map<String, Object> map = GrokUtil.getMapByGroks(grok_2_logbody_allow, log_body);
                    formated.putAll(map);
                    if (messageMap.get("flag") == null && messageMap.get("flag") != "解析失败")
                        event.setMetafieldLoglevel("2");
                    String allow_log_messages = String.valueOf(formated.get("allow_log_messages"));

                    if (allow_log_messages.length() != 0) {
                        try {

                            Map<String, Object> allow_log_messages_map = new HashMap<>();
                            String left = new String();
                            String right = new String();
                            String[] arr = allow_log_messages.split("\" ");
                            for (int i = 0; i < arr.length; i++) {
                                left=arr[i].substring(0,arr[i].indexOf("="));
                                right=arr[i].substring(arr[i].indexOf("=")+1,arr[i].length()).replace("=\"","").replace("\"","");
                                allow_log_messages_map.put(left, right);
                            }
                            formated.putAll(allow_log_messages_map);
                        } catch (Exception ex) {
                            LOGGER.error(ex.getMessage()+" "+event.getMessage());
                            addException(ex.getMessage()+" "+event.getMessage());
                        }
                    }
                }
            } else if (log_level.equals("Alert")) {
                Map<String, Object> map = GrokUtil.getMapByGroks(grok_2_logbody_alert, log_body);
                formated.putAll(map);
                if (messageMap.get("flag") == null && messageMap.get("flag") != "解析失败")
                    event.setMetafieldLoglevel("2");
            } else {
                LOGGER.error("未知的CheckPoint日志格式！");
            }
        }

//        jz：为了防止根据日志样本穷举不出的情况做的字符串切割的解析
        String log_body2 = String.valueOf(formated.get("log_body2"));
        if (GrokUtil.isStringHasValue(log_body2)) {
            try {
                Map<String, Object> log_body_map = new HashMap<>();
                String left = new String();
                String right = new String();
                String[] arr = log_body.split("=");
                for (int i = 0; i < arr.length; i++) {
                    if (i == 0 && arr.length > 1) {
                        left = arr[i].replaceAll("\\s+","");
                        right = arr[i + 1].substring(0, arr[i + 1].lastIndexOf(" "));
                    } else if (i > 0 && arr.length > 1 && i < arr.length - 2) {
                        left = arr[i].substring(arr[i].lastIndexOf(" "), arr[i].length()).replaceAll("\\s+","");
                        right = arr[i + 1].substring(0, arr[i + 1].lastIndexOf(" "));
                    } else if (i > 0 && arr.length > 1 && i == arr.length - 1) {
                        left = arr[i - 1].substring(arr[i - 1].lastIndexOf(" "), arr[i - 1].length()).replaceAll("\\s+","");
                        right = arr[i];
                    }

                    left=GrokUtil.setStringValue(left);
                    right=GrokUtil.setStringValue(right);
                    log_body_map.put(left, right);
                }
                formated.putAll(log_body_map);
            }
            catch (Exception ex){
                LOGGER.error(ex.getMessage());
                addException(ex.getMessage());
            }
        }


        //格式化@timestamp
        if (GrokUtil.isStringHasValue(String.valueOf(formated.get("timestamp")))) {
            formated.put("@timestamp", formated.get("timestamp"));
        } else {
            formated.put("@timestamp", String.valueOf(OffsetDateTime.now()));
        }


        //格式化Metafield
        MetafieldHelper.setMetafield(event, "src_ip", "dst_ip", "", formated);
        if (formated.get("src_ip") != null && formated.get("dst_ip") != null) {
            formated.put("ip_addr_pair", formated.get("src_ip") + "=>" + formated.get("dst_ip"));
        }

        //geoip解析及过滤：
        if (!config.getIpFilter().equals("null")) {
            GrokUtil.filterGeoIP(config, formated);
        } else {
            GrokUtil.setGeoIP2(formated, "src_ip");
            GrokUtil.setGeoIP2(formated, "dst_ip");
        }

        //补充日志来源ip
        if (event.getSource() != null) {
            formated.put("host", event.getSource());
        }

        //补充大的日志种类:设备名-设备
        formated.put("log_class", "checkpoint-firewall");

        //解析层数
        formated.put("format_level", event.getMetafieldLoglevel());

        //字段名对应：
        if (formated.get("logs")!=null) {
            formated.put("doc_logs", formated.get("logs"));
        }
        if (formated.get("bytes")!=null) {
            formated.put("doc_bytes", formated.get("bytes"));
        }
        if (formated.get("received_bytes")!=null) {
            formated.put("doc_received_bytes", formated.get("received_bytes"));
        }
        if (formated.get("sent_bytes")!=null) {
            formated.put("doc_sent_bytes", formated.get("sent_bytes"));
        }

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
                + "\"format_level\":{\"type\":\"keyword\"},"
                + "\"ip_addr_pair\":{\"type\":\"keyword\"},"
                + "\"log_class\":{\"type\":\"keyword\"},"
                + "\"host\":{\"type\":\"keyword\"},"
                + "\"syslog_id\":{\"type\":\"keyword\"},"
                + "\"timestamp\":{\"type\":\"keyword\"},"
                + "\"device_ip\":{\"type\":\"ip\"},"
                + "\"dev_name\":{\"type\":\"keyword\"},"
                + "\"log_level\":{\"type\":\"keyword\"},"
                + "\"filed_id\":{\"type\":\"keyword\"},"
                + "\"action\":{\"type\":\"keyword\"},"
                + "\"log_body\":{\"type\":\"text\"},"
                + "\"uuid\":{\"type\":\"keyword\"},"
                + "\"inzone\":{\"type\":\"keyword\"},"
                + "\"outzone\":{\"type\":\"keyword\"},"
                + "\"rule\":{\"type\":\"keyword\"},"
                + "\"rule_uid\":{\"type\":\"keyword\"},"
                + "\"service_id\":{\"type\":\"keyword\"},"
                + "\"src_ip\":{\"type\":\"ip\"},"
                + "\"dst_ip\":{\"type\":\"ip\"},"
                + "\"proto\":{\"type\":\"keyword\"},"
                + "\"xlate_src\":{\"type\":\"ip\"},"
                + "\"xlate_dst\":{\"type\":\"ip\"},"
                + "\"NAT_rulenum\":{\"type\":\"integer\"},"
                + "\"NAT_addtnl_rulenum\":{\"type\":\"integer\"},"
                + "\"rule_sec\":{\"type\":\"keyword\"},"
                + "\"message_info\":{\"type\":\"keyword\"},"
                + "\"product\":{\"type\":\"keyword\"},"
                + "\"service\":{\"type\":\"keyword\"},"
                + "\"src_port\":{\"type\":\"keyword\"},"
                + "\"xlate_sport\":{\"type\":\"keyword\"},"
                + "\"product_family\":{\"type\":\"keyword\"},"
                + "\"allow_log_messages\":{\"type\":\"keyword\"},"
                + "\"description\":{\"type\":\"keyword\"},"
                + "\"reason\":{\"type\":\"keyword\"},"
                + "\"severity\":{\"type\":\"keyword\"},"
                + "\"update_status\":{\"type\":\"keyword\"},"
                + "\"scheme\":{\"type\":\"keyword\"},"
                + "\"methods\":{\"type\":\"keyword\"},"
                + "\"peer_gateway\":{\"type\":\"keyword\"},"
                + "\"encryption_failure\":{\"type\":\"keyword\"},"
                + "\"vpn_user\":{\"type\":\"keyword\"},"
                + "\"session_uid\":{\"type\":\"keyword\"},"
                + "\"fw_subproduct\":{\"type\":\"keyword\"},"
                + "\"vpn_feature_name\":{\"type\":\"keyword\"},"
                + "\"ICMP\":{\"type\":\"keyword\"},"
                + "\"ICMP_Type\":{\"type\":\"keyword\"},"
                + "\"ICMP_Code\":{\"type\":\"keyword\"},"
                + "\"appi_name\":{\"type\":\"keyword\"},"
                + "\"app_desc\":{\"type\":\"keyword\"},"
                + "\"app_id\":{\"type\":\"keyword\"},"
                + "\"app_category\":{\"type\":\"keyword\"},"
                + "\"matched_category\":{\"type\":\"keyword\"},"
                + "\"app_properties\":{\"type\":\"keyword\"},"
                + "\"app_risk\":{\"type\":\"keyword\"},"
                + "\"app_rule_id\":{\"type\":\"keyword\"},"
                + "\"app_rule_name\":{\"type\":\"keyword\"},"
                + "\"app_sig_id\":{\"type\":\"keyword\"},"
                + "\"proxy_src_ip\":{\"type\":\"keyword\"},"
                + "\"web_client_type\":{\"type\":\"keyword\"},"
                + "\"web_server_type\":{\"type\":\"keyword\"},"
                + "\"resource\":{\"type\":\"keyword\"},"
                + "\"bytes\":{\"type\":\"keyword\"},"
                + "\"sent_bytes\":{\"type\":\"keyword\"},"
                + "\"received_bytes\":{\"type\":\"keyword\"},"
                + "\"browse_time\":{\"type\":\"keyword\"},"
                + "\"Suppressed_logs\":{\"type\":\"keyword\"},"
                + "\"Referrer_self_uid\":{\"type\":\"keyword\"},"
                + "\"Referrer_Parent_uid\":{\"type\":\"keyword\"},"
                + "\"doc_logs\":{\"type\":\"integer\"},"
                + "\"doc_bytes\":{\"type\":\"long\"},"
                + "\"doc_received_bytes\":{\"type\":\"long\"},"
                + "\"doc_sent_bytes\":{\"type\":\"long\"},"
                + "\"src_ip_geoip\": {"
                + "\"properties\": {"
                + "\"location\": {\"type\": \"geo_point\"},"
                + "\"city_name\": {\"type\": \"keyword\"},"
                + "\"continent_code\": {\"type\": \"keyword\"},"
                + "\"country_code2\": {\"type\": \"keyword\"},"
                + "\"country_code3\": {\"type\": \"keyword\"},"
                + "\"country_name\": {\"type\": \"keyword\"},"
                + "\"dma_code\": {\"type\": \"keyword\"},"
                + "\"ip\": {\"type\": \"ip\"},"
                + "\"latitude\": {\"type\": \"float\"},"
                + "\"longitude\": {\"type\": \"float\"},"
                + "\"postal_code\": {\"type\": \"keyword\"},"
                + "\"region_name\": {\"type\": \"keyword\"},"
                + "\"timezone\": {\"type\": \"keyword\"}"
                + "}"
                + "},"
                + "\"dst_ip_geoip\": {"
                + "\"properties\": {"
                + "\"location\": {\"type\": \"geo_point\"},"
                + "\"city_name\": {\"type\": \"keyword\"},"
                + "\"continent_code\": {\"type\": \"keyword\"},"
                + "\"country_code2\": {\"type\": \"keyword\"},"
                + "\"country_code3\": {\"type\": \"keyword\"},"
                + "\"country_name\": {\"type\": \"keyword\"},"
                + "\"dma_code\": {\"type\": \"keyword\"},"
                + "\"ip\": {\"type\": \"ip\"},"
                + "\"latitude\": {\"type\": \"float\"},"
                + "\"longitude\": {\"type\": \"float\"},"
                + "\"postal_code\": {\"type\": \"keyword\"},"
                + "\"region_name\": {\"type\": \"keyword\"},"
                + "\"timezone\": {\"type\": \"keyword\"}"
                + "}"
                + "},"
                + "\"Metafield_type\":{\"type\":\"keyword\"},"
                + "\"Metafield_category\":{\"type\":\"keyword\"},"
                + "\"Metafield_subject\":{\"type\":\"keyword\"},"
                + "\"Metafield_object\":{\"type\":\"keyword\"},"
                + "\"Metafield_loglevel\":{\"type\":\"keyword\"},"
                + "\"Metafield_source\":{\"type\":\"keyword\"},"
                + "\"Metafield_description\":{\"type\":\"keyword\", \"fields\": {\"raw\": {\"type\": \"keyword\"}}}"
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
                + "\"format_level\":{\"type\":\"keyword\"},"
                + "\"ip_addr_pair\":{\"type\":\"keyword\"},"
                + "\"log_class\":{\"type\":\"keyword\"},"
                + "\"host\":{\"type\":\"keyword\"},"
                + "\"syslog_id\":{\"type\":\"keyword\"},"
                + "\"timestamp\":{\"type\":\"keyword\"},"
                + "\"device_ip\":{\"type\":\"ip\"},"
                + "\"dev_name\":{\"type\":\"keyword\"},"
                + "\"log_level\":{\"type\":\"keyword\"},"
                + "\"filed_id\":{\"type\":\"keyword\"},"
                + "\"action\":{\"type\":\"keyword\"},"
                + "\"log_body\":{\"type\":\"text\"},"
                + "\"uuid\":{\"type\":\"keyword\"},"
                + "\"inzone\":{\"type\":\"keyword\"},"
                + "\"outzone\":{\"type\":\"keyword\"},"
                + "\"rule\":{\"type\":\"keyword\"},"
                + "\"rule_uid\":{\"type\":\"keyword\"},"
                + "\"service_id\":{\"type\":\"keyword\"},"
                + "\"src_ip\":{\"type\":\"ip\"},"
                + "\"dst_ip\":{\"type\":\"ip\"},"
                + "\"proto\":{\"type\":\"keyword\"},"
                + "\"xlate_src\":{\"type\":\"ip\"},"
                + "\"xlate_dst\":{\"type\":\"ip\"},"
                + "\"NAT_rulenum\":{\"type\":\"integer\"},"
                + "\"NAT_addtnl_rulenum\":{\"type\":\"integer\"},"
                + "\"rule_sec\":{\"type\":\"keyword\"},"
                + "\"message_info\":{\"type\":\"keyword\"},"
                + "\"product\":{\"type\":\"keyword\"},"
                + "\"service\":{\"type\":\"keyword\"},"
                + "\"src_port\":{\"type\":\"keyword\"},"
                + "\"xlate_sport\":{\"type\":\"keyword\"},"
                + "\"product_family\":{\"type\":\"keyword\"},"
                + "\"allow_log_messages\":{\"type\":\"keyword\"},"
                + "\"description\":{\"type\":\"keyword\"},"
                + "\"reason\":{\"type\":\"keyword\"},"
                + "\"severity\":{\"type\":\"keyword\"},"
                + "\"update_status\":{\"type\":\"keyword\"},"
                + "\"scheme\":{\"type\":\"keyword\"},"
                + "\"methods\":{\"type\":\"keyword\"},"
                + "\"peer_gateway\":{\"type\":\"keyword\"},"
                + "\"encryption_failure\":{\"type\":\"keyword\"},"
                + "\"vpn_user\":{\"type\":\"keyword\"},"
                + "\"session_uid\":{\"type\":\"keyword\"},"
                + "\"fw_subproduct\":{\"type\":\"keyword\"},"
                + "\"vpn_feature_name\":{\"type\":\"keyword\"},"
                + "\"ICMP\":{\"type\":\"keyword\"},"
                + "\"ICMP_Type\":{\"type\":\"keyword\"},"
                + "\"ICMP_Code\":{\"type\":\"keyword\"},"
                + "\"appi_name\":{\"type\":\"keyword\"},"
                + "\"app_desc\":{\"type\":\"keyword\"},"
                + "\"app_id\":{\"type\":\"keyword\"},"
                + "\"app_category\":{\"type\":\"keyword\"},"
                + "\"matched_category\":{\"type\":\"keyword\"},"
                + "\"app_properties\":{\"type\":\"keyword\"},"
                + "\"app_risk\":{\"type\":\"keyword\"},"
                + "\"app_rule_id\":{\"type\":\"keyword\"},"
                + "\"app_rule_name\":{\"type\":\"keyword\"},"
                + "\"app_sig_id\":{\"type\":\"keyword\"},"
                + "\"proxy_src_ip\":{\"type\":\"keyword\"},"
                + "\"web_client_type\":{\"type\":\"keyword\"},"
                + "\"web_server_type\":{\"type\":\"keyword\"},"
                + "\"resource\":{\"type\":\"keyword\"},"
                + "\"bytes\":{\"type\":\"keyword\"},"
                + "\"sent_bytes\":{\"type\":\"keyword\"},"
                + "\"received_bytes\":{\"type\":\"keyword\"},"
                + "\"browse_time\":{\"type\":\"keyword\"},"
                + "\"Suppressed_logs\":{\"type\":\"keyword\"},"
                + "\"Referrer_self_uid\":{\"type\":\"keyword\"},"
                + "\"Referrer_Parent_uid\":{\"type\":\"keyword\"},"
                + "\"doc_logs\":{\"type\":\"integer\"},"
                + "\"doc_bytes\":{\"type\":\"long\"},"
                + "\"doc_received_bytes\":{\"type\":\"long\"},"
                + "\"doc_sent_bytes\":{\"type\":\"long\"},"
                + "\"src_ip_geoip\": {"
                + "\"properties\": {"
                + "\"location\": {\"type\": \"geo_point\"},"
                + "\"city_name\": {\"type\": \"keyword\"},"
                + "\"continent_code\": {\"type\": \"keyword\"},"
                + "\"country_code2\": {\"type\": \"keyword\"},"
                + "\"country_code3\": {\"type\": \"keyword\"},"
                + "\"country_name\": {\"type\": \"keyword\"},"
                + "\"dma_code\": {\"type\": \"keyword\"},"
                + "\"ip\": {\"type\": \"ip\"},"
                + "\"latitude\": {\"type\": \"float\"},"
                + "\"longitude\": {\"type\": \"float\"},"
                + "\"postal_code\": {\"type\": \"keyword\"},"
                + "\"region_name\": {\"type\": \"keyword\"},"
                + "\"timezone\": {\"type\": \"keyword\"}"
                + "}"
                + "},"
                + "\"dst_ip_geoip\": {"
                + "\"properties\": {"
                + "\"location\": {\"type\": \"geo_point\"},"
                + "\"city_name\": {\"type\": \"keyword\"},"
                + "\"continent_code\": {\"type\": \"keyword\"},"
                + "\"country_code2\": {\"type\": \"keyword\"},"
                + "\"country_code3\": {\"type\": \"keyword\"},"
                + "\"country_name\": {\"type\": \"keyword\"},"
                + "\"dma_code\": {\"type\": \"keyword\"},"
                + "\"ip\": {\"type\": \"ip\"},"
                + "\"latitude\": {\"type\": \"float\"},"
                + "\"longitude\": {\"type\": \"float\"},"
                + "\"postal_code\": {\"type\": \"keyword\"},"
                + "\"region_name\": {\"type\": \"keyword\"},"
                + "\"timezone\": {\"type\": \"keyword\"}"
                + "}"
                + "},"
                + "\"Metafield_type\":{\"type\":\"keyword\"},"
                + "\"Metafield_category\":{\"type\":\"keyword\"},"
                + "\"Metafield_subject\":{\"type\":\"keyword\"},"
                + "\"Metafield_object\":{\"type\":\"keyword\"},"
                + "\"Metafield_loglevel\":{\"type\":\"keyword\"},"
                + "\"Metafield_source\":{\"type\":\"keyword\"},"
                + "\"Metafield_description\":{\"type\":\"keyword\", \"fields\": {\"raw\": {\"type\": \"keyword\"}}}"
                + "}"
                + "}";
        return mapping;

    }
}
