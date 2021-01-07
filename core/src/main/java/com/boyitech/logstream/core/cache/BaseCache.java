package com.boyitech.logstream.core.cache;

import com.boyitech.logstream.core.cache.queue.BatchBlockingOffer;
import com.boyitech.logstream.core.cache.queue.BatchBlockingTake;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

public abstract class BaseCache<T> implements BatchBlockingTake<T>, BatchBlockingOffer<T> {
	protected static final Logger LOGGER = LogManager.getLogger("worker");
	protected final String cacheId;

	protected String type;


	public BaseCache (){
		cacheId = UUID.randomUUID().toString();
		this.type = "memory";
	}

	public BaseCache (String cacheId){
		this.cacheId = cacheId;
		this.type = "memory";
	}

	public String getType() {
		return type;
	}

	public String getCacheId() {
		return cacheId;
	}

	public abstract int size();

	public abstract void clear();

}
