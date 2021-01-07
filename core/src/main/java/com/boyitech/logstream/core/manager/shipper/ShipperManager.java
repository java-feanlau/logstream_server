package com.boyitech.logstream.core.manager.shipper;

import com.boyitech.logstream.core.cache.BaseCache;
import com.boyitech.logstream.core.manager.WorkerManager;
import com.boyitech.logstream.core.worker.shipper.BaseShipper;

public interface ShipperManager extends WorkerManager {

//	/**
//	 * 获取shipper，如果不存在则创建新的
//	 * @param config
//	 * @return
//	 */
//	public BaseShipper getShipper(BaseLogStreamConfig config);
//
//	/**
//	 * 根据配置创建新的shipper
//	 * @param config
//	 * @return
//	 * @throws Exception
//	 */
//	public BaseShipper createShipper(BaseLogStreamConfig config) throws Exception;
//
//	/**
//	 * 根据新的配置更新shipper
//	 * @param config
//	 * @return
//	 * @throws Exception
//	 */
//	public BaseShipper updateShipper(BaseLogStreamConfig config) throws Exception;


	/**
	 * 根据配置创建新的shipper
	 * @param shipperConfig,lv1cache
	 * @return
	 * @throws Exception
	 */
	public BaseShipper createShipper( String shipperConfig, BaseCache lv1cache);
	/**
	 * @Author Eric Zheng
	 * @Description 创建指定id的shipper
	 * @Date 11:23 2019/3/29
	 **/
	public BaseShipper createShipper( String shipperID, String shipperConfig, BaseCache lv1cache);
}
