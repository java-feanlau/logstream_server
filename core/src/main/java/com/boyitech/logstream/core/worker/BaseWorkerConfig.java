package com.boyitech.logstream.core.worker;

import java.util.Map;

public abstract class BaseWorkerConfig extends BaseConfig {


	protected Map config;
	protected int threadNum = 1;

	public BaseWorkerConfig(Map config) {
		this.config = config;

		if(config.get("threadNum")!=null) {
			try {
				this.threadNum = Double.valueOf(config.get("threadNum").toString()).intValue();
			}catch(Exception e) {
				LOGGER.debug("线程数解析失�??", e);
			}
		}
	}



	public Map getConfig() {
		return config;
	}

	public int getThreadNum() {
		return threadNum;
	}

	//提供给BaseIndexerConfig覆盖此方法，然后在BaseIndexer里面才能调用该方法
	public String getLogType(){
		return null;
	}
	//同上
	public String getIpfilter() {
		return null;
	}
}
