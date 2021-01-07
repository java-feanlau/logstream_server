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

import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: juzheng
 * @Title: HuaweiUmaAllV1Indexer
 * @date: 2019-07-29T13:25:32.565
 * @Description: 1. 此indexer文件根据indexer通用模版创建
 * 2.完成电机学院华为uma日志解析需求
 */
public class HuaweiUmaAllV1Indexer extends BaseIndexer {
    //此处初始化正则、grok的变量
    private String pattern1;

    private String pattern2UMA;
    private String pattern2UMA_3login;
    private String pattern2UMA_3logout;
    private String[] pattern2UMA_3config;

    private String pattern2pldrun;

    private String pattern2xwin;
    private String pattern2xwin_3login;
    private String pattern2xwin_3logout;

    private String pattern2xapp;
    private String pattern2xapp_3login;
    private String pattern2xapp_3logout;

    private String pattern2pldapp;


    private Grok grok1;

    private Grok grok2UMA;
    private Grok grok2UMA_3login;
    private Grok grok2UMA_3logout;
    private ArrayList<Grok> grok2UMA_3config;

    private Grok grok2pldrun;

    private Grok grok2xwin;
    private Grok grok2xwin_3login;
    private Grok grok2xwin_3logout;

    private Grok grok2xapp;
    private Grok grok2xapp_3login;
    private Grok grok2xapp_3logout;

    private Grok grok2pldapp;
    private BaseIndexerConfig config;



    public HuaweiUmaAllV1Indexer(BaseWorkerConfig config) {
        super(config);
        this.config= (BaseIndexerConfig) config;

    }

    public HuaweiUmaAllV1Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
        this.config= (BaseIndexerConfig) config;

    }

    @Override
    public boolean register() {
        //此处注册正确的正则+grok
        pattern1 = "^(?<loginTime>%{MONTH}(\\s)+%{MONTHDAY}(\\s)+%{TIME})(\\s)+(%{NOTSPACE:device_id}) (%{NOTSPACE:pldsec_type}):(%{GREEDYDATA:pldsec_log_body})";

        pattern2UMA = "^(\\s)+type=(\\\\)?(\\\")?(%{NOTSPACE:uma_type})(\\\\)?(\\\")(\\s)(%{GREEDYDATA:uma_log_body})";
        pattern2UMA_3login = "^date=\\\"(%{TIMESTAMP_ISO8601:pldsec_date})\\\"(\\s+)srcip=\\\"(%{IP:src_ip})\\\"(\\s+)sysuser=\\\"(%{NOTSPACE:sysuser})\\\"(\\s+)module=\\\"(%{NOTSPACE:module})\\\"(\\s+)result=\\\"(%{NOTSPACE:result})(\\\")";
        pattern2UMA_3logout = "^date=\\\"(%{TIMESTAMP_ISO8601:pldsec_date})\\\"(\\s+)srcip=\\\"(%{IP:src_ip})\\\"(\\s+)sysuser=\\\"(%{NOTSPACE:sysuser})\\\"(\\s+)result=\\\"(%{NOTSPACE:result})\\\"";
        pattern2UMA_3config = new String[]{
                "date=\\\"(%{TIMESTAMP_ISO8601:pldsec_date})\\\"(\\s+)srcip=\\\"(%{NOTSPACE:src_ip})\\\"(\\s+)sysuser=\\\"(%{NOTSPACE:sysuser})\\\"(\\s+)module=\\\"(%{NOTSPACE:module})\\\"(\\s+)log_action=\\\"(%{NOTSPACE:log_action})(\\\")",
                "date=\\\"(%{TIMESTAMP_ISO8601:pldsec_date})\\\"(\\s+)srcip=\\\"(%{NOTSPACE:src_ip})\\\"(\\s+)sysuser=\\\"(%{NOTSPACE:sysuser})\\\"(\\s+)module=\\\"(%{NOTSPACE:module})\\\"(\\s+)log_action=\\\"(%{NOTSPACE:log_action})\\\"(\\s+)detail=\\\"(%{GREEDYDATA:detail})(\\\")"
        };

        pattern2pldrun = "^(\\s)+id=(%{WORD:id}) account=(%{NOTSPACE:account}) srvname=(%{NOTSPACE:srvname}) loginname=(%{WORD:login_name}) loginip=(%{NOTSPACE:loginip}) command_id=(%{NOTSPACE:command_id}) (screen_id=(%{NOTSPACE:screen_id})(\\s)+)?(logscreen)?(logcmd)?=(%{GREEDYDATA:screen_messages})";

        pattern2xwin = "^(\\s)+sessiontype=(%{NOTSPACE:sessiontype})(%{GREEDYDATA:session_messages})";
        pattern2xwin_3login = "^(\\s)+sessionid=(%{NOTSPACE:session_id}) mode=(%{NOTSPACE:mode}) loginip=(%{NOTSPACE:loginip}) loginname=(%{NOTSPACE:login_name}) srvaddr=(%{NOTSPACE:srvaddr}) account=(%{NOTSPACE:account})(\\s+)";
        pattern2xwin_3logout = "^(\\s+)sessionid=(%{NOTSPACE:session_id})(\\s+)";

        pattern2xapp = "^(\\s+)sessiontype=(%{NOTSPACE:sessiontype})(%{GREEDYDATA:session_messages})";
        pattern2xapp_3login = "^(\\s+)sessionid=(%{NOTSPACE:session_id}) mode=(%{DATA:mode}) loginip=(%{NOTSPACE:loginip}) loginname=(%{NOTSPACE:login_name}) srvaddr=(%{NOTSPACE:srvaddr}) account=(%{NOTSPACE:account})(\\s+)";
        pattern2xapp_3logout = "^(\\s+)sessionid=(%{NOTSPACE:session_id})(\\s+)";

        pattern2pldapp = "^(\\s)+sessiontype=(%{NOTSPACE:sessiontype}) sessionid=(%{NOTSPACE:session_id}) mode=(%{NOTSPACE:mode}) loginip=(%{NOTSPACE:loginip}) loginname=(%{NOTSPACE:login_name}) srvaddr=(%{NOTSPACE:srvaddr}) account=(%{NOTSPACE:account})(\\s+)";

        grok1 = GrokUtil.getGrok(pattern1);

        grok2UMA = GrokUtil.getGrok(pattern2UMA);
        grok2UMA_3login = GrokUtil.getGrok(pattern2UMA_3login);
        grok2UMA_3logout = GrokUtil.getGrok(pattern2UMA_3logout);
        grok2UMA_3config = GrokUtil.getGroks(pattern2UMA_3config);

        grok2pldrun = GrokUtil.getGrok(pattern2pldrun);

        grok2xwin = GrokUtil.getGrok(pattern2xwin);
        grok2xwin_3login = GrokUtil.getGrok(pattern2xwin_3login);
        grok2xwin_3logout = GrokUtil.getGrok(pattern2xwin_3logout);

        grok2xapp = GrokUtil.getGrok(pattern2xapp);
        grok2xapp_3login = GrokUtil.getGrok(pattern2xapp_3login);
        grok2xapp_3logout = GrokUtil.getGrok(pattern2xapp_3logout);

        grok2pldapp = GrokUtil.getGrok(pattern2pldapp);

        return true;

    }

    @Override
    public boolean format(Event event) {
        //最外层，格式化日志主体
        String message = event.getMessage();
        Map<String, Object> map = GrokUtil.getMap(grok1, message);
        Map<String, Object> map1 = new HashMap<>();
        map1.put("login_time", map.get("loginTime"));
        Map<String, Object> format = event.getFormat();
        format.putAll(map);
        format.putAll(map1);
        format.put("message", message);
        if (map.get("flag") == null && map.get("flag") != "解析失败") {
            event.setMetafieldLoglevel("1");
        }

        //下一层，具体字段继续格式化
        String pldsec_type = String.valueOf(format.get("pldsec_type"));
        String pldsec_log_body = String.valueOf(format.get("pldsec_log_body"));
        if (GrokUtil.isStringHasValue(pldsec_type) && GrokUtil.isStringHasValue(pldsec_log_body)) {
            if (pldsec_type.equals("UMA")) {
                Map<String, Object> map2UMA = GrokUtil.getMap(grok2UMA, pldsec_log_body);
                format.putAll(map2UMA);
                if (map2UMA.get("flag") == null && map2UMA.get("flag") != "解析失败") {
                    event.setMetafieldLoglevel("2");
                }
                String uma_type = String.valueOf(format.get("uma_type"));
                String uma_log_body = String.valueOf(format.get("uma_log_body"));
                if (GrokUtil.isStringHasValue(uma_type) && GrokUtil.isStringHasValue(uma_log_body)) {
                    if (uma_type.equals("login")) {
                        Map<String, Object> map2UMA_3login = GrokUtil.getMap(grok2UMA_3login, uma_log_body);
                        format.putAll(map2UMA_3login);
                        if (map2UMA_3login.get("flag") == null && map2UMA_3login.get("flag") != "解析失败") {
                            event.setMetafieldLoglevel("3");
                        }
                    }
                    if (uma_type.equals("logout")) {
                        Map<String, Object> map2UMA_3logout = GrokUtil.getMap(grok2UMA_3logout, uma_log_body);
                        format.putAll(map2UMA_3logout);
                        if (map2UMA_3logout.get("flag") == null && map2UMA_3logout.get("flag") != "解析失败") {
                            event.setMetafieldLoglevel("3");
                        }
                    }
                    if (uma_type.equals("config")) {
                        Map<String, Object> map2UMA_3config = GrokUtil.getMapByGroks(grok2UMA_3config, uma_log_body);
                        format.putAll(map2UMA_3config);
                        if (map2UMA_3config.get("flag") == null && map2UMA_3config.get("flag") != "解析失败") {
                            event.setMetafieldLoglevel("3");
                        }
                    }
                }
            }

            if (pldsec_type.equals("pldrun")) {
                Map<String, Object> map2pldrun = GrokUtil.getMap(grok2pldrun, pldsec_log_body);
                format.putAll(map2pldrun);
                if (map2pldrun.get("flag") == null && map2pldrun.get("flag") != "解析失败") {
                    event.setMetafieldLoglevel("2");
                }
            }

            if (pldsec_type.equals("xwin")) {
                Map<String, Object> map2xwin = GrokUtil.getMap(grok2xwin, pldsec_log_body);
                format.putAll(map2xwin);
                if (map2xwin.get("flag") == null && map2xwin.get("flag") != "解析失败") {
                    event.setMetafieldLoglevel("2");
                }
                String sessiontype = String.valueOf(format.get("sessiontype"));
                String session_messages = String.valueOf(format.get("session_messages"));
                if (GrokUtil.isStringHasValue(sessiontype) && GrokUtil.isStringHasValue(session_messages)) {
                    if (sessiontype.equals("login")) {
                        Map<String, Object> map2xwin_login = GrokUtil.getMap(grok2xwin_3login, session_messages);
                        format.putAll(map2xwin_login);
                        if (map2xwin_login.get("flag") == null && map2xwin_login.get("flag") != "解析失败") {
                            event.setMetafieldLoglevel("3");
                        }
                    }
                    if (sessiontype.equals("logout")) {
                        Map<String, Object> map2xwin_logout = GrokUtil.getMap(grok2xwin_3logout, session_messages);
                        format.putAll(map2xwin_logout);
                        if (map2xwin_logout.get("flag") == null && map2xwin_logout.get("flag") != "解析失败") {
                            event.setMetafieldLoglevel("3");
                        }

                    }
                }
            }

            if (pldsec_type.equals("xapp")) {
                Map<String, Object> map2xapp = GrokUtil.getMap(grok2xapp, pldsec_log_body);
                format.putAll(map2xapp);
                if (map2xapp.get("flag") == null && map2xapp.get("flag") != "解析失败") {
                    event.setMetafieldLoglevel("2");
                }
                String sessiontype = String.valueOf(format.get("sessiontype"));
                String session_messages = String.valueOf(format.get("session_messages"));
                if (GrokUtil.isStringHasValue(session_messages) && GrokUtil.isStringHasValue(sessiontype)) {
                    if (sessiontype.equals("login")) {
                        Map<String, Object> map2xapp_login = GrokUtil.getMap(grok2xapp_3login, session_messages);
                        format.putAll(map2xapp_login);
                        if (map2xapp_login.get("flag") == null && map2xapp_login.get("flag") != "解析失败") {
                            event.setMetafieldLoglevel("3");
                        }
                    }
                    if (sessiontype.equals("logout")) {
                        Map<String, Object> map2xapp_logout = GrokUtil.getMap(grok2xapp_3logout, session_messages);
                        format.putAll(map2xapp_logout);
                        if (map2xapp_logout.get("flag") == null && map2xapp_logout.get("flag") != "解析失败") {
                            event.setMetafieldLoglevel("3");
                        }
                    }
                }
            }

            if (pldsec_type.equals("pldapp")) {
                Map<String, Object> map2pldapp = GrokUtil.getMap(grok2pldapp, pldsec_log_body);
                format.putAll(map2pldapp);
                if (map2pldapp.get("flag") == null && map2pldapp.get("flag") != "解析失败") {
                    event.setMetafieldLoglevel("2");
                }
            }

        }
        //格式化时间：
        //这是timestamp:
        String loginTime=String.valueOf(format.get("loginTime"));
        loginTime=Year.now()+" "+loginTime;
        if(loginTime.substring(9,10).equals(" ")){
            IndexerTimeUtils.getISO8601Time3(format, loginTime, "yyyy MMM  d HH:mm:ss");
        }
        else{
            IndexerTimeUtils.getISO8601Time3(format, loginTime, "yyyy MMM dd HH:mm:ss");

        }
        //这是pldsec_date字段的格式化
        String pldsec_date = String.valueOf(format.get("pldsec_date"));
        String pldsec_date_ISO8601;
        if (GrokUtil.isStringHasValue(pldsec_date)) {
            pldsec_date_ISO8601 = IndexerTimeUtils.getISO8601Time(pldsec_date, "yyyy-MM-dd HH:mm:ss");
            if (pldsec_date_ISO8601 != null && !pldsec_date_ISO8601.equals("")) {
                format.put("pldsec_date_ISO8601", pldsec_date_ISO8601);
            }
        }

        if (!config.getIpFilter().equals("null")) {
            GrokUtil.filterGeoIP(config, format);
        } else {
            //使用GeoIP对src_ip、dst_ip或部分特殊字段处理，
            GrokUtil.setGeoIP2(format, "src_ip");
            GrokUtil.setGeoIP2(format, "loginip");
            GrokUtil.setGeoIP2(format, "srvaddr");
            GrokUtil.setGeoIP2(format, "srvname");
        }


        //格式化Metafield ，可能需要根基实际日志格式替换部分字段名如client_ip/src_ip、dst_ip
//        if (event.getLogType() != null) {
//            format.put("Metafield_description", event.getLogType());
//            format.put("Metafield_type", event.getLogType());
//        }
        if (event.getSource() != null) {
           // format.put("Metafield_source", event.getSource());
          //  event.setSource(event.getSource());
            format.put("log_source", event.getSource());//增加来源设备标识；
        }
//        if (format.get("src_ip") != null) {
//            event.setSource(String.valueOf(format.get("src_ip")));
//           // format.put("Metafield_object", format.get("src_ip"));
//        }
//        if (format.get("loginip") != null) {
//            event.setSource(String.valueOf(format.get("loginip")));
//           // format.put("Metafield_object", format.get("loginip"));
//        }
//        if (format.get("dst_ip") != null) {
//            event.setSource(String.valueOf(format.get("dst_ip")));
//           // format.put("Metafield_subject", format.get("dst_ip"));
//        } else {
//            format.put("Metafield_subject", event.getSource());
//        }
        MetafieldHelper.setMetafield(event,"src_ip","srvaddr","",format);
        MetafieldHelper.setMetafield(event,"loginip","srvaddr","",format);
        MetafieldHelper.setMetafield(event,"srvname","srvaddr","",format);


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
                + "\"login_time\":{\"type\":\"keyword\"},"
                + "\"device_id\":{\"type\":\"keyword\"},"
                + "\"pldsec_type\":{\"type\":\"keyword\"},"
                + "\"pldsec_log_body\":{\"type\":\"keyword\"},"
                + "\"uma_type\":{\"type\":\"keyword\"},"
                + "\"uma_log_body\":{\"type\":\"keyword\"},"
                + "\"pldsec_date\":{\"type\":\"keyword\"},"
                + "\"src_ip\":{\"type\":\"keyword\"},"
                + "\"sysuser\":{\"type\":\"keyword\"},"
                + "\"module\":{\"type\":\"keyword\"},"
                + "\"result\":{\"type\":\"keyword\"},"
                + "\"log_action\":{\"type\":\"keyword\"},"
                + "\"detail\":{\"type\":\"keyword\"},"
                + "\"id\":{\"type\":\"keyword\"},"
                + "\"account\":{\"type\":\"keyword\"},"
                + "\"srvname\":{\"type\":\"keyword\"},"
                + "\"login_name\":{\"type\":\"keyword\"},"
                + "\"loginip\":{\"type\":\"keyword\"},"
                + "\"command_id\":{\"type\":\"keyword\"},"
                + "\"screen_id\":{\"type\":\"keyword\"},"
                + "\"screen_messages\":{\"type\":\"keyword\"},"
                + "\"sessiontype\":{\"type\":\"keyword\"},"
                + "\"srvaddr\":{\"type\":\"keyword\"},"
                + "\"session_id\":{\"type\":\"keyword\"},"
                + "\"mode\":{\"type\":\"keyword\"},"
                + "\"session_messages\":{\"type\":\"keyword\"},"
                + "\"loginTime\":{\"type\":\"keyword\"},"
                + "\"pldsec_date_ISO8601\":{\"type\":\"keyword\"},"
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
                + "\"loginip_geoip\": {"
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
                + "\"srvaddr_geoip\": {"
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
                + "\"srvname_geoip\": {"
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
                + "\"login_time\":{\"type\":\"keyword\"},"
                + "\"device_id\":{\"type\":\"keyword\"},"
                + "\"pldsec_type\":{\"type\":\"keyword\"},"
                + "\"pldsec_log_body\":{\"type\":\"keyword\"},"
                + "\"uma_type\":{\"type\":\"keyword\"},"
                + "\"uma_log_body\":{\"type\":\"keyword\"},"
                + "\"pldsec_date\":{\"type\":\"keyword\"},"
                + "\"src_ip\":{\"type\":\"keyword\"},"
                + "\"sysuser\":{\"type\":\"keyword\"},"
                + "\"module\":{\"type\":\"keyword\"},"
                + "\"result\":{\"type\":\"keyword\"},"
                + "\"log_action\":{\"type\":\"keyword\"},"
                + "\"detail\":{\"type\":\"keyword\"},"
                + "\"id\":{\"type\":\"keyword\"},"
                + "\"account\":{\"type\":\"keyword\"},"
                + "\"srvname\":{\"type\":\"keyword\"},"
                + "\"login_name\":{\"type\":\"keyword\"},"
                + "\"loginip\":{\"type\":\"keyword\"},"
                + "\"command_id\":{\"type\":\"keyword\"},"
                + "\"screen_id\":{\"type\":\"keyword\"},"
                + "\"screen_messages\":{\"type\":\"keyword\"},"
                + "\"sessiontype\":{\"type\":\"keyword\"},"
                + "\"srvaddr\":{\"type\":\"keyword\"},"
                + "\"session_id\":{\"type\":\"keyword\"},"
                + "\"mode\":{\"type\":\"keyword\"},"
                + "\"session_messages\":{\"type\":\"keyword\"},"
                + "\"loginTime\":{\"type\":\"keyword\"},"
                + "\"pldsec_date_ISO8601\":{\"type\":\"keyword\"},"
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
                + "\"loginip_geoip\": {"
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
                + "\"srvaddr_geoip\": {"
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
                + "\"srvname_geoip\": {"
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