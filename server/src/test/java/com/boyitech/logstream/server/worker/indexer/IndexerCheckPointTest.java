package com.boyitech.logstream.server.worker.indexer;

import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import com.boyitech.logstream.core.worker.indexer.BaseIndexerConfig;
import com.boyitech.logstream.worker.indexer.CheckpointFirewallAllV1Indexer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author juzheng
 * @Title: IndexerCheckPointTest
 * @date 2019/8/23 3:01 PM
 * @Description:
 */
public class IndexerCheckPointTest {
    private BaseIndexer Indexer;

    @Before
    public void InitIndexer() throws InterruptedException {
        System.out.println("---初始化---");
        Map<String, String> map = new HashMap();
        map.put("logType", "1");
        BaseIndexerConfig baseIndexerConfig = new BaseIndexerConfig(map);
        Indexer = new CheckpointFirewallAllV1Indexer(baseIndexerConfig);
        Indexer.register();
    }

    @Test
    public void indexerSingle() {
        System.out.println("---执行Indexer的测试---");
        List<String> testList = new ArrayList<String>();
//        testList.add("<85>1 2019-03-07T14:32:39+08:00 172.16.61.32 CP-GW - Log [Fields@1.3.6.1.4.1.2620 Action=\"monitor\" UUid=\"{0x0,0x0,0x0,0x0}\" src=\"172.16.161.42\" dst=\"103.243.220.234\" proto=\"6\" message_info=\"Address spoofing\" product=\"VPN-1 & FireWall-1\" service=\"80\" s_port=\"49286\" product_family=\"Network\"]\n");
//        testList.add("<85>1 2019-03-07T14:32:39+08:00 172.16.61.32 CP-GW - Log [Fields@1.3.6.1.4.1.2620 Action=\"allow\" UUid=\"{0x5c80acf5,0x0,0x203d10ac,0xc0000001}\" src=\"172.16.161.26\" dst=\"117.18.237.29\" proto=\"6\" appi_name=\"******\" app_desc=\"******\" app_id=\"******\" app_category=\"******\" matched_category=\"******\" app_properties=\"******\" app_risk=\"******\" app_rule_id=\"******\" app_rule_name=\"******\" app_sig_id=\"10075086:3\" proxy_src_ip=\"172.16.161.26\" product=\"Application Control\" service=\"80\" s_port=\"52363\" product_family=\"Network\"]\n");
//        testList.add("<85>1 2019-03-07T14:32:58+08:00 172.16.61.32 CP-GW - Log [Fields@1.3.6.1.4.1.2620 Action=\"allow\" UUid=\"{0x5c80bb1a,0x8,0x203d10ac,0xc0000000}\" src=\"172.16.161.42\" dst=\"106.10.193.31\" proto=\"6\" appi_name=\"******\" app_desc=\"******\" app_id=\"******\" app_category=\"******\" matched_category=\"******\" app_properties=\"******\" app_risk=\"******\" app_rule_id=\"******\" app_rule_name=\"******\" web_client_type=\"Other: Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko\" web_server_type=\"Other: ATS\" app_sig_id=\"60521520:1\" resource=\"http://cms.analytics.yahoo.com/cms?partner_id=MSFT\" proxy_src_ip=\"172.16.161.42\" product=\"Application Control\" service=\"80\" s_port=\"49402\" product_family=\"Network\"]");
//        testList.add("<85>1 2019-03-07T14:35:00+08:00 172.16.61.32 CP-GW - Log [Fields@1.3.6.1.4.1.2620 Action=\"allow\" UUid=\"{0x5c80bb94,0x8,0x203d10ac,0xc0000001}\" src=\"172.16.160.55\" dst=\"117.121.133.231\" proto=\"6\" appi_name=\"******\" app_desc=\"******\" app_id=\"******\" app_category=\"******\" matched_category=\"******\" app_properties=\"******\" app_risk=\"******\" app_rule_id=\"******\" app_rule_name=\"******\" web_client_type=\"Other: Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko\" web_server_type=\"Other: nginx/1.1.19\" resource=\"http://dat.gtags.net/imp/dasp3?a=9&ext_args=&vc=32&vt=29&vpc=2&rvt=29&fr=0&vrt=0&ot=2&os=www.baidu.com&ok=%25E6%2590%25BA%25E7%25A8%258B&u=http%3A%2F%2Fhotels.ctrip.com%2Fhotel%2F712581.html%23ctm_ref%3Dctr_hp_sb_lst&sc=1301*731&ch=utf-8&la=zh-CN&ti=%E6%AD%A6%E6%B1%89%E4%B8%87%E8%BE%BE%E7%91%9E%E5%8D%8E%E9%85%92%E5%BA%97%E9%A2%84%E8%AE%A2%E4%BB%B7%E6%A0%BC%2C%E8%81%94%E7%B3%BB%E7%94%B5%E8%AF%9D%5C%E4%BD%8D%E7%BD%AE%E5%9C%B0%E5%9D%80%E3%80%90%E6%90%BA%E7%A8%8B%E9%85%92%E5%BA%97%E3%80%91&v=3.0.0.9&t=1&r=0.42847194269779476\" proxy_src_ip=\"172.16.160.55\" product=\"URL Filtering\" service=\"80\" s_port=\"50285\" product_family=\"Network\"]");
        //testList.add("<85>1 2019-03-07T14:35:00+08:00 172.16.61.32 CP-GW - Log [Fields@1.3.6.1.4.1.2620 Action=\\\"allow\\\" UUid=\"{0x0,0x0,0x0,0x0}\" ICMP=\"Echo Request\" src=\"172.28.12.36\" dst=\"220.181.38.148\" proto=\"1\" ICMP Type=\"8\" ICMP Code=\"0\" message_info=\"Address spoofing\" product=\"VPN-1 & FireWall-1\" product_family=\"Network\"]");

        for(String s : testList) {
            Event e = new Event();
            e.setMessage(s);
            Indexer.format(e);
            System.out.println(e.getJsonMessage());
        }
    }

    @Test
    public void testLogbody(){
        String log_body="date=2019-03-07 time=16:24:04 logver=52 devid=FGT3HD3917800365 devname=Yantai-300D-2 logid=0000000013 type=traffic subtype=forward level=notice vd=root srcip=172.20.22.56 srcport=64479 srcintf=port3 dstip=204.79.197.200 dstport=443 dstintf=port1 poluuid=8e2e0450-24d7-51e7-ec63-7bd1f9f5ba28 sessionid=585230559 proto=6 action=close policyid=25 dstcountry=United States srccountry=Reserved trandisp=noop service=HTTPS duration=5 sentbyte=132 rcvdbyte=92 sentpkt=3 rcvdpkt=2";
        Map<String, Object> log_body_map = new HashMap<>();
        String left=new String();
        String right=new String();
        String[] arr = log_body.split("=");
        for (int i=0;i<arr.length;i++) {
            if(i==0&&arr.length>1){
                left=arr[i];
                right=arr[i+1].substring(0,arr[i+1].lastIndexOf(" "));
            }
            else if(i>0&&arr.length>1&&i<arr.length-2){
                left=arr[i].substring(arr[i].lastIndexOf(" "),arr[i].length());
                right=arr[i+1].substring(0,arr[i+1].lastIndexOf(" "));
            }
            else if(i>0&&arr.length>1&&i==arr.length-1){
                left=arr[i-1].substring(arr[i-1].lastIndexOf(" "),arr[i-1].length());
                right=arr[i];
            }

            log_body_map.put(left,right);
        }
        System.out.println(GsonHelper.toJson(log_body_map));

    }

    @After
    public void AfterOne(){
        System.out.println("---测试结束---");
    }
}
