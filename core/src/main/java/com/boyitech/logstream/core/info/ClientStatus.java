package com.boyitech.logstream.core.info;

import com.boyitech.logstream.core.setting.RestfulServerSetting;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.IPv4Util;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class ClientStatus {

	private final String clientID;
	private String fingerPrint;
	private String srcIP = null;
	private String dstIp = null;
	private String dstPort = null;
	private long lastHeartBeat = 0;
	private String version = "1.0";
	private volatile double casVersion = 1;

	private final Map<String, ClientShipperStatus> clientShipperStatusMap; // 任务配置记录<shipperID, ClientShipperStatus>

	public ClientStatus(String clientID, String fingerPrint) {
		this.clientID = clientID;
		this.fingerPrint = fingerPrint;
		this.clientShipperStatusMap = new ConcurrentHashMap<>();
		dstIp = IPv4Util.getLocalHostLANAddress().getHostAddress();
		dstPort =  RestfulServerSetting.PORT.getValue().toString();
	}

	public String toResponseHeartbeatBody() {
		Map response = new HashMap();
		response.put("http_status", "200");
		response.put("clientID", clientID);
		response.put("fingerPrint", fingerPrint);

		return GsonHelper.toJson(response);
	}

	public String toResponseConfigBody() {
		Map response = new HashMap();
		response.put("http_status", "200");
		response.put("fingerPrint", fingerPrint);
		response.put("casVersion",casVersion);
		Map<String,String> map = new HashMap();
		for(Entry<String, ClientShipperStatus> e :clientShipperStatusMap.entrySet()) {
			//shipperID -> configStringJSON
			map.put(e.getKey(), e.getValue().getShipperConfig());
		}
		response.put("shipperConfigs", map);
		return GsonHelper.toJson(response);
	}

	public boolean addClientShipperStatus(String shipperID, String clientConfig) {
		Map<String, String> configMap = GsonHelper.fromJson(clientConfig);
		configMap.put("shipperID",shipperID);
        clientShipperStatusMap.put(shipperID, new ClientShipperStatus(configMap));
		casVersion++;
		return true;
	}

	public boolean removeClientShipperStatus(String shiperID) {
		ClientShipperStatus remove = clientShipperStatusMap.remove(shiperID);
		if (remove == null)
			return false;
		casVersion++;
		return true;
	}

	public boolean updateClientShipperStatus(String shiperID, String clientConfig) {
		Map<String, String> configMap = GsonHelper.fromJson(clientConfig);
		if (clientShipperStatusMap.replace(shiperID, new ClientShipperStatus(configMap)) == null) {
			return false;
		}
		casVersion++;
		return true;
	}
	public ClientShipperStatus getClientShipperStatusByKey(String key) {
		if(key == null){
			return null;
		}
		return this.clientShipperStatusMap.get(key);
	}

	public void changeFingerPrint(String fingerPrint) {
		this.fingerPrint = fingerPrint;
	}

	public String getClientShipperConfigJson(){
		StringBuffer sb =new StringBuffer();
		sb.append("{");
		for (Entry<String, ClientShipperStatus> entry : clientShipperStatusMap.entrySet()) {
			sb.append("\""+entry.getKey()+"\": \""+entry.getValue().getShipperConfig()+"\",");
		}
		if(sb.length() !=1){
			sb.deleteCharAt(sb.length() - 1);
		}
		sb.append("}");
		return sb.toString();
	}

	/**
	 * 记录心跳
	 */
	public long beat() {
		this.lastHeartBeat = DateTime.now().getMillis();
		return this.lastHeartBeat;
	}

	public String getUuid() {
		return clientID;
	}


	public String getClientID() {
		return clientID;
	}

	public String getFingerPrint() {
		return fingerPrint;
	}

	public String getSrcIP() {
		return srcIP;
	}

	public void setSrcIP(String srcIP) {
		this.srcIP = srcIP;
	}

	public long getLastHeartBeat() {
		return lastHeartBeat;
	}

	public void setLastHeartBeat(long lastHeartBeat) {
		this.lastHeartBeat = lastHeartBeat;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public double getCasVersion() {
		return casVersion;
	}

	public void setCasVersion(int casVersion) {
		this.casVersion = casVersion;
	}

	public Map<String, ClientShipperStatus> getClientShipperStatusMap() {
		return clientShipperStatusMap;
	}

	public String getDstIp() {
		return dstIp;
	}

	public void setDstIp(String dstIp) {
		this.dstIp = dstIp;
	}

	public String getDstPort() {
		return dstPort;
	}

	public void setDstPort(String dstPort) {
		this.dstPort = dstPort;
	}

	@Override
	public String toString() {
		return "ClientStatus{" +
				"clientID='" + clientID + '\'' +
				", fingerPrint='" + fingerPrint + '\'' +
				", srcIP='" + srcIP + '\'' +
				", lastHeartBeat=" + lastHeartBeat +
				", version='" + version + '\'' +
				", casVersion=" + casVersion +
				", clientShipperStatusMap=" + clientShipperStatusMap +
				'}';
	}

	public void decreaseCasVersion() {
		this.casVersion--;
	}

	public void incrementCasVersion(){
		this.casVersion++;
	}
}
