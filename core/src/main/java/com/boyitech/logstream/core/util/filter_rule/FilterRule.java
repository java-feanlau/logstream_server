package com.boyitech.logstream.core.util.filter_rule;

import java.util.Map;
import java.util.Objects;

import javax.management.InvalidAttributeValueException;

import com.boyitech.logstream.core.info.InetInfo;

public class FilterRule {

    private FilterRuleInterface srcAddr = new EmptyRule();
    private FilterRuleInterface srcPort = new EmptyRule();
    private FilterRuleInterface dstAddr = new EmptyRule();
    private FilterRuleInterface dstPort = new EmptyRule();
    private FilterRuleInterface protocol = new EmptyRule();
    private int priority = 9;

    public FilterRule(Map<String, String> args) throws InvalidAttributeValueException {
        if (args != null) {
            if (args.get("srcAddr") != null) {
                this.srcAddr = new IpRangeRule(args.get("srcAddr"));
            }
            if (args.get("srcPort") != null) {
                this.srcPort = new PortRule(args.get("srcPort"));
            }
            if (args.get("dstAddr") != null) {
                this.dstAddr = new IpRangeRule(args.get("dstAddr"));
            }
            if (args.get("dstPort") != null) {
                this.dstPort = new PortRule(args.get("dstPort"));
            }
            if (args.get("protocol") != null) {
                this.protocol = new ProtocolRule(args.get("protocol"));
            }
        }

    }


    public boolean contains(InetInfo inetInfo) {
        if (!protocol.in(inetInfo.getProtocol())) {
            return false;
        }
        if (!srcAddr.in(inetInfo.getSrcAddr())) {
            return false;
        }
        if (!srcPort.in(inetInfo.getSrcPort())) {
            return false;
        }
        if (!dstAddr.in(inetInfo.getDstAddr())) {
            return false;
        }
        if (!dstPort.in(inetInfo.getDstPort())) {
            return false;
        }
        return true;
    }


    public FilterRuleInterface getSrcAddr() {
        return srcAddr;
    }

    public void setSrcAddr(FilterRuleInterface srcAddr) {
        this.srcAddr = srcAddr;
    }

    public FilterRuleInterface getSrcPort() {
        return srcPort;
    }

    public void setSrcPort(FilterRuleInterface srcPort) {
        this.srcPort = srcPort;
    }

    public FilterRuleInterface getDstAddr() {
        return dstAddr;
    }

    public void setDstAddr(FilterRuleInterface dstAddr) {
        this.dstAddr = dstAddr;
    }

    public FilterRuleInterface getDstPort() {
        return dstPort;
    }

    public void setDstPort(FilterRuleInterface dstPort) {
        this.dstPort = dstPort;
    }

    public FilterRuleInterface getProtocol() {
        return protocol;
    }

    public void setProtocol(FilterRuleInterface protocol) {
        this.protocol = protocol;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FilterRule)) return false;
        FilterRule that = (FilterRule) o;
        return priority == that.priority &&
                Objects.equals(srcAddr, that.srcAddr) &&
                Objects.equals(srcPort, that.srcPort) &&
                Objects.equals(dstAddr, that.dstAddr) &&
                Objects.equals(dstPort, that.dstPort) ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(srcAddr, srcPort, dstAddr, dstPort);
    }
}
