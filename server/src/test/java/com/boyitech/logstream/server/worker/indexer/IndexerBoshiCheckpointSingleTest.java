package com.boyitech.logstream.server.worker.indexer;

import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.GrokUtil;
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
 * @Title: IndexerBoshiFortinetTxtTest
 * @date 2019/8/23 9:48 AM
 * @Description:
 */
public class IndexerBoshiCheckpointSingleTest {
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
        testList.add("<85>1 2019-03-07T14:32:38+08:00 172.16.61.32 CP-GW - Log [Fields@1.3.6.1.4.1.2620 Action=\"monitor\" UUid=\"{0x0,0x0,0x0,0x0}\" src=\"172.16.161.84\"  sr3c=\"172.16.161.84\" dst=\"202.110.112.107\" proto=\"6\" message_info=\"Address spoofing\" NAT_rulenum=\"206\" product=\"VPN-1 & FireWall-1\" service=\"80\" s_port=\"53074\" product_family=\"Network\"]");
        for (String s : testList) {
            Event e = new Event();
            e.setMessage(s);
            Indexer.format(e);
             System.out.println(e.getJsonMessage());
        }
    }

    @Test
    public void testSplit(){
        String log_body="UUid=\"{0x5e1d0c69,0x42,0x1a3d10ac,0xc0000000}\" rule=\"44\" rule_uid=\"{9D089B1C-2901-430C-9689-7A89BB1658D4}\" service_id=\"https\" src=\"101.87.154.245\" dst=\"61.172.24.164\" proto=\"6\" xlatedst=\"172.16.200.66\" NAT_rulenum=\"206\" NAT_addtnl_rulenum=\"1\" product=\"VPN-1 & FireWall-1\" service=\"443\" s_port=\"27470\" product_family=\"Network\"]";
        Map<String, Object> log_body_map = new HashMap<>();
        if (GrokUtil.isStringHasValue(log_body)) {
            try {
                String left = new String();
                String right = new String();
                String[] arr = log_body.split("=");
                for (int i = 0; i < arr.length; i++) {
                    if (i == 0 && arr.length > 1) {
                        left = arr[i].replaceAll("\\s+","");
                        right = arr[i + 1].substring(0, arr[i + 1].lastIndexOf(" "));
                    } else if (i > 0 && arr.length > 1 && i < arr.length - 2) {
                        left = arr[i].substring(arr[i].lastIndexOf(" "), arr[i].length()).replaceAll("\\s+","");
                        right = arr[i + 1].substring(0, arr[i + 1].lastIndexOf(" "));
                    } else if (i > 0 && arr.length > 1 && i == arr.length - 1) {
                        left = arr[i - 1].substring(arr[i - 1].lastIndexOf(" "), arr[i - 1].length()).replaceAll("\\s+","");
                        right = arr[i];
                    }

                    left=GrokUtil.setStringValue(left);
                    right=GrokUtil.setStringValue(right);
                    log_body_map.put(left, right);
                    System.out.println(left);
                    System.out.println(right);
                }
            }
            catch (Exception ex){
                System.out.println(ex);
            }
        }
//        System.out.println(log_body_map.);
    }

    @Test
    public void test() {
        String log_body = "appi_name=\"******\" app_desc=\"******\" app_id=\"******\" app_category=\"******\" matched_category=\"******\" app_properties=\"******\" app_risk=\"******\" app_rule_id=\"******\" app_rule_name=\"******\" web_client_type=\"Other: Microsoft-CryptoAPI/6.1\" web_server_type=\"Apache\" app_sig_id=\"60518126:2\" resource=\"http://ocsp.godaddy.com/MEkwRzBFMEMwQTAJBgUrDgMCGgUABBS2CA1fbGt26xPkOKX4ZguoUjM0TgQUQMK9J47MNIMwojPX+2yz8LQsgM4CCAFF31cf/L7G\" proxy_src_ip=\"172.16.161.42\"";
        Map<String, Object> log_body_map = new HashMap<>();
        String left = new String();
        String right = new String();
        String[] arr = log_body.split("\" ");

        for (int i = 0; i < arr.length; i++) {
            left=arr[i].substring(0,arr[i].indexOf("="));
            right=arr[i].substring(arr[i].indexOf("=")+1,arr[i].length()).replace("=\"","").replace("\"","");
            log_body_map.put(left, right);
        }
        System.out.println(GsonHelper.toJson(log_body_map));
}

    @After
    public void AfterOne() {
        System.out.println("---测试结束---");
    }
}
