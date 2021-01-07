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


public class WindowsDnsAllV1Indexer extends BaseIndexer {
    private BaseIndexerConfig config;
    private String pattern_1;
    private Grok grok_1;
    private String[] pattern_2;
    private ArrayList<Grok> grok_2;


    public WindowsDnsAllV1Indexer(BaseWorkerConfig config) {
        super(config);
        this.config = (BaseIndexerConfig) config;

    }

    public WindowsDnsAllV1Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
        this.config = (BaseIndexerConfig) config;

    }

    @Override
    public boolean register() {
        pattern_1 = "(?<datetime>%{YEAR}/%{MONTHNUM}/%{MONTHDAY} %{TIME})(\\s+)(?<logbody>.*)";

        pattern_2 = new String[]{
                "%{DATA:thread_id_0x16}(\\s+)%{DATA:context}(\\s+)%{DATA:internal_packet_identifier}(\\s+)%{DATA:protocol}(\\s+)%{DATA:action}(\\s+)%{IP:remote_ip}(\\s+)%{DATA:xid_hex}(\\s)%{DATA:style}(\\s+)%{DATA:opcode}(\\s+)\\[%{DATA:array_data}\\](\\s+)%{DATA:question_type}(\\s+)%{GREEDYDATA:question_name}",
                "%{DATA:thread_id_0x16}(\\s+)%{DATA:context},(\\s+)socket=%{DATA:internal_packet_identifier},(\\s+)pcon=%{DATA:protocol},(\\s+)state=%{DATA:action},(\\s+)IP=%{GREEDYDATA:remote_ip}"
        };
        grok_1 = GrokUtil.getGrok(pattern_1);
        grok_2 = GrokUtil.getGroks(pattern_2);

        return true;
    }

    @Override
    public boolean format(Event event) {
        String message = event.getMessage();
        //最外层，格式化日志主体
        try {
            Map<String, Object> format = event.getFormat();
            event.setMetafieldLoglevel("1");
            format.put("format_level", "1");
            Map<String, Object> messageMap = GrokUtil.getMap(grok_1, message);
            String log_body =String.valueOf(messageMap.get("logbody"));
            format.putAll(messageMap);
            format.put("message",message);
            format.put("log_type","windows-dns");
            if (messageMap.get("flag") == null && messageMap.get("flag") != "解析失败") {
                event.setMetafieldLoglevel("2");
                format.put("format_level", "2");
                Map<String, Object> messageMap2 = GrokUtil.getMapByGroks(grok_2, log_body);
                format.putAll(messageMap2);
            }
            String thread_id_0x16= (String) format.get("thread_id_0x16");
            String thread_id=String.valueOf(Integer.parseInt(thread_id_0x16,16));
            format.put("thread_id",thread_id);

            String question_name= (String) format.get("question_name");
            if(GrokUtil.isStringHasValue(question_name)){
                String[] question_name_array = question_name.split("\\)");
                String question_domain = "";
//                String item=new String();
                int k=0;
                int len = question_name_array.length;
                for(int i=0;i<len;i++){
                    k=k+1;
                    question_name_array[i]=question_name_array[i].split("\\(")[0];
                    if(k>1&&k<=len){
                        if(k==len){
                            question_domain=question_domain+question_name_array[i];
                        }
                        else {
                            question_domain=question_domain+question_name_array[i]+".";
                        }
                    }
                }
                format.put("question_domain",question_domain);

            }
            //格式化时间，2019/12/5 13:03:54
            IndexerTimeUtils.getISO8601Time2(format, "datetime", "yyyy/MM/d HH:mm:ss");
            if (format.get("datetime") != null) {
                format.put("datetime", (String) format.get("@timestamp"));
            }
            if (!config.getIpFilter().equals("null")) {
                GrokUtil.filterGeoIP(config, format);
            } else {
                //使用GeoIP对src_ip、dst_ip或部分特殊字段处理，
                GrokUtil.setGeoIP2(format, "remote_ip");
            }

            MetafieldHelper.setMetafield(event, "remote_ip", "", "", format);
            if (format.get("flag") == "解析失败")
                return false;
        } catch (Exception ex) {
            LOGGER.error(ex + "message:" + message);
            addException(ex + "message:" + message);
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
                + "\"datetime\":{\"type\":\"keyword\"},"
                + "\"logbody\":{\"type\":\"keyword\"},"
                + "\"thread_id_0x16\":{\"type\":\"keyword\"},"
                + "\"context\":{\"type\":\"keyword\"},"
                + "\"internal_packet_identifier\":{\"type\":\"keyword\"},"
                + "\"protocol\":{\"type\":\"keyword\"},"
                + "\"action\":{\"type\":\"keyword\"},"
                +"\"remote_ip\":{\"type\":\"keyword\",\"fields\":{\"ip\":{\"type\":\"ip\",\"ignore_malformed\":\"true\"}}},"
                + "\"xid_hex\":{\"type\":\"keyword\"},"
                + "\"style\":{\"type\":\"keyword\"},"
                + "\"opcode\":{\"type\":\"keyword\"},"
                + "\"array_data\":{\"type\":\"keyword\"},"
                + "\"question_type\":{\"type\":\"keyword\"},"
                + "\"question_name\":{\"type\":\"keyword\"},"
                + "\"format_level\":{\"type\":\"keyword\"},"
                + "\"log_type\":{\"type\":\"keyword\"},"
                + "\"thread_id\":{\"type\":\"integer\"},"
                + "\"question_domain\":{\"type\":\"keyword\"},"
                + "\"remote_ip_geoip\": {"
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
                + "\"logbody\":{\"type\":\"keyword\"},"
                + "\"thread_id_0x16\":{\"type\":\"keyword\"},"
                + "\"context\":{\"type\":\"keyword\"},"
                + "\"internal_packet_identifier\":{\"type\":\"keyword\"},"
                + "\"protocol\":{\"type\":\"keyword\"},"
                + "\"action\":{\"type\":\"keyword\"},"
                +"\"remote_ip\":{\"type\":\"keyword\",\"fields\":{\"ip\":{\"type\":\"ip\",\"ignore_malformed\":\"true\"}}},"
                + "\"xid_hex\":{\"type\":\"keyword\"},"
                + "\"style\":{\"type\":\"keyword\"},"
                + "\"opcode\":{\"type\":\"keyword\"},"
                + "\"array_data\":{\"type\":\"keyword\"},"
                + "\"question_type\":{\"type\":\"keyword\"},"
                + "\"question_name\":{\"type\":\"keyword\"},"
                + "\"format_level\":{\"type\":\"keyword\"},"
                + "\"log_type\":{\"type\":\"keyword\"},"
                + "\"thread_id\":{\"type\":\"integer\"},"
                + "\"question_domain\":{\"type\":\"keyword\"},"
                + "\"remote_ip_geoip\": {"
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