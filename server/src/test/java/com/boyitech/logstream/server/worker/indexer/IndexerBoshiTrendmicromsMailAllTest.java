package com.boyitech.logstream.server.worker.indexer;

import com.alibaba.fastjson.JSONObject;
import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.GrokUtil;
import com.boyitech.logstream.core.util.IndexerTimeUtils;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import com.boyitech.logstream.core.worker.indexer.BaseIndexerConfig;
import com.boyitech.logstream.worker.indexer.DbappAptAllV1Indexer;
import com.boyitech.logstream.worker.indexer.TrendmicroImsaMailgatewayV1Indexer;
import com.boyitech.logstream.worker.indexer.YxlinkWafAllV1Indexer;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author juzheng
 * @Title: IndexerBoshiTrendmicromsMailAllTest
 * @date 2019/8/1 11:41 AM
 * @Description:   对博世的趋势邮件网关日志解析过程中出现的问题进行测试的测试类
 */
public class IndexerBoshiTrendmicromsMailAllTest {

    /*
    * @Author juzheng
    * @Description  博世目前的趋势邮件网关通过syslog外发是一个json字符串
    * @Date 11:42 AM 2019/8/1
    * @Param [m]
    * @return void
    */
    @Test
    public void getMessage(){
        String m="{\"severity\":6,\"pid\":\"29237\",\"program\":\"postfix/cleanup\",\"message\":\"6631392059: warning: header X-IMSS-TLS-USAGE: Used from unknown[172.16.200.64]; from=<Jia.Liu@boschhuayu-steering.com> to=<Alexander.Uphoff@bosch.com> proto=ESMTP helo=<BHSHV00004.boschhuayu-steering.com>: Used TLS for UNKNOWN\\n\",\"type\":\"trendmicro-imsa\",\"priority\":22,\"logsource\":\"imsa\",\"@timestamp\":\"2019-07-09T03:49:16.000Z\",\"received_at\":\"2019-07-09T14:15:54+08:00\",\"format_level\":\"0\",\"@version\":\"1\",\"host\":\"192.168.10.6\",\"log_class\":\"trendmicro-imsa\",\"facility\":2,\"severity_label\":\"Informational\",\"timestamp\":\"Jul  9 11:49:16\",\"facility_label\":\"mail\"}\n";
        String nm="6631392059: warning: header X-IMSS-TLS-USAGE: Used from unknown[172.16.200.64]; from=<Jia.Liu@boschhuayu-steering.com> to=<Alexander.Uphoff@bosch.com> proto=ESMTP helo=<BHSHV00004.boschhuayu-steering.com>: Used TLS for UNKNOWN";
        JSONObject pa=JSONObject.parseObject(m);
        String str=pa.getString("message");
        System.out.println(str);
    }



    /*
    * @Author juzheng
    * @Description  测试新增的判断是否是json格式字符串的工具类的测试类
    * @Date 1:30 PM 2019/8/1
    * @Param []
    * @return void
    */
    @Test
    public void testIsJson() {
        String m="{\"severity\":6,\"pid\":\"29237\",\"program\":\"postfix/cleanup\",\"message\":\"6631392059: warning: header X-IMSS-TLS-USAGE: Used from unknown[172.16.200.64]; from=<Jia.Liu@boschhuayu-steering.com> to=<Alexander.Uphoff@bosch.com> proto=ESMTP helo=<BHSHV00004.boschhuayu-steering.com>: Used TLS for UNKNOWN\\n\",\"type\":\"trendmicro-imsa\",\"priority\":22,\"logsource\":\"imsa\",\"@timestamp\":\"2019-07-09T03:49:16.000Z\",\"received_at\":\"2019-07-09T14:15:54+08:00\",\"format_level\":\"0\",\"@version\":\"1\",\"host\":\"192.168.10.6\",\"log_class\":\"trendmicro-imsa\",\"facility\":2,\"severity_label\":\"Informational\",\"timestamp\":\"Jul  9 11:49:16\",\"facility_label\":\"mail\"}\n";
        System.out.println(GrokUtil.isJSONValid(m));
       // System.out.println("\"received_at\":\"2019-08-05T14:54:01.821+08:00\"".length());
    }


    /*
    * @Author juzheng
    * @Description  测试时间格式化
    * @Date 4:11 PM 2019/8/1
    * @Param []
    * @return void
    */
    @Test
    public void testDate(){
        Map<String,Object>format=new HashMap<>();
        format.put("datetime","2019/07/09 00:21:19");
        String datetime=IndexerTimeUtils.getISO8601Time("2019/07/09 00:21:19","yyyy/MM/dd HH:mm:ss");
        System.out.println(datetime);
    }

    /*
    * @Author juzheng
    * @Description 测试字符串切割  例：a@b 获取a
    * @Date 4:12 PM 2019/8/1
    * @Param []
    * @return void
    */
    @Test
    public void testSplitStr(){
        String senderStr[]="uogangshen@163.com".split("@");
    }

    @Test
    /*
    * @Author juzheng
    * @Description  从文本中读取json格式的日志进行测试；
    * @Date 4:49 PM 2019/8/1
    * @Param []
    * @return void
    */
    public void testIndexer() throws InterruptedException {
        Map<String,String> map=new HashMap();
        map.put( "logType","1");
        BaseIndexerConfig baseIndexerConfig = new BaseIndexerConfig(map);
        //1.此处修改Indexer类型：如AheSuccessIndexer
        BaseIndexer Indexer=new TrendmicroImsaMailgatewayV1Indexer(baseIndexerConfig);
        //2.此处修改日志样本的txt文件的路径：
        String FILEPATH = "/Users/juzheng/Downloads/工作文件夹/ys3.2boshi log/asiainfo-mail-test.log";
        Indexer.register();

        System.out.println("---执行Indexer的测试---");
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(new File(FILEPATH)));
            String s = new String();
            int count=0;
            while ((s = br.readLine()) != null)
            {
                count++;
                if(count%10==1&&count!=1&&count!=11){
                  //  s=s.replace("\"message\": \"","").replace("\"","").trim();
                    //System.out.println(s);
                    Event e = new Event();
                    e.setMessage(s);
                    Indexer.format(e);
                    System.out.println(e.getJsonMessage());
                    JSONObject pa=JSONObject.parseObject(e.getJsonMessage());
                    String str=pa.getString("flag");
//                    if (str!=null) {
//                        System.out.println("第" + count + "行日志解析失败！");
//                    }
                   // System.out.println(s);
//                    if(e.getJsonMessage().length()<=47)
//                        System.out.println(e.getMessage());

                }
            }
            br.close();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    @Test
    public void singleTest() throws InterruptedException {
        System.out.println("---初始化---");
        Map<String, String> map = new HashMap();
        map.put("logType", "1");
        BaseIndexerConfig baseIndexerConfig = new BaseIndexerConfig(map);
        //1.此处修改Indexer类型：如ApacheSuccessIndexer
        BaseIndexer Indexer = new TrendmicroImsaMailgatewayV1Indexer(baseIndexerConfig);
        Indexer.register();
        List<String> list = new ArrayList<String>();
        //String ss="2019/07/09 11:18:37 GMT+08:00\t[38536:4151331616] [NORMAL]get entity filename: image219.png";
       // list.add(ss);
        for(String s : list) {
            Event e = new Event();
            e.setMessage(s);
            Indexer.format(e);
            System.out.println(e.getJsonMessage());
        }
    }


}
