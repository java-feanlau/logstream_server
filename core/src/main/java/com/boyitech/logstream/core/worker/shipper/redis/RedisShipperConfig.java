package com.boyitech.logstream.core.worker.shipper.redis;

import com.boyitech.logstream.core.util.GrokUtil;
import com.boyitech.logstream.core.worker.shipper.BaseShipperConfig;

import java.util.List;
import java.util.Map;

public class RedisShipperConfig extends BaseShipperConfig {

    private String host;
    private int port;
    private String passwd;
    private List<String> redisKeys;
    private int DBIndex;
    private int type; //0:json当中message作为原始日志 1:所有做为原始日志
    private String typeMessage; //如果type为0，可以配置提取json中的哪个字段，默认为message

    public RedisShipperConfig(Map config) {
        super(config);
        host = config.get("host").toString();
        port = Double.valueOf(config.get("port").toString()).intValue();
        passwd = config.get("passwd").toString();
        redisKeys = (List) config.get("keys");
        String DBindexString = (String) config.get("DBindex");
        if (DBindexString == null) {
            DBIndex = 0;
        } else {
            DBIndex = Double.valueOf(config.get("DBindex").toString()).intValue();
        }
        type = Double.valueOf(config.get("type").toString()).intValue();
        typeMessage=String.valueOf(config.get("typeMessage"));
        if(!GrokUtil.isStringHasValue(typeMessage)){
            typeMessage="message";
        }
        // redis获取数据使用lua脚本，batchSize指定的是在队列中的最大偏移量
        // 由于初始偏移量为0，所以最大偏移量要减一
        this.batchSize = this.batchSize - 1;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getPasswd() {
        return passwd;
    }

    public List<String> getRedisKeys() {
        return redisKeys;
    }

    public int getDBIndex() {
        return DBIndex;
    }

    public int getType() {
        return type;
    }

    public String getTypeMessage() {
        return typeMessage;
    }
}
