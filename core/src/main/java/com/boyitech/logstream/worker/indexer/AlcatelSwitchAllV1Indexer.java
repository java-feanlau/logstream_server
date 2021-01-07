package com.boyitech.logstream.worker.indexer;

import com.alibaba.fastjson.JSONObject;
import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.GrokUtil;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.MetafieldHelper;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import com.boyitech.logstream.core.worker.indexer.BaseIndexerConfig;
import io.krakens.grok.api.Grok;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Map;

public class AlcatelSwitchAllV1Indexer extends BaseIndexer {
    private BaseIndexerConfig config;

    private String[] pattern_RegexpMessage;
    private String pattern_RegexpDos;
    private String pattern_RegexpArp;

    private ArrayList<Grok> grok_RegexpMessage;
    private Grok grok_RegexpDos;
    private Grok grok_RegexpArp;


    public AlcatelSwitchAllV1Indexer(BaseWorkerConfig config) {
        super(config);
        this.config= (BaseIndexerConfig) config;

    }

    public AlcatelSwitchAllV1Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
        this.config= (BaseIndexerConfig) config;

    }

    @Override
    public boolean register() {
        pattern_RegexpMessage=new String[]{
                "ipni %{DATA:attack_type}(\\s+)%{DATA:log_level}\\(%{DATA:log_level_id}\\)(\\s+)\\[%{DATA:unixtime}\\](\\s+)%{DATA:tag}(\\s+)%{GREEDYDATA:log_body}",
                "SSAPP %{DATA:attack_type}(\\s+)%{DATA:log_level}\\(%{DATA:log_level_id}\\)(\\s+)\\[%{DATA:unixtime}\\](\\s+)%{GREEDYDATA:log_body}"
        };
        pattern_RegexpDos="0: DoS type(\\s+)%{DATA:attack_info} from %{IP:ip_addr}\\/%{DATA:mac_addr} on port %{GREEDYDATA:switch_port}";
        pattern_RegexpArp="%{DATA:attack_info} for(\\s+)%{IP:ip_addr} by %{DATA:mac_addr} port %{GREEDYDATA:switch_port}";
        grok_RegexpMessage=GrokUtil.getGroks(pattern_RegexpMessage);
        grok_RegexpDos=GrokUtil.getGrok(pattern_RegexpDos);
        grok_RegexpArp=GrokUtil.getGrok(pattern_RegexpArp);


        return true;
    }

    @Override
    public boolean format(Event event) {
        //最外层，格式化日志主体
        String messageJson = event.getMessage();//此次获得的是一个json格式的字符串？

       try {
           if (GrokUtil.isJSONValid(messageJson) == true) {//是json

               Map mapType = JSONObject.parseObject(messageJson);
               Map<String, Object> format = event.getFormat();
               format.putAll(mapType);
               JSONObject pa = JSONObject.parseObject(messageJson);
               String message=pa.getString("message");

               Map<String,Object>messageMap=GrokUtil.getMapByGroks(grok_RegexpMessage,message);
               format.putAll(messageMap);
               if (messageMap.get("flag") == null && messageMap.get("flag") != "解析失败") {
                   event.setMetafieldLoglevel("1");
               }

               String tag=String.valueOf(messageMap.get("tag"));
               if(tag.equals("VRF")){
                   Map<String,Object>messageMapVRF=GrokUtil.getMap(grok_RegexpDos,message);
                   format.putAll(messageMapVRF);
                   if (messageMap.get("flag") == null && messageMap.get("flag") != "解析失败") {
                       event.setMetafieldLoglevel("2");
                   }
               }
               else if(tag.equals("arp")){
                   Map<String,Object>messageMaparp=GrokUtil.getMap(grok_RegexpArp,message);
                   format.putAll(messageMaparp);
                   if (messageMap.get("flag") == null && messageMap.get("flag") != "解析失败") {
                       event.setMetafieldLoglevel("2");
                   }
               }
               else{
                   format.put("ys_tag","grokparsefailure");
               }
               //格式化时间
               if(format.get("unixtime")!=null){
                   String unixtime=String.valueOf(format.get("unixtime")).split("\\.")[0];
                   Instant fromUnixTimestamp = Instant.ofEpochSecond(Long.valueOf(unixtime));
                   String unix_time2=fromUnixTimestamp.atZone(ZoneId.of("+08:00")).toString();
                   format.put("@timestamp",unix_time2);
               }

               //ip解析
               if (!config.getIpFilter().equals("null")) {
                   GrokUtil.filterGeoIP(config, format);
               } else {
                   //使用GeoIP对src_ip、dst_ip或部分特殊字段处理，
                   GrokUtil.setGeoIP2(format, "ip_addr");
               }

               MetafieldHelper.setMetafield(event, "", "ip_addr", "", format);
               if (format.get("flag") == "解析失败")
                   return false;

           } else {
//               LOGGER.error("message格式错误:" + messageJson);
               //若不是json格式，就是message的值
               Map<String, Object> format = event.getFormat();
               format.put("message",messageJson);
               String message=messageJson;
               Map<String,Object>messageMap=GrokUtil.getMapByGroks(grok_RegexpMessage,message);
               format.putAll(messageMap);
               if (messageMap.get("flag") == null && messageMap.get("flag") != "解析失败") {
                   event.setMetafieldLoglevel("1");
               }
               String tag=String.valueOf(messageMap.get("tag"));
               if(tag.equals("VRF")){
                   Map<String,Object>messageMapVRF=GrokUtil.getMap(grok_RegexpDos,message);
                   format.putAll(messageMapVRF);
                   if (messageMap.get("flag") == null && messageMap.get("flag") != "解析失败") {
                       event.setMetafieldLoglevel("2");
                   }
               }
               else if(tag.equals("arp")){
                   Map<String,Object>messageMaparp=GrokUtil.getMap(grok_RegexpArp,message);
                   format.putAll(messageMaparp);
                   if (messageMap.get("flag") == null && messageMap.get("flag") != "解析失败") {
                       event.setMetafieldLoglevel("2");
                   }
               }
               else{
                   format.put("ys_tag","grokparsefailure");
               }
               //格式化时间
               if(format.get("unixtime")!=null){
                   String unixtime=String.valueOf(format.get("unixtime")).split("\\.")[0];
                   Instant fromUnixTimestamp = Instant.ofEpochSecond(Long.valueOf(unixtime));
                   String unix_time2=fromUnixTimestamp.atZone(ZoneId.of("+08:00")).toString();
                   format.put("@timestamp",unix_time2);
               }

               //ip解析
               if (!config.getIpFilter().equals("null")) {
                   GrokUtil.filterGeoIP(config, format);
               } else {
                   //使用GeoIP对src_ip、dst_ip或部分特殊字段处理，
                   GrokUtil.setGeoIP2(format, "ip_addr");
               }

               MetafieldHelper.setMetafield(event, "", "ip_addr", "", format);
               if (format.get("flag") == "解析失败")
                   return false;
           }
       }
       catch (Exception ex){
        LOGGER.error(ex+"message:"+messageJson);
        addException(ex+"message:"+messageJson);
        ex.printStackTrace();
       }
        return true;
    }

    @Override
    public void tearDown() {
    }

    //上传的Mapping，要在下面两处空格处加上对应的Mapping字段；
    public static Map getMapping() {
        //language=JSON
        String mapping = "{\"properties\":{"
                + "\"@timestamp\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
                + "\"received_at\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
                + "\"portedAt\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
                + "\"flag\":{\"type\":\"keyword\"},"
                + "\"message\":{\"type\":\"text\"},"
                + "\"attack_type\":{\"type\":\"keyword\"},"
                + "\"log_level\":{\"type\":\"keyword\"},"
                + "\"log_level_id\":{\"type\":\"keyword\"},"
                + "\"unixtime\":{\"type\":\"keyword\"},"
                + "\"tag\":{\"type\":\"keyword\"},"
                + "\"log_body\":{\"type\":\"keyword\"},"
                + "\"attack_info\":{\"type\":\"keyword\"},"
                + "\"ip_addr\":{\"type\":\"keyword\"},"
                + "\"mac_addr\":{\"type\":\"keyword\"},"
                + "\"switch_port\":{\"type\":\"keyword\"},"
                + "\"ys_tag\":{\"type\":\"keyword\"},"
                + "\"ip_addr_geoip\": {"
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
                + "\"attack_type\":{\"type\":\"keyword\"},"
                + "\"log_level\":{\"type\":\"keyword\"},"
                + "\"log_level_id\":{\"type\":\"keyword\"},"
                + "\"unixtime\":{\"type\":\"keyword\"},"
                + "\"tag\":{\"type\":\"keyword\"},"
                + "\"log_body\":{\"type\":\"keyword\"},"
                + "\"attack_info\":{\"type\":\"keyword\"},"
                + "\"ip_addr\":{\"type\":\"keyword\"},"
                + "\"mac_addr\":{\"type\":\"keyword\"},"
                + "\"switch_port\":{\"type\":\"keyword\"},"
                + "\"ys_tag\":{\"type\":\"keyword\"},"
                + "\"ip_addr_geoip\": {"
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