package com.boyitech.logstream.worker.indexer;

import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.*;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import com.boyitech.logstream.core.worker.indexer.BaseIndexerConfig;
import io.krakens.grok.api.Grok;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * @author juzheng
 * @Title: DbappAptAllV1Indexer
 * @date 4:33 PM
 * @Description:   //目前针对电机学院的需求进行优化
 */


public class DbappAptAllV1Indexer extends BaseIndexer {
    private String pattern1;
    private Grok grok1;
    private String[] patterns01;
    private String pattern02;
    private String pattern03;
    private String pattern04;
    private String pattern05;
    private ArrayList<Grok> groks01;
    private Grok grok02;
    private Grok grok03;
    private Grok grok04;
    private Grok grok05;
    private BaseIndexerConfig config;


    public DbappAptAllV1Indexer(BaseWorkerConfig config) {
        super(config);
        this.config= (BaseIndexerConfig) config;
    }

    public DbappAptAllV1Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
        this.config= (BaseIndexerConfig) config;
    }

    @Override
    public boolean register() {
        pattern1="^\\<(?<id>\\d+)\\>(?<timestamp>%{MONTH} %{MONTHDAY} %{TIME} %{YEAR}) (?<user>.*) (%{NOTSPACE:monitor_type})\\~(%{NUMBER:risk_id})\\~(%{NUMBER:t_id})\\~(%{TIMESTAMP_ISO8601:time})\\~(%{IP:src_ip})\\:(%{NUMBER:src_port})\\~(%{IP:dst_ip})\\:(%{NUMBER:dst_port})\\~(%{DATA:risk_type})\\~(%{GREEDYDATA:apt-log-body})";
        patterns01=new String[]{
                "^(%{DATA:risk_name})(\\~){2}(%{DATA:risk_level})\\~(%{NUMBER:date_string})\\~(%{DATA:risk_file})\\~(%{WORD:method}) (%{GREEDYDATA:referrer})$",
                "^(%{DATA:risk_name})(\\~){2}(%{NOTSPACE:risk_level})\\~(%{NUMBER:date_string})\\~(%{DATA:risk_file})\\~(%{GREEDYDATA:referrer})$"
        };
        pattern02="^\\~真实链接\\:(%{DATA:real_link})展现链接\\:(%{DATA:show_link})(\\~)(%{NOTSPACE:risk_level})\\~(%{NUMBER:date_string})\\~(%{DATA:risk_file})\\~(%{GREEDYDATA:referrer})$";
        pattern03="^(%{DATA:risk_name})(\\~){2}(%{NOTSPACE:risk_level})\\~(%{NUMBER:date_string})\\~(%{NOTSPACE:risk_file})\\~(%{WORD:method}) (%{GREEDYDATA:referrer})$";
        pattern04="^(\\~){2}(%{NOTSPACE:risk_level})\\~(%{NUMBER:date_string})\\~(%{NOTSPACE:risk_file})\\~(%{DATA:method}) (%{GREEDYDATA:referrer})$";
        pattern05="^(\\~)真实MailFrom\\:(%{DATA:real_mail})\\~(%{DATA:risk_level})\\~(%{NUMBER:date_string})\\~(%{DATA:risk_file})\\~(%{GREEDYDATA:referrer})$";


        grok1 = GrokUtil.getGrok(pattern1);
        groks01 = GrokUtil.getGroks(patterns01);
        grok02 = GrokUtil.getGrok(pattern02);
        grok03 = GrokUtil.getGrok(pattern03);
        grok04 = GrokUtil.getGrok(pattern04);
        grok05 = GrokUtil.getGrok(pattern05);


        return true;
    }

    @Override
    public boolean format(Event event) {
        String message = event.getMessage();
        //对中文月份进行字符串替换
        message=MonthHelper.replaceChineseMonth(message);

        Map<String, Object> map = GrokUtil.getMap(grok1, message);
        Map<String, Object> format = event.getFormat();
        format.putAll(map);
        format.put("message", message);

        if (map.get("flag") == null && map.get("flag") != "解析失败") {
            event.setMetafieldLoglevel("1");
        }
        String risk_type=String.valueOf(format.get("risk_type"));
        String apt_log_body=String.valueOf(format.get("apt-log-body"));

        if(GrokUtil.isStringHasValue(risk_type)&&GrokUtil.isStringHasValue(apt_log_body)){

            if(risk_type.equals("恶意代码攻击")){
                format.put("Metafield_category", "恶意代码攻击");
                Map<String, Object> messageMap = GrokUtil.getMapByGroks(groks01,apt_log_body);
                format.putAll(messageMap);
                if (messageMap.get("flag") == null && messageMap.get("flag") != "解析失败")
                    event.setMetafieldLoglevel("2");

            }
            if(risk_type.equals("邮件钓鱼")){
                format.put("Metafield_category", "邮件钓鱼");
                Map<String, Object> messageMap = GrokUtil.getMap(grok02,apt_log_body);
                format.putAll(messageMap);
                if (messageMap.get("flag") == null && messageMap.get("flag") != "解析失败")
                    event.setMetafieldLoglevel("2");
            }
            if(risk_type.equals("后门程序访问")){
                format.put("Metafield_category", "后门程序访问");
                Map<String, Object> messageMap = GrokUtil.getMap(grok03,apt_log_body);
                format.putAll(messageMap);
                if (messageMap.get("flag") == null && messageMap.get("flag") != "解析失败")
                    event.setMetafieldLoglevel("2");
            }
            if(risk_type.equals("访问黑IP黑域名")){
                format.put("Metafield_category", "访问黑IP黑域名");
                Map<String, Object> messageMap = GrokUtil.getMap(grok04,apt_log_body);
                format.putAll(messageMap);
                if (messageMap.get("flag") == null && messageMap.get("flag") != "解析失败")
                    event.setMetafieldLoglevel("2");
            }
            if(risk_type.equals("邮件头欺骗")){
                format.put("Metafield_category", "邮件头欺骗");
                Map<String, Object> messageMap = GrokUtil.getMap(grok05,apt_log_body);
                format.putAll(messageMap);
                if (messageMap.get("flag") == null && messageMap.get("flag") != "解析失败")
                    event.setMetafieldLoglevel("2");
            }

        }

        String risk_id=String.valueOf("risk_id");
        if(GrokUtil.isStringHasValue(risk_id)){
            String app_type_id=risk_id;
            format.put("app_type_id",app_type_id);
            String app_type;
            switch (app_type_id){
                case "0":
                    app_type="未知应用";
                    break;
                case "2":
                    app_type="WEB应用";
                    break;
                case "7":
                    app_type="FTP应用";
                    break;
                case "8":
                    app_type="SMTP应用";
                    break;
                case "9":
                    app_type="POP应用";
                    break;
                default :
                    app_type="无法识别应用";
            }
            format.put("app_type",app_type);
        }


        //格式化时间 2015-06-19 15:13:05
        IndexerTimeUtils.getISO8601Time2(format,"time","yyyy-MM-dd HH:mm:ss");

        if (!config.getIpFilter().equals("null")) {
            GrokUtil.filterGeoIP(config, format);
        } else {
            //geoip解析
            GrokUtil.setGeoIP2(format,"src_ip");
            GrokUtil.setGeoIP2(format,"dst_ip");
        }

        //格式化Metafield
        event.setMetafieldLoglevel("1");
//        if(GrokUtil.isStringHasValue(String.valueOf(format.get("src_ip")))){
//            event.setSource(String.valueOf(format.get("src_ip")));
//        }
//        if (event.getLogType() != null) {
//            format.put("Metafield_description", event.getLogType());
//            format.put("Metafield_type", event.getLogType());
//        }
//        if (event.getSource() != null) {
//         //   format.put("Metafield_source", event.getSource());
//            event.setSource(event.getSource());
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
        MetafieldHelper.setMetafield(event,"src_ip","dst_ip","",format);


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
                + "\"app_type\":{\"type\":\"text\"},"
                + "\"app_type_id\":{\"type\":\"text\"},"
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
                + "\"date_string\":{\"type\":\"keyword\"},"
                + "\"dst_ip\":{\"type\":\"keyword\"},"
                + "\"src_ip\":{\"type\":\"ip\"},"
                + "\"apt-log-body\":{\"type\":\"keyword\"},"
                + "\"id\":{\"type\":\"keyword\"},"
                + "\"timestamp\":{\"type\":\"keyword\"},"
                + "\"risk_type\":{\"type\":\"keyword\"},"
                + "\"method\":{\"type\":\"keyword\"},"
                + "\"risk_file\":{\"type\":\"keyword\"},"
                + "\"t_id\":{\"type\":\"keyword\"},"
                + "\"src_port\":{\"type\":\"keyword\"},"
                + "\"risk_level\":{\"type\":\"keyword\"},"
                + "\"referrer\":{\"type\":\"keyword\"},"
                + "\"risk_id\":{\"type\":\"keyword\"},"
                + "\"dst_port\":{\"type\":\"keyword\"},"
                + "\"time\":{\"type\":\"keyword\"},"
                + "\"user\":{\"type\":\"keyword\"},"
                + "\"monitor_type\":{\"type\":\"keyword\"},"
                + "\"risk_name\":{\"type\":\"keyword\"},"
                + "\"show_link\":{\"type\":\"keyword\"},"
                + "\"real_link\":{\"type\":\"keyword\"},"
                + "\"real_mail\":{\"type\":\"keyword\"},"
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
                + "\"app_type\":{\"type\":\"text\"},"
                + "\"app_type_id\":{\"type\":\"text\"},"
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
                + "\"date_string\":{\"type\":\"keyword\"},"
                + "\"dst_ip\":{\"type\":\"keyword\"},"
                + "\"src_ip\":{\"type\":\"ip\"},"
                + "\"apt-log-body\":{\"type\":\"keyword\"},"
                + "\"id\":{\"type\":\"keyword\"},"
                + "\"timestamp\":{\"type\":\"keyword\"},"
                + "\"risk_type\":{\"type\":\"keyword\"},"
                + "\"method\":{\"type\":\"keyword\"},"
                + "\"risk_file\":{\"type\":\"keyword\"},"
                + "\"t_id\":{\"type\":\"keyword\"},"
                + "\"src_port\":{\"type\":\"keyword\"},"
                + "\"risk_level\":{\"type\":\"keyword\"},"
                + "\"referrer\":{\"type\":\"keyword\"},"
                + "\"risk_id\":{\"type\":\"keyword\"},"
                + "\"dst_port\":{\"type\":\"keyword\"},"
                + "\"time\":{\"type\":\"keyword\"},"
                + "\"user\":{\"type\":\"keyword\"},"
                + "\"monitor_type\":{\"type\":\"keyword\"},"
                + "\"risk_name\":{\"type\":\"keyword\"},"
                + "\"show_link\":{\"type\":\"keyword\"},"
                + "\"real_link\":{\"type\":\"keyword\"},"
                + "\"real_mail\":{\"type\":\"keyword\"},"
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

