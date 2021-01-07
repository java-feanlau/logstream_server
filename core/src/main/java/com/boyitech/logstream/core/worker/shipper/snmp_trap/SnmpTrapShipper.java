package com.boyitech.logstream.core.worker.shipper.snmp_trap;

import com.boyitech.logstream.core.cache.BaseCache;
import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.filter_rule.FilterRuler;
import com.boyitech.logstream.core.worker.shipper.BaseShipper;
import com.boyitech.logstream.core.worker.shipper.BaseShipperConfig;
import org.snmp4j.*;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.security.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;

import java.io.IOException;
import java.util.*;

public class SnmpTrapShipper extends BaseShipper implements CommandResponder {

    private ThreadPool threadPool;
    private SnmpTrapShipperConfig trapConfig;
    private Snmp snmp = null;
    private TransportMapping<?> transport;
    // 分配索引的规则
    private FilterRuler ruler;

    public SnmpTrapShipper(BaseShipperConfig config) {
        super(config);
        trapConfig = (SnmpTrapShipperConfig) config;
        // 创建根据五元组分配索引的对象
        ruler = new FilterRuler(trapConfig.getFilterRuleList());
    }

    public SnmpTrapShipper(String shipperID, BaseShipperConfig config) {
        super(shipperID, config);
        trapConfig = (SnmpTrapShipperConfig) config;
        // 创建根据五元组分配索引的对象
        ruler = new FilterRuler(trapConfig.getFilterRuleList());
    }

    @Override
    public boolean register() {
        threadPool = ThreadPool.create("Trap", trapConfig.getThreadNum());
        MultiThreadedMessageDispatcher dispatcher = new MultiThreadedMessageDispatcher(threadPool,
                new MessageDispatcherImpl());
        String addr = trapConfig.getPortocol() + ":" + trapConfig.getHost() + "/" + trapConfig.getPort();
        Address listenAddress = GenericAddress.parse(addr);
        try {
            if (listenAddress instanceof UdpAddress) {
                transport = new DefaultUdpTransportMapping((UdpAddress) listenAddress);
            } else {
                transport = new DefaultTcpTransportMapping((TcpAddress) listenAddress);
            }
        } catch (IOException e) {
            this.recordException("", e);
            this.addException("注册资源失败：" + e.getMessage());
            return false;
        }
        USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(
                MPv3.createLocalEngineID()), 0);
        usm.setEngineDiscoveryEnabled(true);

        snmp = new Snmp(dispatcher, transport);
        snmp.getMessageDispatcher().addMessageProcessingModel(new MPv1());
        snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());
        snmp.getMessageDispatcher().addMessageProcessingModel(new MPv3(usm));
        SecurityModels.getInstance().addSecurityModel(usm);
        snmp.getUSM().addUser(
                new OctetString("MD5DES"),
                new UsmUser(new OctetString("MD5DES"), AuthMD5.ID,
                        new OctetString("UserName"), PrivDES.ID,
                        new OctetString("PasswordUser")));
        snmp.getUSM().addUser(new OctetString("MD5DES"),
                new UsmUser(new OctetString("MD5DES"), null, null, null, null));

        try {
            LOGGER.info("snmp trap开始监听");
            snmp.listen();
            snmp.addCommandResponder(this);
        } catch (IOException e) {
            this.recordException("", e);
            LOGGER.error("启动snmp trap接收时发生异常", e);
            return false;
        }
        return true;
    }

    @Override
    public void tearDown() {
        try {
            snmp.close();
        } catch (IOException e) {
            this.recordException("", e);
            LOGGER.error("停止snmp trap接收时发生异常", e);
        }
        threadPool.interrupt();
    }

    @Override
    public void execute() throws InterruptedException {
        // do nothing
    }

    @Override
    public void processPdu(CommandResponderEvent event) {
        // 判断event来源是否在接收范围内
        Event e = new Event();
        e.setSource(event.getPeerAddress().toString());
        // 构建Event对象并存入缓存
        // 解析Response
        Map m = new HashMap();
        if (event != null && event.getPDU() != null) {
            Vector<? extends VariableBinding> recVBs = event.getPDU().getVariableBindings();
            for (int i = 0; i < recVBs.size(); i++) {
                VariableBinding recVB = recVBs.elementAt(i);
                m.put(recVB.getOid(), recVB.getVariable().toString());
            }
        }
        if (this.mark != null) {
            e.setMark(this.mark);
        }
        e.setMessage(GsonHelper.toJson(m));
        e.setMsgType("snmp_trap");
        e.setIndex(trapConfig.getIndex());
        BaseCache lv1Cache = ruler.findCacheForEvent(e);
        lv1Cache.add(e);
        count.addAndGet(1);
    }


    public static void main(String args[]) {
        //language=JSON
        String configString = "{\"host\":\"172.17.100.100\",\"port\":\"162\",\"protocol\":\"udp\",\"moduleType\":\"snmp_trap\",\"index\":\"linux_linux_systemdmesg_v1-f940-\"}";
        Map<String, String> stringStringMap = GsonHelper.fromJson(configString);
//        HashMap<String, Object> map = new HashMap<>();
//        map.put("moduleType","snmpTrap");
//        map.put("index","test");
//        map.put("host","172.17.20.48");
//        map.put("port","8877");
//        map.put("protocol","udp");
        SnmpTrapShipperConfig shipperConfig = new SnmpTrapShipperConfig(stringStringMap);
        SnmpTrapShipper snmpTrapShipper = new SnmpTrapShipper(shipperConfig);
        snmpTrapShipper.doStart();
    }


}
