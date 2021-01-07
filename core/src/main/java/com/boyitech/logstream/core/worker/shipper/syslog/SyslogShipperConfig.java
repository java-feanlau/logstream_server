package com.boyitech.logstream.core.worker.shipper.syslog;

import com.boyitech.logstream.core.util.filter_rule.FilterRule;
import com.boyitech.logstream.core.worker.shipper.BaseShipperConfig;

import javax.management.InvalidAttributeValueException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
/**
 * @author Eric
 * @Title: SyslogShipperCongif
 * @date 2019/1/3 17:54
 * @Description: TODO
 */
public class SyslogShipperConfig extends BaseShipperConfig {
    private int port;
    private String host;
    private List<String> protocolList = new ArrayList<String>();
    private List<FilterRule> filterRuleList = new ArrayList<FilterRule>();


    public SyslogShipperConfig(Map config) throws InvalidAttributeValueException {
        super(config);
        port = Integer.valueOf((String) config.get("port"));
        host = (String) config.get("host");
        protocolList.add("udp");
        protocolList.add("tcp");
//        List<String> protocols = (List<String>) config.get("protocols");
//        if (protocols == null || protocols.size() == 0) {
//            protocolList.add("udp");
//        } else {
//            for (String protocol : protocols) {
//                protocolList.add(protocol);
//            }
//        }

        //从配置中解析根据五元组设置索引的规则
        List<Map<String, String>> filters = (List) config.get("filters");
        if (config.get("filters") == null || filters.size() == 0) { // 如果没有设置过滤规则，则生成一条只有索引的规则
            filterRuleList.add(new FilterRule(null));
        } else {
            for (Map<String, String> rule : filters) {
                try {
//					rule.put("index", config.get("index").toString());
                    filterRuleList.add(new FilterRule(rule));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public List<String> getProtocolList() {
        return protocolList;
    }

    public List<FilterRule> getFilterRuleList() {
        return filterRuleList;
    }
}


