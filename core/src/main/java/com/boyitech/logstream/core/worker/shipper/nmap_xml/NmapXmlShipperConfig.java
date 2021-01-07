package com.boyitech.logstream.core.worker.shipper.nmap_xml;

import com.boyitech.logstream.core.worker.shipper.BaseShipperConfig;

import java.util.Map;

public class NmapXmlShipperConfig extends BaseShipperConfig {

    private final String readPath;              //监听的目录
    private final int threadPollMax;              //线程池的最大值,
    private final int MaxLineSize;               //一行的最大字节数
    private final String encoding;               //编码格式
    private final int secondOfRead;               //读取的间隔事件，真实含义是积累的修改时间，默认积累5秒的事件

    public NmapXmlShipperConfig(Map configuration) {
        super(configuration);
        readPath = (String) configuration.get("readPath");
        threadPollMax = configuration.get("threadPollMax") != null ?
                Integer.valueOf((String) configuration.get("threadPollMax")) : 1;
        MaxLineSize = configuration.get("MaxLineSize") != null ?
                Integer.valueOf((String) configuration.get("MaxLineSize")) : 4096;
        encoding = configuration.get("encoding") != null ?
                (String) configuration.get("encoding") : "utf8";
        secondOfRead = configuration.get("secondOfRead") != null ?
                Integer.valueOf((String) configuration.get("secondOfRead")) : 5;
    }

    public String getReadPath() {
        return readPath;
    }

    public int getThreadPollMax() {
        return threadPollMax;
    }


    public int getMaxLineSize() {
        return MaxLineSize;
    }

    public String getEncoding() {
        return encoding;
    }

    public int getSecondOfRead() {
        return secondOfRead;
    }

    public Map<String, String> getMultilineRule() {
        return multilineRule;
    }

    public void setMultilineRule(Map<String, String> multilineRule) {
        this.multilineRule = multilineRule;
    }
}
