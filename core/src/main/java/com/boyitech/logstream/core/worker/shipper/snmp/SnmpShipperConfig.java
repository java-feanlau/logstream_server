package com.boyitech.logstream.core.worker.shipper.snmp;

import com.boyitech.logstream.core.worker.shipper.BaseShipperConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Eric
 * @Title: SnmpShipperConfig
 * @date 2019/8/21 9:16
 * @Description: TODO
 */

public class SnmpShipperConfig extends BaseShipperConfig {
    private String targetIp;
    private int targetPort;
    private String community;
    private String oid;     //配置错误将获取不了数据，模式1可以通过getExecetionByWorkId获取异常信息。模式2无法获取异常信息。
    private int loopSecond;  //如果执行时间大于周期时间，周期时间会被延后，不会并发执行。
    private String mode;
    private String version; //1,2,3


    public SnmpShipperConfig(Map config) {
        super(config);
        targetIp = (String) config.get("targetIp");
        if (config.get("targetPort") == null || config.get("targetPort").equals("")  ) {
            targetPort = 161;
        } else {
            targetPort = Integer.valueOf(config.get("targetPort").toString());
        }
        community = (String) config.get("community");
        oid = (String) config.get("oid");
        mode = String.valueOf(config.get("mode"));
        if (config.get("loopSecond") == null || config.get("loopSecond").equals("")  ) {
            loopSecond = 60;
        } else {
            loopSecond = Integer.valueOf(config.get("loopSecond").toString());
        }
        version = config.get("version").toString();
    }


    public int getTargetPort() {
        return targetPort;
    }

    public void setTargetPort(int targetPort) {
        this.targetPort = targetPort;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getLoopSecond() {
        return loopSecond;
    }

    public void setLoopSecond(int loopSecond) {
        this.loopSecond = loopSecond;
    }

    public String getTargetIp() {
        return targetIp;
    }

    public void setTargetIp(String targetIp) {
        this.targetIp = targetIp;
    }

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
