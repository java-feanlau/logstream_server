package com.boyitech.logstream.core.manager;

import com.boyitech.logstream.core.setting.StatsSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public abstract class BaseManager {

	protected static final Logger LOGGER = LogManager.getLogger("main");

	/**
	 * 系统退出
	 */
	public  void exit(){
		System.exit(0);
	};


	public static void sendToGraphite(String[] msg) {
		if (StatsSettings.GRAPHITE_HOST.getValue().equals(""))
			return;
		try {
			DatagramSocket client = new DatagramSocket();
			for (String s : msg) {
				byte[] sendBuf = s.getBytes("utf-8");
				DatagramPacket sendPacket = new DatagramPacket(sendBuf, 0, sendBuf.length, InetAddress.getByName(StatsSettings.GRAPHITE_HOST.getValue()), StatsSettings.GRAPHITE_PORT.getValue());
				client.send(sendPacket);
			}
			client.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
