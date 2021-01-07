package com.boyitech.logstream.core.worker.shipper.nmap_xml.info;

/**
 * @author Eric
 * @Title: Nmap
 * @date 2019/1/28 15:21
 * @Description: TODO
 */
public class Nmap {
    private String version;
    private String arg;
    private String formatStartTime;
    private String formatEndTime;
    private String hostState;
    private String state;
    private String reason;
    private String reasonTtl;
    private String ipv4Host;
    private String macHost;
    private String vendor;
    private String protocol;
    private String port;
    private String portState;
    private String portreason;
    private String portReasonTtl;
    private String name;
    private String method;
    private String conf;
    private String product;
    private String elapsed;
    private String summary;
    private String exit;


    public String getElapsed() {
        return elapsed;
    }

    public void setElapsed(String elapsed) {
        this.elapsed = elapsed;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getExit() {
        return exit;
    }

    public void setExit(String exit) {
        this.exit = exit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getConf() {
        return conf;
    }

    public void setConf(String conf) {
        this.conf = conf;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getHostState() {
        return hostState;
    }

    public void setHostState(String hostState) {
        this.hostState = hostState;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getIpv4Host() {
        return ipv4Host;
    }

    public void setIpv4Host(String ipv4Host) {
        this.ipv4Host = ipv4Host;
    }

    public String getMacHost() {
        return macHost;
    }

    public void setMacHost(String macHost) {
        this.macHost = macHost;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getArg() {
        return arg;
    }

    public void setArg(String arg) {
        this.arg = arg;
    }

    public String getFormatStartTime() {
        return formatStartTime;
    }

    public void setFormatStartTime(String formatStartTime) {
        this.formatStartTime = formatStartTime;
    }

    public String getFormatEndTime() {
        return formatEndTime;
    }

    public void setFormatEndTime(String formatEndTime) {
        this.formatEndTime = formatEndTime;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getReasonTtl() {
        return reasonTtl;
    }

    public void setReasonTtl(String reasonTtl) {
        this.reasonTtl = reasonTtl;
    }

    public String getPortState() {
        return portState;
    }

    public void setPortState(String portState) {
        this.portState = portState;
    }

    public String getPortreason() {
        return portreason;
    }

    public void setPortreason(String portreason) {
        this.portreason = portreason;
    }

    public String getPortReasonTtl() {
        return portReasonTtl;
    }

    public void setPortReasonTtl(String portReasonTtl) {
        this.portReasonTtl = portReasonTtl;
    }
}
