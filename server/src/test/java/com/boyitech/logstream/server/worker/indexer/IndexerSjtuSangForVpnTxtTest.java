package com.boyitech.logstream.server.worker.indexer;

import com.alibaba.fastjson.JSONObject;
import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import com.boyitech.logstream.core.worker.indexer.BaseIndexerConfig;
import com.boyitech.logstream.worker.indexer.SangforVpnAllV1Indexer;
import com.boyitech.logstream.worker.indexer.YamuDnsAllV1Indexer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author juzheng
 * @Title: IndexerSjtuTxtTest
 * @date 2019/7/26 5:20 PM
 * @Description:  针对sjtu电机学院的日志格式化进行测试
 */
public class IndexerSjtuSangForVpnTxtTest {

    private String FILEPATH;
    private BaseIndexer Indexer;
    @Before
    public void Init(){
        Map<String,String> map=new HashMap();
        map.put( "logType","1");
        BaseIndexerConfig baseIndexerConfig = new BaseIndexerConfig(map);
        //1.此处修改Indexer类型：如AheSuccessIndexer
        Indexer=new SangforVpnAllV1Indexer(baseIndexerConfig);
        //2.此处修改日志样本的txt文件的路径：
        FILEPATH = "/Users/juzheng/Downloads/工作文件夹/sdju/sdju-vpn.txt";
        Indexer.register();

    }

    @Test
    public void indexerTxtTest(){
        System.out.println("---执行Indexer的测试---");
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(new File(FILEPATH)));
            String s = new String();
            int count=0;
            while ((s = br.readLine()) != null)
            {
               // Event e = new Event();
                //e.setMessage(s);
               // Indexer.format(e);
                count++;
                if(count%10==1){
                    s=s.replace("\"message\": \"","").replace("\"","").trim();
                   // System.out.println(s);
                    Event e = new Event();
                    e.setMessage(s);
                    Indexer.format(e);
                    System.out.println(e.getJsonMessage());
                    JSONObject pa=JSONObject.parseObject(e.getJsonMessage());
                    String str=pa.getString("flag");
                    if (str!=null) {
                        System.out.println("第" + count + "行日志解析失败！");
                    }
                }
//                JSONObject pa=JSONObject.parseObject(e.getJsonMessage());
//                String str=pa.getString("flag");
//                if (str!=null) {
//                    System.out.println("第" + count + "行日志解析失败！");
//                }
            }
            br.close();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @After
    public void AfterOne(){
        System.out.println("---测试结束---");
    }




}
