package com.boyitech.logstream.core.worker.porter;

import com.boyitech.logstream.core.cache.BaseCache;
import com.boyitech.logstream.core.worker.BaseWorker;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;

import java.util.concurrent.atomic.AtomicLong;

public abstract class BasePorter extends BaseWorker {

	protected BaseCache lv2Cache;
	protected BaseCache lv3Cache;
	protected AtomicLong count =new AtomicLong();

	public BasePorter(BasePorterConfig config) {
		super(config);
		LOGGER.debug("初始化porter");
	}

	public BasePorter(String worerId, BasePorterConfig config) {
		super(worerId, config);
		LOGGER.debug("初始化porter");
	}

	@Override
	public void run() {
		while (runSignal) {
			synchronized (this) {
				try {
					execute();
				}catch (Exception e) {

				}
			}
		}
		tearDown();
		if(countDownLatch!=null) {
			countDownLatch.countDown();
			LOGGER.info(Thread.currentThread().getName()+"退出");
		}
	}

	public BaseCache getLv2Cache() {
		return lv2Cache;
	}

	public void setLv2Cache(BaseCache lv2Cache) {
		this.lv2Cache = lv2Cache;
	}

	public void setLv3Cache(BaseCache lv3Cache) {
		this.lv3Cache = lv3Cache;
	}

	@Override
	public String workerType() {
		return "porter";
	}


	public AtomicLong getCount() {
		return count;
	}
}
