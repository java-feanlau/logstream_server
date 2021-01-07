package com.boyitech.logstream.core.util;

import com.boyitech.logstream.core.info.eneity.IndexerInfosResult;


import java.util.*;

public class LogStreamHelper {

	private static final Map<String, String> log_stream_type = new HashMap();

	static {
		log_stream_type.put("ArrayVpn日志", "array_vpn_all_v1");
		log_stream_type.put("铱迅WAF", "yxlink_waf_all_v1");
		log_stream_type.put("山石防火墙", "hillstone_firewall_all_v1");
		log_stream_type.put("LinuxAudit", "linux_linux_audit_v1");
		log_stream_type.put("LinuxExtras", "linux_linux_extrasyum_v1");
		log_stream_type.put("LinuxFtpd", "linux_linux_ftpd_v1");
		log_stream_type.put("LinuxGeneralCron", "linux_linux_generalcron_v1");
		log_stream_type.put("LinuxGeneralMail", "linux_linux_generalmail_v1");
		log_stream_type.put("LinuxGeneralMessage", "linux_linux_generalmessage_v1");
		log_stream_type.put("LinuxGeneralSecure", "linux_linux_generalsecure_v1");
		log_stream_type.put("LinuxOsLog", "linux_linux_oslog_v1");
		log_stream_type.put("LinuxSystemDmesg", "linux_linux_systemdmesg_v1");

//		log_stream_type.put()("SNMP TRAP日志", "snmp_trap");
		log_stream_type.put("apache失败日志", "apache_apache_error_v1");
		log_stream_type.put("apache成功日志", "apache_apache_success_v1");
		log_stream_type.put("Nginx失败日志", "nginx_nginx_error_v1");
		log_stream_type.put("Nginx成功日志", "nginx_nginx_success_v1");
		log_stream_type.put("Squid访问日志", "squid_squid_access_v1");
		log_stream_type.put("博弋catchview历史日志", "boyi_catchview_history_v1");
		log_stream_type.put("Netflow日志", "vcenter_netflow_v10_v1");
// 	log_stream_type.put()("Netflow日志","netflow");
//		log_stream_type.put()("syslog", "syslog");

		log_stream_type.put("nmap", "nmap_nmap_all_v1");
		log_stream_type.put("A10日志", "a10_loadblance_all_v1");
		log_stream_type.put("安桓APT日志", "dbapp_apt_all_v1");
		log_stream_type.put("亚信TDA日志", "asiainfo_tda_all_v1");
		log_stream_type.put("CheckPoint日志", "checkpoint_firewall_all_v1");
		log_stream_type.put("Fortinet日志", "fortinet_firewall_all_v2");
		log_stream_type.put("IBMAppJVMOut日志", "ibm_websphere_appjvmout_v1");
		log_stream_type.put("IBMHttpAccess日志", "ibm_websphere_httpaccess_v1");
		log_stream_type.put("IBMHttpError日志", "ibm_websphere_httperror_v1");
		log_stream_type.put("微软WebIIS日志", "microsoft_webiis_all_v1");
		log_stream_type.put("Mysql错误日志", "oracle_mysql_error_v1");
		log_stream_type.put("Oracle日志", "oracle_oracle_log_v1");
		log_stream_type.put("PanabitApp日志", "panabit_panabit_app_v1");
		log_stream_type.put("PanabitUrl日志", "panabit_panabit_url_v1");
		log_stream_type.put("深信服防火墙日志", "sangfor_firewall_all_v1");
		log_stream_type.put("Sas日志", "sas_sas_log_v1");
		log_stream_type.put("Windows日志", "microsoft_windows_all_v1");

        //ys3.2新增的三个商学院日志解析
		log_stream_type.put("Genvision多媒体管理平台日志","genvision_multimedia_all_v1");
		log_stream_type.put("H3C出口路由器日志","h3c_outerrouter_all_v1");
		log_stream_type.put("牙木DNS日志","yamu_dns_all_v1");

		//ys3.2新增的电机学院的日志解析(暂时屏蔽，待实际测试后开放)
		//log_stream_type.put()("深信服VPN日志","sangfor_vpn_all_v1");
		//log_stream_type.put()("华为USG日志","huawei_usg_all_v1");
		//log_stream_type.put()("华为UMA堡垒机日志","huawei_uma_all_v1");
		//log_stream_type.put()("安桓数据库审计日志","dbapp_audit_all_v1");
		//log_stream_type.put()("Drcom-radius日志","drcom_radius_all_v1");

		//ys3.2新增的博世华域的日志解析
		log_stream_type.put("趋势科技邮件网关日志","trendmicro_imsa_mailgateway_v1");
		log_stream_type.put("深信服上网行为管理日志","sangfor_accesscontrol_all_v1");//电机学院也有此需求，但实际写的时候参考的博世的日志

		 //time:3:49 PM 2019/8/20 新增不进行任何格式化操作的默认indexer
		log_stream_type.put("无格式化处理日志","boyi_default_all_v1");
        log_stream_type.put("snmp日志","snmp");

        log_stream_type.put("爱数网盘日志","eisoo_network_disk_v1");
		log_stream_type.put("阿尔卡特交换机日志","alcatel_switch_all_v1");
		log_stream_type.put("windows域DHCP日志","windows_dhcp_all_v1");
		log_stream_type.put("windows域DNS日志","windows_dns_all_v1");
	}

	public static Map getLogTypes() {
		return log_stream_type;
	}

	public static Map getServerShipperTypes() {
		Map map = new HashMap();
		map.put("redis", "redis");
//		map.put("syslog", "syslog");
//		map.put("tcp/udp", "tcp_udp");
//		map.put("上传文件", "upload_file");
		map.put("文件", "file");
//		map.put("snmpTrap", "snmp_trap");
		map.put("snmp", "snmp");
//		map.put("kepware", "kepware");

		map.put("netflow", "netflow");
		map.put("syslog", "syslog");
//		map.put("syslog", "syslog_listener");
//		map.put("nmap_xml", "nmap_xml");
		map.put("windows", "event_log");
//		map.put("snmp", "snmp");
		return map;
	}

	public static Map getClientShipperTypes() {
		Map map = new HashMap();
		map.put("redis", "redis");
//		map.put("syslog", "syslog");
		map.put("tcp/udp", "tcp_udp");
		map.put("上传文件", "upload_file");
		map.put("文件", "file");
		map.put("snmp trap", "snmp_trap");
		map.put("kepware", "kepware");
		map.put("netflow", "netflow");
		map.put("syslog", "syslog");
		map.put("nmap_xml", "nmap_xml");
//		map.put("windows", "event_log");
//		map.put("snmp", "snmp");
		return map;
	}

	public static Map getIPFields(){
		Map <String,Map<String,String>>map=new HashMap();

		Map map_array_vpn_all_v1=new HashMap();
		map_array_vpn_all_v1.put("src_ip","ArrayVPN源IP");
		map_array_vpn_all_v1.put("dst_ip","ArrayVPN目的IP");
		map.put("array_vpn_all_v1",map_array_vpn_all_v1);

		Map map_yxlink_waf_all_v1=new HashMap();
		map_yxlink_waf_all_v1.put("src_ip","依迅WAF源IP");
		map_yxlink_waf_all_v1.put("dst_ip","依迅WAF目的IP");
		map.put("yxlink_waf_all_v1",map_yxlink_waf_all_v1);

		Map map_hillstone_firewall_all_v1=new HashMap();
		map_hillstone_firewall_all_v1.put("src_ip","山石防火墙源IP");
		map_hillstone_firewall_all_v1.put("dst_ip","山石防火墙目的IP");
		map.put("hillstone_firewall_all_v1",map_hillstone_firewall_all_v1);

		Map map_linux_linux_audit_v1=new HashMap();
		map.put( "linux_linux_audit_v1",map_linux_linux_audit_v1);

		Map map_linux_linux_extrasyum_v1=new HashMap();
		map.put( "linux_linux_extrasyum_v1",map_linux_linux_extrasyum_v1);

		Map map_linux_linux_ftpd_v1=new HashMap();
		map.put( "linux_linux_ftpd_v1",map_linux_linux_ftpd_v1);

		Map map_linux_linux_generalcron_v1=new HashMap();
		map.put( "linux_linux_generalcron_v1",map_linux_linux_generalcron_v1);

		Map map_linux_linux_generalmail_v1=new HashMap();
		map.put("linux_linux_generalmail_v1",map_linux_linux_generalmail_v1);

		Map map_linux_linux_generalmessage_v1=new HashMap();
		map.put("linux_linux_generalmessage_v1",map_linux_linux_generalmessage_v1);

		Map map_linux_linux_generalsecure_v1=new HashMap();
		map.put("linux_linux_generalsecure_v1",map_linux_linux_generalsecure_v1);

		Map map_linux_linux_oslog_v1=new HashMap();
		map.put("linux_linux_oslog_v1",map_linux_linux_oslog_v1);

		Map map_linux_linux_systemdmesg_v1=new HashMap();
		map.put("linux_linux_systemdmesg_v1",map_linux_linux_systemdmesg_v1);

		Map map_apache_apache_error_v1=new HashMap();
		map_apache_apache_error_v1.put("client_ip","Apache客户端IP");
		map.put( "apache_apache_error_v1",map_apache_apache_error_v1);

		Map map_apache_apache_success_v1=new HashMap();
		map_apache_apache_success_v1.put("remoteAddr","Apache客户端IP");
		map.put("apache_apache_success_v1",map_apache_apache_success_v1);

		Map map_nginx_nginx_error_v1=new HashMap();
		map_nginx_nginx_error_v1.put("client_ip","Nginx客户端IP");
		map.put("nginx_nginx_error_v1",map_nginx_nginx_error_v1);

		Map map_nginx_nginx_success_v1=new HashMap();
		map_nginx_nginx_success_v1.put("remoteAddr","Nginx客户端IP");
		map.put("nginx_nginx_success_v1",map_nginx_nginx_success_v1);

		Map map_squid_squid_access_v1=new HashMap();
		map_squid_squid_access_v1.put("client_ip","Squid客户端IP");
		map.put("squid_squid_access_v1",map_squid_squid_access_v1);

		Map map_boyi_catchview_history_v1=new HashMap();
		map.put("boyi_catchview_history_v1",map_boyi_catchview_history_v1);

		Map map_vcenter_netflow_all_v1=new HashMap();
		map_vcenter_netflow_all_v1.put("srcAddr","NetFlow源IP");
		map_vcenter_netflow_all_v1.put("dstAddr","NetFlow目的IP");
		map.put("vcenter_netflow_v10_v1",map_vcenter_netflow_all_v1);

		Map map_nmap_nmap_all_v1=new HashMap();
		map.put("nmap_nmap_all_v1",map_nmap_nmap_all_v1);

		Map map_a10_loadblance_all_v1=new HashMap();
		map.put("a10_loadblance_all_v1",map_a10_loadblance_all_v1);

		Map map_dbapp_apt_all_v1=new HashMap();
		map_dbapp_apt_all_v1.put("src_ip","安桓APT源IP");
		map_dbapp_apt_all_v1.put("dst_ip","安桓APT目的IP");
		map.put("dbapp_apt_all_v1",map_dbapp_apt_all_v1);

		Map map_asiainfo_tda_all_v1=new HashMap();
		map.put("asiainfo_tda_all_v1",map_asiainfo_tda_all_v1);

		Map map_checkpoint_firewall_all_v1=new HashMap();
		map.put("checkpoint_firewall_all_v1",map_checkpoint_firewall_all_v1);

		Map map_fortinet_firewall_all_v1=new HashMap();
		map.put("fortinet_firewall_all_v2",map_fortinet_firewall_all_v1);

		Map map_ibm_websphere_appjvmout_v1=new HashMap();
		map.put("ibm_websphere_appjvmout_v1",map_ibm_websphere_appjvmout_v1);

		Map map_ibm_websphere_httpaccess_v1=new HashMap();
		map.put("ibm_websphere_httpaccess_v1",map_ibm_websphere_httpaccess_v1);

		Map map_ibm_websphere_httperror_v1=new HashMap();
		map.put("ibm_websphere_httperror_v1",map_ibm_websphere_httperror_v1);

		Map map_microsoft_webiis_all_v1=new HashMap();
		map.put("microsoft_webiis_all_v1",map_microsoft_webiis_all_v1);

		Map map_oracle_mysql_error_v1=new HashMap();
		map.put("oracle_mysql_error_v1",map_oracle_mysql_error_v1);

		Map map_oracle_oracle_log_v1=new HashMap();
		map.put("oracle_oracle_log_v1",map_oracle_oracle_log_v1);

		Map map_panabit_panabit_app_v1=new HashMap();
		map.put("panabit_panabit_app_v1",map_panabit_panabit_app_v1);

		Map map_panabit_panabit_url_v1=new HashMap();
		map.put( "panabit_panabit_url_v1",map_panabit_panabit_url_v1);

		Map map_sangfor_firewall_all_v1=new HashMap();
		map_sangfor_firewall_all_v1.put("src_ip","深信服防火墙源IP");
		map_sangfor_firewall_all_v1.put("dst_ip","深信服防火墙目的IP");
		map.put("sangfor_firewall_all_v1",map_sangfor_firewall_all_v1);

		Map map_sas_sas_log_v1=new HashMap();
		map_sas_sas_log_v1.put("src_ip","Sas源IP");
		map.put("sas_sas_log_v1",map_sas_sas_log_v1);

		Map map_microsoft_windows_all_v1=new HashMap();
		map.put( "microsoft_windows_all_v1",map_microsoft_windows_all_v1);

		Map map_genvision_multimedia_all_v1=new HashMap();
		map.put("genvision_multimedia_all_v1",map_genvision_multimedia_all_v1);

		Map map_h3c_outerrouter_all_v1=new HashMap();
		map_h3c_outerrouter_all_v1.put("login_src_ip","H3c进出口路由器登录源IP");
		map_h3c_outerrouter_all_v1.put("cmd_IP","H3C进出口路由器命令源IP");
		map.put("h3c_outerrouter_all_v1",map_h3c_outerrouter_all_v1);

		Map map_yamu_dns_all_v1=new HashMap();
		map_yamu_dns_all_v1.put("A1_ip","牙木DNSA1IP");
		map_yamu_dns_all_v1.put("src_ip","牙木DNSA1IP");
		map.put("yamu_dns_all_v1",map_yamu_dns_all_v1);

		Map map_eisoo_network_disk_v1=new HashMap();
		map.put("eisoo_network_disk_v1",map_eisoo_network_disk_v1);

		Map map_alcatel_switch_all_v1=new HashMap();
		map.put("alcatel_switch_all_v1",map_alcatel_switch_all_v1);

		Map map_windows_dhcp_all_v1=new HashMap<>();
		map.put("windows_dhcp_all_v1",map_windows_dhcp_all_v1);

		Map map_windows_dns_all_v1=new HashMap<>();
		map.put("windows_dns_all_v1",map_windows_dns_all_v1);
		//以下5个暂时屏蔽
//		Map map_sangfor_vpn_all_v1=new HashMap();
//		map_sangfor_vpn_all_v1.put("src_ip","深信服VPN源IP");
//		map_sangfor_vpn_all_v1.put("dst_ip","深信服VPN目的IP");
//		map.put("sangfor_vpn_all_v1",map_sangfor_vpn_all_v1);
//
//		Map map_huawei_usg_all_v1=new HashMap();
//		map_huawei_usg_all_v1.put("s_ip","华为USG源IP");
//		map_huawei_usg_all_v1.put("d_ip","华为USG目的IP");
//		map.put("huawei_usg_all_v1",map_huawei_usg_all_v1);
//
//		Map map_huawei_uma_all_v1=new HashMap();
//		map_huawei_uma_all_v1.put("src_ip","华为堡垒机src_ip");
//		map_huawei_uma_all_v1.put("loginip","华为堡垒机loginip");
//		map_huawei_uma_all_v1.put("srvaddr","华为堡垒机srvaddr");
//		map_huawei_uma_all_v1.put("srvname","华为堡垒机srvname");
//		map.put("huawei_uma_all_v1",map_huawei_uma_all_v1);
//
//		Map map_dbapp_audit_all_v1=new HashMap();
//		map_dbapp_audit_all_v1.put("sip","安桓数据库审计源IP");
//		map_dbapp_audit_all_v1.put("dip","安桓数据库审计目的IP");
//		map.put("dbapp_audit_all_v1",map_dbapp_audit_all_v1);
//
//		Map map_drcom_radius_all_v1=new HashMap();
//		map.put("drcom_radius_all_v1",map_drcom_radius_all_v1);

		Map map_trendmicro_imsa_mailgateway_v1=new HashMap();
		map_trendmicro_imsa_mailgateway_v1.put("relay_server_ip","趋势邮件网关转发服务器IP");
		map_trendmicro_imsa_mailgateway_v1.put("client_ip","趋势邮件网关客户端IP");
		map.put("trendmicro_imsa_mailgateway_v1",map_trendmicro_imsa_mailgateway_v1);

		Map map_sangfor_accesscontrol_all_v1=new HashMap();
		map_sangfor_accesscontrol_all_v1.put("hst_ip","深信服AC主机IP");
		map_sangfor_accesscontrol_all_v1.put("dst_ip","深信服AC目的IP");
		map.put("sangfor_accesscontrol_all_v1",map_sangfor_accesscontrol_all_v1);//电机学院也有此需求，但实际写的时候参考的博世的日志

		Map map_default=new HashMap();
		map.put("boyi_default_all_v1",map_default);


		return map;
	}

	public static Map getAllIndexerInfos(){
		Map map=new HashMap();

		IndexerInfosResult indexerInfosResults_array_vpn_all_v1=new IndexerInfosResult();
		indexerInfosResults_array_vpn_all_v1.setVendor("array");
		indexerInfosResults_array_vpn_all_v1.setApplication_name("VPN");
		indexerInfosResults_array_vpn_all_v1.setIndexer_version("V1");
		indexerInfosResults_array_vpn_all_v1.setLog_style("all");
		String tag_indexerInfosResults_array_vpn_all_v1[]={"安全设备","VPN","Array"};
		indexerInfosResults_array_vpn_all_v1.setTags(tag_indexerInfosResults_array_vpn_all_v1);
		map.put("array_vpn_all_v1",indexerInfosResults_array_vpn_all_v1);

		IndexerInfosResult yxlink_waf_all_v1=new IndexerInfosResult();
		yxlink_waf_all_v1.setVendor("yxlink");
		yxlink_waf_all_v1.setApplication_name("WAF");
		yxlink_waf_all_v1.setIndexer_version("V1");
		yxlink_waf_all_v1.setLog_style("all");
		String tags_yxlink_waf_all_v1[]={"安全设备","Web应用防护","依迅"};
		yxlink_waf_all_v1.setTags(tags_yxlink_waf_all_v1);
		map.put("yxlink_waf_all_v1",yxlink_waf_all_v1);

		IndexerInfosResult map_hillstone_firewall_all_v1=new IndexerInfosResult();
		map_hillstone_firewall_all_v1.setVendor("hillStone");
		map_hillstone_firewall_all_v1.setApplication_name("fireWall");
		map_hillstone_firewall_all_v1.setIndexer_version("V1");
		map_hillstone_firewall_all_v1.setLog_style("all");
		String tag_map_hillstone_firewall_all_v1[]={"安全设备","防火墙","山石Hillstone"};
		map_hillstone_firewall_all_v1.setTags(tag_map_hillstone_firewall_all_v1);
		map.put("hillstone_firewall_all_v1",map_hillstone_firewall_all_v1);

		IndexerInfosResult map_linux_linux_audit_v1=new IndexerInfosResult();
		String tag_map_linux_linux_audit_v1[]={"操作系统","Linux主机日志"};
		map_linux_linux_audit_v1.setVendor("linux");
		map_linux_linux_audit_v1.setApplication_name("linux");
		map_linux_linux_audit_v1.setIndexer_version("V1");
		map_linux_linux_audit_v1.setLog_style("audit");
		map_linux_linux_audit_v1.setTags(tag_map_linux_linux_audit_v1);
		map.put("linux_linux_audit_v1",map_linux_linux_audit_v1);

		IndexerInfosResult map_linux_linux_extrasyum_v1=new IndexerInfosResult();
		String tag_map_linux_linux_extrasyum_v1[]={"操作系统","Linux主机日志"};
		map_linux_linux_extrasyum_v1.setVendor("linux");
		map_linux_linux_extrasyum_v1.setApplication_name("linux");
		map_linux_linux_extrasyum_v1.setIndexer_version("V1");
		map_linux_linux_extrasyum_v1.setLog_style("extrasyum");
		map_linux_linux_extrasyum_v1.setTags(tag_map_linux_linux_extrasyum_v1);
		map.put( "linux_linux_extrasyum_v1",map_linux_linux_extrasyum_v1);

		IndexerInfosResult map_linux_linux_ftpd_v1=new IndexerInfosResult();
		String tag_map_linux_linux_ftpd_v1[]={"操作系统","Linux主机日志"};
		map_linux_linux_ftpd_v1.setVendor("linux");
		map_linux_linux_ftpd_v1.setApplication_name("linux");
		map_linux_linux_ftpd_v1.setIndexer_version("V1");
		map_linux_linux_ftpd_v1.setLog_style("ftpd");
		map_linux_linux_ftpd_v1.setTags(tag_map_linux_linux_ftpd_v1);
		map.put( "linux_linux_ftpd_v1",map_linux_linux_ftpd_v1);

		IndexerInfosResult map_linux_linux_generalcron_v1=new IndexerInfosResult();
		String tag_map_linux_linux_generalcron_v1[]={"操作系统","Linux主机日志"};
		map_linux_linux_generalcron_v1.setVendor("linux");
		map_linux_linux_generalcron_v1.setApplication_name("linux");
		map_linux_linux_generalcron_v1.setIndexer_version("V1");
		map_linux_linux_generalcron_v1.setLog_style("generalcron");
		map_linux_linux_generalcron_v1.setTags(tag_map_linux_linux_generalcron_v1);
		map.put( "linux_linux_generalcron_v1",map_linux_linux_generalcron_v1);

		IndexerInfosResult map_linux_linux_generalmail_v1=new IndexerInfosResult();
		String tag_map_linux_linux_generalmail_v1[]={"操作系统","Linux主机日志"};
		map_linux_linux_generalmail_v1.setVendor("linux");
		map_linux_linux_generalmail_v1.setApplication_name("linux");
		map_linux_linux_generalmail_v1.setIndexer_version("V1");
		map_linux_linux_generalmail_v1.setLog_style("generalmail");
		map_linux_linux_generalmail_v1.setTags(tag_map_linux_linux_generalmail_v1);
		map.put("linux_linux_generalmail_v1",map_linux_linux_generalmail_v1);

		IndexerInfosResult map_linux_linux_generalmessage_v1=new IndexerInfosResult();
		String tag_map_linux_linux_generalmessage_v1[]={"操作系统","Linux主机日志"};
		map_linux_linux_generalmessage_v1.setVendor("linux");
		map_linux_linux_generalmessage_v1.setApplication_name("linux");
		map_linux_linux_generalmessage_v1.setIndexer_version("V1");
		map_linux_linux_generalmessage_v1.setLog_style("generalmessage");
		map_linux_linux_generalmessage_v1.setTags(tag_map_linux_linux_generalmessage_v1);
		map.put("linux_linux_generalmessage_v1",map_linux_linux_generalmessage_v1);

		IndexerInfosResult map_linux_linux_generalsecure_v1=new IndexerInfosResult();
		String tag_map_linux_linux_generalsecure_v1[]={"操作系统","Linux主机日志"};
		map_linux_linux_generalsecure_v1.setVendor("linux");
		map_linux_linux_generalsecure_v1.setApplication_name("linux");
		map_linux_linux_generalsecure_v1.setIndexer_version("V1");
		map_linux_linux_generalsecure_v1.setLog_style("generalsecure");
		map_linux_linux_generalsecure_v1.setTags(tag_map_linux_linux_generalsecure_v1);
		map.put("linux_linux_generalsecure_v1",map_linux_linux_generalsecure_v1);

		IndexerInfosResult map_linux_linux_oslog_v1=new IndexerInfosResult();
		String tag_map_linux_linux_oslog_v1[]={"操作系统","Linux主机日志"};
		map_linux_linux_oslog_v1.setVendor("linux");
		map_linux_linux_oslog_v1.setApplication_name("linux");
		map_linux_linux_oslog_v1.setIndexer_version("V1");
		map_linux_linux_oslog_v1.setLog_style("oslog");
		map_linux_linux_oslog_v1.setTags(tag_map_linux_linux_oslog_v1);
		map.put("linux_linux_oslog_v1",map_linux_linux_oslog_v1);

		IndexerInfosResult map_linux_linux_systemdmesg_v1=new IndexerInfosResult();
		String tag_map_linux_linux_systemdmesg_v1[]={"操作系统","Linux主机日志"};
		map_linux_linux_systemdmesg_v1.setVendor("linux");
		map_linux_linux_systemdmesg_v1.setApplication_name("linux");
		map_linux_linux_systemdmesg_v1.setIndexer_version("V1");
		map_linux_linux_systemdmesg_v1.setLog_style("systemdmesg");
		map_linux_linux_systemdmesg_v1.setTags(tag_map_linux_linux_systemdmesg_v1);
		map.put("linux_linux_systemdmesg_v1",map_linux_linux_systemdmesg_v1);

		IndexerInfosResult map_apache_apache_error_v1=new IndexerInfosResult();
		String tag_map_apache_apache_error_v1[]={"网络应用","Web应用","Apache"};
		map_apache_apache_error_v1.setVendor("apacheSoftwareFoundation");
		map_apache_apache_error_v1.setApplication_name("apache");
		map_apache_apache_error_v1.setIndexer_version("V1");
		map_apache_apache_error_v1.setLog_style("error");
		map_apache_apache_error_v1.setTags(tag_map_apache_apache_error_v1);
		map.put( "apache_apache_error_v1",map_apache_apache_error_v1);

		IndexerInfosResult map_apache_apache_success_v1=new IndexerInfosResult();
		String tag_map_apache_apache_success_v1[]={"网络应用","Web应用","Apache"};
		map_apache_apache_success_v1.setVendor("apacheSoftwareFoundation");
		map_apache_apache_success_v1.setApplication_name("apache");
		map_apache_apache_success_v1.setIndexer_version("V1");
		map_apache_apache_success_v1.setLog_style("success");
		map_apache_apache_success_v1.setTags(tag_map_apache_apache_success_v1);
		map.put("apache_apache_success_v1",map_apache_apache_success_v1);

		IndexerInfosResult map_nginx_nginx_error_v1=new IndexerInfosResult();
		String tag_map_nginx_nginx_error_v1[]={"网络应用","Web应用","Nginx"};
		map_nginx_nginx_error_v1.setVendor("nginx");
		map_nginx_nginx_error_v1.setApplication_name("nginx");
		map_nginx_nginx_error_v1.setIndexer_version("V1");
		map_nginx_nginx_error_v1.setLog_style("error");
		map_nginx_nginx_error_v1.setTags(tag_map_nginx_nginx_error_v1);
		map.put("nginx_nginx_error_v1",map_nginx_nginx_error_v1);

		IndexerInfosResult map_nginx_nginx_success_v1=new IndexerInfosResult();
		String tag_map_nginx_nginx_success_v1[]={"网络应用","Web应用","Nginx"};
		map_nginx_nginx_success_v1.setVendor("nginx");
		map_nginx_nginx_success_v1.setApplication_name("nginx");
		map_nginx_nginx_success_v1.setIndexer_version("V1");
		map_nginx_nginx_success_v1.setLog_style("success");
		map_nginx_nginx_success_v1.setTags(tag_map_nginx_nginx_success_v1);
		map.put("nginx_nginx_success_v1",map_nginx_nginx_success_v1);

		IndexerInfosResult map_squid_squid_access_v1=new IndexerInfosResult();
		String tag_map_squid_squid_access_v1[]={"网络应用","反向代理","Squid"};
		map_squid_squid_access_v1.setVendor("squid");
		map_squid_squid_access_v1.setApplication_name("squid");
		map_squid_squid_access_v1.setIndexer_version("V1");
		map_squid_squid_access_v1.setLog_style("access");
		map_squid_squid_access_v1.setTags(tag_map_squid_squid_access_v1);
		map.put("squid_squid_access_v1",map_squid_squid_access_v1);

		IndexerInfosResult map_boyi_catchview_history_v1=new IndexerInfosResult();
		String tag_map_boyi_catchview_history_v1[]={"其他","系统性能历史数据日志","博弋","catchview"};
		map_boyi_catchview_history_v1.setVendor("boyi");
		map_boyi_catchview_history_v1.setApplication_name("catchview");
		map_boyi_catchview_history_v1.setIndexer_version("V1");
		map_boyi_catchview_history_v1.setLog_style("history");
		map_boyi_catchview_history_v1.setTags(tag_map_boyi_catchview_history_v1);
		map.put("boyi_catchview_history_v1",map_boyi_catchview_history_v1);

		IndexerInfosResult map_vecenter_netflow_all_v1=new IndexerInfosResult();
		String tag_map_vecenter_netflow_all_v1[]={"网络流量","Netflow"};
		map_vecenter_netflow_all_v1.setVendor("vcenter");
		map_vecenter_netflow_all_v1.setApplication_name("netflow");
		map_vecenter_netflow_all_v1.setIndexer_version("V1");
		map_vecenter_netflow_all_v1.setLog_style("V10");
		map_vecenter_netflow_all_v1.setTags(tag_map_vecenter_netflow_all_v1);
		map.put("vcenter_netflow_v10_v1",map_vecenter_netflow_all_v1);

		IndexerInfosResult map_nmap_nmap_all_v1=new IndexerInfosResult();
		String tag_map_nmap_nmap_all_v1[]={};//高：提供的pdf无此相关数据 @Date 3:36 PM 2019/8/16
		map_nmap_nmap_all_v1.setVendor("nmap");
		map_nmap_nmap_all_v1.setApplication_name("nmap");
		map_nmap_nmap_all_v1.setIndexer_version("V1");
		map_nmap_nmap_all_v1.setLog_style("all");
		map_nmap_nmap_all_v1.setTags(tag_map_nmap_nmap_all_v1);
		map.put("nmap_nmap_all_v1",map_nmap_nmap_all_v1);

		IndexerInfosResult map_a10_loadblance_all_v1=new IndexerInfosResult();
		String tag_map_a10_loadblance_all_v1[]={"网络设备","负载均衡","A10"};
		map_a10_loadblance_all_v1.setVendor("A10");
		map_a10_loadblance_all_v1.setApplication_name("loadblance");
		map_a10_loadblance_all_v1.setIndexer_version("V1");
		map_a10_loadblance_all_v1.setLog_style("all");
		map_a10_loadblance_all_v1.setTags(tag_map_a10_loadblance_all_v1);
		map.put("a10_loadblance_all_v1",map_a10_loadblance_all_v1);

		IndexerInfosResult map_dbapp_apt_all_v1=new IndexerInfosResult();
		String tag_map_dbapp_apt_all_v1[]={"安全设备","apt攻击预警平台","安桓"};
		map_dbapp_apt_all_v1.setVendor("dbapp");
		map_dbapp_apt_all_v1.setApplication_name("apt");
		map_dbapp_apt_all_v1.setIndexer_version("V1");
		map_dbapp_apt_all_v1.setLog_style("all");
		map_dbapp_apt_all_v1.setTags(tag_map_dbapp_apt_all_v1);
		map.put("dbapp_apt_all_v1",map_dbapp_apt_all_v1);

		IndexerInfosResult map_asiainfo_tda_all_v1=new IndexerInfosResult();
		String tag_map_asiainfo_tda_all_v1[]={"安全设备","深度威胁发现设备","亚信","TDA"};
		map_asiainfo_tda_all_v1.setVendor("asiainfo");
		map_asiainfo_tda_all_v1.setApplication_name("tda");
		map_asiainfo_tda_all_v1.setIndexer_version("V1");
		map_asiainfo_tda_all_v1.setLog_style("all");
		map_asiainfo_tda_all_v1.setTags(tag_map_asiainfo_tda_all_v1);
		map.put("asiainfo_tda_all_v1",map_asiainfo_tda_all_v1);

		IndexerInfosResult map_checkpoint_firewall_all_v1=new IndexerInfosResult();
		String map_map_checkpoint_firewall_all_v1[]={"安全设备","防火墙","CheckPoint"};
		map_checkpoint_firewall_all_v1.setVendor("checkPoint");
		map_checkpoint_firewall_all_v1.setApplication_name("firewall");
		map_checkpoint_firewall_all_v1.setIndexer_version("V1");
		map_checkpoint_firewall_all_v1.setLog_style("all");
		map_checkpoint_firewall_all_v1.setTags(map_map_checkpoint_firewall_all_v1);
		map.put("checkpoint_firewall_all_v1",map_checkpoint_firewall_all_v1);

		IndexerInfosResult map_fortinet_firewall_all_v1=new IndexerInfosResult();
		String tag_map_fortinet_firewall_all_v1[]={"安全设备","防火墙","Fortinet"};
		map_fortinet_firewall_all_v1.setVendor("fortinet");
		map_fortinet_firewall_all_v1.setApplication_name("firewall");
		map_fortinet_firewall_all_v1.setIndexer_version("V2");
		map_fortinet_firewall_all_v1.setLog_style("all");
		map_fortinet_firewall_all_v1.setTags(tag_map_fortinet_firewall_all_v1);
		map.put("fortinet_firewall_all_v2",map_fortinet_firewall_all_v1);

		IndexerInfosResult map_ibm_websphere_appjvmout_v1=new IndexerInfosResult();
		String tag_map_ibm_websphere_appjvmout_v1[]={"网络应用","中间件系统","IBM WebSphere"};
		map_ibm_websphere_appjvmout_v1.setVendor("IBM");
		map_ibm_websphere_appjvmout_v1.setApplication_name("websphere");
		map_ibm_websphere_appjvmout_v1.setIndexer_version("V1");
		map_ibm_websphere_appjvmout_v1.setLog_style("appjvmout");
		map_ibm_websphere_appjvmout_v1.setTags(tag_map_ibm_websphere_appjvmout_v1);
		map.put("ibm_websphere_appjvmout_v1",map_ibm_websphere_appjvmout_v1);

		IndexerInfosResult map_ibm_websphere_httpaccess_v1=new IndexerInfosResult();
		String tag_map_ibm_websphere_httpaccess_v1[]={"网络应用","中间件系统","IBM WebSphere"};
		map_ibm_websphere_httpaccess_v1.setVendor("IBM");
		map_ibm_websphere_httpaccess_v1.setApplication_name("websphere");
		map_ibm_websphere_httpaccess_v1.setIndexer_version("V1");
		map_ibm_websphere_httpaccess_v1.setLog_style("httpaccess");
		map_ibm_websphere_httpaccess_v1.setTags(tag_map_ibm_websphere_httpaccess_v1);
		map.put("ibm_websphere_httpaccess_v1",map_ibm_websphere_httpaccess_v1);

		IndexerInfosResult map_ibm_websphere_httperror_v1=new IndexerInfosResult();
		String tag_map_ibm_websphere_httperror_v1[]={"网络应用","中间件系统","IBM WebSphere"};
		map_ibm_websphere_httperror_v1.setVendor("IBM");
		map_ibm_websphere_httperror_v1.setApplication_name("websphere");
		map_ibm_websphere_httperror_v1.setIndexer_version("V1");
		map_ibm_websphere_httperror_v1.setLog_style("httperror");
		map_ibm_websphere_httperror_v1.setTags(tag_map_ibm_websphere_httperror_v1);
		map.put("ibm_websphere_httperror_v1",map_ibm_websphere_httperror_v1);

		IndexerInfosResult map_microsoft_webiis_all_v1=new IndexerInfosResult();
		String tag_map_microsoft_webiis_all_v1[]={"网络应用","Web应用","IIS"};
		map_microsoft_webiis_all_v1.setVendor("microsoft");
		map_microsoft_webiis_all_v1.setApplication_name("webiis");
		map_microsoft_webiis_all_v1.setIndexer_version("V1");
		map_microsoft_webiis_all_v1.setLog_style("all");
		map_microsoft_webiis_all_v1.setTags(tag_map_microsoft_webiis_all_v1);
		map.put("microsoft_webiis_all_v1",map_microsoft_webiis_all_v1);

		IndexerInfosResult map_oracle_mysql_error_v1=new IndexerInfosResult();
		String tag_map_oracle_mysql_error_v1[]={"网络应用","数据库应用","Oracle"};
		map_oracle_mysql_error_v1.setVendor("oracle");
		map_oracle_mysql_error_v1.setApplication_name("mysql");
		map_oracle_mysql_error_v1.setIndexer_version("V1");
		map_oracle_mysql_error_v1.setLog_style("error");
		map_oracle_mysql_error_v1.setTags(tag_map_oracle_mysql_error_v1);
		map.put("oracle_mysql_error_v1",map_oracle_mysql_error_v1);

		IndexerInfosResult map_oracle_oracle_log_v1=new IndexerInfosResult();
		String tag_map_oracle_oracle_log_v1[]={"网络应用","数据库应用","Oracle"};
		map_oracle_oracle_log_v1.setVendor("oracle");
		map_oracle_oracle_log_v1.setApplication_name("oracle");
		map_oracle_oracle_log_v1.setIndexer_version("V1");
		map_oracle_oracle_log_v1.setLog_style("log");
		map_oracle_oracle_log_v1.setTags(tag_map_oracle_oracle_log_v1);
		map.put("oracle_oracle_log_v1",map_oracle_oracle_log_v1);

		IndexerInfosResult map_panabit_panabit_app_v1=new IndexerInfosResult();
		String tag_map_panabit_panabit_app_v1[]={"网络设备","流控设备","派网","Panabit"};
		map_panabit_panabit_app_v1.setVendor("panabit");
		map_panabit_panabit_app_v1.setApplication_name("panabit");
		map_panabit_panabit_app_v1.setIndexer_version("V1");
		map_panabit_panabit_app_v1.setLog_style("app");
		map_panabit_panabit_app_v1.setTags(tag_map_panabit_panabit_app_v1);
		map.put("panabit_panabit_app_v1",map_panabit_panabit_app_v1);

		IndexerInfosResult map_panabit_panabit_url_v1=new IndexerInfosResult();
		String tag_map_panabit_panabit_url_v1[]={"网络设备","流控设备","派网","Panabit"};
		map_panabit_panabit_url_v1.setVendor("panabit");
		map_panabit_panabit_url_v1.setApplication_name("panabit");
		map_panabit_panabit_url_v1.setIndexer_version("V1");
		map_panabit_panabit_url_v1.setLog_style("url");
		map_panabit_panabit_url_v1.setTags(tag_map_panabit_panabit_url_v1);
		map.put( "panabit_panabit_url_v1",map_panabit_panabit_url_v1);

		IndexerInfosResult map_sangfor_firewall_all_v1=new IndexerInfosResult();
		String tag_map_sangfor_firewall_all_v1[]={"安全设备","防火墙","深信服","下一代防火墙"};
		map_sangfor_firewall_all_v1.setVendor("sangFor");
		map_sangfor_firewall_all_v1.setApplication_name("firewall");
		map_sangfor_firewall_all_v1.setIndexer_version("V1");
		map_sangfor_firewall_all_v1.setLog_style("all");
		map_sangfor_firewall_all_v1.setTags(tag_map_sangfor_firewall_all_v1);
		map.put("sangfor_firewall_all_v1",map_sangfor_firewall_all_v1);

		IndexerInfosResult map_sas_sas_log_v1=new IndexerInfosResult();
		String tag_map_sas_sas_log_v1[]={"安全设备","数据库审计","绿盟","SAS"};
		map_sas_sas_log_v1.setVendor("sas");
		map_sas_sas_log_v1.setApplication_name("sas");
		map_sas_sas_log_v1.setIndexer_version("V1");
		map_sas_sas_log_v1.setLog_style("log");
		map_sas_sas_log_v1.setTags(tag_map_sas_sas_log_v1);
		map.put("sas_sas_log_v1",map_sas_sas_log_v1);

		IndexerInfosResult map_microsoft_windows_all_v1=new IndexerInfosResult();
		String tag_map_microsoft_windows_all_v1[]={"操作系统","Windows主机日志"};
		map_microsoft_windows_all_v1.setVendor("microsoft");
		map_microsoft_windows_all_v1.setApplication_name("windows");
		map_microsoft_windows_all_v1.setIndexer_version("V1");
		map_microsoft_windows_all_v1.setLog_style("all");
		map_microsoft_windows_all_v1.setTags(tag_map_microsoft_windows_all_v1);
		map.put( "microsoft_windows_all_v1",map_microsoft_windows_all_v1);

		IndexerInfosResult map_genvision_multimedia_all_v1=new IndexerInfosResult();
		String tag_map_genvision_multimedia_all_v1[]={"业务系统","多媒体智慧管理平台"};
		map_genvision_multimedia_all_v1.setVendor("genvision");
		map_genvision_multimedia_all_v1.setApplication_name("multimedia");
		map_genvision_multimedia_all_v1.setIndexer_version("v1");
		map_genvision_multimedia_all_v1.setLog_style("all");
		map_genvision_multimedia_all_v1.setTags(tag_map_genvision_multimedia_all_v1);
		map.put("genvision_multimedia_all_v1",map_genvision_multimedia_all_v1);

		IndexerInfosResult map_h3c_outerrouter_all_v1=new IndexerInfosResult();
		String tag_map_h3c_outerrouter_all_v1[]={"网络设备","交换机","H3C"};
		map_h3c_outerrouter_all_v1.setVendor("H3C");
		map_h3c_outerrouter_all_v1.setApplication_name("outerrouter");
		map_h3c_outerrouter_all_v1.setIndexer_version("V1");
		map_h3c_outerrouter_all_v1.setLog_style("all");
		map_h3c_outerrouter_all_v1.setTags(tag_map_h3c_outerrouter_all_v1);
		map.put("h3c_outerrouter_all_v1",map_h3c_outerrouter_all_v1);

		IndexerInfosResult map_yamu_dns_all_v1=new IndexerInfosResult();
		String tag_map_yamu_dns_all_v1[]={"网络设备","DNS服务器","牙木"};
		map_yamu_dns_all_v1.setVendor("yamu");
		map_yamu_dns_all_v1.setApplication_name("DNS");
		map_yamu_dns_all_v1.setIndexer_version("V1");
		map_yamu_dns_all_v1.setLog_style("all");
		map_yamu_dns_all_v1.setTags(tag_map_yamu_dns_all_v1);
		map.put("yamu_dns_all_v1",map_yamu_dns_all_v1);

		//以下5个暂时屏蔽
//		IndexerInfosResult map_sangfor_vpn_all_v1=new IndexerInfosResult();
//		String tag_map_sangfor_vpn_all_v1[]={"安全设备","VPN","深信服"};
//		map_sangfor_vpn_all_v1.setVendor("sangFor");
//		map_sangfor_vpn_all_v1.setApplication_name("VPN");
//		map_sangfor_vpn_all_v1.setIndexer_version("V1");
//		map_sangfor_vpn_all_v1.setLog_style("all");
//		map_sangfor_vpn_all_v1.setTags(tag_map_sangfor_vpn_all_v1);
//		map.put("sangfor_vpn_all_v1",map_sangfor_vpn_all_v1);
//
//		IndexerInfosResult map_huawei_usg_all_v1=new IndexerInfosResult();
//		String tag_map_huawei_usg_all_v1[]={"安全设备","统一安全网关","华为","USG"};
//		map_huawei_usg_all_v1.setVendor("huaWei");
//		map_huawei_usg_all_v1.setApplication_name("USG");
//		map_huawei_usg_all_v1.setIndexer_version("V1");
//		map_huawei_usg_all_v1.setLog_style("all");
//		map_huawei_usg_all_v1.setTags(tag_map_huawei_usg_all_v1);
//		map.put("huawei_usg_all_v1",map_huawei_usg_all_v1);
//
//		IndexerInfosResult map_huawei_uma_all_v1=new IndexerInfosResult();
//		String tag_map_huawei_uma_all_v1[]={"网络设备","堡垒机","华为"};
//		map_huawei_uma_all_v1.setVendor("huaWei");
//		map_huawei_uma_all_v1.setApplication_name("uma");
//		map_huawei_uma_all_v1.setIndexer_version("V1");
//		map_huawei_uma_all_v1.setLog_style("all");
//		map_huawei_uma_all_v1.setTags(tag_map_huawei_uma_all_v1);
//		map.put("huawei_uma_all_v1",map_huawei_uma_all_v1);
//
//		IndexerInfosResult map_dbapp_audit_all_v1=new IndexerInfosResult();
//		String tag_map_dbapp_audit_all_v1[]={"安全设备","数据库审计","安桓"};
//		map_dbapp_audit_all_v1.setVendor("dbapp");
//		map_dbapp_audit_all_v1.setApplication_name("audit");
//		map_dbapp_audit_all_v1.setIndexer_version("V1");
//		map_dbapp_audit_all_v1.setLog_style("all");
//		map_dbapp_audit_all_v1.setTags(tag_map_dbapp_audit_all_v1);
//		map.put("dbapp_audit_all_v1",map_dbapp_audit_all_v1);
//
//        IndexerInfosResult map_drcom_radius_all_v1=new IndexerInfosResult();
//        String tag_map_drcom_radius_all_v1={"业务系统","城市热点Drcom"};
//		map_drcom_radius_all_v1.setVendor("drcom");
//		map_drcom_radius_all_v1.setApplication_name("radius");
//		map_drcom_radius_all_v1.setIndexer_version("V1");
//		map_drcom_radius_all_v1.setLog_style("all");
//		map_drcom_radius_all_v1.setTags(tag_map_drcom_radius_all_v1);
//		map.put("drcom_radius_all_v1",map_drcom_radius_all_v1);

		IndexerInfosResult map_trendmicro_imsa_mailgateway_v1=new IndexerInfosResult();
		String tag_map_trendmicro_imsa_mailgateway_v1[]={"安全设备","邮件安全网关","趋势科技","IMSA"};
		map_trendmicro_imsa_mailgateway_v1.setVendor("trendMicro");
		map_trendmicro_imsa_mailgateway_v1.setApplication_name("imsa");
		map_trendmicro_imsa_mailgateway_v1.setIndexer_version("V1");
		map_trendmicro_imsa_mailgateway_v1.setLog_style("mailgateway");
		map_trendmicro_imsa_mailgateway_v1.setTags(tag_map_trendmicro_imsa_mailgateway_v1);
		map.put("trendmicro_imsa_mailgateway_v1",map_trendmicro_imsa_mailgateway_v1);

		IndexerInfosResult map_sangfor_accesscontrol_all_v1=new IndexerInfosResult();
		String tag_map_sangfor_accesscontrol_all_v1[]={"安全设备","上网行为管理","深信服"};
		map_sangfor_accesscontrol_all_v1.setVendor("sangFor");
		map_sangfor_accesscontrol_all_v1.setApplication_name("accessControl");
		map_sangfor_accesscontrol_all_v1.setIndexer_version("V1");
		map_sangfor_accesscontrol_all_v1.setLog_style("all");
		map_sangfor_accesscontrol_all_v1.setTags(tag_map_sangfor_accesscontrol_all_v1);
		map.put("sangfor_accesscontrol_all_v1",map_sangfor_accesscontrol_all_v1);

		IndexerInfosResult map_eisoo_network_disk_v1=new IndexerInfosResult();
		String tag_eisoo_network_disk_v1[]={"网络设备","网盘","爱数"};
		map_eisoo_network_disk_v1.setVendor("eisoo");
		map_eisoo_network_disk_v1.setApplication_name("netdisk");
		map_eisoo_network_disk_v1.setIndexer_version("v1");
		map_eisoo_network_disk_v1.setLog_style("all");
		map_eisoo_network_disk_v1.setTags(tag_eisoo_network_disk_v1);


		return map;
	}

	public static void main(String[] args) {
		System.out.println(GsonHelper.toJson(getAllIndexerInfos()));
	}

}
