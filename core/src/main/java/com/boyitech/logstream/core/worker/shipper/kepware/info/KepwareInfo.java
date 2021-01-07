package com.boyitech.logstream.core.worker.shipper.kepware.info;

import java.util.Date;

/**
 * @author Eric
 * @Title: KepwareInfo
 * @date 2019/1/14 17:40
 * @Description: TODO
 */
public class KepwareInfo {
    private String time;
    private String LogLevel;
    private String Username;
    private String source;
    private String event;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLogLevel() {
        return LogLevel;
    }

    public void setLogLevel(String logLevel) {
        LogLevel = logLevel;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }
}
