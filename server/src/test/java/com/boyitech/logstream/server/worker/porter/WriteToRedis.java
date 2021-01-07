package com.boyitech.logstream.server.worker.porter;

import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

/**
 * @author juzheng
 * @Title: WriteToRedis
 * @date 2019/8/25 10:28 AM
 * @Description: 向redis中写入数据的测试工具类
 */
public class WriteToRedis {


    /*
    * @Author juzheng
    * @Description 这是一个从redisA中读取写入另一个redisB的故事
    * @Date 10:32 AM 2019/8/25
    * @Param []
    * @return void
    */
    @Test
    public void redisaToRedisb(){
        String logtype="fortinet-firewall";

        Jedis jedis1 = new Jedis("172.17.250.200", 6379,1000000000);
        jedis1.auth("2b3c18c71d4172e698d26e2b99996eddc97f04ac343731a20516b1f1cebc7a17");
        jedis1.connect();
        jedis1.select(2);
        long llen = jedis1.llen(logtype);
        List<String> list0 = jedis1.lrange(logtype, 0, 10000);
        jedis1.close();

        Jedis jedis2=new Jedis("IP",6379,1000000);
        jedis2.auth("pass");
        jedis2.connect();
        jedis2.select(0);
        for (int i=0;i<list0.size();i++)
            jedis2.lpush(logtype,list0.get(i));
        jedis2.close();

    }

    /*
    * @Author juzheng
    * @Description 这是一个从txt文本读取原始日志写入redisB的故事
    * @Date 10:34 AM 2019/8/25
    * @Param []
    * @return void
    */
    @Test
    public void txtToRedisb(){
        String FILEPATH ="/Users/juzheng/Downloads/sangfor-firewall/2020.01/index_0/type_0/data_0.json";
        String key="testboshi4";
        //redis连接
        Jedis jedis = new Jedis("172.17.250.200", 6379);
        jedis.auth("2b3c18c71d4172e698d26e2b99996eddc97f04ac343731a20516b1f1cebc7a17");
        jedis.select(14);

//        int count=0;//日志计数
//        int max=413;//因为总的日志数量太多了，可以设置先取max条
//        List<String> filelist = new ArrayList<>();
//        try {
//            BufferedReader br = new BufferedReader(new FileReader( new File(FILEPATH)));
//            String s = null;
//            while ((s = br.readLine()) != null) {
////                filelist.add(s);
////                count++;
//                jedis.lpush(key,s);
//            }
//            br.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        for (int i=0;i<filelist.size();i++)
//            jedis.lpush(key,filelist.get(i));
        Pipeline pipeline = jedis.pipelined();
        jedis.close();
    }


    @Test
    public void piperedis(){
        Jedis jedis = new Jedis("172.17.250.200", 6379);
        jedis.auth("2b3c18c71d4172e698d26e2b99996eddc97f04ac343731a20516b1f1cebc7a17");
        jedis.select(14);
        Pipeline p = jedis.pipelined();
        p.setex("key_a", 120000, "11111");
        p.setex("key_b", 120000, "2222");
        //第一个参数是redis中的key,第二个参数是key的过期时间(s),第三个参数是key对应的value值.
        p.sync();
        if (jedis != null && jedis.isConnected()) {
            jedis.close();
        }

    }

    @Test
    public void pipe2(){
        String FILEPATH="/Users/juzheng/Downloads/博世华域爱数网盘日志格式化需求/bhss-eisoo/2020.02/index_0/type_0/data_0.json";
        Jedis jedis = new Jedis("172.17.30.10", 6379,1000000000);
        jedis.auth("d115deabc61ac79fc179dd75efaf720f519d35b0ef7c2d473c74b32c4877eca6");
        jedis.select(9);
        Pipeline pipelined = jedis.pipelined();
        String key = "bhss-eisoo";
        long begin = System.currentTimeMillis();

        try {
            BufferedReader br = new BufferedReader(new FileReader( new File(FILEPATH)));
            String s = null;
            while ((s = br.readLine()) != null) {
                    pipelined.rpush(key,s);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        pipelined.sync();
        jedis.close();
        long end = System.currentTimeMillis();
        System.out.println("use pipeline batch set total time：" + (end - begin));

    }



}
