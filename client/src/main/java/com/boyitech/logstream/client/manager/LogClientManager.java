package com.boyitech.logstream.client.manager;

public interface LogClientManager {

    void handleHeartBeatResponse(String content);

    void handleUpdateConfigResponse(String heartConfig);

    String getClientShipperStatus();
}
