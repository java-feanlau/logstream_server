package com.boyitech.logstream.core.worker.porter;

import com.boyitech.logstream.core.worker.BaseWorkerConfig;

import java.util.Map;

public class BasePorterConfig extends BaseWorkerConfig {
	protected String moduleType;

	public BasePorterConfig(Map config) {
		super(config);
		if (config.get("moduleType") != null && config.get("moduleType") != "") {
			this.moduleType = config.get("moduleType").toString();
		} else {
			throw new RuntimeException("配置必须包含moduleType字段");
		}
	}


	public String getModuleType() {
		return moduleType;
	}
}
