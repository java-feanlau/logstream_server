package com.boyitech.logstream.core.worker.shipper.netflow;

import com.boyitech.logstream.core.worker.shipper.BaseShipperConfig;

import java.util.Map;

public class NetflowShipperConfig extends BaseShipperConfig {

    private int port;
    private String host;
    private String version;

    public NetflowShipperConfig(Map configuration) {
        super(configuration);
        port = configuration.get("port") != null ?
                Integer.valueOf((String) configuration.get("port")) : 9996;
        host = (String) configuration.get("host");
        version=String.valueOf(configuration.get("version"));

    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
