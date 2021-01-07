package com.boyitech.logstream.worker.indexer;


import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.GrokUtil;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.MetafieldHelper;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import io.krakens.grok.api.Grok;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author juzheng
 * @Title: FortinetIndexer
 * @date 2019/3/20 10:23 AM
 * @Description: （博世）Fortinet防火墙日志的解析：完成1.流量日志-转发流量（traffic，1种格式） 2.事件日志-系统（event，三种格式）的解析。解析分为两层。
 *
 */
public class FortinetFirewallAllV1Indexer extends BaseIndexer {
    private  Grok grok_1;
    private  Grok grok_2_traffic;
    private  ArrayList<Grok> groks_2_event;
    private  String pattern_1;   //匹配最外层
    private String pattern_2_traffic;  //第二层，具体匹配第一层的log-body字段，
    private String[] patterns_2_event;

    public FortinetFirewallAllV1Indexer(BaseWorkerConfig config) {
        super(config);
    }

    public FortinetFirewallAllV1Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
    }

    @Override
    public boolean register() {

        pattern_1= "\\<(%{NUMBER:syslog_id})\\>date=(?<date>%{YEAR}-%{MONTHNUM}-%{MONTHDAY})(\\s+)time=(%{TIME:time})(\\s+)(logver=(%{NUMBER:logver})(\\s+)){0,1}devid=(%{NOTSPACE:dev_id})(\\s+)devname=(%{NOTSPACE:dev_name})(\\s+)logid=(%{NOTSPACE:log_id})(\\s+)type=(%{NOTSPACE:log_type})(\\s+)subtype=(%{NOTSPACE:log_subtype})(\\s+)level=(%{NOTSPACE:log_level})(\\s+)vd=(%{NOTSPACE:log_vd})(\\s+)(%{GREEDYDATA:log_body})";

        pattern_2_traffic="srcip=(%{IP:src_ip})(\\s+)(srcport=(%{NUMBER:src_port})(\\s+)){0,1}srcintf=(\\\"){0,1}(%{DATA:src_intf})(\\\"){0,1}(\\s+)dstip=(%{IP:dst_ip})(\\s+)(dstport=(%{NUMBER:dst_port})(\\s+)){0,1}dstintf=(\\\"){0,1}(%{DATA:dst_intf})(\\\"){0,1}(\\s+)(poluuid=(%{NOTSPACE:pol_uuid})(\\s+)){0,1}sessionid=(%{NOTSPACE:session_id})(\\s+)proto=(%{NOTSPACE:proto})(\\s+)(action=(%{NOTSPACE:action})(\\s+)){0,1}(status=(%{NOTSPACE:status})(\\s+)){0,1}policyid=(%{NOTSPACE:policy_id})(\\s+)(dstcountry=\\\"(%{DATA:dst_country})\\\"(\\s+)){0,1}(srccountry=\\\"(%{DATA:src_country})\\\"(\\s+)){0,1}trandisp=(%{NOTSPACE:trandisp})(\\s+)(service=(%{DATA:service})(\\s+)){0,1}duration=(%{NUMBER:duration})(\\s+)(%{GREEDYDATA:subfix_log_messages})";
        patterns_2_event=new String[]{
                "action=\\\"(%{DATA:action})\\\"(\\s+)cpu=(%{NUMBER:cpu})(\\s+)mem=(%{NUMBER:mem})(\\s+)totalsession=(%{NUMBER:total_session})(\\s+)msg=\\\"(%{DATA:msg})\\\"",
                "logdesc=(%{DATA:logdes})(\\s+)action=(%{NOTSPACE:action})(\\s+)cpu=(%{NUMBER:cpu})(\\s+)mem=(%{NUMBER:mem})(\\s+)totalsession=(%{NUMBER:total_session})(\\s+)disk=(%{NUMBER:disk})(\\s+)bandwidth=(%{NOTSPACE:bandwidth})(\\s+)setuprate=(%{NUMBER:setup_rate})(\\s+)disklograte=(%{NUMBER:disklog_rate})(\\s+)fazlograte=(%{NUMBER:fazlog_rate})(\\s+)msg=(%{GREEDYDATA:msg})"
        };

        grok_1 = GrokUtil.getGrok(pattern_1);
        grok_2_traffic = GrokUtil.getGrok(pattern_2_traffic);
        groks_2_event=GrokUtil.getGroks(patterns_2_event);

        return true;
    }

    @Override
    public boolean format(Event event) {

        String message = event.getMessage();
        Map<String, Object> messageMap = GrokUtil.getMap(grok_1, message);
        Map<String, Object> formated = event.getFormat();
        formated.putAll(messageMap);
        formated.put("message",message);
        if (messageMap.get("flag") == null && messageMap.get("flag") != "解析失败") {
            event.setMetafieldLoglevel("1");
        }

        String log_body=String.valueOf(formated.get("log_body"));
        if (log_body!=null)
        {
            String log_type=String.valueOf(formated.get("log_type"));
            if(log_type.equals("traffic")){
                formated.put("Metafield_category", "traffic");
                Map<String, Object> messageMap_traffic = GrokUtil.getMap(grok_2_traffic,log_body);
                formated.putAll(messageMap_traffic);
                if (messageMap.get("flag") == null && messageMap.get("flag") != "解析失败")
                    event.setMetafieldLoglevel("2");

                String subfix_log_messages=String.valueOf(formated.get("subfix_log_messages"));
                if(subfix_log_messages.length()!=0) {
                    Map<String,Object> subfix_log_messages_map=new HashMap<>();
                    String left=new String();
                    String right=new String();
                    String[] arr = subfix_log_messages.split("\\s+");
                    for (String ss : arr) {
                        left=ss.substring(0, ss.indexOf("="));
                        right=ss.substring(ss.indexOf("=")+1);
                        subfix_log_messages_map.put(left,right);
                    }
                    formated.putAll(subfix_log_messages_map);

                    if (messageMap.get("flag") == null && messageMap.get("flag") != "解析失败")
                        event.setMetafieldLoglevel("3");
                }
            }

            if(log_type.equals("event")){
                formated.put("Metafield_category", "event");
                Map<String, Object> messageMap_event = GrokUtil.getMapByGroks(groks_2_event,log_body);
                formated.putAll(messageMap_event);
                if (messageMap.get("flag") == null && messageMap.get("flag") != "解析失败")
                    event.setMetafieldLoglevel("2");
            }
        }

        //格式化@timestamp
        //date=2019-03-07 time=16:41:26
        String date=(String)formated.get("date");
        String time=(String)formated.get("time");
        String timestamp=date+" "+time; //2019-03-07 16:41:26 -->>2019-04-02T16:26:10.223+08:00
        if(date!=null&&time!=null)
        {
            String timestamp1=GrokUtil.formTime(timestamp,"yyyy-MM-dd HH:mm:ss","yyyy-MM-dd'T'HH:mm:ssXXX");
            formated.put("@timestamp",timestamp1);
        }


        //格式化Metafield
//        if(GrokUtil.isStringHasValue(String.valueOf(formated.get("src_ip")))){
//            event.setSource(String.valueOf(formated.get("src_ip")));
//        }
//        if (event.getLogType() != null) {
//            formated.put("Metafield_description", event.getLogType());
//            formated.put("Metafield_type", event.getLogType());
//        }
//        if (event.getSource() != null) {
//            formated.put("Metafield_source", event.getSource());
//        }
//
//        if (formated.get("/") != null) {
//            formated.put("Metafield_object", formated.get("src_ip"));
//        }
//        if (formated.get("dst_ip") != null) {
//            formated.put("Metafield_subject", formated.get("dst_ip"));
//        }else {
//            formated.put("Metafield_subject", event.getSource());
//        }
        MetafieldHelper.setMetafield(event,"src_ip","dst_ip","",formated);
        if(formated.get("src_ip") != null && formated.get("dst_ip") != null){
            formated.put("ip_addr_pair", formated.get("src_ip")+"=>"+formated.get("dst_ip"));
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
                + "\"syslog_id\":{\"type\":\"keyword\"},"
                + "\"date\":{\"type\":\"keyword\"},"
                + "\"time\":{\"type\":\"keyword\"},"
                + "\"logver\":{\"type\":\"keyword\"},"
                + "\"dev_id\":{\"type\":\"keyword\"},"
                + "\"dev_name\":{\"type\":\"keyword\"},"
                + "\"log_id\":{\"type\":\"keyword\"},"
                + "\"log_type\":{\"type\":\"keyword\"},"
                + "\"log_subtype\":{\"type\":\"keyword\"},"
                + "\"log_level\":{\"type\":\"keyword\"},"
                + "\"log_vd\":{\"type\":\"keyword\"},"
                + "\"log_body\":{\"type\":\"text\"},"
                + "\"src_ip\":{\"type\":\"ip\"},"
                + "\"ip_address_pair\":{\"type\":\"keyword\"},"
                + "\"src_port\":{\"type\":\"keyword\"},"
                + "\"src_intf\":{\"type\":\"keyword\"},"
                + "\"dst_ip\":{\"type\":\"ip\"},"
                + "\"dst_port\":{\"type\":\"keyword\"},"
                + "\"dst_intf\":{\"type\":\"keyword\"},"
                + "\"pol_uuid\":{\"type\":\"keyword\"},"
                + "\"session_id\":{\"type\":\"keyword\"},"
                + "\"proto\":{\"type\":\"integer\"},"
                + "\"action\":{\"type\":\"keyword\"},"
                + "\"status\":{\"type\":\"keyword\"},"
                + "\"policy_id\":{\"type\":\"keyword\"},"
                + "\"dst_country\":{\"type\":\"keyword\"},"
                + "\"src_country\":{\"type\":\"keyword\"},"
                + "\"trandisp\":{\"type\":\"keyword\"},"
                + "\"service\":{\"type\":\"keyword\"},"
                + "\"duration\":{\"type\":\"integer\"},"
                + "\"subfix_log_messages\":{\"type\":\"text\"},"
                + "\"cpu\":{\"type\":\"long\"},"
                + "\"mem\":{\"type\":\"long\"},"
                + "\"total_session\":{\"type\":\"long\"},"
                + "\"msg\":{\"type\":\"keyword\"},"
                + "\"logdesc\":{\"type\":\"keyword\"},"
                + "\"disk\":{\"type\":\"long\"},"
                + "\"bandwidth\":{\"type\":\"keyword\"},"
                + "\"setup_rate\":{\"type\":\"long\"},"
                + "\"disklog_rate\":{\"type\":\"long\"},"
                + "\"fazlog_rate\":{\"type\":\"long\"},"
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
                + "\"syslog_id\":{\"type\":\"keyword\"},"
                + "\"date\":{\"type\":\"keyword\"},"
                + "\"time\":{\"type\":\"keyword\"},"
                + "\"logver\":{\"type\":\"keyword\"},"
                + "\"dev_id\":{\"type\":\"keyword\"},"
                + "\"dev_name\":{\"type\":\"keyword\"},"
                + "\"log_id\":{\"type\":\"keyword\"},"
                + "\"log_type\":{\"type\":\"keyword\"},"
                + "\"log_subtype\":{\"type\":\"keyword\"},"
                + "\"log_level\":{\"type\":\"keyword\"},"
                + "\"log_vd\":{\"type\":\"keyword\"},"
                + "\"log_body\":{\"type\":\"text\"},"
                + "\"src_ip\":{\"type\":\"ip\"},"
                + "\"ip_address_pair\":{\"type\":\"keyword\"},"
                + "\"src_port\":{\"type\":\"keyword\"},"
                + "\"src_intf\":{\"type\":\"keyword\"},"
                + "\"dst_ip\":{\"type\":\"ip\"},"
                + "\"dst_port\":{\"type\":\"keyword\"},"
                + "\"dst_intf\":{\"type\":\"keyword\"},"
                + "\"pol_uuid\":{\"type\":\"keyword\"},"
                + "\"session_id\":{\"type\":\"keyword\"},"
                + "\"proto\":{\"type\":\"integer\"},"
                + "\"action\":{\"type\":\"keyword\"},"
                + "\"status\":{\"type\":\"keyword\"},"
                + "\"policy_id\":{\"type\":\"keyword\"},"
                + "\"dst_country\":{\"type\":\"keyword\"},"
                + "\"src_country\":{\"type\":\"keyword\"},"
                + "\"trandisp\":{\"type\":\"keyword\"},"
                + "\"service\":{\"type\":\"keyword\"},"
                + "\"duration\":{\"type\":\"integer\"},"
                + "\"subfix_log_messages\":{\"type\":\"text\"},"
                + "\"cpu\":{\"type\":\"long\"},"
                + "\"mem\":{\"type\":\"long\"},"
                + "\"total_session\":{\"type\":\"long\"},"
                + "\"msg\":{\"type\":\"keyword\"},"
                + "\"logdesc\":{\"type\":\"keyword\"},"
                + "\"disk\":{\"type\":\"long\"},"
                + "\"bandwidth\":{\"type\":\"keyword\"},"
                + "\"setup_rate\":{\"type\":\"long\"},"
                + "\"disklog_rate\":{\"type\":\"long\"},"
                + "\"fazlog_rate\":{\"type\":\"long\"},"
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
