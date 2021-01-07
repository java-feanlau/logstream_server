package com.boyitech.logstream.core.cache.queue;

import java.util.List;
import java.util.concurrent.TimeUnit;

public interface BatchBlockingTake<E> {
	/*
	 * 获取�?多batch大小的数�?
	 */
	public List<E> poll(int batch);

	/*
	 * 阻塞式获取，阻塞时间超过指定时间后返回已获取的数据�??
	 */
	public List<E> poll(int batch, long timeout, TimeUnit unit) throws InterruptedException;

	/*
	 * 阻塞式获取，直到取满batch大小数据才返�?
	 */
	public List<E> take(int batch) throws InterruptedException;
}
