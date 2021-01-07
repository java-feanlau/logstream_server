package com.boyitech.logstream.core.cache.queue;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;

public interface BatchBlockingQueue<E> extends BlockingQueue<E>, BatchBlockingTake<E>, java.io.Serializable {

	//public boolean add(Collection<? extends E> c);

	//public boolean offer(Collection<? extends E> c);

	//public boolean offer(Collection<? extends E> c, long timeout, TimeUnit unit);

	//批量写入只实现了阻塞式的
	public void put(Collection<? extends E> c) throws InterruptedException;

	public boolean add(E e);

	public boolean putWithoutBlock(Collection<? extends E> c) throws InterruptedException;

}
