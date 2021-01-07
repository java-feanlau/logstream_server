package com.boyitech.logstream.worker.indexer;

import com.alibaba.fastjson.JSONObject;
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


public class EisooNetworkDiskV1Indexer extends BaseIndexer {
    private BaseIndexerConfig config;
    private String pattern_1;
    private Grok grok_1;

    private String[] patterns;
    private ArrayList<Grok> groks;

    public EisooNetworkDiskV1Indexer(BaseWorkerConfig config) {
        super(config);
        this.config = (BaseIndexerConfig) config;

    }

    public EisooNetworkDiskV1Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
        this.config = (BaseIndexerConfig) config;

    }

    @Override
    public boolean register() {
        pattern_1 = "%{CHTIME:datetime},%{DATA:user_name},%{DATA:log_level},%{DATA:action},%{DATA:ip},%{DATA:mac},%{DATA:action_desc},%{DATA:action_details},%{GREEDYDATA:user_agent},\\{%{GREEDYDATA:log_body}\\}";
        grok_1 = GrokUtil.getGrok(pattern_1);

        patterns = new String[]{
                "%{CHTIME:datetime},%{DATA:user_name},%{DATA:log_level},%{DATA:action},%{DATA:ip},%{DATA:mac},%{DATA:action_desc},%{DATA:action_details},%{GREEDYDATA:user_agent},\\{%{GREEDYDATA:log_body}\\}",
                "%{GREEDYDATA:message2}"
        };
        groks = GrokUtil.getGroks(patterns);

        return true;
    }

    @Override
    public boolean format(Event event) {
        String message = event.getMessage();
        //"facility_label":
        // local1,登录
        //local2,管理
        //local3,操作
            Map<String, Object> format = event.getFormat();
            format.put("message", event.getMessage());
            event.setMetafieldLoglevel("1");
            format.put("format_level", "1");
            String facility_label = String.valueOf(format.get("facility_label"));
            if (facility_label.equals("local1")) {//登录
                format.put("log_type", "eisoo-access");
                Map<String, Object> messageMap = GrokUtil.getMap(grok_1, message);
                String log_body = "{" + String.valueOf(messageMap.get("log_body")) + "}";
                Map log_body_map = JSONObject.parseObject(log_body);
                format.putAll(messageMap);
                format.putAll(log_body_map);
                if (messageMap.get("flag") == null && messageMap.get("flag") != "解析失败")
                    event.setMetafieldLoglevel("2");
                format.put("format_level", "2");
            }
            if (facility_label.equals("local2")) {//管理
                format.put("log_type", "eisoo-admin");
                Map<String, Object> messageMap = GrokUtil.getMap(grok_1, message);
                String log_body = "{" + String.valueOf(messageMap.get("log_body")) + "}";
                Map log_body_map = JSONObject.parseObject(log_body);
                format.putAll(messageMap);
                format.putAll(log_body_map);
                if (messageMap.get("flag") == null && messageMap.get("flag") != "解析失败")
                    event.setMetafieldLoglevel("2");
                format.put("format_level", "2");
            }
            if (facility_label.equals("local3")) {//操作
                format.put("log_type", "eisoo-operation");
                Map<String, Object> messageMap = GrokUtil.getMap(grok_1, message);
                String log_body = "{" + String.valueOf(messageMap.get("log_body")) + "}";
                Map log_body_map = JSONObject.parseObject(log_body);
                String filename_suffix = new String();
                String filename = new String();
                if (messageMap.get("action_desc") != null && messageMap.get("action_desc") != "") {
                    String action_desc = (String) messageMap.get("action_desc");
                    if (action_desc.indexOf("文件夹") != -1) {
                        filename_suffix = "";
                        filename = "";
                    } else {
                        filename = action_desc.substring(action_desc.indexOf("“") + 1, action_desc.indexOf("”"));
                        if (filename.contains(".")) {
                            int suffixlength = filename.split("\\.").length;
                            filename_suffix = filename.split("\\.")[suffixlength - 1];
                        } else {
                            filename_suffix = "";
                        }
                    }
                }
                format.put("filename_suffix", filename_suffix);
                format.put("filename", filename);
                format.putAll(messageMap);
                format.putAll(log_body_map);
                if (messageMap.get("flag") == null && messageMap.get("flag") != "解析失败")
                    event.setMetafieldLoglevel("2");
                format.put("format_level", "2");
            }
            if (!GrokUtil.isStringHasValue(facility_label)) {
                Map<String, Object> messageMap = GrokUtil.getMapByGroks(groks, message);
                if (messageMap.get("message2") == null) {
                    String log_body = "{" + String.valueOf(messageMap.get("log_body")) + "}";
//                    Map log_body_map = JSONObject.parseObject(log_body);
                    Map log_body_map=GsonHelper.fromJson(log_body);
                    String filename_suffix = new String();
                    String filename = new String();
                    String action_desc = (String) messageMap.get("action_desc");
                    if (GrokUtil.isStringHasValue(String.valueOf(messageMap.get("action_desc")))&&action_desc.contains("“")) {
                        if (action_desc.indexOf("文件夹") != -1) {
                            filename_suffix = "";
                            filename = "";
                        } else {
                            filename = action_desc.substring(action_desc.indexOf("“") + 1, action_desc.indexOf("”"));
                            if (filename.contains(".")) {
                                int suffixlength = filename.split("\\.").length;
                                filename_suffix = filename.split("\\.")[suffixlength - 1];
                            } else {
                                filename_suffix = "";
                            }
                        }
                        format.put("filename_suffix", filename_suffix);
                        format.put("filename", filename);
                    }

                    format.putAll(messageMap);
                    format.putAll(log_body_map);
                    if (messageMap.get("flag") == null && messageMap.get("flag") != "解析失败")
                        event.setMetafieldLoglevel("2");
                    format.put("format_level", "2");
                } else {
                    format.put("ys_tag", "grokparsefailure");
                }
            }

            //格式化时间，样本：
            if (!GrokUtil.isStringHasValue(String.valueOf(format.get("@timestamp")))) {
                IndexerTimeUtils.getISO8601Time2(format, "datetime", "yyyy-MM-dd HH:mm:ss");
            }
            if (!config.getIpFilter().equals("null")) {
                GrokUtil.filterGeoIP(config, format);
            } else {
                //使用GeoIP对src_ip、dst_ip或部分特殊字段处理，
                GrokUtil.setGeoIP2(format, "ip");
            }

            MetafieldHelper.setMetafield(event, "host", "ip", "", format);
            if (format.get("flag") == "解析失败")
                return false;
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
                + "\"datetime\":{\"type\":\"keyword\"},"
                + "\"user_name\":{\"type\":\"keyword\"},"
                + "\"log_level\":{\"type\":\"keyword\"},"
                + "\"action\":{\"type\":\"keyword\"},"
                + "\"ip\":{\"type\":\"keyword\"},"
                + "\"mac\":{\"type\":\"keyword\"},"
                + "\"action_desc\":{\"type\":\"keyword\"},"
                + "\"action_details\":{\"type\":\"keyword\"},"
                + "\"user_agent\":{\"type\":\"keyword\"},"
                + "\"log_body\":{\"type\":\"keyword\"},"
                + "\"severity\":{\"type\":\"keyword\"},"
                + "\"priority\":{\"type\":\"keyword\"},"
                + "\"pid\":{\"type\":\"keyword\"},"
                + "\"host\":{\"type\":\"keyword\"},"
                + "\"timestamp\":{\"type\":\"keyword\"},"
                + "\"format_level\":{\"type\":\"keyword\"},"
                + "\"facility_label\":{\"type\":\"keyword\"},"
                + "\"severity_label\":{\"type\":\"keyword\"},"
                + "\"log_class\":{\"type\":\"keyword\"},"
                + "\"logsource\":{\"type\":\"keyword\"},"
                + "\"program\":{\"type\":\"keyword\"},"
                + "\"@version\":{\"type\":\"keyword\"},"
                + "\"type\":{\"type\":\"keyword\"},"
                + "\"filename_suffix\":{\"type\":\"keyword\"},"
                + "\"filename\":{\"type\":\"keyword\"},"
                + "\"ys_tag\":{\"type\":\"keyword\"},"
                + "\"ip_geoip\": {"
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
                + "\"datetime\":{\"type\":\"keyword\"},"
                + "\"user_name\":{\"type\":\"keyword\"},"
                + "\"log_level\":{\"type\":\"keyword\"},"
                + "\"action\":{\"type\":\"keyword\"},"
                + "\"ip\":{\"type\":\"keyword\"},"
                + "\"mac\":{\"type\":\"keyword\"},"
                + "\"action_desc\":{\"type\":\"keyword\"},"
                + "\"action_details\":{\"type\":\"keyword\"},"
                + "\"user_agent\":{\"type\":\"keyword\"},"
                + "\"log_body\":{\"type\":\"keyword\"},"
                + "\"severity\":{\"type\":\"keyword\"},"
                + "\"priority\":{\"type\":\"keyword\"},"
                + "\"pid\":{\"type\":\"keyword\"},"
                + "\"host\":{\"type\":\"keyword\"},"
                + "\"timestamp\":{\"type\":\"keyword\"},"
                + "\"format_level\":{\"type\":\"keyword\"},"
                + "\"facility_label\":{\"type\":\"keyword\"},"
                + "\"severity_label\":{\"type\":\"keyword\"},"
                + "\"log_class\":{\"type\":\"keyword\"},"
                + "\"logsource\":{\"type\":\"keyword\"},"
                + "\"program\":{\"type\":\"keyword\"},"
                + "\"@version\":{\"type\":\"keyword\"},"
                + "\"type\":{\"type\":\"keyword\"},"
                + "\"filename_suffix\":{\"type\":\"keyword\"},"
                + "\"filename\":{\"type\":\"keyword\"},"
                + "\"ys_tag\":{\"type\":\"keyword\"},"
                + "\"ip_geoip\": {"
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