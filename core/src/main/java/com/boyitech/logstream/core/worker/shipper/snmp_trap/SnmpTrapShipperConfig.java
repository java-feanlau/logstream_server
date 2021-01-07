package com.boyitech.logstream.core.worker.shipper.snmp_trap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.boyitech.logstream.core.util.filter_rule.FilterRule;
import com.boyitech.logstream.core.worker.shipper.BaseShipperConfig;

public class SnmpTrapShipperConfig extends BaseShipperConfig {

	private String host;
	private int port;
	private String portocol;
	private List<FilterRule> filterRuleList = new ArrayList<FilterRule>();

	public SnmpTrapShipperConfig(Map config) {
		super(config);
		host = config.get("host").toString();
		port = Double.valueOf(config.get("port").toString()).intValue();
		if(config.get("protocol")!=null) {
			portocol = config.get("protocol").toString();
		}else {
			portocol = "udp";
		}
		//从配置中解析根据五元组设置索引的规则
		List<Map<String, String>> filters = (List)config.get("filters");
		if(config.get("filters")==null) { // 如果没有设置过滤规则，则生成一条只有索引的规则
//			filterRuleList.add(new FilterRule(config.get("index").toString()));
		}else {
			for(Map<String, String> rule : filters) {
				try {
//					rule.put("index", config.get("index").toString());
					filterRuleList.add(new FilterRule(rule));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getPortocol() {
		return portocol;
	}

	public List<FilterRule> getFilterRuleList() {
		return filterRuleList;
	}

}
