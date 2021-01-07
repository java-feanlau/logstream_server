package com.boyitech.logstream.core.worker.shipper.netflow.info;


import com.boyitech.logstream.core.worker.shipper.netflow.utils.ByteUtils;

import java.util.ArrayList;

/**
 * @author Eric
 * @Title: netflow
 * @date 2018/12/19 10:46
 * @Description: TODO
 */
public class Netflow {
    private long version;
    private long length;
    private String Timestamp;
    private long flowSequence;
    private long domainId;
    private long flowSetId;
    private long flowSetLength;
    private ArrayList<Flow> normalFlows = new ArrayList<>();
    private static final int V10_HEADER_SIZE = 20;
    private static final int V10_FLOW_SIZE = 72;

    public Netflow() {
    }

    public Netflow(byte[] buf, int len) {

        if (len < V10_HEADER_SIZE) {
            System.err.println("    * incomplete header *");
            return;
        }
        this.version = ByteUtils.byte2long(buf, 0, 2);
        this.length = ByteUtils.byte2long(buf, 2, 2);
        long time = ByteUtils.byte2long(buf, 4, 4);
        this.Timestamp = ByteUtils.long2time(time);
        this.flowSequence = ByteUtils.byte2long(buf, 8, 4);
        this.domainId = ByteUtils.byte2long(buf, 12, 4);
        this.flowSetId = ByteUtils.byte2long(buf, 16, 2);
        this.flowSetLength = ByteUtils.byte2long(buf, 18, 2);
        int flowNum = (len - V10_HEADER_SIZE) % V10_FLOW_SIZE == 0 ? (len - V10_HEADER_SIZE) / V10_FLOW_SIZE : 0;
        // System.out.println(ByteUtils.byte2long(buf, 20, 4));
        //  System.out.println(Utils.Bytes4ToInt(buf, 20 )+"~~~~~~~~~~~~~~~~``");
        for (int i = 0; i < flowNum; i++) {
            Flow flow = new Flow(buf, V10_HEADER_SIZE + i * V10_FLOW_SIZE);
            normalFlows.add(flow);
        }

    }


    public String getTimestamp() {
        return Timestamp;
    }

    public void setTimestamp(String timestamp) {
        Timestamp = timestamp;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public long getFlowSequence() {
        return flowSequence;
    }

    public void setFlowSequence(long flowSequence) {
        this.flowSequence = flowSequence;
    }

    public long getDomainId() {
        return domainId;
    }

    public void setDomainId(long domainId) {
        this.domainId = domainId;
    }

    public long getFlowSetId() {
        return flowSetId;
    }

    public void setFlowSetId(long flowSetId) {
        this.flowSetId = flowSetId;
    }

    public long getFlowSetLength() {
        return flowSetLength;
    }

    public void setFlowSetLength(long flowSetLength) {
        this.flowSetLength = flowSetLength;
    }

    public ArrayList<Flow> getNormalFlows() {
        return normalFlows;
    }

    public void setNormalFlows(ArrayList<Flow> normalFlows) {

        this.normalFlows = normalFlows;
    }
}
