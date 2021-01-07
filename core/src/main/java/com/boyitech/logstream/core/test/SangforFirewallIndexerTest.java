package com.boyitech.logstream.core.test;

import com.boyitech.logstream.core.util.GeoIPHelper;
import com.maxmind.geoip2.exception.GeoIp2Exception;

import java.io.IOException;
import java.util.Map;

public class SangforFirewallIndexerTest {

	public static void main(String args[]) throws IOException, GeoIp2Exception {
//		SangforFirewallIndexer indexer = new SangforFirewallIndexer(null);
//		indexer.register();
//		List<String> test = new ArrayList<String>();
//		test.add("日志类型:WAF应用防护日志, 源IP:101.81.205.173, 源端�?:60381, 目的IP:202.120.199.79, 目的端口:80, 攻击类型:WEB登录弱口令防�?, 严重级别:�?, 系统动作:被记�?, URL:cas.edu.sh.cn/CAS/login?service=http://jsgl.21shte.net/reception/UserIndex.html");
//		test.add("日志类型:服务控制或应用控�?, 用户:(null), 源IP:10.27.250.26, 源端�?:49581, 目的IP:10.0.255.171, 目的端口:80, 应用类型:Other, 应用名称:http, 系统动作:被记�?");
//		test.add("日志类型:流量审计, 应用类型:发�?�邮�?, 用户�?/主机:202.120.199.13, 上行流量(KB):50561, 下行流量(KB):672987, 总流�?(KB):723548");
//		test.add("日志类型:网站访问, 用户:(null), 源IP:164.132.161.34, 目的IP:202.120.199.22, 应用名称:创宇�?, 系统动作:拒绝, URL:http://erchome.shedu.net/list.html?cate_id=299&amp;cate1_id=356");
//		for(String s : test) {
//			Event e = new Event();
//			e.setMessage(s);
//			indexer.format(e);
//			System.out.println(e.getJsonMessage());
//		}

		Map info = GeoIPHelper.getInstance().getGeoIPInfo("202.120.199.79");
		System.out.println(info);
	}

}
