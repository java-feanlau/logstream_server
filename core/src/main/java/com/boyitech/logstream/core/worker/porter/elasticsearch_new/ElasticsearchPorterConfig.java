package com.boyitech.logstream.core.worker.porter.elasticsearch_new;

import com.boyitech.logstream.core.worker.porter.BasePorterConfig;

import java.util.Map;

public class ElasticsearchPorterConfig extends BasePorterConfig {
    private String ip;
    private String port;
    private Integer oneIndexMaxCount;
    private String dateTimepattern;
    private Integer maxCountFlush;
    private Integer maxSizeFlush;
    private Integer maxTimeFlush;

    public ElasticsearchPorterConfig(Map config) {
        super(config);
        this.ip = (String) config.get("ip");
        this.port = (String) config.get("port");
        if (config.get("oneIndexMaxCount") == null) {
            this.oneIndexMaxCount = 100000;
        } else {
            this.oneIndexMaxCount = Integer.parseInt(String.valueOf(config.get("oneIndexMaxCount")));
        }
        if (config.get("dateTimepattern") == null) {
            this.dateTimepattern = "YYYY.MM";
        } else {
            this.dateTimepattern = String.valueOf(config.get("dateTimepattern"));
        }
        if (config.get("maxCountFlush") == null) {
            this.maxCountFlush = 1000;
        } else {
            this.maxCountFlush = Integer.parseInt(String.valueOf(config.get("maxCountFlush")));
        }
        if (config.get("maxSizeFlush") == null) {
            this.maxSizeFlush = 10;
        } else {
            this.maxSizeFlush = Integer.parseInt(String.valueOf(config.get("maxSizeFlush")));
        }
        if (config.get("maxTimeFlush") == null) {
            this.maxTimeFlush = 5;
        } else {
            this.maxTimeFlush = Integer.parseInt(String.valueOf(config.get("maxTimeFlush")));
        }


    }

    public Integer getOneIndexMaxCount() {
        return oneIndexMaxCount;
    }

    public String getDateTimepattern() {
        return dateTimepattern;
    }

    public String getPort() {
        return port;
    }

    public String getIp() {
        return ip;
    }

    public Integer getMaxCountFlush() {
        return maxCountFlush;
    }

    public Integer getMaxSizeFlush() {
        return maxSizeFlush;
    }

    public Integer getMaxTimeFlush() {
        return maxTimeFlush;
    }
}
