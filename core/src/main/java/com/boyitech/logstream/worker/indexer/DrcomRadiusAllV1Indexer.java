package com.boyitech.logstream.worker.indexer;


import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.GrokUtil;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.IndexerTimeUtils;
import com.boyitech.logstream.core.util.MetafieldHelper;
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
 * @author: juzheng
 * @Title: DrcomRadiusAllV1Indexer
 * @date: 2019-07-31T09:22:19.316
 * @Description: 1.此indexer文件根据indexer通用模版创建
 * 2.完成电机学院drcom日志格式化
 */
public class DrcomRadiusAllV1Indexer extends BaseIndexer {
    //此处初始化正则、grok的变量
    private Grok grok1;
    private String pattern1;

    public DrcomRadiusAllV1Indexer(BaseWorkerConfig config) {
        super(config);
    }

    public DrcomRadiusAllV1Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
    }

    @Override
    public boolean register() {
        //此处注册正确的正则+grok
        pattern1 = "^\\S+\\t(%{NOTSPACE:login_user})\\t(%{TIMESTAMP_ISO8601:login_time})\\t(%{TIMESTAMP_ISO8601:logout_time})\\t(%{NOTSPACE:duration_time})\\t(%{NOTSPACE:flux})\\t(%{IP:client_ip})\\t(%{GREEDYDATA:client_mac})";
        grok1 = GrokUtil.getGrok(pattern1);
        return true;

    }

    @Override
    public boolean format(Event event) {
        //最外层，格式化日志主体
        String message = event.getMessage();
        Map<String, Object> map = GrokUtil.getMap(grok1, message);
        Map<String, Object> format = event.getFormat();
        format.putAll(map);
        format.put("message", message);
        if (map.get("flag") == null && map.get("flag") != "解析失败") {
            event.setMetafieldLoglevel("1");
        }

        //时间类型字段标准化
        String login_time = String.valueOf(format.get("login_time"));
        String login_time_ISO8601;
        if (GrokUtil.isStringHasValue(login_time)) {
            login_time_ISO8601 = IndexerTimeUtils.getISO8601Time(login_time, "yyyy-MM-dd HH:mm:ss");
            if (login_time_ISO8601 != null && !login_time_ISO8601.equals("")) {
                format.put("login_time_ISO8601", login_time_ISO8601);
            }
        }

        String logout_time = String.valueOf(format.get("logout_time"));
        String logout_time_ISO8601;
        if (GrokUtil.isStringHasValue(logout_time)) {
            logout_time_ISO8601 = IndexerTimeUtils.getISO8601Time(logout_time, "yyyy-MM-dd HH:mm:ss");
            if (logout_time_ISO8601 != null && !logout_time_ISO8601.equals("")) {
                format.put("logout_time_ISO8601", logout_time_ISO8601);
            }
        }
        //格式化时间，样本：
        IndexerTimeUtils.getISO8601Time2(format, "", "");


        //格式化Metafield ，可能需要根基实际日志格式替换部分字段名如client_ip/src_ip、dst_ip
        event.setMetafieldLoglevel("2");
//        if(GrokUtil.isStringHasValue(String.valueOf(format.get("client_ip")))){
//            event.setSource(String.valueOf(format.get("client_ip")));
//        }
//        if (event.getLogType() != null) {
//            format.put("Metafield_description", event.getLogType());
//            format.put("Metafield_type", event.getLogType());
//        }
        if (event.getSource() != null) {
           // format.put("Metafield_source", event.getSource());
          //  event.setSource(event.getSource());
            format.put("log_source", event.getSource());//增加来源设备标识；
        }
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
        MetafieldHelper.setMetafield(event,"client_ip","client_ip","",format);
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
                + "\"login_user\":{\"type\":\"keyword\"},"
                + "\"login_time\":{\"type\":\"keyword\"},"
                + "\"logout_time\":{\"type\":\"keyword\"},"
                + "\"duration_time\":{\"type\":\"keyword\"},"
                + "\"flux\":{\"type\":\"keyword\"},"
                + "\"client_ip\":{\"type\":\"keyword\"},"
                + "\"client_mac\":{\"type\":\"keyword\"},"
                + "\"login_time_ISO8601\":{\"type\":\"keyword\"},"
                + "\"logout_time_ISO8601\":{\"type\":\"keyword\"},"
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
                + "\"login_user\":{\"type\":\"keyword\"},"
                + "\"login_time\":{\"type\":\"keyword\"},"
                + "\"logout_time\":{\"type\":\"keyword\"},"
                + "\"duration_time\":{\"type\":\"keyword\"},"
                + "\"flux\":{\"type\":\"keyword\"},"
                + "\"client_ip\":{\"type\":\"keyword\"},"
                + "\"client_mac\":{\"type\":\"keyword\"},"
                + "\"login_time_ISO8601\":{\"type\":\"keyword\"},"
                + "\"logout_time_ISO8601\":{\"type\":\"keyword\"},"
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