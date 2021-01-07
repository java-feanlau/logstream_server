package com.boyitech.logstream.server.worker.indexer;

import com.alibaba.fastjson.JSONObject;
import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import com.boyitech.logstream.core.worker.indexer.BaseIndexerConfig;
import com.boyitech.logstream.worker.indexer.EisooNetworkDiskV1Indexer;
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
 * @Title: IndexerTxtTest
 * @date 2019/7/9 1:41 PM
 * @Description: txt格式的
 */
public class IndexerTxtTest {

    private String FILEPATH;
    private BaseIndexer Indexer;
    @Before
    public void Init() throws InterruptedException {
        Map<String,String> map=new HashMap();
        map.put( "logType","1");
        BaseIndexerConfig baseIndexerConfig = new BaseIndexerConfig(map);
        //1.此处修改Indexer类型：如AheSuccessIndexer
        Indexer=new EisooNetworkDiskV1Indexer(baseIndexerConfig);
        //2.此处修改日志样本的txt文件的路径：
        FILEPATH = "/Users/juzheng/Downloads/博世华域爱数网盘日志格式化需求/hbas-eisoo/2020.02/index_0/type_0/data_0.json";
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
            {   System.out.println(count);
                System.out.println(s);
                Map map=JSONObject.parseObject(s);
                String typeMessageValue=String.valueOf(map.get("message"));
                Event e = new Event();
                e.setMessage(typeMessageValue);
                Indexer.format(e);
                count++;

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
