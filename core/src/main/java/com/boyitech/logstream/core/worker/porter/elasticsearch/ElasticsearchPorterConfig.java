package com.boyitech.logstream.core.worker.porter.elasticsearch;

import com.boyitech.logstream.core.worker.porter.BasePorterConfig;

import java.util.Map;

public class ElasticsearchPorterConfig extends BasePorterConfig {
	private String ip;
	private String port;

	public ElasticsearchPorterConfig(Map config) {
		super(config);
		this.ip = (String) config.get("ip");
		this.port = (String) config.get("port");

	}

	public String getPort() {
		return port;
	}

	public String getIp() {
		return ip;
	}

}
