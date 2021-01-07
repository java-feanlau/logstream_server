package com.boyitech.logstream.core.worker.shipper.netflow.info;


import com.boyitech.logstream.core.worker.shipper.netflow.utils.ByteUtils;

/**
 * @author Eric
 * @Title: Flow
 * @date 2018/12/19 11:24
 * @Description: TODO
 */
public class Flow {
    long setId;                //2
    String srcAddr;               //4
    String dstAddr;               //4
    long octets;                //8
    long packets;              //8
    String startTime;            //8
    String endTime;               //8
    long srcPort;               //2
    long dstPort;                //2
    long inputInt;              //4
    long outputInt;            //4
    long segmentId;            //8
    byte protocol;               //1
    byte flowEndReason;        //1
    byte tcpFlags;              //1
    byte ipToS;                 //1
    byte MaxTTL;                 //1
    byte Direction;             //1

    //添加一个version
    long version;

    public Flow() {
    }

    public Flow(byte[] buf, int off) {
        long srcAddrL = ByteUtils.Bytes4ToLong(buf, off);
        long dstAddrL = ByteUtils.Bytes4ToLong(buf, off + 4);
        this.octets = ByteUtils.byte2long(buf, off + 8, 8);
        this.packets = ByteUtils.byte2long(buf, off + 16, 8);
        long startTimeL = ByteUtils.byte2long(buf, off + 24, 8);
        long endTimeL = ByteUtils.byte2long(buf, off + 32, 8);
        this.startTime = ByteUtils.long2time(startTimeL);
        this.endTime = ByteUtils.long2time(endTimeL);
        this.srcPort = ByteUtils.byte2long(buf, off + 40, 2);
        this.dstPort = ByteUtils.byte2long(buf, off + 42, 2);
        this.inputInt = ByteUtils.Bytes4ToInt(buf, off + 44);
        this.outputInt = ByteUtils.Bytes4ToInt(buf, off + 48);
        this.segmentId = ByteUtils.byte2long(buf, off + 52, 8);
        this.protocol = buf[off + 60];
        this.flowEndReason = buf[off + 61];
        this.tcpFlags = buf[off + 62];
        this.ipToS = buf[off + 63];
        this.MaxTTL = buf[off + 64];
        this.Direction = buf[off + 65];

        this.srcAddr = ByteUtils.long2ip(srcAddrL);
        this.dstAddr = ByteUtils.long2ip(dstAddrL);

    }


    public String getSrcAddr() {
        return srcAddr;
    }

    public void setSrcAddr(String srcAddr) {
        this.srcAddr = srcAddr;
    }

    public String getDstAddr() {
        return dstAddr;
    }

    public void setDstAddr(String dstAddr) {
        this.dstAddr = dstAddr;
    }

    public long getOctets() {
        return octets;
    }

    public void setOctets(long octets) {
        this.octets = octets;
    }

    public long getPackets() {
        return packets;
    }

    public void setPackets(long packets) {
        this.packets = packets;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public long getSrcPort() {
        return srcPort;
    }

    public void setSrcPort(long srcPort) {
        this.srcPort = srcPort;
    }

    public long getDstPort() {
        return dstPort;
    }

    public void setDstPort(long dstPort) {
        this.dstPort = dstPort;
    }

    public long getInputInt() {
        return inputInt;
    }

    public void setInputInt(long inputInt) {
        this.inputInt = inputInt;
    }

    public long getOutputInt() {
        return outputInt;
    }

    public void setOutputInt(long outputInt) {
        this.outputInt = outputInt;
    }

    public long getSegmentId() {
        return segmentId;
    }

    public void setSegmentId(long segmentId) {
        this.segmentId = segmentId;
    }

    public byte getProtocol() {
        return protocol;
    }

    public void setProtocol(byte protocol) {
        this.protocol = protocol;
    }

    public byte getFlowEndReason() {
        return flowEndReason;
    }

    public void setFlowEndReason(byte flowEndReason) {
        this.flowEndReason = flowEndReason;
    }

    public byte getTcpFlags() {
        return tcpFlags;
    }

    public void setTcpFlags(byte tcpFlags) {
        this.tcpFlags = tcpFlags;
    }

    public byte getIpToS() {
        return ipToS;
    }

    public void setIpToS(byte ipToS) {
        this.ipToS = ipToS;
    }

    public byte getMaxTTL() {
        return MaxTTL;
    }

    public void setMaxTTL(byte maxTTL) {
        MaxTTL = maxTTL;
    }

    public byte getDirection() {
        return Direction;
    }

    public void setDirection(byte direction) {
        Direction = direction;
    }

    public long getSetId() {
        return setId;
    }

    public void setSetId(long setId) {
        this.setId = setId;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
