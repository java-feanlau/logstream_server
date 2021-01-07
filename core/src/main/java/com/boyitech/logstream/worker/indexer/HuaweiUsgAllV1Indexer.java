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
import java.util.*;

/**
 * @author: juzheng
 * @Title: HuaweiUsgAllV1Indexer
 * @date: 2019-07-28T17:06:08.003
 * @Description: 1.此indexer文件根据indexer通用模版创建,
 * 2.完成电机学院的华为usg日志格式化，解析时参考的v2版本的logstash的indexer的conf写的
 */
public class HuaweiUsgAllV1Indexer extends BaseIndexer {
    //此处初始化正则、grok的变量
    private Grok grok1;
    private Grok grok2_SESSION_TEARDOWN;
    private Grok grok2_PACKET_DENY;
    private String pattern1;
    private String pattern2_SESSION_TEARDOWN;
    private String pattern2_PACKET_DENY;
    private BaseIndexerConfig config;

    public HuaweiUsgAllV1Indexer(BaseWorkerConfig config) {
        super(config);
        this.config= (BaseIndexerConfig) config;

    }

    public HuaweiUsgAllV1Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
        this.config= (BaseIndexerConfig) config;

    }

    @Override
    public boolean register() {
        //此处注册正确的正则+grok

        pattern1 = "%%01(%{NOTSPACE:ModuleName})/(%{NUMBER:Severity})/(%{NOTSPACE:Brief})\\(%{NOTSPACE:IS}\\):(%{GREEDYDATA:Description})";
        pattern2_SESSION_TEARDOWN = "^IPVer=(%{NUMBER:ip_version}),Protocol=(%{NOTSPACE:protocol}),SourceIP=(%{IP:s_ip}),DestinationIP=(%{IP:d_ip}),SourcePort=(%{NUMBER:s_port}),DestinationPort=(%{NUMBER:d_port}),BeginTime=(%{NUMBER:begin_time_raw}),EndTime=(%{NUMBER:end_time_raw}),SendPkts=(%{NUMBER:send_packets}),SendBytes=(%{NUMBER:send_bytes}),RcvPkts=(%{NUMBER:received_packets}),RcvBytes=(%{NUMBER:received_bytes}),SourceVpnID=(%{NUMBER:source_vpn_id}),DestinationVpnID=(%{NUMBER:destination_vpn_id}),SourceZone=(%{NOTSPACE:source_zone}),DestinationZone=(%{NOTSPACE:destination_zone}),PolicyName=(%{NOTSPACE:policy_name}),CloseReason=(%{NOTSPACE:closereason})";
        pattern2_PACKET_DENY = "^IPVer=(%{NUMBER:ip_version}),Protocol=(%{NOTSPACE:protocol}),SourceIP=(%{IP:s_ip}),DestinationIP=(%{IP:d_ip}),SourcePort=(%{NUMBER:s_port}),DestinationPort=(%{NUMBER:d_port}),BeginTime=(%{NUMBER:begin_time_raw}),EndTime=(%{NUMBER:end_time_raw}),SourceVpnID=(%{NUMBER:source_vpn_id}),DestinationVpnID=(%{NUMBER:destination_vpn_id}),SourceZone=(%{NOTSPACE:source_zone}),DestinationZone=(%{NOTSPACE:destination_zone}),PolicyName=(%{NOTSPACE:policy_name}),CloseReason=(%{NOTSPACE:closereason})";

        grok1 = GrokUtil.getGrok(pattern1);
        grok2_SESSION_TEARDOWN = GrokUtil.getGrok(pattern2_SESSION_TEARDOWN);
        grok2_PACKET_DENY = GrokUtil.getGrok(pattern2_PACKET_DENY);
        return true;

    }

    @Override
    public boolean format(Event event) {
        //最外层，格式化日志主体
        String message = event.getMessage();
        Map<String, Object> map = GrokUtil.getMap(grok1, message);
        Map<String, Object> map1 = new HashMap<>();
        if (map.containsKey("IS") && map.get("IS") != null) {
            String I_S = String.valueOf(map.get("IS"));
            map1.put("I/S", I_S);
        }
        ;
        Map<String, Object> format = event.getFormat();
        format.putAll(map);
        format.putAll(map1);
        format.put("message", message);
        if (map.get("flag") == null && map.get("flag") != "解析失败") {
            event.setMetafieldLoglevel("1");
        }

        //下一层，具体字段继续格式化
        String ModuleName = String.valueOf(format.get("ModuleName"));
        String Description = String.valueOf(format.get("Description"));
        if (GrokUtil.isStringHasValue(ModuleName) && ModuleName.equals("SECLOG")) {
            String Brief = String.valueOf(format.get("Brief"));
            if (GrokUtil.isStringHasValue(Brief) && Brief.equals("SESSION_TEARDOWN") && GrokUtil.isStringHasValue(Description)) {
                Map<String, Object> map2 = GrokUtil.getMap(grok2_SESSION_TEARDOWN, Description);
                format.putAll(map2);
                IndexerTimeUtils.getISO8601TimeFromUnixTime(format, "begin_time_raw", "begin_time");
                IndexerTimeUtils.getISO8601TimeFromUnixTime(format, "end_time_raw", "end_time");
            }
            if (GrokUtil.isStringHasValue(Brief) && Brief.equals("PACKET_DENY") && GrokUtil.isStringHasValue(Description)) {
                Map<String, Object> map3 = GrokUtil.getMap(grok2_PACKET_DENY, Description);
                format.putAll(map3);
                IndexerTimeUtils.getISO8601TimeFromUnixTime(format, "begin_time_raw", "begin_time");
                IndexerTimeUtils.getISO8601TimeFromUnixTime(format, "end_time_raw", "end_time");
            }

            if (map.get("flag") == null && map.get("flag") != "解析失败") {
                event.setMetafieldLoglevel("2");
            }

        }

        //格式化时间
        IndexerTimeUtils.getISO8601Time2(format, "", "");

        if (!config.getIpFilter().equals("null")) {
            GrokUtil.filterGeoIP(config, format);
        } else {
            //使用GeoIP对src_ip、dst_ip或部分特殊字段处理，
            GrokUtil.setGeoIP2(format, "s_ip");
            GrokUtil.setGeoIP2(format, "d_ip");
        }

        //格式化Metafield ，可能需要根基实际日志格式替换部分字段名如client_ip/src_ip、dst_ip
//        if (event.getLogType() != null) {
//            format.put("Metafield_description", event.getLogType());
//            format.put("Metafield_type", event.getLogType());
//        }
        if (event.getSource() != null) {
           // format.put("Metafield_source", event.getSource());
           // event.setSource(event.getSource());
            format.put("log_source", event.getSource());//增加来源设备标识；
        }
//        if (format.get("s_ip") != null) {
//            event.setSource(String.valueOf(format.get("s_ip")));
//           // format.put("Metafield_object", format.get("s_ip"));
//        }
//        if (format.get("d_ip") != null) {
//            event.setSource(String.valueOf(format.get("d_ip")));
//           // format.put("Metafield_subject", format.get("d_ip"));
//        } else {
//            format.put("Metafield_subject", event.getSource());
//        }
        MetafieldHelper.setMetafield(event,"s_ip","d_ip","",format);
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
                + "\"ModuleName\":{\"type\":\"keyword\"},"
                + "\"Severity\":{\"type\":\"keyword\"},"
                + "\"Brief\":{\"type\":\"keyword\"},"
                + "\"IS\":{\"type\":\"keyword\"},"
                + "\"Description\":{\"type\":\"keyword\"},"
                + "\"ip_version\":{\"type\":\"keyword\"},"
                + "\"protocol\":{\"type\":\"keyword\"},"
                + "\"s_ip\":{\"type\":\"keyword\"},"
                + "\"d_ip\":{\"type\":\"keyword\"},"
                + "\"s_port\":{\"type\":\"keyword\"},"
                + "\"d_port\":{\"type\":\"keyword\"},"
                + "\"begin_time_raw\":{\"type\":\"keyword\"},"
                + "\"end_time_raw\":{\"type\":\"keyword\"},"
                + "\"send_packets\":{\"type\":\"keyword\"},"
                + "\"send_bytes\":{\"type\":\"keyword\"},"
                + "\"received_packets\":{\"type\":\"keyword\"},"
                + "\"received_bytes\":{\"type\":\"keyword\"},"
                + "\"source_vpn_id\":{\"type\":\"keyword\"},"
                + "\"source_zone\":{\"type\":\"keyword\"},"
                + "\"destination_zone\":{\"type\":\"keyword\"},"
                + "\"policy_name\":{\"type\":\"keyword\"},"
                + "\"closereason\":{\"type\":\"keyword\"},"
                + "\"I/S\":{\"type\":\"keyword\"},"
                + "\"begin_time\":{\"type\":\"keyword\"},"
                + "\"end_time\":{\"type\":\"keyword\"},"
                + "\"s_ip_geoip\": {"
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
                + "\"d_ip_geoip\": {"
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
                + "\"ModuleName\":{\"type\":\"keyword\"},"
                + "\"Severity\":{\"type\":\"keyword\"},"
                + "\"Brief\":{\"type\":\"keyword\"},"
                + "\"IS\":{\"type\":\"keyword\"},"
                + "\"Description\":{\"type\":\"keyword\"},"
                + "\"ip_version\":{\"type\":\"keyword\"},"
                + "\"protocol\":{\"type\":\"keyword\"},"
                + "\"s_ip\":{\"type\":\"keyword\"},"
                + "\"d_ip\":{\"type\":\"keyword\"},"
                + "\"s_port\":{\"type\":\"keyword\"},"
                + "\"d_port\":{\"type\":\"keyword\"},"
                + "\"begin_time_raw\":{\"type\":\"keyword\"},"
                + "\"end_time_raw\":{\"type\":\"keyword\"},"
                + "\"send_packets\":{\"type\":\"keyword\"},"
                + "\"send_bytes\":{\"type\":\"keyword\"},"
                + "\"received_packets\":{\"type\":\"keyword\"},"
                + "\"received_bytes\":{\"type\":\"keyword\"},"
                + "\"source_vpn_id\":{\"type\":\"keyword\"},"
                + "\"source_zone\":{\"type\":\"keyword\"},"
                + "\"destination_zone\":{\"type\":\"keyword\"},"
                + "\"policy_name\":{\"type\":\"keyword\"},"
                + "\"closereason\":{\"type\":\"keyword\"},"
                + "\"I/S\":{\"type\":\"keyword\"},"
                + "\"begin_time\":{\"type\":\"keyword\"},"
                + "\"end_time\":{\"type\":\"keyword\"},"
                + "\"s_ip_geoip\": {"
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
                + "\"d_ip_geoip\": {"
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