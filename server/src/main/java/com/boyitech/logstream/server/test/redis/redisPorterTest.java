package com.boyitech.logstream.server.test.redis;

import redis.clients.jedis.Jedis;

import java.util.UUID;

/**
 * @author Eric
 * @Title: redisPorterTest
 * @date 2019/5/14 9:24
 * @Description: TODO
 */
public class redisPorterTest {
    private String valueOfInput1;
    private Jedis jedis;


    public void Prepare(){
        valueOfInput1 = UUID.randomUUID().toString();
        jedis = new Jedis("172.17.30.10",6379);
        jedis.auth("d115deabc61ac79fc179dd75efaf720f519d35b0ef7c2d473c74b32c4877eca6");

        jedis.select(0);
        for (;;){
            jedis.lpush("redisNginx","114.92.62.135 -[01/Dec/2018:10:18:37 +0800] \"GET /images/ico_01.gif HTTP/1.1\" 200 1967 \"http://www.sbs.edu.cn/\" \"Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 10.0; WOW64; Trident/7.0; .NET4.0C; .NET4.0E; .NET CLR 2.0.50727; .NET CLR 3.0.30729; .NET CLR 3.5.30729; InfoPath.3)\"\n");
        }

    }


}
