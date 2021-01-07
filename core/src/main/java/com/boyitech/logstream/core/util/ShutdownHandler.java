package com.boyitech.logstream.core.util;

import com.boyitech.logstream.core.manager.BaseManager;
import org.joda.time.DateTime;

import sun.misc.Signal;
import sun.misc.SignalHandler;

@SuppressWarnings("restriction")
public class ShutdownHandler implements SignalHandler {

	private BaseManager lsManager;

	public ShutdownHandler(BaseManager lsManager) {
		this.lsManager = lsManager;
	}

	@Override
	public void handle(Signal signal) {
		invokeShutdownHook();
		Runtime.getRuntime().exit(0);
	}

	private void invokeShutdownHook() {
		Thread t = new Thread(new ShutdownHook(), "ShutdownHook-Thread");
		Runtime.getRuntime().addShutdownHook(t);
	}

	class ShutdownHook implements Runnable {
		@Override
		public void run() {
			System.out.println(Thread.currentThread().getName()+"开始退出程序");
			long start = DateTime.now().getMillis();
			lsManager.exit();
			System.out.println("退出完成,耗时:"+(DateTime.now().minus(start)).getMillis());
			GeoIPHelper.getInstance().close();
		}

	}
}
