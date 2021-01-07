package com.boyitech.logstream.core.worker.shipper.netflow.utils;

import com.boyitech.logstream.core.worker.shipper.netflow.info.Flow;
import com.boyitech.logstream.core.worker.shipper.netflow.info.Netflow;
import io.netty.buffer.ByteBuf;
import io.netty.channel.socket.DatagramPacket;

/**
 * @author Eric
 * @Title: PacketToNetflow
 * @date 2018/12/25 10:31
 * @Description: TODO
 */
public class PacketToNetflowDecoder {
    private static final int V10_HEADER_SIZE = 10;
    private static final int V10_FLOW_SIZE_256 = 72;
    private static final int V10_FLOW_SIZE_258 = 68;
    private static final int V10_FLOW_SIZE_260 = 96;
    private static final int V10_FLOW_SIZE_262 = 92;

    public static Netflow change(DatagramPacket msg) {
        ByteBuf content = msg.content();
        int len = content.readableBytes();

        if (len < V10_HEADER_SIZE) {
            System.err.println("    * incomplete header *");
            return null;
        }

        Netflow netflow = new Netflow();
        byte[] buf = new byte[len];
        content.readBytes(buf);
        netflow.setVersion(ByteUtils.byte2long(buf, 0, 2));
        long length = ByteUtils.byte2long(buf, 2, 2);
        netflow.setLength(length);
        long time = ByteUtils.byte2long(buf, 4, 4);
        netflow.setTimestamp(ByteUtils.long2time(time));
        netflow.setFlowSequence(ByteUtils.byte2long(buf, 8, 4));
        netflow.setDomainId(ByteUtils.byte2long(buf, 12, 4));
        boolean flag = true;
        int index = 16;
        while (flag) {
//            netflow.setFlowSetId(ByteUtils.byte2long(buf, 16, 2));
//            netflow.setFlowSetLength(ByteUtils.byte2long(buf, 18, 2));
            long setId = ByteUtils.byte2long(buf, index, 2);
            switch ((int) setId) {
                case 256:
                    long setLength = ByteUtils.byte2long(buf, index + 2, 2);

                    int flowNum = (setLength - 4) % V10_FLOW_SIZE_256 == 0 ? (int) ((setLength - 4) / V10_FLOW_SIZE_256) : 0;
                    for (int i = 0; i < flowNum; i++) {
                        Flow flow = new Flow();
                        flow.setVersion(V10_HEADER_SIZE);
                        flow.setSetId(V10_FLOW_SIZE_256);
                        int off = index + 4 + i * V10_FLOW_SIZE_256;
                        long srcAddrL = ByteUtils.Bytes4ToLong(buf, off);
                        long dstAddrL = ByteUtils.Bytes4ToLong(buf, off + 4);
                        flow.setOctets(ByteUtils.byte2long(buf, off + 8, 8));
                        flow.setPackets(ByteUtils.byte2long(buf, off + 16, 8));
                        long startTimeL = ByteUtils.byte2long(buf, off + 24, 8);
                        long endTimeL = ByteUtils.byte2long(buf, off + 32, 8);
                        flow.setStartTime(ByteUtils.long2time(startTimeL));
                        flow.setEndTime(ByteUtils.long2time(endTimeL));
                        flow.setSrcPort(ByteUtils.byte2long(buf, off + 40, 2));
                        flow.setDstPort(ByteUtils.byte2long(buf, off + 42, 2));
                        flow.setInputInt(ByteUtils.Bytes4ToInt(buf, off + 44));
                        flow.setOutputInt(ByteUtils.Bytes4ToInt(buf, off + 48));
                        flow.setSegmentId(ByteUtils.byte2long(buf, off + 52, 8));
                        flow.setProtocol(buf[off + 60]);
                        flow.setFlowEndReason(buf[off + 61]);
                        flow.setTcpFlags(buf[off + 62]);
                        flow.setIpToS(buf[off + 63]);
                        flow.setMaxTTL(buf[off + 64]);
                        flow.setDirection(buf[off + 65]);

                        flow.setSrcAddr(ByteUtils.long2ip(srcAddrL));
                        flow.setDstAddr(ByteUtils.long2ip(dstAddrL));
                        netflow.getNormalFlows().add(flow);
                    }
                    index += setLength;
                    if (index == length) {
                        flag = false;
                    }
                    break;
                case 258:
                    long setLength1 = ByteUtils.byte2long(buf, index + 2, 2);

                    int flowNum1 = (setLength1 - 4) % V10_FLOW_SIZE_258 == 0 ? (int) ((setLength1 - 4) / V10_FLOW_SIZE_258) : 0;
                    for (int i = 0; i < flowNum1; i++) {
                        Flow flow = new Flow();
                        flow.setVersion(V10_HEADER_SIZE);
                        flow.setSetId(V10_FLOW_SIZE_258);
                        int off = index + 4 + i * V10_FLOW_SIZE_258;
                        long srcAddrL = ByteUtils.Bytes4ToLong(buf, off);
                        long dstAddrL = ByteUtils.Bytes4ToLong(buf, off + 4);
                        flow.setOctets(ByteUtils.byte2long(buf, off + 8, 8));
                        flow.setPackets(ByteUtils.byte2long(buf, off + 16, 8));
                        long startTimeL = ByteUtils.byte2long(buf, off + 24, 8);
                        long endTimeL = ByteUtils.byte2long(buf, off + 32, 8);
                        flow.setStartTime(ByteUtils.long2time(startTimeL));
                        flow.setEndTime(ByteUtils.long2time(endTimeL));
                        flow.setInputInt(ByteUtils.Bytes4ToInt(buf, off + 40));
                        flow.setOutputInt(ByteUtils.Bytes4ToInt(buf, off + 44));
//                        flow.setSrcPort(ByteUtils.byte2long(buf, off + 40, 2));
//                        flow.setDstPort(ByteUtils.byte2long(buf, off + 42, 2));
                        flow.setInputInt(ByteUtils.Bytes4ToInt(buf, off + 44));
                        flow.setOutputInt(ByteUtils.Bytes4ToInt(buf, off + 48));
                        flow.setSegmentId(ByteUtils.byte2long(buf, off + 48, 8));
                        flow.setProtocol(buf[off + 56]);
                        flow.setFlowEndReason(buf[off + 57]);
                        flow.setTcpFlags(buf[off + 58]);
                        flow.setIpToS(buf[off + 59]);
                        flow.setMaxTTL(buf[off + 60]);
                        flow.setDirection(buf[off + 61]);

                        flow.setSrcAddr(ByteUtils.long2ip(srcAddrL));
                        flow.setDstAddr(ByteUtils.long2ip(dstAddrL));
                        netflow.getNormalFlows().add(flow);
                    }
                    index += setLength1;
                    if (index == length) {
                        flag = false;
                    }
                    break;
                case 260:
                    long setLength2 = ByteUtils.byte2long(buf, index + 2, 2);

                    int flowNum2 = (setLength2 - 4) % V10_FLOW_SIZE_260 == 0 ? (int) ((setLength2 - 4) / V10_FLOW_SIZE_260) : 0;
                    for (int i = 0; i < flowNum2; i++) {
                        Flow flow = new Flow();
                        flow.setSetId(V10_FLOW_SIZE_260);
                        flow.setVersion(V10_HEADER_SIZE);
                        int off = index + 4 + i * V10_FLOW_SIZE_260;
                        long srcAddrL = ByteUtils.byte2long(buf, off,16);
                        long dstAddrL = ByteUtils.byte2long(buf, off + 16,16);
                        flow.setOctets(ByteUtils.byte2long(buf, off + 32, 8));
                        flow.setPackets(ByteUtils.byte2long(buf, off + 40, 8));
                        long startTimeL = ByteUtils.byte2long(buf, off + 48, 8);
                        long endTimeL = ByteUtils.byte2long(buf, off + 56, 8);
                        flow.setStartTime(ByteUtils.long2time(startTimeL));
                        flow.setEndTime(ByteUtils.long2time(endTimeL));
                        flow.setSrcPort(ByteUtils.byte2long(buf, off + 64, 2));
                        flow.setDstPort(ByteUtils.byte2long(buf, off + 66, 2));
                        flow.setInputInt(ByteUtils.Bytes4ToInt(buf, off + 68));
                        flow.setOutputInt(ByteUtils.Bytes4ToInt(buf, off + 72));
                        flow.setSegmentId(ByteUtils.byte2long(buf, off + 76, 8));
                        flow.setProtocol(buf[off + 84]);
                        flow.setFlowEndReason(buf[off + 85]);
                        flow.setTcpFlags(buf[off + 86]);
                        flow.setIpToS(buf[off + 87]);
                        flow.setMaxTTL(buf[off + 88]);
                        flow.setDirection(buf[off + 89]);

                        flow.setSrcAddr(ByteUtils.long2ip(srcAddrL));
                        flow.setDstAddr(ByteUtils.long2ip(dstAddrL));
                        netflow.getNormalFlows().add(flow);
                    }
                    index += setLength2;
                    if (index == length) {
                        flag = false;
                    }
                    break;
                case 262:
                    long setLength4 = ByteUtils.byte2long(buf, index + 2, 2);

                    int flowNum4 = (setLength4 - 4) % V10_FLOW_SIZE_262 == 0 ? (int) ((setLength4 - 4) / V10_FLOW_SIZE_262) : 0;
                    for (int i = 0; i < flowNum4; i++) {
                        Flow flow = new Flow();
                        flow.setSetId(V10_FLOW_SIZE_262);
                        flow.setVersion(V10_HEADER_SIZE);
                        int off = index + 4 + i * V10_FLOW_SIZE_262;
                        long srcAddrL = ByteUtils.byte2long(buf, off,16);
                        long dstAddrL = ByteUtils.byte2long(buf, off + 16,16);
                        flow.setOctets(ByteUtils.byte2long(buf, off + 32, 8));
                        flow.setPackets(ByteUtils.byte2long(buf, off + 40, 8));
                        long startTimeL = ByteUtils.byte2long(buf, off + 48, 8);
                        long endTimeL = ByteUtils.byte2long(buf, off + 56, 8);
                        flow.setStartTime(ByteUtils.long2time(startTimeL));
                        flow.setEndTime(ByteUtils.long2time(endTimeL));
                       // flow.setSrcPort(ByteUtils.byte2long(buf, off + 64, 2));
                       // flow.setDstPort(ByteUtils.byte2long(buf, off + 66, 2));
                        flow.setInputInt(ByteUtils.Bytes4ToInt(buf, off + 64));
                        flow.setOutputInt(ByteUtils.Bytes4ToInt(buf, off + 68));
                        flow.setSegmentId(ByteUtils.byte2long(buf, off + 72, 8));
                        flow.setProtocol(buf[off + 80]);
                        flow.setFlowEndReason(buf[off + 81]);
                        flow.setTcpFlags(buf[off + 82]);
                        flow.setIpToS(buf[off + 83]);
                        flow.setMaxTTL(buf[off + 84]);
                        flow.setDirection(buf[off + 85]);

                        flow.setSrcAddr(ByteUtils.long2ip(srcAddrL));
                        flow.setDstAddr(ByteUtils.long2ip(dstAddrL));
                        netflow.getNormalFlows().add(flow);
                    }
                    index += setLength4;
                    if (index == length) {
                        flag = false;
                    }
                    break;
                case 2:
                    long setLength3 = ByteUtils.byte2long(buf, index + 2, 2);
                    index += setLength3;
                    if (index == length) {
                        flag = false;
                    }
                    break;
                default:
                    //忽略其他格式的信息，如果想测试是否还有其他格式，可以在此处返回null
                    long setLength5 = ByteUtils.byte2long(buf, index + 2, 2);
                    index += setLength5;
                    if (index == length) {
                        flag = false;
                    }
                    break;
            }
        }
        return netflow;
    }
}