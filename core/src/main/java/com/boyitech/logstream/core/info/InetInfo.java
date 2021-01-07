package com.boyitech.logstream.core.info;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

//todo 没有空处理
public class InetInfo {
	// 网络数据五元组信息
	private String srcAddr;
	private int srcPort;
	private String dstAddr;
	private int dstPort;
	private String protocol;
	public InetInfo(){
		super();
	}
	public InetInfo(String srcAddr, int srcPort, String dstAddr, int dstPort, String protocol) {
		this.srcAddr = srcAddr;
		this.srcPort = srcPort;
		this.dstAddr = dstAddr;
		this.dstPort = dstPort;
		this.protocol = protocol;
	}

	public String getSrcAddr() {
		return srcAddr;
	}

	public void setSrcAddr(String srcAddr) {
		this.srcAddr = srcAddr;
	}

	public int getSrcPort() {
		return srcPort;
	}

	public void setSrcPort(int srcPort) {
		this.srcPort = srcPort;
	}

	public String getDstAddr() {
		return dstAddr;
	}

	public void setDstAddr(String dstAddr) {
		this.dstAddr = dstAddr;
	}

	public int getDstPort() {
		return dstPort;
	}

	public void setDstPort(int dstPort) {
		this.dstPort = dstPort;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	@Override
	public String toString() {

		return "InetInfo{" +
				"srcAddr='" + srcAddr + '\'' +
				", srcPort=" + srcPort +
				", dstAddr='" + dstAddr + '\'' +
				", dstPort=" + dstPort +
				", protocol='" + protocol + '\'' +
				'}';

	}
}
