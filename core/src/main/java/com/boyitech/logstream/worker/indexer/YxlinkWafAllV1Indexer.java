package com.boyitech.logstream.worker.indexer;


import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.GrokUtil;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.MetafieldHelper;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import com.boyitech.logstream.core.worker.indexer.BaseIndexerConfig;
import io.krakens.grok.api.Grok;
import io.krakens.grok.api.GrokCompiler;
import io.krakens.grok.api.Match;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Eric
 * @Title: SbsDeviceWafIndexer
 * @date 2018/12/10 17:13
 * @Description: TODO
 */
public class YxlinkWafAllV1Indexer extends BaseIndexer {

    private  Grok grok;
    private  String patterns;
    private BaseIndexerConfig config;


    public YxlinkWafAllV1Indexer(BaseWorkerConfig config) {
        super(config);
        this.config= (BaseIndexerConfig) config;

    }

    public YxlinkWafAllV1Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
        this.config= (BaseIndexerConfig) config;

    }

    @Override
    public boolean register() {
        patterns = "^(\\s)*(\\<(?<id>\\d+)\\>)\\s?%{NOTSPACE:device_type}\\s" +
                "%{DATA:recording_time}\\:%{NOTSPACE:log_level}#WAFSPLIT#%{NOTSPACE:device_name}" +
                "#WAFSPLIT#%{CHTIME:attack_time}" +
                "#WAFSPLIT#%{IP:src_ip}#WAFSPLIT#%{NUMBER:src_port}#WAFSPLIT#%{IP:dst_ip}" +
                "#WAFSPLIT#%{NUMBER:dst_port}#WAFSPLIT#%{DATA:rule_name}#WAFSPLIT#%{DATA:rule_type}" +
                "#WAFSPLIT#%{DATA:risk_level}#WAFSPLIT#(%{DATA:risk})(#WAFSPLIT#(%{DATA:solution}))" +
                "(#WAFSPLIT#%{DATA:intercept_style}|)(#WAFSPLIT#%{DATA:http_request}|)(#WAFSPLIT#(?<url>[\\s\\S]*)|)$";
//        patterns="^(\\S+)?(\\<(?<id>\\d+)\\>)(%{WORD:device_type})\\s(%{GREEDYDATA:recording_time})\\:%{NOTSPACE:log_level}#WAFSPLIT#(%{NOTSPACE:device_name})#WAFSPLIT#(?<attack_time>%{YEAR}-%{MONTHNUM}-%{MONTHDAY} %{HOUR}:(?:[0-5][0-9]|[0-9]):%{SECOND})#WAFSPLIT#(?<src_ip>%{IP})#WAFSPLIT#(?<src_port>\\d+)#WAFSPLIT#(?<dst_ip>%{IP})#WAFSPLIT#(?<dst_port>\\d+)#WAFSPLIT#(?<rule_name>[\\s\\S]*)#WAFSPLIT#(?<rule_type>\\S+)#WAFSPLIT#(?<risk_level>\\S+)#WAFSPLIT#(?<risk>[\\s\\S]*)#WAFSPLIT#(?<solution>[\\s\\S]*)#WAFSPLIT#(?<intercept_style>\\S+)#WAFSPLIT#(?<http_request>\\S+)#WAFSPLIT#(?<url>[\\s\\S]*)$";
        GrokCompiler grokCompiler = GrokCompiler.newInstance();
        grokCompiler.registerDefaultPatterns();
//        2018-10-24 10:32:42
        grokCompiler.register("CHTIME", "%{YEAR}-%{MONTHNUM}-%{MONTHDAY} %{HOUR}:(?:[0-5][0-9]|[0-9]):%{SECOND}");


        grok = grokCompiler.compile(patterns, true);
        grok=GrokUtil.getGrok(patterns);

        return true;
    }

    int i = 0;

    @Override
    public boolean format(Event event) {

/**
 * #WAFSPLIT#(?<src_ip>%{IP})#WAFSPLIT#(?<src_port>\d+)#WAFSPLIT#(?<dst_ip>%{IP})#WAFSPLIT#(?<dst_port>\d+)#WAFSPLIT#(?<rule_name>[\s\S]*)#WAFSPLIT#(?<rule_type>\S+)#WAFSPLIT#(?<risk_level>\S+)#WAFSPLIT#(?<risk>[\s\S]*)#WAFSPLIT#(?<solution>[\s\S]*)#WAFSPLIT#(?<intercept_style>\S+)#WAFSPLIT#(?<http_request>\S+)#WAFSPLIT#(?<url>[\s\S]*)$"
 */
        String message = event.getMessage();
        message=message.replace("\"\"","");
        Match grokMatch = grok.match(message);
        if (grokMatch.isNull()) {
            HashMap<String, Object> flgs = new HashMap<String, Object>();
            flgs.put("flag", "解析失败");
//            System.out.println("格式有误 patterns:" + patterns + " message:" + message);
            return false;

        } else {
            Map<String, Object> messageMap = grokMatch.capture();

            Map<String, Object> formated = event.getFormat();
            formated.putAll(messageMap);
            formated.put("message", event.getMessage());

            if (!config.getIpFilter().equals("null")) {
                GrokUtil.filterGeoIP(config, formated);
            } else {
                GrokUtil.setGeoIP(formated);
            }

            //格式化时间
            String accessTime = (String) formated.get("attack_time");
            if (accessTime != null && accessTime.trim().length() != 0) {
                Date time = new Date();
                //2018-09-09 19:15:14
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                try {
                    time = sdf.parse(accessTime);
                } catch (ParseException e) {
                    LOGGER.error("@timestamp时间格式化出错");
                }
                SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
                accessTime = sdf2.format(time);
                formated.put("@timestamp", accessTime);
            }


            //格式化Metafield
            event.setMetafieldLoglevel("1");
            MetafieldHelper.setMetafield(event,"src_ip","dst_ip","",formated);
            if (event.getSource() != null) {
                //time:10:13 AM 2019/10/28配合告警
                event.setMetafieldSource(event.getSource());
            }
            if(event.getMetafieldSource()==null&&event.getClientIP()!=null)
            {
                event.setMetafieldSource(event.getClientIP());
            }
            return true;

        }

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
                + "\"id\":{\"type\":\"keyword\"},"
                + "\"device_type\":{\"type\":\"keyword\"},"
                + "\"recording_time\":{\"type\":\"keyword\"},"
                + "\"log_level\":{\"type\":\"keyword\"},"
                + "\"device_name\":{\"type\":\"keyword\"},"
                + "\"attack_time\":{\"type\":\"keyword\"},"
                + "\"src_ip\":{\"type\":\"ip\"},"
                + "\"src_port\":{\"type\":\"integer\"},"
                + "\"dst_ip\":{\"type\":\"ip\"},"
                + "\"dst_port\":{\"type\":\"integer\"},"
                + "\"rule_name\":{\"type\":\"keyword\"},"
                + "\"rule_type\":{\"type\":\"keyword\"},"
                + "\"risk_level\":{\"type\":\"keyword\"},"
                + "\"risk\":{\"type\":\"keyword\"},"
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
                + "\"solution\":{\"type\":\"keyword\"},"
                + "\"intercept_style\":{\"type\":\"keyword\"},"
                + "\"http_request\":{\"type\":\"keyword\"},"
                + "\"url\":{\"type\":\"keyword\"},"
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
                + "\"id\":{\"type\":\"keyword\"},"
                + "\"device_type\":{\"type\":\"keyword\"},"
                + "\"recording_time\":{\"type\":\"keyword\"},"
                + "\"log_level\":{\"type\":\"keyword\"},"
                + "\"device_name\":{\"type\":\"keyword\"},"
                + "\"attack_time\":{\"type\":\"keyword\"},"
                + "\"src_ip\":{\"type\":\"ip\"},"
                + "\"src_port\":{\"type\":\"integer\"},"
                + "\"dst_ip\":{\"type\":\"ip\"},"
                + "\"dst_port\":{\"type\":\"integer\"},"
                + "\"rule_name\":{\"type\":\"keyword\"},"
                + "\"rule_type\":{\"type\":\"keyword\"},"
                + "\"risk_level\":{\"type\":\"keyword\"},"
                + "\"risk\":{\"type\":\"keyword\"},"
                + "\"solution\":{\"type\":\"keyword\"},"
                + "\"intercept_style\":{\"type\":\"keyword\"},"
                + "\"http_request\":{\"type\":\"keyword\"},"
                + "\"url\":{\"type\":\"keyword\"},"
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
