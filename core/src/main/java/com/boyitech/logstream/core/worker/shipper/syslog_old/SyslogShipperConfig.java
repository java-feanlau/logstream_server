package com.boyitech.logstream.core.worker.shipper.syslog_old;

import com.boyitech.logstream.core.util.filter_rule.FilterRule;
import com.boyitech.logstream.core.worker.shipper.BaseShipperConfig;

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
    private String protocol;
    private List<FilterRule> filterRuleList = new ArrayList<FilterRule>();

    public SyslogShipperConfig(Map config) {
        super(config);
        port =  Integer.valueOf((String) config.get("port"));
        host = (String) config.get("host");
        protocol = config.get("protocol") != null ?
                (String) config.get("protocol") : "udp";
        //从配置中解析根据五元组设置索引的规则
//        List<Map<String, String>> filters = (List)config.get("filters");
//        if(config.get("filters")==null) { // 如果没有设置过滤规则，则生成一条只有索引的规则
//            filterRuleList.add(new FilterRule(config.get("index").toString()));
//        }else {
//            for(Map<String, String> rule : filters) {
//                try {
//                    filterRuleList.add(new FilterRule(rule));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public String getProtocol() {
        return protocol;
    }

    public List<FilterRule> getFilterRuleList() {
        return filterRuleList;
    }
}


