package com.boyitech.logstream.core.info;

import com.boyitech.logstream.core.setting.WorkerSettings;
import com.boyitech.logstream.core.util.GsonHelper;
import org.apache.logging.log4j.LogManager;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;

public class Event {
    static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger("main");
    // 如果是文件采集的话标识所采集文件的路径。如果是redis采集的话标识redisKey
    private String key;
    // 标识 shipperID
    private String mark;
    // 来源 Metafield_source
    private String source;

    // 日志类型 Metafield_type  何种indexer
    private String logType;

    // 数据类型 Metafield_category  何种shipper
    private String msgType;
    // 原始数据
    private String message;

    private String index;
    private int esShards = WorkerSettings.ESSHARDS.getValue();
    private int esReplicas = WorkerSettings.ESREPLICAS.getValue();

    //为解决博世bug新增，值类似202001
    private int esIndex=0;
    // 格式化后的结果
    private Map<String, Object> format = new HashMap();
    // 创建时间
    private DateTime timestamp;
    private DateTime receivedAt;
    private DateTime indexedAt;
    private DateTime portedAt;
    private int retry = 0;


    public String getMetafieldType() {
        return metafieldType;
    }

    public void setMetafieldType(String metafieldType) {
        this.metafieldType = metafieldType;
    }

    //Metafield字段
    private String metafieldType; //indexer
    private String metafieldCategory; //shipper
    private String metafieldSubject; //进入es的ip
    private String metafieldObject; //中间转发ip
    private String metafieldLoglevel = "0";
    private String metafieldSource; //生成日志的ip
    private String metafieldDescription = "弋搜采集日志";

    private String clientIP;  //采集代理的ip


    public String getMetafieldCategory() {
        return metafieldCategory;
    }

    public void setMetafieldCategory(String metafieldCategory) {
        this.metafieldCategory = metafieldCategory;
    }

    public String getMetafieldSubject() {
        return metafieldSubject;
    }

    public void setMetafieldSubject(String metafieldSubject) {
        this.metafieldSubject = metafieldSubject;
    }

    public String getMetafieldObject() {
        return metafieldObject;
    }

    public void setMetafieldObject(String metafieldObject) {
        this.metafieldObject = metafieldObject;
    }

    public String getMetafieldLoglevel() {
        return metafieldLoglevel;
    }

    public void setMetafieldLoglevel(String metafieldLoglevel) {
        this.metafieldLoglevel = metafieldLoglevel;
    }

    public String getMetafieldSource() {
        return metafieldSource;
    }

    public void setMetafieldSource(String metafieldSource) {
        this.metafieldSource = metafieldSource;
    }

    public String getMetafieldDescription() {
        return metafieldDescription;
    }

    public void setMetafieldDescription(String metafieldDescription) {
        this.metafieldDescription = metafieldDescription;
    }

    public String getClientIP() {
        return clientIP;
    }

    public void setClientIP(String clientIP) {
        this.clientIP = clientIP;
    }

    public Event() {
        this.timestamp = DateTime.now();
        this.receivedAt = this.timestamp;
    }

    public Event(Map map) {
        if (map.get("key") != null)
            this.key = map.get("key").toString();
        if (map.get("mark") != null)
            this.mark = map.get("mark").toString();
        if (map.get("source") != null)
            this.source = map.get("source").toString();
        if (map.get("logType") != null)
            this.logType = map.get("logType").toString();
//        try {
//            if (map.get("inetInfo") != null)
//              //  this.inetInfo = (InetInfo) map.get("inetInfo");
//                InetInfo inetInfo=JSONObject.parseObject(map.get("inetInfo"));
//        }
//        catch (Exception e){
//            LOGGER.error("Event error:"+e);
//        }
        if (map.get("msgType") != null)
            this.msgType = map.get("msgType").toString();
        if (map.get("message") != null)
            this.message = map.get("message").toString();
        if (map.get("index") != null)
            this.index = map.get("index").toString();
        if (map.get("shipped_at") != null) {
            DateTime dt = DateTime.parse(map.get("shipped_at").toString());
            this.receivedAt = dt;
            this.timestamp = dt;
        } else {
            this.timestamp = DateTime.now();
            this.receivedAt = this.timestamp;
        }
    }

    public int getEsShards() {
        return esShards;
    }

    public void setEsShards(int esShards) {
        this.esShards = esShards;
    }

    public int getEsReplicas() {
        return esReplicas;
    }

    public void setEsReplicas(int esReplicas) {
        this.esReplicas = esReplicas;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        if (metafieldType == null) {
            setMetafieldType(logType);
        }
        this.logType = logType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
        if (metafieldCategory == null) {
            setMetafieldCategory(msgType);
        }
    }

    public Map<String, Object> getFormat() {
        return format;
    }

    public void setFormat(Map<String, Object> format) {
        this.format = format;
    }

    public DateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(DateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getRetry() {
        return retry;
    }

    public void increaseRetry() {
        retry++;
    }

    public DateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(DateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    public DateTime getIndexedAt() {
        return indexedAt;
    }

    public void setIndexedAt(DateTime indexedAt) {
        this.indexedAt = indexedAt;
    }

    public DateTime getPortedAt() {
        return portedAt;
    }

    public void setPortedAt(DateTime portedAt) {
        this.portedAt = portedAt;
    }

    public void setRetry(int retry) {
        this.retry = retry;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        if (metafieldSubject == null) {
            setMetafieldSubject(source);
        }
        if (metafieldObject == null) {
            setMetafieldObject(source);
        }
        if (metafieldSource == null) {
            setMetafieldSource(source);
        }
        this.source = source;
    }

    public String getJsonMessage() {
//		format.put("received_at", receivedAt.toString(PatternHelper.DateTimeFormatter));
//		format.put("indexer_at", indexerAt.toString(PatternHelper.DateTimeFormatter));
//		format.put("porter_at", porterAt.toString(PatternHelper.DateTimeFormatter));
        if (receivedAt != null) {
            format.put("received_at", receivedAt.toString());
        }
        if (portedAt != null) {
            format.put("ported_at", portedAt.toString());
        }
        return GsonHelper.toJson(format);
    }


    public Map bulkMap() {
        Map map = new HashMap();
        map.put("key", this.key);
        map.put("mark", this.mark);
        map.put("source", this.source);
        map.put("logType", this.logType);
        map.put("msgType", this.msgType);
        map.put("message", this.message);
        map.put("index", this.index);
        if (receivedAt != null) {
            map.put("received_at", receivedAt.toString());
        }
        return map;
    }

    @Override
    public String toString() {
        return "Event{" +
                "key='" + key + '\'' +
                ", mark='" + mark + '\'' +
                ", source='" + source + '\'' +
                ", logType='" + logType + '\'' +
                ", msgType='" + msgType + '\'' +
                ", message='" + message + '\'' +
                ", index='" + index + '\'' +
                ", receivedAt=" + receivedAt +
                ", format=" + format +
                '}';
    }

    public int getEsIndex() {
        return esIndex;
    }

    public void setEsIndex(int esIndex) {
        this.esIndex = esIndex;
    }
}
