package com.boyitech.logstream.core.worker;

import java.io.IOException;
import java.util.List;

/**
 * @author Night
 *
 */
public interface Worker {

	/**
	 * 执行启动
	 */
	public boolean doStart();

	/**
	 * 执行停止
	 */
	public boolean doStop();


	/**
	 * 强制关闭
	 * @return
	 */
	public boolean forceStop();

	/**
	 * 执行重启
	 */
	public boolean doRestart();

	/**
	 * 是否在运�??
	 * @return
	 */
	public boolean isAlive();

	/**
	 * 是否完成运行
	 * @return
	 */
	public boolean isDone();

	/**
	 * 执行停止并销�??
	 */
	public boolean doDestroy();

	/**
	 * 运行结束后的处理阶段
	 */
	public void tearDown();

	/**
	 * 实际功能
	 * @throws InterruptedException
	 */
	public void execute() throws InterruptedException, IOException;

	/**
	 * 记录发生的异�??
	 */
	public void recordException(String msg,Exception e);

	/**
	 * 获取可配置参数列�??
	 * @return
	 */
	public static List getWorkerParametersTemplate() {
		return null;
	};

}
