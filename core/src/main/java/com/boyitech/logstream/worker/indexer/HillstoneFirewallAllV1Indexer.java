package com.boyitech.logstream.worker.indexer;


import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.GrokUtil;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.MetafieldHelper;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import com.boyitech.logstream.core.worker.BaseWorker;
import com.boyitech.logstream.core.worker.indexer.BaseIndexerConfig;
import io.krakens.grok.api.Grok;
import org.apache.commons.lang.time.DateFormatUtils;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Eric
 * @Title: SbsDeviceFirewallV1
 * @date 2018/12/5 15:43
 */
public class HillstoneFirewallAllV1Indexer extends BaseIndexer {

    private String[] patterns1;
    private String[] patterns2;
    private String[] patterns3;
    private String[] patterns4;
    private String[] patterns5;
    private String[] patterns6;
    private String pattern1;
    private String pattern2;
    private String pattern3;
    private String pattern4;
    private String pattern5;
    private String pattern6;
    private ArrayList<Grok> groks1;
    private ArrayList<Grok> groks2;
    private ArrayList<Grok> groks3;
    private ArrayList<Grok> groks4;
    private ArrayList<Grok> groks5;
    private ArrayList<Grok> groks6;
    private Grok grok1;
    private Grok grok2;
    private Grok grok3;
    private Grok grok4;
    private Grok grok5;
    private Grok grok6;
    private BaseIndexerConfig config;

    public HillstoneFirewallAllV1Indexer(BaseWorkerConfig config) {
        super(config);
        this.config= (BaseIndexerConfig) config;

    }

    public HillstoneFirewallAllV1Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
        this.config= (BaseIndexerConfig) config;

    }

    @Override
    public boolean register() {

        patterns1 = new String[]{
                "^(\\<(?<id>\\d+)\\>)(?<timestamp>%{MONTH}(\\s+)%{MONTHDAY}(\\s+)%{TIME})\\s%{NOTSPACE:host_name}\\s%{NOTSPACE:t_id}\\s%{WORD:log_type}\\@%{WORD:specific_type}\\:\\s%{GREEDYDATA:log-body}",
                "^(\\S+)(\\<(?<id>\\d+)\\>)(?<timestamp>%{MONTH}(\\s+)%{MONTHDAY}(\\s+)%{TIME})\\s%{NOTSPACE:host_name}\\s%{NOTSPACE:t_id}\\s%{WORD:log_type}\\@%{WORD:specific_type}\\:\\s%{GREEDYDATA:log-body}",
                "^%{NOTSPACE:t_id}\\s%{WORD:log_type}\\@%{WORD:specific_type}\\:\\s%{GREEDYDATA:log-body}"
        };
        patterns2 = new String[]{
                "^SESSION\\:(\\s+)%{IPORHOST:src_ip}\\:%{NUMBER:src_port}->%{IPORHOST:dst_ip}\\:%{NUMBER:dst_port}\\((?<protocol>\\w+)\\), interface\\s(?<interface>\\S+)?, vr\\s(?<vr>\\S+), policy\\s%{NOTSPACE:policy_id}, user\\s%{NOTSPACE:client_user}, host\\s%{NOTSPACE:client_host},(\\s+)(?<identifier>\\w+)(\\s+)%{GREEDYDATA:session-messages}",
                "^SESSION\\:(\\s+)%{IPORHOST:src_ip}\\:%{USER:src_port}->%{IPORHOST:dst_ip}\\:%{NUMBER:dst_port}\\((?<protocol>\\w+)\\), interface\\s(?<interface>\\S+)?, vr\\s(?<vr>\\S+), policy\\s%{NOTSPACE:policy_id}, user\\s%{NOTSPACE:client_user}, host\\s%{NOTSPACE:client_host},(\\s+)(?<identifier>\\w+)(\\s+)%{GREEDYDATA:session-messages}"};
        patterns3 = new String[]{
                "^packets\\s%{NOTSPACE:send_packets},(send bytes\\s%{NOTSPACE:send_bytes}),(receive packets\\s%{NOTSPACE:receive_packets}),(receive bytes\\s%{NOTSPACE:receive_bytes}),(start time\\s(?<startTime>%{YEAR}-%{MONTHNUM}-%{MONTHDAY} %{TIME})),(close time\\s(?<closeTime>%{YEAR}-%{MONTHNUM}-%{MONTHDAY} %{TIME})),%{GREEDYDATA:the_end}",
                "^packets\\s%{NOTSPACE:send_packets},(send bytes\\s%{NOTSPACE:send_bytes}),(receive packets\\s%{NOTSPACE:receive_packets}),(receive bytes\\s%{NOTSPACE:receive_bytes}),(start time\\s(?<startTime>%{YEAR}-%{MONTHNUM}-%{MONTHDAY} %{TIME})),%{GREEDYDATA:the_end}",
                "^packets\\s%{NOTSPACE:send_packets},(send bytes\\s%{NOTSPACE:send_bytes}),(receive packets\\s%{NOTSPACE:receive_packets}),(receive bytes\\s%{NOTSPACE:receive_bytes}),%{GREEDYDATA:the_end}",
                "^packets\\s%{NOTSPACE:send_packets},(send bytes\\s%{NOTSPACE:send_bytes}),(receive packets\\s%{NOTSPACE:receive_packets}),%{GREEDYDATA:the_end}",
                "^packets\\s%{NOTSPACE:send_packets},(send bytes\\s%{NOTSPACE:send_bytes}),%{GREEDYDATA:the_end}"
        };
        patterns4 = new String[]{
                "^(%{IP:src_ip})\\:%{NUMBER:src_port}->%{IP:dst_ip}\\:%{NUMBER:dst_port}\\((?<protocol>\\w+)\\), interface\\s(?<interface>\\S+)?, vr\\s(?<vr>\\S+), policy\\s%{NOTSPACE:policy_id}, user\\s%{NOTSPACE:client_user}, host\\s%{NOTSPACE:client_host},(\\s+)(?<identifier>\\w+)(\\s+)%{GREEDYDATA:session-messages}$",
                "^(%{IP:src_ip})\\:%{USER:src_port}->%{IP:dst_ip}\\:%{USER:dst_port}\\((?<protocol>\\w+)\\), interface\\s(?<interface>\\S+)?, vr\\s(?<vr>\\S+), policy\\s%{NOTSPACE:policy_id}, user\\s%{NOTSPACE:client_user}, host\\s%{NOTSPACE:client_host},(\\s+)(?<identifier>\\w+)(\\s+)%{GREEDYDATA:session-messages}$",
                "^(%{IP:src_ip})\\:%{NUMBER:src_port}->%{IP:dst_ip}\\:%{NUMBER:dst_port}\\((?<protocol>\\w+)\\), interface\\s(?<interface>\\S+)?, vr\\s(?<vr>\\S+), policy\\s%{NOTSPACE:policy_id},(\\s+)%{GREEDYDATA:session-messages}$",
                "^(%{IP:src_ip})\\:%{NUMBER:src_port}->%{IP:dst_ip}\\:%{NUMBER:dst_port}\\((?<protocol>\\w+)\\), interface\\s(?<interface>\\S+)?, vr\\s(?<vr>\\S+),(\\s+)%{GREEDYDATA:session-messages}$",
                "^(%{IP:src_ip})\\:%{NUMBER:src_port}->%{IP:dst_ip}\\:%{NUMBER:dst_port}\\((?<protocol>\\w+)\\), interface\\s(?<interface>\\S+)?, %{GREEDYDATA:session-messages}$",
                "^(%{IP:src_ip})\\:%{NUMBER:src_port}->%{IP:dst_ip}\\:%{NUMBER:dst_port}\\((?<protocol>\\w+)\\), %{GREEDYDATA:session-messages}$",
                "^(%{IP:src_ip})\\:%{NUMBER:src_port}->%{IP:dst_ip}\\:%{NUMBER:dst_port}%{GREEDYDATA:session-messages}$"
        };
        patterns5 = new String[]{
                "^%{IP:src_ip}\\:%{NUMBER:src_port}\\S+%{IP:dst_ip}\\:%{NUMBER:dst_port}\\((?<protocol>\\w+)\\),(\\s+)snat to(\\s+)%{IP:snat_to_ip}\\:%{NUMBER:snat_to_port}, vr\\s(?<vr>\\S+), user\\s%{NOTSPACE:client_user}, host\\s%{NOTSPACE:client_host}, rule\\s(?<rule>\\d+)%{GREEDYDATA:the_end}",
                "^%{IP:src_ip}\\:%{NUMBER:src_port}\\S+%{IP:dst_ip}\\:%{NUMBER:dst_port}\\((?<protocol>\\w+)\\),(\\s+)snat to(\\s+)%{IP:snat_to_ip}\\:%{NUMBER:snat_to_port}, vr\\s(?<vr>\\S+), user\\s%{NOTSPACE:client_user}, host\\s%{NOTSPACE:client_host}%{GREEDYDATA:the_end}",
                "^%{IP:src_ip}\\:%{NUMBER:src_port}\\S+%{IP:dst_ip}\\:%{NUMBER:dst_port}\\((?<protocol>\\w+)\\),(\\s+)snat to(\\s+)%{IP:snat_to_ip}\\:%{NUMBER:snat_to_port}, vr\\s(?<vr>\\S+), user\\s%{NOTSPACE:client_user},%{GREEDYDATA:the_end}",
                "^%{IP:src_ip}\\:%{NUMBER:src_port}\\S+%{IP:dst_ip}\\:%{NUMBER:dst_port}\\((?<protocol>\\w+)\\),(\\s+)snat to(\\s+)%{IP:snat_to_ip}\\:%{NUMBER:snat_to_port}, vr\\s(?<vr>\\S+),%{GREEDYDATA:the_end}",
                "^%{IP:src_ip}\\:%{NUMBER:src_port}\\S+%{IP:dst_ip}\\:%{NUMBER:dst_port}\\((?<protocol>\\w+)\\),(\\s+)snat to(\\s+)%{IP:snat_to_ip}\\:%{NUMBER:snat_to_port},%{GREEDYDATA:the_end}",
                "^%{IP:src_ip}\\:%{NUMBER:src_port}->%{IP:dst_ip}\\:%{NUMBER:dst_port}\\(%{DATA:protocol}\\),(\\s+)snat to(\\s+)%{IP:snat_to_ip}\\:%{NUMBER:snat_to_port}, vr\\s(?<vr>\\S+), user\\s%{NOTSPACE:client_user}, host\\s%{NOTSPACE:client_host}, rule\\s(?<rule>\\d+)%{GREEDYDATA:the_end}",
                "^%{IP:src_ip}\\:%{NUMBER:src_port}\\S+%{IP:dst_ip}\\:%{NUMBER:dst_port}\\((?<protocol>\\w+)\\),%{GREEDYDATA:the_end}"

        };
        patterns6 = new String[]{
                "^%{WORD:flood-protocol}\\s%{DATA:flood-type}(\\s)attack%{DATA:the_mid-1}(\\s)(?<area>\\S+)\\:\\:(?<interface>\\S+)(\\s+)%{IP:src_ip}%{DATA:the_mid-2}%{IP:dst_ip}%{DATA:the_mid-3}(\\s)%{NUMBER:occurred_num}(\\s)%{DATA:the_mid-4}(\\s)%{NUMBER:occurred_times}(\\s)seconds%{GREEDYDATA:the_end}",
                "^%{WORD:flood-protocol}\\s%{DATA:flood-type}(\\s)attack%{DATA:the_mid-1}(\\s)(?<area>\\S+)\\:\\:(?<interface>\\S+)(\\s+)%{IP:src_ip}%{DATA:the_mid-2}%{IP:dst_ip}%{GREEDYDATA:the_end}",
                "^%{WORD:flood-protocol}\\s%{DATA:flood-type}(\\s)attack%{DATA:the_mid-1}(\\s+)dstip(\\s)%{IP:src_ip}%{DATA:the_mid-3}(\\s)%{NUMBER:occurred_num}%{DATA:the_mid-4}%{NUMBER:occurred_times}(\\s)seconds%{GREEDYDATA:the_end}"
        };

        pattern1 = "%{WORD:session_action}%{GREEDYDATA:the_end}";
        pattern2 = "^%{NOTSPACE:flow_type}\\:(\\s+)%{GREEDYDATA:log_messages}";
        pattern3 = "^%{WORD:session_action}%{GREEDYDATA:the_end}";
        pattern4 = "^default\\\\(%{NOTSPACE:policy_style}\\\\)\", \"^%{NOTSPACE:policy_style}";
        pattern5 = "^Admin%{DATA:the_mid-1}\"%{DATA:login_user}\"(\\s+)%{DATA:the_mid-2}through(\\s+)%{WORD:login_type}%{DATA:the_mid-3}%{IP:login_ip}%{GREEDYDATA:the_end}";
        pattern6="From %{IP:src_ip}:%{NUMBER:src_port}\\(%{GREEDYDATA:protocol}\\) to %{IP:dst_ip}\\:%{NUMBER:dst_port}\\(\\-\\), threat name: %{DATA:threat_name}, threat type: %{DATA:threat_type}, threat subtype: %{DATA:threat_subtype}, App/Protocol: %{DATA:threat_protocol}, action: %{DATA:threat_action}, defender: %{DATA:threat_defender}, severity: %{DATA:threat_severity}, %{GREEDYDATA:threat_body}";
        groks1 = GrokUtil.getGroks(patterns1);
        groks2 = GrokUtil.getGroks(patterns2);
        groks3 = GrokUtil.getGroks(patterns3);
        groks4 = GrokUtil.getGroks(patterns4);
        groks5 = GrokUtil.getGroks(patterns5);
        groks6 = GrokUtil.getGroks(patterns6);
        grok1 = GrokUtil.getGrok(pattern1);
        grok2 = GrokUtil.getGrok(pattern2);
        grok3 = GrokUtil.getGrok(pattern3);
        grok4 = GrokUtil.getGrok(pattern4);
        grok5 = GrokUtil.getGrok(pattern5);
        grok6=GrokUtil.getGrok(pattern6);
        return true;
    }

    @Override
    public boolean format(Event event) {
        String message = event.getMessage();
        Map<String, Object> messageMap = GrokUtil.getMapByGroks(groks1, message);
        Map<String, Object> formated = event.getFormat();
        formated.putAll(messageMap);
        formated.put("message", event.getMessage());
        if (messageMap.get("flag") == null && messageMap.get("flag") != "解析失败") {
            event.setMetafieldLoglevel("1");
        }
        String log_type = (String) formated.get("log_type");
        String specific_type = (String) formated.get("specific_type");
        String log_body = (String) formated.get("log-body");
        if (log_type.equals("Traffic")) {
            formated.put("Metafield_category", "Traffic");
            /**************/
            if (specific_type.equals("SECURITY")) {
                String[] s11 = {};
                Map<String, Object> messageMap11 = GrokUtil.getMapByGroks(groks2, log_body);
                formated.putAll(messageMap11);
                if (messageMap.get("flag") == null && messageMap.get("flag") != "解析失败") {
                    event.setMetafieldLoglevel("2");
                }
                String identifier = (String) formated.get("identifier");
                String session_messages = (String) formated.get("session-messages");
                if (identifier.equals("session")) {

                    Map<String, Object> map = GrokUtil.getMap(grok1, session_messages);
                    formated.putAll(map);
                    event.setMetafieldLoglevel("3");
                }
                if (identifier.equals("send")) {
                    Map<String, Object> map = GrokUtil.getMapByGroks(groks3, session_messages);
                    formated.putAll(map);
                    if (messageMap.get("flag") == null && messageMap.get("flag") != "解析失败") {
                        event.setMetafieldLoglevel("3");
                    }
                }
            }
            /**************/
            if (specific_type.equals("FLOW")) {
                Map<String, Object> map = GrokUtil.getMap(grok2, log_body);
                formated.putAll(map);
                if (messageMap.get("flag") == null && messageMap.get("flag") != "解析失败") {
                    event.setMetafieldLoglevel("2");
                }
                String flow_type = (String) formated.get("flow_type");
                String log_messages = (String) formated.get("log_messages");
                if (flow_type.equals("SESSION") ) {

                    Map<String, Object> map1 = GrokUtil.getMapByGroks(groks4, log_messages);
                    formated.putAll(map1);
                    if (messageMap.get("flag") == null && messageMap.get("flag") != "解析失败") {
                        event.setMetafieldLoglevel("2");
                    }
                    String identifier = (String) formated.get("identifier");
                    String session_messages = (String) formated.get("session-messages");

                    if (identifier != null && identifier.equals("session")) {
                        Map<String, Object> map2 = GrokUtil.getMap(grok3, session_messages);
                        formated.putAll(map2);
                        if (messageMap.get("flag") == null && messageMap.get("flag") != "解析失败") {
                            event.setMetafieldLoglevel("3");
                        }
                    }
                    if (identifier != null && identifier.equals("policy")) {

                        GrokUtil.getMap(grok4, session_messages);
                        if (messageMap.get("flag") == null && messageMap.get("flag") != "解析失败") {
                            event.setMetafieldLoglevel("3");
                        }
                    }
                    if (identifier != null && identifier.equals("send")) {

                        GrokUtil.getMapByGroks(groks3, session_messages);
                        if (messageMap.get("flag") == null && messageMap.get("flag") != "解析失败") {
                            event.setMetafieldLoglevel("3");
                        }
                    }
                }
                if (flow_type.equals("NAT")) {

                    Map<String, Object> map1 = GrokUtil.getMapByGroks(groks5, log_messages);
                    formated.putAll(map1);
                    if (messageMap.get("flag") == null && messageMap.get("flag") != "解析失败") {
                        event.setMetafieldLoglevel("3");
                    }
                }
            }

        }

        if (log_type.equals("Event")) {
            formated.put("Metafield_category", "Event");
            /**************/
            if (specific_type != null && specific_type.equals("MGMT")) {
                GrokUtil.getMap(grok5, log_body);
                if (messageMap.get("flag") == null && messageMap.get("flag") != "解析失败") {
                    event.setMetafieldLoglevel("2");
                }
            }
        }

        if (log_type.equals("Security")) {
            formated.put("Metafield_category", "Security");
            if (specific_type.equals("FLOW")) {
                Map<String, Object> mapByMuch = GrokUtil.getMapByGroks(groks6, log_body);
                formated.putAll(mapByMuch);
                if (messageMap.get("flag") == null && messageMap.get("flag") != "解析失败") {
                    event.setMetafieldLoglevel("2");
                }
            }

        }

        if(log_type.equals("Threat")){
            formated.put("Metafield_category", "Threat");
            if(specific_type.equals("FLOW")){
                Map<String, Object> mapByMuch = GrokUtil.getMap(grok6, log_body);
                formated.putAll(mapByMuch);
                if (messageMap.get("flag") == null && messageMap.get("flag") != "解析失败") {
                    event.setMetafieldLoglevel("2");
                }
            }
        }


        if (!config.getIpFilter().equals("null")) {
            GrokUtil.filterGeoIP(config, formated);
        } else {
            GrokUtil.setGeoIP(formated);
        }

        //格式化时间
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        String timestamp = (String) formated.get("timestamp");
        if (timestamp != null && timestamp.trim().length() != 0) {
            timestamp = year + " " + timestamp;
            Date time = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd HH:mm:ss", Locale.US);
            try {
                time = sdf.parse(timestamp);
            } catch (ParseException e) {
                BaseWorker.LOGGER.error("@timestamp时间格式化出错");
            }
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
            timestamp = sdf2.format(time);
        } else {
            String pattern = "yyyy-MM-dd'T'HH:mm:ssXXX";
            timestamp = DateFormatUtils.format(new Date(), pattern);
        }
        formated.put("@timestamp", timestamp);

        MetafieldHelper.setMetafield(event,"src_ip","dst_ip","",formated);

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
                + "\"id\":{\"type\":\"keyword\"},"
                + "\"host_name\":{\"type\":\"keyword\"},"
                + "\"t_id\":{\"type\":\"keyword\"},"
                + "\"log_type\":{\"type\":\"keyword\"},"
                + "\"specific_type\":{\"type\":\"keyword\"},"
                + "\"log-body\":{\"type\":\"text\"},"
                + "\"src_ip\":{\"type\":\"ip\"},"
                + "\"src_port\":{\"type\":\"integer\"},"
                + "\"dst_port\":{\"type\":\"integer\"},"
                + "\"dst_ip\":{\"type\":\"ip\"},"
                + "\"protocol\":{\"type\":\"keyword\"},"
                + "\"interface\":{\"type\":\"keyword\"},"
                + "\"vr\":{\"type\":\"keyword\"},"
                + "\"policy_id\":{\"type\":\"keyword\"},"
                + "\"client_user\":{\"type\":\"keyword\"},"
                + "\"client_host\":{\"type\":\"keyword\"},"
                + "\"identifier\":{\"type\":\"keyword\"},"
                + "\"session-messages\":{\"type\":\"keyword\"},"
                + "\"session_action\":{\"type\":\"keyword\"},"
                + "\"the_end\":{\"type\":\"keyword\"},"
                + "\"send_packets\":{\"type\":\"keyword\"},"
                + "\"send_bytes\":{\"type\":\"keyword\"},"
                + "\"receive_packets\":{\"type\":\"keyword\"},"
                + "\"receive_bytes\":{\"type\":\"keyword\"},"
                + "\"startTime\":{\"type\":\"keyword\"},"
                + "\"closeTime\":{\"type\":\"keyword\"},"
                + "\"flow_type\":{\"type\":\"keyword\"},"
                + "\"log_messages\":{\"type\":\"keyword\"},"
                + "\"rule\":{\"type\":\"keyword\"},"
                + "\"the_mid-1\":{\"type\":\"keyword\"},"
                + "\"the_mid-2\":{\"type\":\"keyword\"},"
                + "\"the_mid-3\":{\"type\":\"keyword\"},"
                + "\"the_mid-4\":{\"type\":\"keyword\"},"
                + "\"login_user\":{\"type\":\"keyword\"},"
                + "\"login_type\":{\"type\":\"keyword\"},"
                + "\"login_ip\":{\"type\":\"ip\"},"
                + "\"flood-protocol\":{\"type\":\"keyword\"},"
                + "\"flood-type\":{\"type\":\"keyword\"},"
                + "\"area\":{\"type\":\"keyword\"},"
                + "\"occurred_num\":{\"type\":\"keyword\"},"
                + "\"occurred_times\":{\"type\":\"keyword\"},"
                + "\"threat_name\":{\"type\":\"keyword\"},"
                + "\"threat_type\":{\"type\":\"keyword\"},"
                + "\"threat_subtype\":{\"type\":\"keyword\"},"
                + "\"threat_protocol\":{\"type\":\"keyword\"},"
                + "\"threat_action\":{\"type\":\"keyword\"},"
                + "\"threat_defender\":{\"type\":\"keyword\"},"
                + "\"threat_severity\":{\"type\":\"keyword\"},"
                + "\"threat_body\":{\"type\":\"keyword\"},"
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
                + "\"t_id\":{\"type\":\"keyword\"},"
                + "\"host_name\":{\"type\":\"keyword\"},"
                + "\"t_id\":{\"type\":\"keyword\"},"
                + "\"log_type\":{\"type\":\"keyword\"},"
                + "\"specific_type\":{\"type\":\"keyword\"},"
                + "\"log-body\":{\"type\":\"text\"},"
                + "\"src_ip\":{\"type\":\"ip\"},"
                + "\"src_port\":{\"type\":\"integer\"},"
                + "\"dst_port\":{\"type\":\"integer\"},"
                + "\"dst_ip\":{\"type\":\"ip\"},"
                + "\"protocol\":{\"type\":\"keyword\"},"
                + "\"interface\":{\"type\":\"keyword\"},"
                + "\"vr\":{\"type\":\"keyword\"},"
                + "\"policy_id\":{\"type\":\"keyword\"},"
                + "\"client_user\":{\"type\":\"keyword\"},"
                + "\"client_host\":{\"type\":\"keyword\"},"
                + "\"identifier\":{\"type\":\"keyword\"},"
                + "\"session-messages\":{\"type\":\"keyword\"},"
                + "\"session_action\":{\"type\":\"keyword\"},"
                + "\"the_end\":{\"type\":\"keyword\"},"
                + "\"send_packets\":{\"type\":\"keyword\"},"
                + "\"send_bytes\":{\"type\":\"keyword\"},"
                + "\"receive_packets\":{\"type\":\"keyword\"},"
                + "\"receive_bytes\":{\"type\":\"keyword\"},"
                + "\"startTime\":{\"type\":\"keyword\"},"
                + "\"closeTime\":{\"type\":\"keyword\"},"
                + "\"flow_type\":{\"type\":\"keyword\"},"
                + "\"log_messages\":{\"type\":\"keyword\"},"
                + "\"rule\":{\"type\":\"keyword\"},"
                + "\"the_mid-1\":{\"type\":\"keyword\"},"
                + "\"the_mid-2\":{\"type\":\"keyword\"},"
                + "\"the_mid-3\":{\"type\":\"keyword\"},"
                + "\"the_mid-4\":{\"type\":\"keyword\"},"
                + "\"login_user\":{\"type\":\"keyword\"},"
                + "\"login_type\":{\"type\":\"keyword\"},"
                + "\"login_ip\":{\"type\":\"ip\"},"
                + "\"flood-protocol\":{\"type\":\"keyword\"},"
                + "\"flood-type\":{\"type\":\"keyword\"},"
                + "\"area\":{\"type\":\"keyword\"},"
                + "\"occurred_num\":{\"type\":\"keyword\"},"
                + "\"occurred_times\":{\"type\":\"keyword\"},"
                + "\"threat_name\":{\"type\":\"keyword\"},"
                + "\"threat_type\":{\"type\":\"keyword\"},"
                + "\"threat_subtype\":{\"type\":\"keyword\"},"
                + "\"threat_protocol\":{\"type\":\"keyword\"},"
                + "\"threat_action\":{\"type\":\"keyword\"},"
                + "\"threat_defender\":{\"type\":\"keyword\"},"
                + "\"threat_severity\":{\"type\":\"keyword\"},"
                + "\"threat_body\":{\"type\":\"keyword\"},"
                + "\"client_ip_geoip\": {"
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
                + "\"Metafield_description\":{\"type\":\"text\", \"fields\": {\"raw\": {\"type\": \"keyword\"}}}"
                + "}"
                + "}";
        return mapping;
    }
}
