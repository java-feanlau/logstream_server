package com.boyitech.logstream.worker.indexer;


import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.GrokUtil;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.IndexerTimeUtils;
import com.boyitech.logstream.core.util.MetafieldHelper;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import io.krakens.grok.api.Grok;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author: juzheng
 * @Title: GenvisionMultimediaAllV1Indexer
 * @date: 2019-07-18T15:39:37.193
 * @Description: 1.此indexer文件根据indexer模版创建
 * 2.处理商学院---多媒体管理平台日志格式化
 */
public class GenvisionMultimediaAllV1Indexer extends BaseIndexer {
    //此处初始化正则、grok的变量
    private Grok grok1;
    private String pattern1;

    public GenvisionMultimediaAllV1Indexer(BaseWorkerConfig config) {
        super(config);
    }

    public GenvisionMultimediaAllV1Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
    }

    @Override
    public boolean register() {
        //此处注册正确的正则+grok
        pattern1 = "^(%{IP:duomeiti_device_ip})\\:(%{WORD:duomeiti_device_port})$";
        grok1 = GrokUtil.getGrok(pattern1);
        return true;

    }

    @Override
    public boolean format(Event event) {
        //最外层，格式化日志主体,商学院多媒体日志没有message字段
        Map<String, Object> format = event.getFormat();
        String message1=event.getMessage();
        Map map1=GsonHelper.fromJson(message1);
        format.putAll(map1);
        format.put("message", message1);
        String Date = String.valueOf(map1.get("Date"));
        String Ip = String.valueOf(map1.get("Ip"));
        String OperateTime = String.valueOf(map1.get("OperateTime"));
        if (GrokUtil.isStringHasValue(Date)==true && GrokUtil.isStringHasValue(Ip)==true) {
            Map<String, Object> map = GrokUtil.getMap(grok1, Ip);
            format.putAll(map);
           // IndexerTimeUtils.getISO8601Time2(format, "Date", "yyyy-MM-dd HH:mm:ss");
            IndexerTimeUtils.getISO8601Time(Date,"yyyy-MM-dd HH:mm:ss");
            if (map.get("flag") == null && map.get("flag") != "解析失败") {
                event.setMetafieldLoglevel("1");
            }
        }
        if (GrokUtil.isStringHasValue(OperateTime)==true) {
            //IndexerTimeUtils.getISO8601Time2(format, "OperateTime", "yyyy-MM-dd HH:mm:ss.s");
            IndexerTimeUtils.getISO8601Time(OperateTime,"yyyy-MM-dd HH:mm:ss.s");
        }

        //格式化Metafield ，可能需要根基实际日志格式替换部分字段名如client_ip/src_ip、dst_ip

        event.setMetafieldLoglevel("2");
//        if (event.getLogType() != null) {
//            format.put("Metafield_description", event.getLogType());
//            format.put("Metafield_type", event.getLogType());
//        }
//        if (event.getSource() != null) {
//            format.put("Metafield_source", event.getSource());
//            format.put("log_source", event.getSource());//增加来源设备标识；
//        }
//        if (format.get("duomeiti_device_ip") != null) {
//            format.put("Metafield_object", format.get("duomeiti_device_ip"));
//            event.setSource(String.valueOf(format.get("duomeiti_device_ip")));
//        }
//        if (format.get("dst_ip") != null) {
//            format.put("Metafield_subject", format.get("dst_ip"));
//        } else {
//            format.put("Metafield_subject", event.getSource());
//        }
        MetafieldHelper.setMetafield(event,"duomeiti_device_ip","duomeiti_device_ip","",format);
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
                + "\"duomeiti_device_ip\":{\"type\":\"keyword\"},"
                + "\"duomeiti_device_port\":{\"type\":\"keyword\"},"
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
                + "\"duomeiti_device_ip\":{\"type\":\"keyword\"},"
                + "\"duomeiti_device_port\":{\"type\":\"keyword\"},"
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