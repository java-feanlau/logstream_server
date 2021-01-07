package com.boyitech.logstream.worker.indexer;


import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.IndexerTimeUtils;
import com.boyitech.logstream.core.util.MetafieldHelper;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;

import java.util.Map;

/**
 * @author: juzheng
 * @Title: DefaultIndexer
 * @date: 2019-08-20T15:40:03.893
 * @Description: 此indexer文件根据indexer通用模版创建 ，
 * 不进行任何格式化操作的默认Indexer
 */
public class SnmpIndexer extends BaseIndexer {

    public SnmpIndexer(BaseWorkerConfig config) {
        super(config);
    }

    public SnmpIndexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
    }

    @Override
    public boolean register() {
        return true;
    }

    @Override
    public boolean format(Event event) {
        String message = event.getMessage();
        Map<String, String> stringMap = GsonHelper.fromJson(message);
        String oid = stringMap.get("oid");
        String variable = stringMap.get("variable");

        String syntax = stringMap.get("syntax");
        Map<String, Object> format = event.getFormat();
        format.put("message", message);
        format.put("oid", oid);
        try {
            double variableD = Double.parseDouble(variable);
            format.put("variableD", variableD);
        }catch (NumberFormatException e){
            format.put("variable", variable);
        }
        format.put("syntax", syntax);

        IndexerTimeUtils.getISO8601Time2(format, "", "");

        MetafieldHelper.setMetafield(event, "", "", "", format);

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
                + "\"oid\":{\"type\":\"keyword\"},"
                + "\"variableD\":{\"type\":\"double\"},"
                + "\"variable\":{\"type\":\"keyword\"},"
                + "\"syntax\":{\"type\":\"keyword\"},"
                + "\"message\":{\"type\":\"text\"},"
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
                + "\"oid\":{\"type\":\"keyword\"},"
                + "\"variableD\":{\"type\":\"double\"},"
                + "\"variable\":{\"type\":\"keyword\"},"
                + "\"syntax\":{\"type\":\"keyword\"},"
                + "\"message\":{\"type\":\"text\"},"
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