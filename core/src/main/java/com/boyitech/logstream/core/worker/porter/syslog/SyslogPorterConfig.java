package com.boyitech.logstream.core.worker.porter.syslog;

import com.boyitech.logstream.core.worker.porter.BasePorterConfig;

import java.util.Map;

public class SyslogPorterConfig extends BasePorterConfig {
    private String ip;
    private String port;

    public SyslogPorterConfig(Map config) {
        super(config);
        ip = (String) config.get("ip");
        port = (String) config.get("port");
    }

    public String getIp() {
        return ip;
    }

    public String getPort() {
        return port;
    }
}
