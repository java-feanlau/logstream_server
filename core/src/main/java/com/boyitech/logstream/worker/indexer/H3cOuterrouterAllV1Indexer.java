package com.boyitech.logstream.worker.indexer;

import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.GrokUtil;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.IndexerTimeUtils;
import com.boyitech.logstream.core.util.MetafieldHelper;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;

import com.boyitech.logstream.core.worker.indexer.BaseIndexerConfig;
import io.krakens.grok.api.Grok;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author: juzheng
 * @Title: H3cOuterrouterAllV1Indexer
 * @date: 2019-07-17T15:46:04.543
 * @Description: 1.此indexer文件根据indexer模版创建
 * 2.完成商学院---出口路由器日志格式化型号：SR6602
 */
public class H3cOuterrouterAllV1Indexer extends BaseIndexer {
    //此处初始化正则、grok的变量
    private Grok grok1;
    private Grok grok_LOGIN5;
    private Grok grok_OPTMOD4;
    private Grok grok_SHELL4;
    private ArrayList<Grok> grok_SHELL5;
    private Grok grok_SHELL6;
    private Grok grok_CFGMAN5;

    private String pattern1;
    private String pattern_LOGIN5;
    private String pattern_OPTMOD4;
    private String pattern_SHELL4;
    private String[] pattern_SHELL5;
    private String pattern_SHELL6;
    private String pattern_CFGMAN5;
    private BaseIndexerConfig config;

    public H3cOuterrouterAllV1Indexer(BaseWorkerConfig config) {
        super(config);
        this.config= (BaseIndexerConfig) config;

    }

    public H3cOuterrouterAllV1Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
        this.config= (BaseIndexerConfig) config;

    }

    @Override
    public boolean register() {
        //此处注册正确的正则+grok

        pattern1 = "((<%{NUMBER:id}>(?<time>%{MONTH} %{MONTHDAY} %{TIME} %{YEAR}))?)(\\s*)H3C(\\s)%%10(%{NOTSPACE:log_from_process})\\/(%{NUMBER:log_type_num})\\/(%{DATA:log_type_name}):(\\s)(%{GREEDYDATA:log_body})(\\\\u0000)?$";
        pattern_LOGIN5 = "%{NOTSPACE:login_user}(\\s)%{NOTSPACE:login_result}(\\s)to(\\s)log(\\s)in(\\s)from(\\s)%{IP:login_src_ip}.";
        pattern_OPTMOD4 = "%{NOTSPACE:physical_port}:(\\s)%{GREEDYDATA:module_log_body}";
        pattern_SHELL4 = "-User=%{NOTSPACE:cmd_user}-IPAddr=%{IP:cmd_IP};(\\s)Command(\\s)%{NOTSPACE:cmd_command}(\\s)in(\\s)view(\\s)system(\\s)%{NOTSPACE:cmd_result}(\\s)to(\\s)be(\\s)matched.";
        pattern_SHELL5 = new String[]{
                "(%{NOTSPACE:login_user})(\\s)logged(\\s)in(\\s)from(\\s)(%{IP:login_src_ip}).",
                "(%{NOTSPACE:login_user})(\\s)logged(\\s)out(\\s)from(\\s)(%{IP:login_src_ip})."
        };
        pattern_SHELL6 = "-Line=(%{NOTSPACE:cmd_line})-IPAddr=(%{IP:cmd_IP})-User=(%{NOTSPACE:cmd_user});(\\s)Command(\\s)is(\\s)(%{GREEDYDATA:cmd_command})";
        pattern_CFGMAN5 = "-EventIndex=(%{NUMBER:event_index})-CommandSource=(%{NOTSPACE:command_source})-ConfigSource=(%{NOTSPACE:config_source})-ConfigDestination=(%{NOTSPACE:config_destination});(\\s)(%{GREEDYDATA:config_body}).";

        grok1 = GrokUtil.getGrok(pattern1);
        grok_LOGIN5 = GrokUtil.getGrok(pattern_LOGIN5);
        grok_OPTMOD4 = GrokUtil.getGrok(pattern_OPTMOD4);
        grok_SHELL4 = GrokUtil.getGrok(pattern_SHELL4);
        grok_SHELL5 = GrokUtil.getGroks(pattern_SHELL5);
        grok_SHELL6 = GrokUtil.getGrok(pattern_SHELL6);
        grok_CFGMAN5 = GrokUtil.getGrok(pattern_CFGMAN5);
        return true;

    }

    @Override
    public boolean format(Event event) {
        //最外层，格式化日志主体
        String message = event.getMessage();
        Map<String, Object> map = GrokUtil.getMap(grok1, message);
        Map<String, Object> format = event.getFormat();
        format.putAll(map);
        format.put("message", message);
        if (map.get("flag") == null && map.get("flag") != "解析失败") {
            event.setMetafieldLoglevel("1");
        }

        //下一层，具体字段继续格式化
        String log_from_process = String.valueOf(format.get("log_from_process"));
        String log_type_num = String.valueOf(format.get("log_type_num"));
        String log_body = String.valueOf(format.get("log_body"));
        if (GrokUtil.isStringHasValue(log_from_process) && GrokUtil.isStringHasValue(log_type_num) && GrokUtil.isStringHasValue(log_body)) {
            if (log_from_process.equals("LOGIN") && log_type_num.equals("5")) {
                Map<String, Object> mapLogin5 = GrokUtil.getMap(grok_LOGIN5, log_body);
                format.putAll(mapLogin5);
                if (mapLogin5.get("flag") == null && mapLogin5.get("flag") != "解析失败")
                    event.setMetafieldLoglevel("2");
            }

            if (log_from_process.equals("OPTMOD") && log_type_num.equals("4")) {
                Map<String, Object> mapOPTMOD4 = GrokUtil.getMap(grok_OPTMOD4, log_body);
                format.putAll(mapOPTMOD4);
                if (mapOPTMOD4.get("flag") == null && mapOPTMOD4.get("flag") != "解析失败")
                    event.setMetafieldLoglevel("2");
            }

            if (log_from_process.equals("SHELL") && log_type_num.equals("4")) {
                Map<String, Object> mapSHELL4 = GrokUtil.getMap(grok_SHELL4, log_body);
                format.putAll(mapSHELL4);
                if (mapSHELL4.get("flag") == null && mapSHELL4.get("flag") != "解析失败")
                    event.setMetafieldLoglevel("2");
            }

            if (log_from_process.equals("SHELL") && log_type_num.equals("5")) {
                Map<String, Object> mapSHELL5 = GrokUtil.getMapByGroks(grok_SHELL5, log_body);
                format.putAll(mapSHELL5);
                if (mapSHELL5.get("flag") == null && mapSHELL5.get("flag") != "解析失败")
                    event.setMetafieldLoglevel("2");
            }

            if (log_from_process.equals("SHELL") && log_type_num.equals("6")) {
                Map<String, Object> mapSHELL6 = GrokUtil.getMap(grok_SHELL6, log_body);
                format.putAll(mapSHELL6);
                if (mapSHELL6.get("flag") == null && mapSHELL6.get("flag") != "解析失败")
                    event.setMetafieldLoglevel("2");
            }

            if (log_from_process.equals("CFGMAN") && log_type_num.equals("5")) {
                Map<String, Object> mapCFGMAN5 = GrokUtil.getMap(grok_CFGMAN5, log_body);
                format.putAll(mapCFGMAN5);
                if (mapCFGMAN5.get("flag") == null && mapCFGMAN5.get("flag") != "解析失败")
                    event.setMetafieldLoglevel("2");
            }
        }

        //格式化时间，此日志内无时间字段，
        IndexerTimeUtils.getISO8601Time2(format, "time", "");



        if (!config.getIpFilter().equals("null")) {
            GrokUtil.filterGeoIP(config, format);
        } else {
            //使用GeoIP对src_ip、dst_ip或部分特殊字段处理，
            GrokUtil.setGeoIP2(format, "login_src_ip");
            GrokUtil.setGeoIP2(format, "cmd_IP");
        }

        //格式化Metafield ，可能需要根据实际日志格式替换部分字段名如client_ip/src_ip、dst_ip

        event.setMetafieldLoglevel("2");
//        if (event.getLogType() != null) {
//            format.put("Metafield_description", event.getLogType());
//            format.put("Metafield_type", event.getLogType());
//        }
        if (event.getSource() != null) {
           // format.put("Metafield_source", event.getSource());
            //event.setSource(event.getSource());
            format.put("log_source", event.getSource());//增加来源设备标识；
        }
//        if (format.get("login_src_ip") != null) {
//            event.setSource(String.valueOf(format.get("login_src_ip")));
//           // format.put("Metafield_object", format.get("login_src_ip"));
//        }
//        if (format.get("dst_ip") != null) {
//            event.setSource(String.valueOf(format.get("dst_ip")));
//            // format.put("Metafield_subject", format.get("dst_ip"));
//        } else {
//            format.put("Metafield_subject", event.getSource());
//        }
        MetafieldHelper.setMetafield(event,"login_src_ip","cmd_IP","",format);
        if (format.get("flag") == "解析失败")
            return false;
        return true;
    }

    @Override
    public void tearDown() {
    }

    //上传的Mapping，要在下面两处空格处加上对应的Mapping字段；
    public static Map getMapping() {
        String mapping = "{\"properties\":{"
                + "\"@timestamp\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
                + "\"received_at\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
                + "\"portedAt\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
                + "\"flag\":{\"type\":\"keyword\"},"
                + "\"message\":{\"type\":\"text\"},"
                + "\"log_from_process\":{\"type\":\"keyword\"},"
                + "\"log_type_num\":{\"type\":\"keyword\"},"
                + "\"log_type_name\":{\"type\":\"keyword\"},"
                + "\"log_body\":{\"type\":\"keyword\"},"
                + "\"login_user\":{\"type\":\"keyword\"},"
                + "\"login_result\":{\"type\":\"keyword\"},"
                + "\"login_src_ip\":{\"type\":\"keyword\"},"
                + "\"physical_port\":{\"type\":\"keyword\"},"
                + "\"module_log_body\":{\"type\":\"keyword\"},"
                + "\"cmd_user\":{\"type\":\"keyword\"},"
                + "\"cmd_IP\":{\"type\":\"keyword\"},"
                + "\"cmd_command\":{\"type\":\"keyword\"},"
                + "\"cmd_result\":{\"type\":\"keyword\"},"
                + "\"cmd_line\":{\"type\":\"keyword\"},"
                + "\"event_index\":{\"type\":\"keyword\"},"
                + "\"command_source\":{\"type\":\"keyword\"},"
                + "\"config_source\":{\"type\":\"keyword\"},"
                + "\"config_destination\":{\"type\":\"keyword\"},"
                + "\"config_body\":{\"type\":\"keyword\"},"
                + "\"id\":{\"type\":\"keyword\"},"
                + "\"time\":{\"type\":\"keyword\"},"
                + "\"login_src_ip_geoip\": {"
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
                + "\"cmd_IP_geoip\": {"
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
                + "\"log_from_process\":{\"type\":\"keyword\"},"
                + "\"log_type_num\":{\"type\":\"keyword\"},"
                + "\"log_type_name\":{\"type\":\"keyword\"},"
                + "\"log_body\":{\"type\":\"keyword\"},"
                + "\"login_user\":{\"type\":\"keyword\"},"
                + "\"login_result\":{\"type\":\"keyword\"},"
                + "\"login_src_ip\":{\"type\":\"keyword\"},"
                + "\"physical_port\":{\"type\":\"keyword\"},"
                + "\"module_log_body\":{\"type\":\"keyword\"},"
                + "\"cmd_user\":{\"type\":\"keyword\"},"
                + "\"cmd_IP\":{\"type\":\"keyword\"},"
                + "\"cmd_command\":{\"type\":\"keyword\"},"
                + "\"cmd_result\":{\"type\":\"keyword\"},"
                + "\"cmd_line\":{\"type\":\"keyword\"},"
                + "\"event_index\":{\"type\":\"keyword\"},"
                + "\"command_source\":{\"type\":\"keyword\"},"
                + "\"config_source\":{\"type\":\"keyword\"},"
                + "\"config_destination\":{\"type\":\"keyword\"},"
                + "\"config_body\":{\"type\":\"keyword\"},"
                + "\"id\":{\"type\":\"keyword\"},"
                + "\"time\":{\"type\":\"keyword\"},"
                + "\"login_src_ip_geoip\": {"
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
                + "\"cmd_IP_geoip\": {"
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