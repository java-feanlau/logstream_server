package com.boyitech.logstream.client.factory;

import com.boyitech.logstream.client.manager.ClientManager;
import com.boyitech.logstream.client.manager.cache.YSClientCacheManager;
import com.boyitech.logstream.client.manager.shipper.YSClientShipperManager;

public class ManagerFactory {

	private static class InstanceHolder {
		private static final YSClientCacheManager clientCacheManager = new YSClientCacheManager();
		private static final YSClientShipperManager clientShipperManager = new YSClientShipperManager();
		private static final ClientManager clientManager = new ClientManager();//必须放后面
	}

	public static final ClientManager getClientManager() {
		return InstanceHolder.clientManager;
	}

	public static final YSClientCacheManager getYSClientCacheManager() {
		return InstanceHolder.clientCacheManager;
	}

	public static final YSClientShipperManager getYSClientShipperManager() {
		return InstanceHolder.clientShipperManager;
	}

}
