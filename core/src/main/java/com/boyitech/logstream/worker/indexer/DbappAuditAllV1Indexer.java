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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * @author: juzheng
 * @Title: DbappAuditAllV1Indexer
 * @date: 2019-07-30T16:34:57.070
 * @Description: 1.此indexer文件根据indexer通用模版创建
 * 2.进行电机学院---安桓数据库审计日志格式化
 */
public class DbappAuditAllV1Indexer extends BaseIndexer {
    //此处初始化正则、grok的变量
    private Grok grok1;
    private Grok grok2;
    private String pattern1;
    private String pattern2;
    private BaseIndexerConfig config;

    public DbappAuditAllV1Indexer(BaseWorkerConfig config) {
        super(config);
        this.config = (BaseIndexerConfig) config;

    }

    public DbappAuditAllV1Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
        this.config = (BaseIndexerConfig) config;

    }

    @Override
    public boolean register() {
        //此处注册正确的正则+grok
        pattern1 = "senderind/(?<senderid>\\S+)(\\s+)logtype/(?<logtype>\\S+),accessid/(?<accessid>\\d+),apptype/(?<apptype>\\S+),objtype/(?<objtype>\\S+),objval/(?<objval>\\S+),ope/(?<ope>\\S+),result/(?<result>\\S+),loginuser/(?:(?<loginuser>\\S+)|),happentime/(?<happentime>%{YEAR}-%{MONTHNUM}-%{MONTHDAY} %{TIME}),sip/(?<sip>%{IP}),sport/(?<sport>\\d+),dip/(?<dip>%{IP}),dport/(?<dport>\\d+),payload/(?<payload>.*)";
        pattern2 = "^(?<operating>\\S+)(\\s+)(%{GREEDYDATA:payload_body})";

        grok1 = GrokUtil.getGrok(pattern1);
        grok2 = GrokUtil.getGrok(pattern2);
        return true;

    }

    @Override
    public boolean format(Event event) {
        //最外层，格式化日志主体
        String message = event.getMessage();
        message = message.replace("\\u0000", "").replace("\\\\u0000", "");//去除异常字符；
        Map<String, Object> map = GrokUtil.getMap(grok1, message);
        Map<String, Object> format = event.getFormat();
        format.putAll(map);
        format.put("message", message);
        if (map.get("flag") == null && map.get("flag") != "解析失败") {
            event.setMetafieldLoglevel("1");
        }

        //下一层，具体字段继续格式化
        String payload = String.valueOf(format.get("payload"));
        if (GrokUtil.isStringHasValue(payload)) {
            Map<String, Object> map2 = GrokUtil.getMap(grok2, payload);
            format.putAll(map2);
            if (map.get("flag") == null && map.get("flag") != "解析失败") {
                event.setMetafieldLoglevel("2");
            }
        }

        //时间类型字段标准化
        String happentime = String.valueOf(format.get("happentime"));
        String happentime_ISO8601;
        if (GrokUtil.isStringHasValue(happentime)) {
            happentime_ISO8601 = IndexerTimeUtils.getISO8601Time(happentime, "yyyy-MM-dd HH:mm:ss");
            if (happentime_ISO8601 != null && !happentime_ISO8601.equals("")) {
                format.put("happentime_ISO8601", happentime_ISO8601);
            }
        }

        //格式化时间，样本：
        IndexerTimeUtils.getISO8601Time2(format, "happentime", "yyyy-MM-dd HH:mm:ss");

        //使用GeoIP对src_ip、dst_ip或部分特殊字段处理，
        GrokUtil.setGeoIP2(format, "sip");
        GrokUtil.setGeoIP2(format, "dip");

        if (!config.getIpFilter().equals("null")) {
            GrokUtil.filterGeoIP(config, format);
        } else {
            //使用GeoIP对src_ip、dst_ip或部分特殊字段处理，
            GrokUtil.setGeoIP2(format, "sip");
            GrokUtil.setGeoIP2(format, "dip");

        }

        //格式化Metafield ，可能需要根基实际日志格
        // 式替换部分字段名如client_ip/src_ip、dst_ip
//        if(GrokUtil.isStringHasValue(String.valueOf(format.get("sip")))){
//            event.setSource(String.valueOf(format.get("sip")));
//        }
        event.setMetafieldLoglevel("2");

//        if (event.getLogType() != null) {
//            format.put("Metafield_description", event.getLogType());
//            format.put("Metafield_type", event.getLogType());
//        }
        if (event.getSource() != null) {
          //  format.put("Metafield_source", event.getSource());
           // event.setSource(event.getSource());
            format.put("log_source", event.getSource());//增加来源设备标识；
        }
//        if (format.get("sip") != null) {
//            event.setSource(String.valueOf(format.get("sip")));
//          //  format.put("Metafield_object", format.get("sip"));
//        }
//        if (format.get("dip") != null) {
//            event.setSource(String.valueOf(format.get("dip")));
//            //  format.put("Metafield_subject", format.get("dip"));
//        } else {
//            format.put("Metafield_subject", event.getSource());
//        }
        MetafieldHelper.setMetafield(event,"sip","dip","",format);
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
                + "\"senderid\":{\"type\":\"keyword\"},"
                + "\"logtype\":{\"type\":\"keyword\"},"
                + "\"accessid\":{\"type\":\"keyword\"},"
                + "\"apptype\":{\"type\":\"keyword\"},"
                + "\"objtype\":{\"type\":\"keyword\"},"
                + "\"objval\":{\"type\":\"keyword\"},"
                + "\"ope\":{\"type\":\"keyword\"},"
                + "\"result\":{\"type\":\"keyword\"},"
                + "\"loginuser\":{\"type\":\"keyword\"},"
                + "\"happentime\":{\"type\":\"keyword\"},"
                + "\"sip\":{\"type\":\"keyword\"},"
                + "\"sport\":{\"type\":\"keyword\"},"
                + "\"dip\":{\"type\":\"keyword\"},"
                + "\"dport\":{\"type\":\"keyword\"},"
                + "\"payload\":{\"type\":\"keyword\"},"
                + "\"operating\":{\"type\":\"keyword\"},"
                + "\"payload_body\":{\"type\":\"keyword\"},"
                + "\"happentime_ISO8601\":{\"type\":\"keyword\"},"
                + "\"sip_geoip\": {"
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
                + "\"dip_geoip\": {"
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
                + "\"senderid\":{\"type\":\"keyword\"},"
                + "\"logtype\":{\"type\":\"keyword\"},"
                + "\"accessid\":{\"type\":\"keyword\"},"
                + "\"apptype\":{\"type\":\"keyword\"},"
                + "\"objtype\":{\"type\":\"keyword\"},"
                + "\"objval\":{\"type\":\"keyword\"},"
                + "\"ope\":{\"type\":\"keyword\"},"
                + "\"result\":{\"type\":\"keyword\"},"
                + "\"loginuser\":{\"type\":\"keyword\"},"
                + "\"happentime\":{\"type\":\"keyword\"},"
                + "\"sip\":{\"type\":\"keyword\"},"
                + "\"sport\":{\"type\":\"keyword\"},"
                + "\"dip\":{\"type\":\"keyword\"},"
                + "\"dport\":{\"type\":\"keyword\"},"
                + "\"payload\":{\"type\":\"keyword\"},"
                + "\"operating\":{\"type\":\"keyword\"},"
                + "\"payload_body\":{\"type\":\"keyword\"},"
                + "\"happentime_ISO8601\":{\"type\":\"keyword\"},"
                + "\"sip_geoip\": {"
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
                + "\"dip_geoip\": {"
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