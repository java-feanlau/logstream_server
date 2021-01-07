package com.boyitech.logstream.server.worker.indexer;


import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import com.boyitech.logstream.core.worker.indexer.BaseIndexerConfig;
import com.boyitech.logstream.worker.indexer.LinuxLinuxGeneralmailV1Indexer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author juzheng
 * @Title: IndexerBoshiFortinetSingleTest
 * @date 2019/8/22 2:14 PM
 * @Description:
 */
public class IndexerBoshiFortinetSingleTest {
        private BaseIndexer Indexer;

        @Before
        public void InitIndexer() throws InterruptedException {
            System.out.println("---初始化---");
            Map<String, String> map = new HashMap();
            map.put("logType", "1");
            BaseIndexerConfig baseIndexerConfig = new BaseIndexerConfig(map);
            //1.此处修改Indexer类型：如ApacheSuccessIndexer
            Indexer = new LinuxLinuxGeneralmailV1Indexer(baseIndexerConfig);
            Indexer.register();
        }

        @Test
        public void indexerSingle() {
            System.out.println("---执行Indexer的测试---");
            List<String> testList = new ArrayList<String>();
           // testList.add("<189>date=2019-03-07 time=16:24:04 logver=52 devid=FGT3HD3917800365 devname=Yantai-300D-2 logid=0000000013 type=traffic subtype=forward level=notice vd=root srcip=101.132.139.188 srcport=52108 srcintf=\"port1\" dstip=172.20.253.110 dstport=8000 dstintf=\"port3\" poluuid=16bc4164-2655-51e7-18dd-52bb3570b89d sessionid=585230569 proto=6 action=close policyid=77 dstcountry=\"Reserved\" srccountry=\"Reserved\" trandisp=noop service=8000/tcp duration=5 sentbyte=132 rcvdbyte=44 sentpkt=3 rcvdpkt=1");
            //testList.add("<189>date=2019-03-07 time=16:24:05 logver=52 devid=FGT3HD3917800365 devname=Yantai-300D-2 logid=0000000013 type=traffic subtype=for ward level=notice vd=root srcip=172.20.20.108 srcport=54710 srcintf=\"port3\" dstip=190.1.10.6 dstport=53 dstintf=\"port1\" poluuid=b2c9c9ae-24d5-51e7-e904-4777323acc2f sessionid=585222108 proto=17 action=accept policyid=7 dstcountry=\"Argentina\" srccountry=\"Reserved\" trandisp=noop service=DNS duration=184 sentbyte=134 rcvdbyte=0 sentpkt=2 shapersentname=\"high-priority\"");
            //testList.add("Sun May 31 13:23:36 2015 1 172.100.48.251 242740 /sbs/sxzzllkjyb/xyxw/201505/W020150531769886203389.jpg b _ i r sbsforwcmftp ftp 0 * c");
            testList.add("Dec 18 04:00:05 localhost postfix/sendmail[2670]: warning: valid_hostname: numeric hostname: 231.133.13");
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
