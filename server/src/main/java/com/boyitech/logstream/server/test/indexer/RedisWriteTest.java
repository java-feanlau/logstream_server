package com.boyitech.logstream.server.test.indexer;


import redis.clients.jedis.Jedis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author juzheng
 * @Title: RedisWriteTest
 * @date 2019/4/28 1:33 PM
 * @Description:  集成测试中，针对Redis写入的测试
 */
public class RedisWriteTest {
    private final static  String  FILEPATH = "/Users/juzheng/Downloads/博弋工作文件夹/普通日志文件及相关/storeFire.txt";

    public static void main(String args[]){
        RedisWriteTest test = new RedisWriteTest();
        test.RedisOUTIN();
        test.Rediswrite();
    }


    public void Rediswrite(){
        //redis连接
        Jedis jedis = new Jedis("172.17.30.10", 6379);
        jedis.auth("d115deabc61ac79fc179dd75efaf720f519d35b0ef7c2d473c74b32c4877eca6");
        jedis.select(0);

        int count=0;//日志计数
        int max=12000;//因为总的日志数量太多了，可以设置先取max条
        List<String> filelist = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader( new File(FILEPATH)));
            String s = null;
            while ((s = br.readLine()) != null&&count<=max) {
                    filelist.add(s);
                    count++;
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i=0;i<filelist.size();i++)
            jedis.lpush("storeFire",filelist.get(i));
        jedis.close();
    }

    public void  RedisOUTIN(){
        //从一个reidis导出到另一个
        String logtype="fortinet-firewall";
        Jedis jedis1 = new Jedis("172.17.250.200", 6379);
        jedis1.auth("2b3c18c71d4172e698d26e2b99996eddc97f04ac343731a20516b1f1cebc7a17");
        jedis1.connect();
        jedis1.select(1);
        List<String> list0 = new ArrayList<>();
        long llen = jedis1.llen(logtype);
        list0 = jedis1.lrange(logtype, 0, llen);
        jedis1.close();

        Jedis jedis2 = new Jedis("172.17.30.10", 6379);
        jedis2.auth("d115deabc61ac79fc179dd75efaf720f519d35b0ef7c2d473c74b32c4877eca6");
        jedis2.select(0);
        jedis2.connect();
        for (int i=0;i<list0.size();i++)
            jedis2.lpush(logtype,list0.get(i));
           // System.out.println(logtype+list0.get(i));
        jedis2.close();
    }
}
