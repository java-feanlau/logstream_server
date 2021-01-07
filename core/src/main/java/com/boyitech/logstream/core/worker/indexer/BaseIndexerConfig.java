package com.boyitech.logstream.core.worker.indexer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;

import java.util.Map;

public class BaseIndexerConfig extends BaseWorkerConfig {
	private String logType;

	private String ipFilter;

	public BaseIndexerConfig(Map config) {
		super(config);
		if(config.get("logType")!=null && config.get("logType")!="") {
			this.logType = config.get("logType").toString();
		}
		else {
			throw new RuntimeException("配置必须包含logType字段");
		}

		if(config.get("ipFilter")!=null && config.get("ipFilter")!=""&&config.containsKey("ipFilter")&&String.valueOf(config.get("ipFilter")).trim().length()>2){
			String jsonString = JSON.toJSONString(config);
			JSONObject strJSON = JSONObject.parseObject(jsonString);
			String ipFilter = strJSON.getString("ipFilter");
			this.ipFilter=ipFilter;
		}
		else {
			this.ipFilter="null";
			LOGGER.info("没有配置ip过滤");
		}

	}


    public String getLogType() {
		return logType;
	}

	public String getIpFilter() {
		return ipFilter;
	}

	public void setIpFilter(String ipFilter) {
		this.ipFilter = ipFilter;
	}
}
