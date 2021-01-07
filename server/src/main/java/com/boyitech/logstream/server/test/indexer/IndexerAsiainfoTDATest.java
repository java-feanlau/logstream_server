//package com.boyitech.logstream.server.test.indexer;
//
//import com.alibaba.fastjson.JSONObject;
//import com.boyitech.logstream.core.info.Event;
//import com.boyitech.logstream.core.worker.indexer.BaseIndexerConfig;
//import com.boyitech.logstream.worker.indexer.AsiainfoTDAIndexer;
//import com.google.gson.Gson;
//import redis.clients.jedis.Jedis;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * @author juzheng
// * @Title: IndexerAsiainfoTDATest
// * @date 2019/4/23 09:54 AM
// * @Description: 亚信TDA的indexer测试
// */
//public class IndexerAsiainfoTDATest {
//
//    private static final String FILEPATH = "/Users/juzheng/Downloads/IndexFile_Test_Out.txt";
//    //针对亚信TDA的indexer的自动化的单元测试框架，只需改动elk解析后的日志路径Path即可
//    /**
//     * 流程：
//     * 连接redis，读原始日志数据-->>
//     * 转义,取的message字段-->>
//     * 解析--->>
//     * 与es对应的比较-->>
//     * 相同就过，不相同输出日志位置-->>
//     **/
//    //说明：1.日志对比的过程是把单条日志转化成Map，字段顺序不同也可；
//    //     2.出现大量的日志记录不同情况：原因：es与indexer定义的字段名不同，日志文件类型与indexer解析的不匹配，indexer本身解析的问题
//    public static void main(String args[]){
//        Jedis jedis = new Jedis("172.17.250.200", 6379);
//        jedis.auth("2b3c18c71d4172e698d26e2b99996eddc97f04ac343731a20516b1f1cebc7a17");
//        Gson gson = new Gson();
//        jedis.select(1);
//        List list0 = new ArrayList();
//        long llen = jedis.llen("asiainfo-tda");
//        list0 = jedis.lrange("asiainfo-tda", 0, llen);
//        List<Event> list = new ArrayList<>();
//        for(int i=0;i<llen;i++){
//            list.add(new Event());
//            list.get(i).setMessage(list0.get(i).toString());
//        }
//        jedis.close();
//
//        List<Event>list2=new ArrayList<>();
//        for (Event e:list){
//            JSONObject pa=JSONObject.parseObject(e.getMessage());
//            String str=pa.getString("message").replaceAll("\n", "");
//            Event ee=new Event();
//            ee.setMessage(str);
//            list2.add(ee);
//        }
//
//        Map<String,String> map=new HashMap();
//        map.put( "logType","1");
//        BaseIndexerConfig baseIndexerConfig = new BaseIndexerConfig(map);
//        AsiainfoTDAIndexer Indexer=new AsiainfoTDAIndexer(baseIndexerConfig);
//        Indexer.register();
//        List<Map<String,Object>> es_json=new ArrayList<Map<String,Object>>();//List集合中存放Map集合
//        List<Map<String,Object>> indexer_json=new ArrayList<Map<String,Object>>();//List集合中存放Map集合
//        try
//        {
//            BufferedReader br = new BufferedReader(new FileReader(new File(FILEPATH)));
//            String s = new String();
//
//            while ((s = br.readLine()) != null)
//            {   Gson gson1 = new Gson();
//               // System.out.println(s);
//                Map<String,Object> map1 = new HashMap<>();
//                map1 = gson1.fromJson(s, map1.getClass());
//                es_json.add(map1);
//            }
//            br.close();
//
//        }
//        catch (Exception ee)
//        {
//            ee.printStackTrace();
//        }
//
//        for(Event e:list2) {
//            Indexer.format(e);
//            Gson gson2 = new Gson();
//            Map<String,Object> map2 = new HashMap<>();
//            map2 = gson2.fromJson(e.getJsonMessage(), map2.getClass());
//            indexer_json.add(map2);
//        }
//
//        // diff 存放不同的元素
//        long count=0;
//        List<Map<String ,Object>> diff1 = new ArrayList<Map<String,Object>>();
//        List<Map<String ,Object>> diff2 = new ArrayList<Map<String,Object>>();
//        //if(es_json.size()==indexer_json.size()) {
//        for (int i = 0; i < es_json.size(); i++) {
//            es_json.get(i).remove("@timestamp");
//            es_json.get(i).remove("received_at");
//            es_json.get(i).remove("portedAt");
//             es_json.get(i).remove("message");
//            es_json.get(i).remove("flag");
//            es_json.get(i).remove("Metafield_type");
//            es_json.get(i).remove("Metafield_category");
//            es_json.get(i).remove("Metafield_subject");
//            es_json.get(i).remove("Metafield_object");
//            es_json.get(i).remove("Metafield_loglevel");
//            es_json.get(i).remove("Metafield_source");
//            es_json.get(i).remove("Metafield_description");
//
//            indexer_json.get(i).remove("@timestamp");
//            indexer_json.get(i).remove("received_at");
//            indexer_json.get(i).remove("portedAt");
//            indexer_json.get(i).remove("message");
//            indexer_json.get(i).remove("flag");
//            indexer_json.get(i).remove("Metafield_type");
//            indexer_json.get(i).remove("Metafield_category");
//            indexer_json.get(i).remove("Metafield_subject");
//            indexer_json.get(i).remove("Metafield_object");
//            indexer_json.get(i).remove("Metafield_loglevel");
//            indexer_json.get(i).remove("Metafield_source");
//            indexer_json.get(i).remove("Metafield_description");
//
//            if (!(es_json.get(i).equals(indexer_json.get(i)))) {
//                //if(es_json.get(i).get("message").equals(indexer_json.get(i).get("message")))
//                //{
//                diff1.add(es_json.get(i));
//                System.out.println("WARN------第"+i+"行日志解析不匹配------");
//                count++;
//                //}
//                //else
//                //  diff2.add(es_json.get(i));
//                //System.out.println("INFO------第"+i+"行原日志不同------");
//            }
//            if(count==llen/4){
//                System.out.println("出现大量日志错误！！！！");
//                break;
//            }
//        }
//        System.out.println("共有"+count+"条日志不匹配！！！");
//        // }
//        /*else
//            {
//                System.out.println("------两个文件长度不同！未执行比较------");
//                System.out.println("------es日志长度："+es_json.size()+",indexer日志长度：" +indexer_json.size()+"------");
//            }
//*/
//        // System.out.println(es_json.get(0));
//         //System.out.println(indexer_json.get(0));
//
//    }
//}
