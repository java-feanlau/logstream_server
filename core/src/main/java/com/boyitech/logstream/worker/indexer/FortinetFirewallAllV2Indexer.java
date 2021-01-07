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
 * @Title: FortinetIndexer
 * @date 2019/3/20 10:23 AM
 * @Description: （博世）Fortinet防火墙日志的解析：完成1.流量日志-转发流量（traffic，1种格式） 2.事件日志-系统（event，三种格式）的解析。解析分为两层。
 * 3. //time:11:31 AM 2019/8/22根据实际需求进行修改成v2版本
 */
public class FortinetFirewallAllV2Indexer extends BaseIndexer {
    private Grok grok_1;
    private String pattern_1;

    private BaseIndexerConfig config;

    public FortinetFirewallAllV2Indexer(BaseWorkerConfig config) {
        super(config);
        this.config = (BaseIndexerConfig) config;

    }

    public FortinetFirewallAllV2Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
        this.config = (BaseIndexerConfig) config;
    }

    @Override
    public boolean register() {
        pattern_1 = "\\<(%{NUMBER:doc_syslogid})\\>%{GREEDYDATA:doc_logbody}";
        grok_1 = GrokUtil.getGrok(pattern_1);
        return true;
    }

    @Override
    public boolean format(Event event) {
        //grok解析
        String message = event.getMessage();
        Map<String, Object> messageMap = GrokUtil.getMap(grok_1, message);
        Map<String, Object> formated = event.getFormat();
        formated.putAll(messageMap);
        formated.put("message", message);
        if (messageMap.get("flag") == null && messageMap.get("flag") != "解析失败") {
            event.setMetafieldLoglevel("1");
        }

        //kv字段切割，
        // doc_
      //  String log_body = String.valueOf(formated.get("doc_logbody")).replace("\"","");
        String log_body=String.valueOf(formated.get("doc_logbody"));
        if (GrokUtil.isStringHasValue(log_body)) {
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
                    log_body_map.put("doc_"+left, right);
                }
                formated.putAll(log_body_map);
            }
            catch (Exception ex){
                LOGGER.error(ex.getMessage());
                addException(ex.getMessage());
            }
        }

        //源、目的ip重命名
        if(formated.get("doc_srcip")!=null){
            formated.put("src_ip",formated.get("doc_srcip"));
            formated.remove("doc_srcip");
        }
        if(formated.get("doc_dstip")!=null){
            formated.put("dst_ip",formated.get("doc_dstip"));
            formated.remove("doc_dstip");
        }
        //源、目的端口重命名
        String src_port = String.valueOf(formated.get("doc_srcport"));
        String dst_port = String.valueOf(formated.get("doc_dstport"));
        if (GrokUtil.isStringHasValue(src_port)) {
            formated.put("src_port", src_port);
        }
        if (GrokUtil.isStringHasValue(dst_port)) {
            formated.put("dst_port", dst_port);
        }

        //geoip解析及过滤：
        if (!config.getIpFilter().equals("null")) {
            GrokUtil.filterGeoIP(config, formated);
        } else {
            GrokUtil.setGeoIP2(formated, "src_ip");
            GrokUtil.setGeoIP2(formated, "dst_ip");
        }

        //格式化@timestamp
        //例:date=2019-03-07 time=16:41:26
        String date = String.valueOf(formated.get("doc_date"));
        String time = String.valueOf(formated.get("doc_time"));
        String timestamp = date + " " + time; //2019-03-07 16:41:26 -->>2019-04-02T16:26:10.223+08:00
        if (GrokUtil.isStringHasValue(date) && GrokUtil.isStringHasValue(time)) {
            String timestamp1 = GrokUtil.formTime(timestamp, "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ssXXX");
            formated.put("@timestamp", timestamp1);
        }
        else {
            formated.put("@timestamp",String.valueOf(OffsetDateTime.now()));
        }

        //解析层数
        if (messageMap.get("flag") == null && messageMap.get("flag") != "解析失败") {
            event.setMetafieldLoglevel("2");
            formated.put("format_level", event.getMetafieldLoglevel());
        }

        //源、目的ip拼接新字段
        MetafieldHelper.setMetafield(event, "src_ip", "dst_ip", "", formated);
        if (formated.get("src_ip") != null && formated.get("dst_ip") != null) {
            formated.put("ip_addr_pair", formated.get("src_ip") + "=>" + formated.get("dst_ip"));
        }

        //补充日志来源ip
        if (event.getSource() != null) {
            formated.put("host", event.getSource());
        }

        //补充大的日志种类:设备名-设备
        formated.put("log_class", "fortinet-firewall");

        //博世Fortinet防火墙日志新增一个字段doc_byte ,其值等于doc_sentbyte+doc_rcvdbyte,mapping类型与这两者保持一致
        if(formated.get("doc_sentbyte")!=null&&formated.get("doc_rcvdbyte")!=null){
            long doc_sentbyte= Long.valueOf((String) formated.get("doc_sentbyte"));
            long doc_rcvdbyte=Long.valueOf((String) formated.get("doc_rcvdbyte"));
            long doc_byte=doc_sentbyte+doc_rcvdbyte;
            formated.put("doc_byte",doc_byte);
        }
        if (formated.get("flag") == "解析失败")
            return false;
        return true;

    }

    @Override
    public void tearDown() {
    }

    public static Map getMapping() {
        //language=JSON
        String mapping = "{\"properties\":{\"@timestamp\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},\"received_at\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},\"portedAt\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},\"flag\":{\"type\":\"keyword\"},\"message\":{\"type\":\"text\"},\"doc_byte\":{\"type\":\"long\"},\"doc_syslogid\":{\"type\":\"keyword\"},\"doc_logbody\":{\"type\":\"text\"},\"doc_srcip\":{\"type\":\"ip\"},\"doc_dstip\":{\"type\":\"ip\"},\"doc_srcport\":{\"type\":\"keyword\"},\"doc_dstport\":{\"type\":\"keyword\"},\"doc_date\":{\"type\":\"keyword\"},\"doc_time\":{\"type\":\"keyword\"},\"doc_poluuid\":{\"type\":\"keyword\"},\"doc_proto\":{\"type\":\"integer\"},\"doc_sentpkt\":{\"type\":\"long\"},\"doc_sentbyte\":{\"type\":\"long\"},\"doc_rcvdbyte\":{\"type\":\"long\"},\"doc_rcvdpkt\":{\"type\":\"long\"},\"doc_logver\":{\"type\":\"keyword\"},\"doc_devid\":{\"type\":\"keyword\"},\"doc_devname\":{\"type\":\"keyword\"},\"doc_logid\":{\"type\":\"keyword\"},\"doc_type\":{\"type\":\"keyword\"},\"doc_subtype\":{\"type\":\"keyword\"},\"doc_level\":{\"type\":\"keyword\"},\"doc_service\":{\"type\":\"keyword\"},\"doc_policyid\":{\"type\":\"keyword\"},\"doc_sessionid\":{\"type\":\"keyword\"},\"doc_dstcountry\":{\"type\":\"keyword\"},\"doc_action\":{\"type\":\"keyword\"},\"doc_trandisp\":{\"type\":\"keyword\"},\"doc_dstintf\":{\"type\":\"keyword\"},\"doc_duration\":{\"type\":\"integer\"},\"doc_vd\":{\"type\":\"keyword\"},\"doc_srcintf\":{\"type\":\"keyword\"},\"doc_srccountry\":{\"type\":\"keyword\"},\"doc_status\":{\"type\":\"keyword\"},\"doc_cpu\":{\"type\":\"long\"},\"doc_mem\":{\"type\":\"long\"},\"doc_totalsession\":{\"type\":\"long\"},\"doc_disk\":{\"type\":\"long\"},\"doc_msg\":{\"type\":\"keyword\"},\"doc_bandwidth\":{\"type\":\"keyword\"},\"doc_logdesc\":{\"type\":\"keyword\"},\"doc_setuprate\":{\"type\":\"long\"},\"doc_disklograte\":{\"type\":\"long\"},\"doc_fazlograte\":{\"type\":\"long\"},\"src_ip\":{\"type\":\"ip\"},\"dst_ip\":{\"type\":\"ip\"},\"src_port\":{\"type\":\"keyword\"},\"dst_port\":{\"type\":\"keyword\"},\"format_level\":{\"type\":\"keyword\"},\"ip_addr_pair\":{\"type\":\"keyword\"},\"log_class\":{\"type\":\"keyword\"},\"host\":{\"type\":\"keyword\"},\"src_ip_geoip\":{\"properties\":{\"location\":{\"type\":\"geo_point\"},\"city_name\":{\"type\":\"keyword\"},\"continent_code\":{\"type\":\"keyword\"},\"country_code2\":{\"type\":\"keyword\"},\"country_code3\":{\"type\":\"keyword\"},\"country_name\":{\"type\":\"keyword\"},\"dma_code\":{\"type\":\"keyword\"},\"ip\":{\"type\":\"ip\"},\"latitude\":{\"type\":\"float\"},\"longitude\":{\"type\":\"float\"},\"postal_code\":{\"type\":\"keyword\"},\"region_name\":{\"type\":\"keyword\"},\"timezone\":{\"type\":\"keyword\"}}},\"dst_ip_geoip\":{\"properties\":{\"location\":{\"type\":\"geo_point\"},\"city_name\":{\"type\":\"keyword\"},\"continent_code\":{\"type\":\"keyword\"},\"country_code2\":{\"type\":\"keyword\"},\"country_code3\":{\"type\":\"keyword\"},\"country_name\":{\"type\":\"keyword\"},\"dma_code\":{\"type\":\"keyword\"},\"ip\":{\"type\":\"ip\"},\"latitude\":{\"type\":\"float\"},\"longitude\":{\"type\":\"float\"},\"postal_code\":{\"type\":\"keyword\"},\"region_name\":{\"type\":\"keyword\"},\"timezone\":{\"type\":\"keyword\"}}},\"Metafield_type\":{\"type\":\"keyword\"},\"Metafield_category\":{\"type\":\"keyword\"},\"Metafield_subject\":{\"type\":\"keyword\"},\"Metafield_object\":{\"type\":\"keyword\"},\"Metafield_loglevel\":{\"type\":\"keyword\"},\"Metafield_source\":{\"type\":\"keyword\"},\"Metafield_description\":{\"type\":\"keyword\",\"fields\":{\"raw\":{\"type\":\"keyword\"}}}},\"dynamic_templates\":[{\"prefix_match\":{\"match\":\"doc_*\",\"mapping\":{\"type\":\"keyword\"}}}]}";
        return GsonHelper.fromJson(mapping);
    }


    public static String getMappingString() {
        String mapping = "{\"properties\":{\"@timestamp\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},\"received_at\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},\"portedAt\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},\"flag\":{\"type\":\"keyword\"},\"message\":{\"type\":\"text\"},\"doc_byte\":{\"type\":\"long\"},\"doc_syslogid\":{\"type\":\"keyword\"},\"doc_logbody\":{\"type\":\"text\"},\"doc_srcip\":{\"type\":\"ip\"},\"doc_dstip\":{\"type\":\"ip\"},\"doc_srcport\":{\"type\":\"keyword\"},\"doc_dstport\":{\"type\":\"keyword\"},\"doc_date\":{\"type\":\"keyword\"},\"doc_time\":{\"type\":\"keyword\"},\"doc_poluuid\":{\"type\":\"keyword\"},\"doc_proto\":{\"type\":\"integer\"},\"doc_sentpkt\":{\"type\":\"long\"},\"doc_sentbyte\":{\"type\":\"long\"},\"doc_rcvdbyte\":{\"type\":\"long\"},\"doc_rcvdpkt\":{\"type\":\"long\"},\"doc_logver\":{\"type\":\"keyword\"},\"doc_devid\":{\"type\":\"keyword\"},\"doc_devname\":{\"type\":\"keyword\"},\"doc_logid\":{\"type\":\"keyword\"},\"doc_type\":{\"type\":\"keyword\"},\"doc_subtype\":{\"type\":\"keyword\"},\"doc_level\":{\"type\":\"keyword\"},\"doc_service\":{\"type\":\"keyword\"},\"doc_policyid\":{\"type\":\"keyword\"},\"doc_sessionid\":{\"type\":\"keyword\"},\"doc_dstcountry\":{\"type\":\"keyword\"},\"doc_action\":{\"type\":\"keyword\"},\"doc_trandisp\":{\"type\":\"keyword\"},\"doc_dstintf\":{\"type\":\"keyword\"},\"doc_duration\":{\"type\":\"integer\"},\"doc_vd\":{\"type\":\"keyword\"},\"doc_srcintf\":{\"type\":\"keyword\"},\"doc_srccountry\":{\"type\":\"keyword\"},\"doc_status\":{\"type\":\"keyword\"},\"doc_cpu\":{\"type\":\"long\"},\"doc_mem\":{\"type\":\"long\"},\"doc_totalsession\":{\"type\":\"long\"},\"doc_disk\":{\"type\":\"long\"},\"doc_msg\":{\"type\":\"keyword\"},\"doc_bandwidth\":{\"type\":\"keyword\"},\"doc_logdesc\":{\"type\":\"keyword\"},\"doc_setuprate\":{\"type\":\"long\"},\"doc_disklograte\":{\"type\":\"long\"},\"doc_fazlograte\":{\"type\":\"long\"},\"src_ip\":{\"type\":\"ip\"},\"dst_ip\":{\"type\":\"ip\"},\"src_port\":{\"type\":\"keyword\"},\"dst_port\":{\"type\":\"keyword\"},\"format_level\":{\"type\":\"keyword\"},\"ip_addr_pair\":{\"type\":\"keyword\"},\"log_class\":{\"type\":\"keyword\"},\"host\":{\"type\":\"keyword\"},\"src_ip_geoip\":{\"properties\":{\"location\":{\"type\":\"geo_point\"},\"city_name\":{\"type\":\"keyword\"},\"continent_code\":{\"type\":\"keyword\"},\"country_code2\":{\"type\":\"keyword\"},\"country_code3\":{\"type\":\"keyword\"},\"country_name\":{\"type\":\"keyword\"},\"dma_code\":{\"type\":\"keyword\"},\"ip\":{\"type\":\"ip\"},\"latitude\":{\"type\":\"float\"},\"longitude\":{\"type\":\"float\"},\"postal_code\":{\"type\":\"keyword\"},\"region_name\":{\"type\":\"keyword\"},\"timezone\":{\"type\":\"keyword\"}}},\"dst_ip_geoip\":{\"properties\":{\"location\":{\"type\":\"geo_point\"},\"city_name\":{\"type\":\"keyword\"},\"continent_code\":{\"type\":\"keyword\"},\"country_code2\":{\"type\":\"keyword\"},\"country_code3\":{\"type\":\"keyword\"},\"country_name\":{\"type\":\"keyword\"},\"dma_code\":{\"type\":\"keyword\"},\"ip\":{\"type\":\"ip\"},\"latitude\":{\"type\":\"float\"},\"longitude\":{\"type\":\"float\"},\"postal_code\":{\"type\":\"keyword\"},\"region_name\":{\"type\":\"keyword\"},\"timezone\":{\"type\":\"keyword\"}}},\"Metafield_type\":{\"type\":\"keyword\"},\"Metafield_category\":{\"type\":\"keyword\"},\"Metafield_subject\":{\"type\":\"keyword\"},\"Metafield_object\":{\"type\":\"keyword\"},\"Metafield_loglevel\":{\"type\":\"keyword\"},\"Metafield_source\":{\"type\":\"keyword\"},\"Metafield_description\":{\"type\":\"keyword\",\"fields\":{\"raw\":{\"type\":\"keyword\"}}}},\"dynamic_templates\":[{\"prefix_match\":{\"match\":\"doc_*\",\"mapping\":{\"type\":\"keyword\"}}}]}";
        return mapping;
    }
}
