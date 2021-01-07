package com.boyitech.logstream.core.worker.shipper.snmp;

import com.boyitech.logstream.core.cache.BaseCache;
import com.boyitech.logstream.core.factory.CacheFactory;
import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.worker.shipper.BaseShipper;
import com.boyitech.logstream.core.worker.shipper.BaseShipperConfig;
import com.google.common.collect.Lists;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SnmpShipper extends BaseShipper {
    private final SnmpShipperConfig config;
    private final int DEFAULT_VERSION;
    private final String DEFAULT_PROTOCOL = "udp";
    private final int DEFAULT_PORT;
    private final long DEFAULT_TIMEOUT = 3 * 1000L;
    private final int DEFAULT_RETRY = 3;
    private final int LOOP_TIME;
    ScheduledExecutorService service;
    Snmp snmp = null;
    CommunityTarget target = null;

    public SnmpShipper(BaseShipperConfig config) {
        super(config);
        this.config = (SnmpShipperConfig) config;
        DEFAULT_PORT = this.config.getTargetPort();
        LOOP_TIME = this.config.getLoopSecond();
        String version = this.config.getVersion();
        switch (version) {
            case "1":
                DEFAULT_VERSION = SnmpConstants.version1;
                break;
            case "2":
                DEFAULT_VERSION = SnmpConstants.version2c;
                break;
            case "3":
                DEFAULT_VERSION = SnmpConstants.version3;
                break;
            default:
                DEFAULT_VERSION = SnmpConstants.version2c;
        }
    }

    public SnmpShipper(String porterID, BaseShipperConfig config) {
        super(porterID, config);
        this.config = (SnmpShipperConfig) config;
        DEFAULT_PORT = this.config.getTargetPort();
        LOOP_TIME = this.config.getLoopSecond();
        String version = this.config.getVersion();
        switch (version) {
            case "1":
                DEFAULT_VERSION = SnmpConstants.version1;
                break;
            case "2":
                DEFAULT_VERSION = SnmpConstants.version2c;
                break;
            case "3":
                DEFAULT_VERSION = SnmpConstants.version3;
                break;
            default:
                DEFAULT_VERSION = SnmpConstants.version2c;
        }
    }

    @Override
    public boolean register() {
        String model = config.getMode();
        String ip = config.getTargetIp();
        String community = config.getCommunity();
        String oid = config.getOid();

//        try {
        target = createDefault(ip, community);
//            DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
//            snmp = new Snmp(transport);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(() -> {
            switch (model) {
                case "1":
                    String s = oid.replaceAll("，", ",");
                    String[] split = s.split(",");
                    ArrayList<String> oidList = Lists.newArrayList(split);
                    snmpGetList(ip, community, oidList);
                    break;
                case "2":
                    snmpWalk(ip, community, oid);
            }
        }, 2, LOOP_TIME, TimeUnit.SECONDS);
        return true;
    }

    @Override
    public void tearDown() {
        if (service != null) {
            service.shutdown();
        }
//        try {
//            if (snmp != null) {
//                snmp.close();
//            }
//        } catch (IOException e) {
//            LOGGER.error(e);
//            addException(e.toString());
//        }
    }

    @Override
    public void execute() throws InterruptedException, IOException {


    }


    /*根据OID列表，一次获取多条OID数据，并且以List形式返回*/
    public void snmpGetList(String ip, String community, ArrayList<String> oidList) {
        try {
            DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            PDU pdu = new PDU();

            for (String oid : oidList) {
                try {
                    pdu.add(new VariableBinding(new OID(oid)));
                } catch (NumberFormatException e) {
                    LOGGER.error("oid格式有误：" + oid);
                    addException("oid格式有误：" + oid);
                }
            }


            snmp.listen();
            pdu.setType(PDU.GET);
            ResponseEvent respEvent = snmp.send(pdu, target);
            PDU response = respEvent.getResponse();
            if (response == null) {
                LOGGER.error("响应超时：目标ip，目标端口或者团体名配置错误");
                addException("响应超时：目标ip，目标端口或者团体名配置错误");
            } else {
                for (int i = 0; i < response.size(); i++) {
                    VariableBinding vb = response.get(i);
                    if (vb.isException() == true) {
                        LOGGER.error("获取异常，指定的oid没有数据:" + vb.getOid());
                        addException("获取异常，指定的oid没有数据:" + vb.getOid());
                    }
                    Event event = new Event();
                    event.setIndex(config.getIndex());
                    event.setSource(ip);
                    if (this.mark != null) {
                        event.setMark(this.getMark());
                    }
                    event.setMsgType("snmp_shipper");
                    event.setMessage(
                            "{\"oid\":\"" + vb.getOid() + "\"," +
                                    "\"variable\":\"" + vb.getVariable() + "\"," +
                                    "\"syntax\":\"" + vb.getSyntax() + "\"" +
                                    "}");
                    count.addAndGet(1);
//                    System.out.println(event.getMessage());
                    lv1Cache.put(event);
                }

            }
        } catch (Exception e) {
            LOGGER.error("SNMP Get Exception:" + e);
            addException("SNMP Get Exception:" + e);
        } finally {
            if (snmp != null) {
                try {
                    snmp.close();
                } catch (IOException ex1) {
                    snmp = null;
                }
            }

        }
    }


    /*根据OID，获取单条消息*/
    public void snmpGet(String ip, String community, String oid) {
//        CommunityTarget target = createDefault(ip, community);

        try {

            DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            PDU pdu = new PDU();
            // pdu.add(new VariableBinding(new OID(new int[]
            // {1,3,6,1,2,1,1,2})));
            pdu.add(new VariableBinding(new OID(oid)));

            snmp.listen();
            pdu.setType(PDU.GET);
            ResponseEvent respEvent = snmp.send(pdu, target);
            PDU response = respEvent.getResponse();
            if (response == null) {
                LOGGER.error("响应超时：目标ip，目标端口或者团体名配置错误");
                addException("响应超时：目标ip，目标端口或者团体名配置错误");
            } else {
                int size = response.size();
                for (int i = 0; i < size; i++) {
                    VariableBinding vb = response.get(i);
                    if (vb.isException() == true) {
                        LOGGER.error("获取异常，指定的oid没有数据:" + vb.getOid());
                        addException("获取异常，指定的oid没有数据:" + vb.getOid());
                        continue;
                    }
                    Event event = new Event();
                    event.setIndex(config.getIndex());
                    event.setSource(ip);
                    if (this.mark != null) {
                        event.setMark(this.getMark());
                    }
                    event.setMsgType("snmp_shipper");
                    event.setMessage(
                            "{\"oid\":\"" + vb.getOid() + "\"," +
                                    "\"variable\":\"" + vb.getVariable() + "\"," +
                                    "\"syntax\":\"" + vb.getSyntax() + "\"" +
                                    "}");
                    count.addAndGet(1);
                    lv1Cache.put(event);
                }

            }
        } catch (Exception e) {
            LOGGER.error("SNMP Get Exception:" + e);
            addException("SNMP Get Exception:" + e);
        } finally {
            if (snmp != null) {
                try {
                    snmp.close();
                } catch (IOException ex1) {
                    snmp = null;
                }
            }

        }
    }

    /*根据targetOID，获取树形数据*/
    public void snmpWalk(String ip, String community, String targetOid) {
        CommunityTarget target = createDefault(ip, community);
        TransportMapping transport;
        Snmp snmp = null;
        try {
            transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            snmp.listen();
            PDU pdu = new PDU();
            OID targetOID = new OID(targetOid);
            pdu.add(new VariableBinding(targetOID));
            boolean finished = false;
            int num = 1;
            while (!finished) {
                VariableBinding vb = null;
                ResponseEvent respEvent = snmp.getNext(pdu, target);
                PDU response = respEvent.getResponse();
                if (null == response) {
                    LOGGER.error("响应超时：目标ip，目标端口或者团体名配置错误");
                    addException("响应超时：目标ip，目标端口或者团体名配置错误");
                    break;
                } else {
                    vb = response.get(0);
                }
                // check finish
                finished = checkWalkFinished(targetOID, pdu, vb);
                if (finished) {
                    if (num == 1) {
                        LOGGER.error("获取异常，指定的oid没有子节点数据:" + vb.getOid());
                        addException("获取异常，指定的oid没有子节点数据:" + vb.getOid());
                    }
                }
                if (!finished) {
                    Event event = new Event();
                    event.setIndex(config.getIndex());
                    event.setSource(ip);
                    if (this.mark != null) {
                        event.setMark(this.getMark());
                    }
                    event.setMsgType("snmp_shipper");

                    event.setMessage(
                            "{\"oid\":\"" + vb.getOid() + "\"," +
                                    "\"variable\":\"" + vb.getVariable() + "\"," +
                                    "\"syntax\":\"" + vb.getSyntax() + "\"" +
                                    "}");
                    count.addAndGet(1);
                    lv1Cache.put(event);
                    num++;
//                    System.out.println(event.getFormat());
                    ;
                    // Set up the variable binding for the next entry.
                    pdu.setRequestID(new Integer32(0));
                    pdu.set(0, vb);
                }
            }
        }catch (NumberFormatException e1){
            LOGGER.error("oid格式有误：" + targetOid);
            addException("oid格式有误：" + targetOid);
        }
        catch (Exception e) {
            LOGGER.error("SNMP Get Exception:" + e);
            addException("SNMP Get Exception:" + e);
        } finally {
            if (snmp != null) {
                try {
                    snmp.close();
                } catch (IOException ex1) {
                    snmp = null;  //help GC
                }
            }
        }
    }

    /**
     * 创建对象communityTarget，用于返回target
     */
    public CommunityTarget createDefault(String ip, String community) {
        Address address = GenericAddress.parse(DEFAULT_PROTOCOL + ":" + ip
                + "/" + DEFAULT_PORT);
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(community));
        target.setAddress(address);
        target.setVersion(DEFAULT_VERSION);
        target.setTimeout(DEFAULT_TIMEOUT); // milliseconds
        target.setRetries(DEFAULT_RETRY);
        return target;
    }

    private boolean checkWalkFinished(OID targetOID, PDU pdu,
                                      VariableBinding vb) {
        boolean finished = false;
        if (pdu.getErrorStatus() != 0) {
            LOGGER.error(pdu.getErrorStatusText());
            addException(pdu.getErrorStatusText());
            finished = true;
        } else if (vb.getOid() == null) {
            LOGGER.error("oid为空");
            addException("oid为空");
            finished = true;
        } else if (vb.getOid().size() < targetOID.size()) {
            LOGGER.error("vb.getOid().size() < targetOID.size()");
            addException("vb.getOid().size() < targetOID.size()");
            finished = true;
        } else if (Null.isExceptionSyntax(vb.getVariable().getSyntax())) {
            //提取到了最后一子节点
//            LOGGER.error("Null.isExceptionSyntax(vb.getVariable().getSyntax())");
//            addException("Null.isExceptionSyntax(vb.getVariable().getSyntax())");
            finished = true;
        } else if (targetOID.leftMostCompare(targetOID.size(), vb.getOid()) != 0) {
            //一批次取完了
            finished = true;
        } else if (vb.getOid().compareTo(targetOID) <= 0) {
            LOGGER.error("Variable received is not lexicographic successor of requested one:" + vb.toString() + "<=" + targetOID);
            addException("Variable received is not lexicographic successor of requested one:" + vb.toString() + "<=" + targetOID);
            finished = true;
        }
        return finished;

    }

    public static void main(String args[]) throws InterruptedException {
        String json = "{\"targetIp\":\"192.168.100.253\",\"targetPort\":\"161\",\"community\":\"public\",\"oid\":\"1.3.6.1.4.1.248.16.100.2.9.\",\"loopSecond\":\"5\",\"mode\":\"2\",\"version\":\"2\",\"moduleType\":\"snmp\",\"index\":\"snmp-dc4e-\"}";
        Map<String, String> map = GsonHelper.fromJson(json);
        BaseCache cache = CacheFactory.createCache();
        SnmpShipperConfig snmpShipperConfig = new SnmpShipperConfig(map);
        SnmpShipper snmpShipper = new SnmpShipper(snmpShipperConfig);
        snmpShipper.setLv1Cache(cache);
        snmpShipper.doStart();
        while (true) {
            System.out.println(cache.take(1));
        }


//        List<String> objects = new ArrayList<>();
//        objects.add("1.0.8802.1.1.1.1.1.2.1.4.1");
//        objects.add("1.0.8802.1.1.1.1.1.2.1.3.10");
//        objects.add("1.0.8802.1.1.1.1.1.2.1.3.9");
//        snmpShipper.register();
//        snmpShipper.snmpGetList("1.1.1.1", null, objects);
    }
}
