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
 * @Title: ApacheApacheAccessFilebeatIndexer
 * @date: 2019-08-19T17:17:49.164
 * @Description: 1.此indexer文件根据indexer通用模版创建
 * 2.移植FileBeat的版本
 */
public class ApacheApacheAccessFilebeatIndexer extends BaseIndexer {
    //此处初始化正则、grok的变量
    private ArrayList<Grok> groks1;
    private Grok grok2;
    private String[] patterns1;
    private String pattern2;
    private BaseIndexerConfig config;

    public ApacheApacheAccessFilebeatIndexer(BaseWorkerConfig config) {
        super(config);
        this.config = (BaseIndexerConfig) config;
    }

    public ApacheApacheAccessFilebeatIndexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
        this.config = (BaseIndexerConfig) config;
    }

    @Override
    public boolean register() {
        //此处注册正确的正则+grok

        patterns1 = new String[]{
                "%{IPORHOST:source.address} - %{DATA:user.name} \\[%{HTTPDATE:apache.access.time}\\] \"(?:%{WORD:http.request.method} %{DATA:url.original} HTTP/%{NUMBER:http.version}|-)?\" %{NUMBER:http.response.status_code:long} (?:%{NUMBER:http.response.body.bytes:long}|-)( \"%{DATA:http.request.referrer}\")?( \"%{DATA:user_agent.original}\")?",
                "%{IPORHOST:source.address} - %{DATA:user.name} \\[%{HTTPDATE:apache.access.time}\\] \"-\" %{NUMBER:http.response.status_code:long} -",
                "\\[%{HTTPDATE:apache.access.time}\\] %{IPORHOST:source.address} %{DATA:apache.access.ssl.protocol} %{DATA:apache.access.ssl.cipher} \"%{WORD:http.request.method} %{DATA:url.original} HTTP/%{NUMBER:http.version}\" %{NUMBER:http.response.body.bytes:long}"
        };
        pattern2 = "^(%{IP:source.ip}|%{HOSTNAME:source.domain})$";

        groks1 = GrokUtil.getGroks(patterns1);
        grok2 = GrokUtil.getGrok(pattern2);
        return true;

    }

    @Override
    public boolean format(Event event) {
        //最外层，格式化日志主体
        String message = event.getMessage();
        Map<String, Object> map = GrokUtil.getMapByGroks(groks1, message);
        Map<String, Object> format = event.getFormat();
        format.putAll(map);
        format.put("message", message);
        if (map.get("flag") == null && map.get("flag") != "解析失败") {
            event.setMetafieldLoglevel("1");
        }

        //下一层，具体字段继续格式化
        String source_address = String.valueOf(format.get("source.address"));
        if (GrokUtil.isStringHasValue(source_address)) {
            Map<String, Object> map2 = GrokUtil.getMap(grok2, source_address);
            format.putAll(map2);
            if (map.get("flag") == null && map.get("flag") != "解析失败") {
                event.setMetafieldLoglevel("2");
            }
        }

        //格式化时间，样本：
        IndexerTimeUtils.getISO8601Time2(format, "apache.access.time", "dd/MMM/yyyy:H:m:s Z");

        //使用GeoIP对src_ip、dst_ip或部分特殊字段处理，
      //  GrokUtil.setGeoIP2(format, "source.ip");

        if(!config.getIpFilter().equals("null")){
            GrokUtil.filterGeoIP(config,format);
        }
        else {
            GrokUtil.setGeoIP2(format,"source.ip");
        }

        //格式化Metafield ，可能需要根基实际日志格式替换部分字段名如client_ip/src_ip、dst_ip
        event.setMetafieldLoglevel("2");

        //加一些自定义的字段,仅做尝试：
//        Map about=new HashMap();
//        Map about_details=new HashMap();
//        about.put("about.logs.infos",about_details);
//        about_details.put("org","apache");
//        about_details.put("style","access");
//        about_details.put("from","filebeat");
//        about_details.put("from.version","7.3.0");
//        format.putAll(about);

        MetafieldHelper.setMetafield(event, "source.ip", "source.ip", "", format);
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
                + "\"source.address\":{\"type\":\"keyword\"},"
                + "\"user.name\":{\"type\":\"keyword\"},"
                + "\"apache.access.time\":{\"type\":\"keyword\"},"
                + "\"http.request.method\":{\"type\":\"keyword\"},"
                + "\"url.original\":{\"type\":\"keyword\"},"
                + "\"http.version\":{\"type\":\"keyword\"},"
                + "\"http.response.status_code\":{\"type\":\"long\"},"
                + "\"http.response.body.bytes\":{\"type\":\"long\"},"
                + "\"http.request.referrer\":{\"type\":\"keyword\"},"
                + "\"user_agent.original\":{\"type\":\"keyword\"},"
                + "\"apache.access.ssl.protocol\":{\"type\":\"keyword\"},"
                + "\"apache.access.ssl.cipher\":{\"type\":\"keyword\"},"
                + "\"source.ip\":{\"type\":\"keyword\"},"
                + "\"source.domain\":{\"type\":\"keyword\"},"
                + "\"source.ip_geoip\": {"
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
                + "\"source.address\":{\"type\":\"keyword\"},"
                + "\"user.name\":{\"type\":\"keyword\"},"
                + "\"apache.access.time\":{\"type\":\"keyword\"},"
                + "\"http.request.method\":{\"type\":\"keyword\"},"
                + "\"url.original\":{\"type\":\"keyword\"},"
                + "\"http.version\":{\"type\":\"keyword\"},"
                + "\"http.response.status_code\":{\"type\":\"long\"},"
                + "\"http.response.body.bytes\":{\"type\":\"long\"},"
                + "\"http.request.referrer\":{\"type\":\"keyword\"},"
                + "\"user_agent.original\":{\"type\":\"keyword\"},"
                + "\"apache.access.ssl.protocol\":{\"type\":\"keyword\"},"
                + "\"apache.access.ssl.cipher\":{\"type\":\"keyword\"},"
                + "\"source.ip\":{\"type\":\"keyword\"},"
                + "\"source.domain\":{\"type\":\"keyword\"},"
                + "\"source.ip_geoip\": {"
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