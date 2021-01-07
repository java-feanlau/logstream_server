package com.boyitech.logstream.core.manager;

public interface WorkerManager {

	/**
	 * 启动指定worker
	 * @param key
	 */
	public boolean startWorker(String key);

	/**
	 * 停止指定worker
	 * @param key
	 */
	public boolean stopWorker(String key);


	/**
	 * 销毁一个worker
	 * @param key
	 */
	public boolean destroyWorker(String key);

}
