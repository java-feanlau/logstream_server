package com.boyitech.logstream.server.worker.indexer;

import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import com.boyitech.logstream.core.worker.indexer.BaseIndexerConfig;
import com.boyitech.logstream.worker.indexer.HuaweiUmaAllV1Indexer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author juzheng
 * @Title: IndexerSjtuHuaweiUmaTxtTest
 * @date 2019/7/29 1:43 PM
 * @Description:  电机学院华为uma日志格式化的单元测试,
 */
public class IndexerSjtuHuaweiUmaSingleTest {
    private BaseIndexer Indexer;

    @Before
    public void InitIndexer() throws InterruptedException {
        System.out.println("---初始化---");
        Map<String, String> map = new HashMap();
        map.put("logType", "1");
        BaseIndexerConfig baseIndexerConfig = new BaseIndexerConfig(map);
        //1.此处修改Indexer类型：如ApacheSuccessIndexer
        Indexer = new HuaweiUmaAllV1Indexer(baseIndexerConfig);
        Indexer.register();
    }

    @Test
    public void indexerSingle() {
        System.out.println("---执行Indexer的测试---");
        List<String> testList = new ArrayList<String>();
        testList.add("Jul  2 16:03:24 UMA UMA: type=\"login\" date=\"2019-07-02 16:03:24\" srcip=\"172.20.7.10\" sysuser=\"wangfeng\" module=\"XWIN\" result=\"success\"");
        testList.add("Jul  2 14:34:42 UMA UMA: type=\"logout\" date=\"2019-07-02 14:34:42\" srcip=\"172.20.7.9\" sysuser=\"zfsoft2\" result=\"SUCCESS\"");
        testList.add("Jul  3 12:42:42 UMA UMA: type=\"config\" date=\"2019-07-03 12:42:42\" srcip=\"172.17.111.162\" sysuser=\"superman\" module=\"User\" log_action=\"update\" detail=\"data_before:status=2 ;data_after:status=1 ");
        testList.add("Jul 15 11:15:45 UMA pldrun: id=1166920931 account=root srvname=172.19.100.61 loginname=huanghua loginip=172.19.22.43 command_id=12 screen_id=4 logscreen=Using JRE_HOME:        /opt/supwisdom/jdk1.8  \\n");
        testList.add("Jul 15 10:25:22 UMA xwin: sessiontype=logout sessionid=session_1563157484475113 \\n");
        testList.add("Jul 15 14:09:02 UMA xwin: sessiontype=login sessionid=session_1563170941668287 mode=RDP loginip=2.0.1.72 loginname=zfsoft2 srvaddr=172.20.6.32 account=administrator \\n");
        testList.add("Jul 15 11:45:01 UMA pldapp: sessiontype=login sessionid=135017419 mode=ssh loginip=172.19.22.43 loginname=huanghua srvaddr=172.19.100.20 account=root \\n");
        for(String s : testList) {
            Event e = new Event();
            e.setMessage(s);
            Indexer.format(e);
            System.out.println(e.getJsonMessage());
        }
    }

    @After
    public void AfterOne(){
        System.out.println("---测试结束---");
    }
}
