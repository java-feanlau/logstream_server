package com.boyitech.logstream.worker.indexer;


import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.GrokUtil;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.IndexerTimeUtils;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import io.krakens.grok.api.Grok;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * @author: ${author}
 * @Title: ${className}
 * @date: ${date}
 * @Description: 此indexer文件根据indexer通用模版创建
 */
public class ${className} extends BaseIndexer {
    //此处初始化正则、grok的变量
    private ArrayList<Grok> groks1;
    private Grok grok2;
    private String[] patterns1;
    private String pattern2;

    public ${className}(BaseWorkerConfig config) {
        super(config);
    }

    public ${className}(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
    }

    @Override
    public boolean register() {
    //此处注册正确的正则+grok

        patterns1=new String[] {
         "",
         ""
        };
        pattern2="";

        groks1=GrokUtil.getGroks(patterns1);
        grok2=GrokUtil.getGrok(pattern2);
        return true;

    }

    @Override
    public boolean format(Event event) {
    //最外层，格式化日志主体
    String message = event.getMessage();
    Map<String, Object> map = GrokUtil.getMapByGroks(groks1,message);
    Map<String, Object> format = event.getFormat();
    format.putAll(map);
    format.put("message",message);
    if (map.get("flag") == null && map.get("flag") != "解析失败") {
    event.setMetafieldLoglevel("1");
    }

    //下一层，具体字段继续格式化
    String A=String.valueOf(format.get("A"));
    if(GrokUtil.isStringHasValue(A)){
    Map<String, Object> map2 = GrokUtil.getMap(grok2,A);
    format.putAll(map2);
    if (map.get("flag") == null && map.get("flag") != "解析失败") {
    event.setMetafieldLoglevel("2");
    }
    }

    //格式化时间，样本：
    IndexerTimeUtils.getISO8601Time2(format,"","");

    //使用GeoIP对src_ip、dst_ip或部分特殊字段处理，
    GrokUtil.setGeoIP2(format,"ip");

    //格式化Metafield ，可能需要根基实际日志格式替换部分字段名如client_ip/src_ip、dst_ip
    event.setMetafieldLoglevel("2");
    if (event.getLogType() != null) {
    format.put("Metafield_description", event.getLogType());
    format.put("Metafield_type", event.getLogType());
    }
    if (event.getSource() != null) {
    format.put("Metafield_source", event.getSource());
    format.put("log_source",event.getSource());//增加来源设备标识；
    }
    if (format.get("src_ip") != null) {
    format.put("Metafield_object", format.get("src_ip"));
    }
    if (format.get("dst_ip") != null) {
    format.put("Metafield_subject", format.get("dst_ip"));
    }else {
    format.put("Metafield_subject", event.getSource());
    }
    if (format.get("flag") == "解析失败")
    return false;
    return true;
    }

    @Override
    public void tearDown() {}

    //上传的Mapping，要在下面两处空格处加上对应的Mapping字段；
    public static Map getMapping() {
    String mapping = "{\"properties\":{"
    + "\"@timestamp\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
    + "\"received_at\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
    + "\"portedAt\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
    + "\"flag\":{\"type\":\"keyword\"},"
    + "\"message\":{\"type\":\"text\"},"

    + "\"_geoip\": {"
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


    + "\"_geoip\": {"
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