package com.boyitech.logstream.server.factory;

import com.boyitech.logstream.core.manager.cache.YSCacheManager;
import com.boyitech.logstream.server.manager.ServerManager;
import com.boyitech.logstream.server.manager.client.ServerClientManager;
import com.boyitech.logstream.server.manager.indexer.YSIndexerManager;
import com.boyitech.logstream.server.manager.porter.YSPorterManager;
import com.boyitech.logstream.server.manager.shipper.YSShipperManager;

public class SingleManagerFactory {

	private static class InstanceHolder{
		public static final YSShipperManager shipperManager = new YSShipperManager();
		public static final YSIndexerManager indexerManager = new YSIndexerManager();
		public static final YSPorterManager porterManager = new  YSPorterManager();
		public static final YSCacheManager cacheManager = new YSCacheManager();
		public static final ServerClientManager serverClientManager = new ServerClientManager();
		public static final ServerManager serverManager = new ServerManager(); //必须放在最后面
	}

	public static ServerManager getServerManager(){
		return InstanceHolder.serverManager;
	}

	public static YSShipperManager getShipperManager(){
		return InstanceHolder.shipperManager;
	}

	public static YSIndexerManager getIndexerManager(){
		return InstanceHolder.indexerManager;
	}

	public static YSCacheManager getCacheManager(){
		return InstanceHolder.cacheManager;
	}

	public static ServerClientManager getServerClientManager(){
		return InstanceHolder.serverClientManager;
	}

	public static YSPorterManager getPorterManager(){
		return InstanceHolder.porterManager;
	}




}
