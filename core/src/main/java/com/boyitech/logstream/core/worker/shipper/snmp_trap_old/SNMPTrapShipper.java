package com.boyitech.logstream.core.worker.shipper.snmp_trap_old;

import com.boyitech.logstream.core.cache.BaseCache;
import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.info.InetInfo;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

public class SNMPTrapShipper extends BaseShipper implements CommandResponder {

	private ThreadPool threadPool;
	private SNMPTrapShipperConfig trapConfig;
	private Snmp snmp = null;
	private TransportMapping<?> transport;
	// 分配索引的规则
	private FilterRuler ruler;

	public SNMPTrapShipper(BaseShipperConfig config) {
		super(config);
		trapConfig = (SNMPTrapShipperConfig)config;
		// 创建根据五元组分配索引的对象
		ruler = new FilterRuler(trapConfig.getFilterRuleList());
	}

	public SNMPTrapShipper(String shipperID,BaseShipperConfig config) {
		super(shipperID,config);
		trapConfig = (SNMPTrapShipperConfig)config;
		// 创建根据五元组分配索引的对象
		ruler = new FilterRuler(trapConfig.getFilterRuleList());
	}

	@Override
	public boolean register() {
		threadPool = ThreadPool.create("Trap", trapConfig.getThreadNum());
		MultiThreadedMessageDispatcher dispatcher = new MultiThreadedMessageDispatcher(threadPool,
				new MessageDispatcherImpl());
		String addr = trapConfig.getPortocol()+":"+trapConfig.getHost()+"/"+trapConfig.getPort();
		Address listenAddress = GenericAddress.parse(addr);
		try {
			if (listenAddress instanceof UdpAddress) {
				transport = new DefaultUdpTransportMapping((UdpAddress) listenAddress);
			} else {
				transport = new DefaultTcpTransportMapping((TcpAddress) listenAddress);
			}
		} catch (IOException e) {
			this.recordException("",e);
			this.addException("注册资源失败："+e.getMessage());
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
			this.recordException("",e);
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
			this.recordException("",e);
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
		InetInfo info = new InetInfo();
		String srcIp = event.getPeerAddress().toString().split("/")[0];
		info.setSrcAddr(srcIp);
		Event e = ruler.findIndexForEvent(info);
		if(e == null){
			return;
		}
		e.setSource(event.getPeerAddress().toString());
		//e.setInetInfo(info);
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
        if(this.mark!=null) {
			e.setMark(this.mark);
		}
		e.setMessage(GsonHelper.toJson(m));
		e.setMsgType("snmp_trap");
		e.setIndex(trapConfig.getIndex());
		BaseCache lv1Cache = ruler.findCacheForEvent(e);
		lv1Cache.add(e);
		count.addAndGet(1);
	}

	public boolean doStart() {
		try {
			// 如果worker正在运行，返回false
			if(this.isAlive()) {
				return false;
			}
			if(threadsNum < 1) {
				return false;
			}
			// register,进行初始化
			try {
				register();
			} catch (Exception e) {
				LOGGER.error("worker异常", e);
				return false;
			}
			return true;
		} catch (Exception e){
			LOGGER.error("worker异常", e);
			return false;
		}
	}

	public boolean doStop(CountDownLatch countDownLatch) {
		this.countDownLatch = countDownLatch;
		try {
			snmp.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		countDownLatch.countDown();
		return true;
	}

	public boolean doStop() {
		try {
			snmp.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	public boolean isAlive() {
		if(transport==null) {
			return false;
		}else {
			return transport.isListening();
		}
	}





//	public static List getWorkerParametersTemplate() {
//		return parametersTemplate;
//	}

//	private static final List parametersTemplate;


//	// 构建参数模板
//	static {
//		parametersTemplate = new ArrayList();
//		Map field1 = new HashMap();
//		field1.put("fieldName", "host");
//		field1.put("displayName", "监听地址");
//		field1.put("fieldType", "string");
//		field1.put("advancedType", "ip");
//		field1.put("required", true);
//		field1.put("default", null);
//		field1.put("range", new ArrayList());
//		parametersTemplate.add(field1);
//		Map field2 = new HashMap();
//		field2.put("fieldName", "port");
//		field2.put("displayName", "监听端口");
//		field2.put("fieldType", "string");
//		field2.put("advancedType", "port");
//		field2.put("required", true);
//		field2.put("default", null);
//		field2.put("range", new ArrayList());
//		parametersTemplate.add(field2);
//		Map field3 = new HashMap();
//		field3.put("fieldName", "filter");
//		field3.put("displayName", "来源地址");
//		field3.put("fieldType", "array");
//		field3.put("advancedType", "text");
//		field3.put("required", true);
//		field3.put("default", "*");
//		field3.put("range", new ArrayList());
//		parametersTemplate.add(field3);
//		Map field4 = new HashMap();
//		field4.put("fieldName", "protocol");
//		field4.put("displayName", "协议类型");
//		field4.put("fieldType", "string");
//		field4.put("advancedType", "text");
//		field4.put("required", true);
//		field4.put("default", "udp");
//		List array = new ArrayList();
//		array.add("tcp");
//		array.add("udp");
//		field4.put("range", array);
//		parametersTemplate.add(field4);
//	}



}
