package com.boyitech.logstream.core.info;

import com.boyitech.logstream.core.info.exception.ExceptionInfo;
import com.boyitech.logstream.core.setting.SystemSettings;
import com.boyitech.logstream.core.util.GsonHelper;

import java.util.Map;

public class ClientShipperStatus {

    private String ShipperID;
//    @Expose
//    private String logType;

    private Map shipperConfig; // 采集任务配置信息

    private boolean isRunning = false; // 运行状态

    private ExceptionInfo[] exceptions = new ExceptionInfo[SystemSettings.EXCEPTIONSlENGTH.getValue()]; //最近十条的异常信息


    public ClientShipperStatus(Map config) {
        this.ShipperID = (String) config.get("shipperID");
        // TODO 对配置是否正确进行校验
        this.shipperConfig = config;
    }


    public String getShipperID() {
        return ShipperID;
    }

    public void setShipperID(String shipperID) {
        ShipperID = shipperID;
    }


    public String getShipperConfig() {

        return GsonHelper.toJson(shipperConfig);
    }

    public void setShipperConfig(Map shipperConfig) {
        this.shipperConfig = shipperConfig;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public ExceptionInfo[] getExceptions() {
        return exceptions;
    }

    public void setExceptions(ExceptionInfo[] exceptions) {
        this.exceptions = exceptions;
    }


}
