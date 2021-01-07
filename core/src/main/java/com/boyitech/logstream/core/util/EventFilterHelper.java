package com.boyitech.logstream.core.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

public class EventFilterHelper {

	static final Logger LOGGER = LogManager.getLogger("main");
	private static final Set<String> SHIPPERSET = new HashSet<String>();


	public static void addShipperMark(String shipperID) {
		LOGGER.debug("添加任务标志：" + shipperID);
		SHIPPERSET.add(shipperID);
	}

	public static void removeShipperMark(String shipperID) {
		LOGGER.debug("移除任务标志：" + shipperID);
		SHIPPERSET.remove(shipperID);
	}


	public static boolean hasShipperMark(String shipperID) {
		return SHIPPERSET.contains(shipperID);
	}

}
