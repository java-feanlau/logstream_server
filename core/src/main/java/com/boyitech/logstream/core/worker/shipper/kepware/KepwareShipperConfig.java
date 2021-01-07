package com.boyitech.logstream.core.worker.shipper.kepware;

import com.boyitech.logstream.core.worker.shipper.BaseShipperConfig;

import java.util.Map;

public class KepwareShipperConfig extends BaseShipperConfig {

    private final String readPath;                         //监听的目录
    private final int secondOfRead;                         //读取的间隔事件，真实含义是积累的修改时间，默认积累5秒的事件
    public KepwareShipperConfig(Map configuration) {
        super(configuration);
        readPath = (String) configuration.get("readPath");
        secondOfRead = configuration.get("secondOfRead") != null ?
                Integer.valueOf((String) configuration.get("secondOfRead")) : 5;
    }

    public String getReadPath() {
        return readPath;
    }

    public int getSecondOfRead() {
        return secondOfRead;
    }
}
