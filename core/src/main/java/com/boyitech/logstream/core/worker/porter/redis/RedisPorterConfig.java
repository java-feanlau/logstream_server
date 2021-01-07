package com.boyitech.logstream.core.worker.porter.redis;

import com.boyitech.logstream.core.worker.porter.BasePorterConfig;
import com.boyitech.logstream.core.worker.shipper.BaseShipperConfig;

import java.util.List;
import java.util.Map;

public class RedisPorterConfig extends BasePorterConfig {

    private String host;
    private int port;
    private String passwd;
    private String redisKeys;
    private int DBindex;

    public RedisPorterConfig(Map config) {
        super(config);
        host = config.get("host").toString();
        port = Double.valueOf(config.get("port").toString()).intValue();
        passwd = config.get("passwd").toString();
        redisKeys = config.get("keys").toString();
        String DBindexString = (String) config.get("DBindex");
        if (DBindexString == null) {
            DBindex = 1;
        } else {
            DBindex = Double.valueOf(config.get("DBindex").toString()).intValue();
        }
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

    public String getRedisKeys() {
        return redisKeys;
    }

    public int getDBIndex() {
        return DBindex;
    }
}
