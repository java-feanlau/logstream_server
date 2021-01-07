package com.boyitech.logstream.server.worker.indexer;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.boyitech.logstream.core.cache.BaseCache;
import com.boyitech.logstream.core.factory.CacheFactory;
import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.GeoIPHelper;
import com.boyitech.logstream.core.util.GrokUtil;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.IPv4Util;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import com.boyitech.logstream.core.worker.indexer.BaseIndexerConfig;
import com.boyitech.logstream.server.manager.ServerManager;
import com.boyitech.logstream.worker.indexer.ApacheApacheErrorV1Indexer;
import com.boyitech.logstream.worker.indexer.ArrayVpnAllV1Indexer;
import com.boyitech.logstream.worker.indexer.AsiainfoTdaAllV1Indexer;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;


/**
 * @author juzheng
 * @Title: IndexerIpFilter
 * @date 2019/8/7 3:43 PM
 * @Description: 测试ip过滤的测试类
 */
public class IndexerIpFilter {
    @Test
    public void initServerAPP() {
        //启动
        ServerManager instance = new ServerManager();
        String soapStart = instance.startIndexerWorker("");
        Map<String, String> startMap = GsonHelper.fromJson(soapStart);
        String startStatus = startMap.get("soap_status");
        assertEquals("200", startStatus);
    }

    @Test
    public void testFilterGeoIP() {
        //String strConfig="{\"logType\":\"array_vpn_all_v1\",\"ipFilter\":{\"src_ip\":[\"{192.100.0.0-192.100.255.255}\",\"{192.101.0.0-192.101.255.255}\"],\"dst_ip\":[\"{192.100.0.0-192.100.255.255}\",\"{102.100.0.0-102.100.255.255}\"]}}";
        String strConfig = "{\"logType\":\"array_vpn_all_v1\",\"ipFilter\":{\"src_ip\":[\"{192.100.10.10-192.100.105.255}\"],\"dst_ip\":[\"{192.100.0.0-192.100.255.255}\",\"{102.100.0.0-102.100.255.255}\"]}}";
        Map mapConfig = GsonHelper.fromJson(strConfig);
        BaseIndexerConfig baseIndexerConfig = new BaseIndexerConfig(mapConfig);
        // GrokUtil.filterGeoIP(baseIndexerConfig);
    }

    /*
     * @Author juzheng
     * @Description 测试字符串是否为合法的ipv4
     * @Date 1:58 PM 2019/8/8
     * @Param []
     * @return void
     */
    @Test
    public void testisIPV4() {
        String scannerIn = "101.32.255.255-";
        String regEx = "(?<![0-9])(?:(?:25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})[.](?:25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})[.](?:25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})[.](?:25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2}))(?![0-9])";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(scannerIn);
        Boolean aBoolean = matcher.matches();
        System.out.println("输入的参数" + scannerIn + "校验" + (aBoolean ? "" : "不") + "通过");
        System.out.println();

        IPv4Util.getIPAddrScope("127.168.1.5/14");
    }

    /*
    * @Author juzheng
    * @Description 测试indexer对于内网ip解析
    * @Date 9:19 AM 2019/8/9
    * @Param []
    * @return void
    */
    @Test
    public void testFilterGeoip() throws InterruptedException {
        //String strConfig = "{\"logType\":\"array_vpn_all_v1\",\"ipFilter\":{\"src_ip\":[\"{192.100.10.10/24}\"],\"dst_ip\":[\"{192.100.0.0/24}\",\"{102.100.0.0/24}\"]}}";
        //language=JSON
        String strConfig="{\n" +
                "    \"logType\": \"array_vpn_all_v1\",\n" +
                "    \"ipFilter\": {\n" +
                "        \"src_ip\": [\n" +
                "            \"192.100.0.0/24\",\n" +
                "            \"172.16.0.0/30\"\n" +
                "        ],\n" +
                "        \"dst_ip\": [\n" +
                "            \"192.100.0.0/24\",\n" +
                "            \"172.16.0.0/30\"\n" +
                "        ]\n" +
                "    }\n" +
                "}";
        Map mapConfig = GsonHelper.fromJson(strConfig);
        BaseIndexerConfig baseIndexerConfig = new BaseIndexerConfig(mapConfig);
        BaseIndexer indexer = new ApacheApacheErrorV1Indexer(baseIndexerConfig);
        indexer.register();
        List<String> testList = new ArrayList<String>();
       // testList.add("id=ArrayOS time=\"2019-1-1 08:35:05\" timezone=CST(+0800) fw=AN pri=6 vpn=sslvpn user=16204040109 proto=ip src=192.100.73.169 sport=63232 dst= dport=443 type=vpn msg=\"VPN: A new VPN tunnel has been established successfully.\"\u0000");
        testList.add("[Sat Dec 01 11gfdsa:58:29 2018] [error] [client 192.100.0.0] File does not exist: /opt/apache/htdocs/wwwroot.zip, referer: http://zp.sbs.edu.cn/wwwroot.zip\n");
        for(String s : testList) {
            Event e = new Event();
            e.setMessage(s);
            indexer.format(e);
            System.out.println(e.getJsonMessage());
        }

    }


    /*
    * @Author juzheng
    * @Description 测试遍历map
    * @Date 9:20 AM 2019/8/9
    * @Param
    * @return
    */
    @Test
    public void testGeoip() throws IOException, GeoIp2Exception {
        Map geoIPInfo = GeoIPHelper.getInstance().getGeoIPInfo("102.220.249.2");
        System.out.println(geoIPInfo);
    }

    /*
    * @Author juzheng
    * @Description 测试ip过滤的最终版，支持~也支持/
    * @Date 2:21 PM 2019/8/15
    * @Param []
    * @return void
    */
    @Test
    public void testIPFilterFinallVersion(){
        String strConfig="{\n" +
                "    \"logType\": \"array_vpn_all_v1\",\n" +
                "    \"ipFilter\": {\n" +
                "        \"src_ip\": [\n" +
                "            \"101.32we.255.255\",\n" +
                "            \"172.16.0.e16.0.3\"\n" +
                "        ],\n" +
                "        \"dst_ip\": [\n" +
                "            \"192.100.0.e100.0.255\",\n" +
                "            \"172.16.0.0e.16.0.3\"\n" +
                "        ]\n" +
                "    }\n" +
                "}";
        Map mapConfig = GsonHelper.fromJson(strConfig);
        BaseIndexerConfig baseIndexerConfig = new BaseIndexerConfig(mapConfig);
        Map<String,Object> format=new HashMap<>();
        format.put("src_ip","101.32.42.22");
        format.put("dst_ip","171.11.11.11");
        boolean ipFilter=GrokUtil.filterGeoIP(baseIndexerConfig,format);
        System.out.println(ipFilter);
        System.out.println(format.size());

    }

    @Test
    public void testIPtoIP(){
        int[] ipAddrsInt = IPv4Util.getIPIntScope("172.16.0.0/30");
        String ipAddrMin = IPv4Util.intToIp(ipAddrsInt[0]);
        String ipAddrMax = IPv4Util.intToIp(ipAddrsInt[1]);
        System.out.println(ipAddrMin+"~"+ipAddrMax);
        //192.100.0.0~192.100.0.255  192.100.0.0/24
        //172.16.0.0~172.16.0.3      172.16.0.0/30
    }

    @Test
    public void testIPs(){
        String strConfig="{\n" +
                "    \"logType\": \"array_vpn_all_v1\"\n" +
                "}";
        Map mapConfig = GsonHelper.fromJson(strConfig);
        BaseIndexerConfig baseIndexerConfig = new BaseIndexerConfig(mapConfig);
        String ipFilter=baseIndexerConfig.getIpFilter();
        if(GrokUtil.isStringHasValue(ipFilter)){
            Map mapIpFilter = JSONObject.parseObject(ipFilter);
            for (Object key : mapIpFilter.keySet()) {
                JSONArray ipValues = (JSONArray) mapIpFilter.get(key);
                if(ipValues.size()>0){
                    for(int i=0;i<ipValues.size();i++){
                        if(!IPv4Util.isIPv4s(ipValues.get(i).toString())){
                            System.out.println(key+"  "+i);
                        }
                    }
                }
            }
        }
    }


}
