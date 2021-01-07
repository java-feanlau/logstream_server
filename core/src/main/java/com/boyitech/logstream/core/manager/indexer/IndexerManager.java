package com.boyitech.logstream.core.manager.indexer;

import com.boyitech.logstream.core.cache.BaseCache;
import com.boyitech.logstream.core.manager.WorkerManager;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;

public interface IndexerManager extends WorkerManager {



	/**
	 * 根据配置创建新的indexer
	 * @param confg
	 * @return
	 */
	public BaseIndexer createIndexer(String confg, BaseCache lv1cache, BaseCache lv2cache);

}
