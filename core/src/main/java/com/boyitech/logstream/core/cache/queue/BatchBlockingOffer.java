package com.boyitech.logstream.core.cache.queue;

import java.util.Collection;

public interface BatchBlockingOffer<E> {

	public void put(Collection<? extends E> c) throws InterruptedException;

	public void put(E c) throws InterruptedException;

	public boolean add(E e);

	public boolean offer(E e);

	public boolean putWithoutBlock(Collection<? extends E> c) throws InterruptedException;

}
