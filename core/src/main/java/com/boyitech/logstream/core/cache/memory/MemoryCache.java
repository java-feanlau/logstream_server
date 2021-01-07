package com.boyitech.logstream.core.cache.memory;

import com.boyitech.logstream.core.cache.BaseCache;
import com.boyitech.logstream.core.setting.CacheSettings;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MemoryCache<T> extends BaseCache<T> {

	private ArrayBatchBlockingQueue<T> memoryQueue;


	public MemoryCache() {
		super();
		memoryQueue = new ArrayBatchBlockingQueue<T>(CacheSettings.MEMORYCACHESIZE.getValue());
	}

	public MemoryCache(int size) {
		super();
		memoryQueue = new ArrayBatchBlockingQueue<T>(size);
	}

	public MemoryCache(String cacheID,int size) {
		super(cacheID);
		memoryQueue = new ArrayBatchBlockingQueue<T>(size);
	}

	//非阻塞式，取满或者队列为空返回
	@Override
	public List<T> poll(int batch) {
		return memoryQueue.poll(batch);
	}

	//非阻塞式，取满或者队列为空，或者超过指定时间返回
	@Override
	public List<T> poll(int batch, long timeout, TimeUnit unit) throws InterruptedException {
		return memoryQueue.poll(batch, timeout, unit);
	}
	//阻塞式，取满返回
	@Override
	public List<T> take(int batch) throws InterruptedException {
		return memoryQueue.take(batch);
	}

	//队列满了则等待
	@Override
	public void put(Collection<? extends T> c) throws InterruptedException {
		memoryQueue.put(c);
	}

	//队列满了则等待
	@Override
	public void put(T e) throws InterruptedException {
		memoryQueue.put(e);
	}

	//队列满了抛出队列已满异常
	@Override
	public boolean add(T e) {
		return memoryQueue.add(e);
	}

	//队列满了则返回false
	@Override
	public boolean offer(T e) {
		return memoryQueue.offer(e);
	}
	//批量写入Collection中的数据，写入完成前队列满了则返回false，完全写入则返回true。
	@Override
	public boolean putWithoutBlock(Collection<? extends T> c) throws InterruptedException {
		return memoryQueue.putWithoutBlock(c);
	}

	@Override
	public int size() {
		return memoryQueue==null? 0 : memoryQueue.size();
	}

	@Override
	public void clear() {
		if(memoryQueue!=null)
			memoryQueue.clear();
	}


}
