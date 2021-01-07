package com.boyitech.logstream.worker.indexer;

import com.alibaba.fastjson.JSON;
import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.MetafieldHelper;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Eric
 *
 * @date 2019/1/15 16:15
 * @Description: TODO
 */
public class BoyiCatchviewHistoryV1Indexer extends BaseIndexer {

    public BoyiCatchviewHistoryV1Indexer(BaseWorkerConfig config) {
        super(config);
    }

    public BoyiCatchviewHistoryV1Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
    }

    public boolean format(Event event) {
        String message = event.getMessage();
        Map<String, Object> allMap = (Map<String, Object>) JSON.parse(message);
        Map<String, Object> format = event.getFormat();
        format.put("message", message);
        format.putAll(allMap);
        Object value = allMap.get("value");


        if (value instanceof Integer) {
            format.put("value_long", Long.parseLong(value.toString()));
        }else if (value instanceof Long) {
            format.put("value_long", (long) value);
        }else if(value instanceof BigDecimal){
            format.put("value_float", ((BigDecimal) value).floatValue());
        }
        Integer clock = (Integer) format.get("clock");
        Integer ns = (Integer) format.get("ns");
        String substring = ns.toString().substring(0, 3);
        String time = clock + "" + substring;
        Date date = new Date(Long.valueOf(time));
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX"); //iso860
        format.put("@timestamp", sdf2.format(date));


        //格式化Metafield
//       // event.setMetafieldLoglevel("1");
//        // format.put("Metafield_category", "Security");
//        if (event.getLogType() != null) {
//            format.put("Metafield_description", event.getLogType());
//            format.put("Metafield_type", event.getLogType());
//        }
//        if (event.getSource() != null) {
//          //  format.put("Metafield_source", event.getSource());
//            event.setSource(event.getSource());
//        }

//        if (format.get("srcAddr") != null) {
//            event.setSource(String.valueOf(format.get("srcAddr")));
//           // format.put("Metafield_object", format.get("srcAddr"));
//        }
//        if (format.get("dstAddr") != null) {
//            event.setSource(String.valueOf(format.get("dstAddr")));
//            // format.put("Metafield_subject", format.get("dstAddr"));
//        } else {
//            format.put("Metafield_subject", event.getSource());
//        }
        event.setMetafieldLoglevel("1");
        MetafieldHelper.setMetafield(event,"srcAddr","dstAddr","",format);
        if (format.get("flag") == "解析失败")
            return false;
        return true;
    }

    @Override
    public void tearDown() {

    }

    public boolean isIPv4(String addr) {
        if (addr.length() < 7 || addr.length() > 15 || "".equals(addr)) {
            return false;
        }
        /**
         * 判断IP格式和范围
         */
        String rexp = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";

        Pattern pat = Pattern.compile(rexp);

        Matcher mat = pat.matcher(addr);

        boolean ipAddress = mat.find();

        return ipAddress;
    }

    public static Map getMapping() {
        String mapping = "{\"properties\":{"
                + "\"@timestamp\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
                + "\"received_at\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
                + "\"portedAt\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
                + "\"flag\":{\"type\":\"keyword\"},"
                + "\"message\":{\"type\":\"text\"},"
                + "\"ns\":{\"type\":\"long\"},"
                + "\"groups\":{\"type\":\"keyword\"},"
                + "\"clock\":{\"type\":\"keyword\"},"
                + "\"itemid\":{\"type\":\"keyword\"},"
                + "\"host\":{\"type\":\"keyword\"},"
                + "\"name\":{\"type\":\"keyword\"},"
                + "\"applications\":{\"type\":\"keyword\"},"
                + "\"Metafield_type\":{\"type\":\"keyword\"},"
                + "\"Metafield_category\":{\"type\":\"keyword\"},"
                + "\"Metafield_subject\":{\"type\":\"keyword\"},"
                + "\"Metafield_object\":{\"type\":\"keyword\"},"
                + "\"Metafield_loglevel\":{\"type\":\"keyword\"},"
                + "\"Metafield_source\":{\"type\":\"keyword\"},"
                + "\"Metafield_description\":{\"type\":\"text\", \"fields\": {\"raw\": {\"type\": \"keyword\"}}},"
                + "\"value\":{\"type\":\"keyword\"},"
                + "\"value_long\":{\"type\":\"long\"},"
                + "\"value_float\":{\"type\":\"float\"}"
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
                + "\"ns\":{\"type\":\"long\"},"
                + "\"groups\":{\"type\":\"keyword\"},"
                + "\"clock\":{\"type\":\"keyword\"},"
                + "\"itemid\":{\"type\":\"keyword\"},"
                + "\"host\":{\"type\":\"keyword\"},"
                + "\"name\":{\"type\":\"keyword\"},"
                + "\"applications\":{\"type\":\"keyword\"},"
                + "\"Metafield_type\":{\"type\":\"keyword\"},"
                + "\"Metafield_category\":{\"type\":\"keyword\"},"
                + "\"Metafield_subject\":{\"type\":\"keyword\"},"
                + "\"Metafield_object\":{\"type\":\"keyword\"},"
                + "\"Metafield_loglevel\":{\"type\":\"keyword\"},"
                + "\"Metafield_source\":{\"type\":\"keyword\"},"
                + "\"Metafield_description\":{\"type\":\"text\", \"fields\": {\"raw\": {\"type\": \"keyword\"}}},"
                + "\"value\":{\"type\":\"keyword\"},"
                + "\"value_long\":{\"type\":\"long\"},"
                + "\"value_float\":{\"type\":\"float\"}"
                + "}"
                + "}";
        return mapping;
    }


}
