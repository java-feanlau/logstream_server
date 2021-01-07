package com.boyitech.logstream.core.worker.shipper.file;


import com.boyitech.logstream.core.worker.shipper.BaseShipperConfig;

import java.util.ArrayList;
import java.util.Map;

public class FileShipperConfig extends BaseShipperConfig {

    private final String readPath;              //监听的目录
    private final int threadPollMax;              //线程池的最大值,
    private final Boolean ignoreOld;              //是否忽略老日志
    private final int saveOffsetTime;             //多少秒记录一次偏移量，默认5秒
    private final Long ignoreFileOfTime;          //程序启动的时候，在不忽略老日志的情况下，忽略多少秒没更新的日志 24小时
    private final String fileNameMatch;           //文件通配符
//    private final String linesPattern;            //多行正则表达式
//    private final int MaxLineNum;                //多行匹配的最大行数
    private final int MaxLineSize;               //一行的最大字节数
    private final String encoding;               //编码格式
    private final int secondOfRead;               //读取的间隔事件，真实含义是积累的修改时间，默认积累5秒的事件

    public FileShipperConfig(Map configuration) {
        super(configuration);
        readPath = ((ArrayList<String>) configuration.get("readPath")).get(0);
        threadPollMax = configuration.get("threadPollMax") != null ?
                Integer.valueOf(configuration.get("threadPollMax").toString()) : 1;
        ignoreOld = configuration.get("ignoreOld") != null ?
                Boolean.valueOf(configuration.get("ignoreOld").toString()) : false;
        saveOffsetTime = configuration.get("saveOffsetTime") != null ?
                Integer.valueOf(configuration.get("saveOffsetTime").toString()) : 5;
        ignoreFileOfTime = configuration.get("ignoreFileOfTime") != null ?
                Long.valueOf(configuration.get("ignoreFileOfTime").toString()) : 864000L;
        fileNameMatch = configuration.get("fileNameMatch") !=null?
                configuration.get("fileNameMatch").toString() : "*";
//        linesPattern = configuration.get("linesPattern").toString();
//        MaxLineNum = configuration.get("MaxLineNum") != null ?
//                Integer.valueOf(configuration.get("MaxLineNum").toString()) : 50;
        MaxLineSize = configuration.get("MaxLineSize") != null ?
                Integer.valueOf(configuration.get("MaxLineSize").toString()) : 4096;
        encoding = configuration.get("encoding") != null ?
                configuration.get("encoding").toString() : "utf8";
        secondOfRead = configuration.get("secondOfRead") != null ?
                Integer.valueOf(configuration.get("secondOfRead").toString()) : 5;
    }

    public String getReadPath() {
        return readPath;
    }

    public int getThreadPollMax() {
        return threadPollMax;
    }

    public Boolean getIgnoreOld() {
        return ignoreOld;
    }

    public int getSaveOffsetTime() {
        return saveOffsetTime;
    }

    public long getIgnoreFileOfTime() {
        return ignoreFileOfTime;
    }

    public String getFileNameMatch() {
        return fileNameMatch;
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
