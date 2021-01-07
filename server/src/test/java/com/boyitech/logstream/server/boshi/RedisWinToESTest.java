package com.boyitech.logstream.server.boshi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.boyitech.logstream.core.cache.BaseCache;
import com.boyitech.logstream.core.factory.CacheFactory;
import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.setting.StatsSettings;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import com.boyitech.logstream.core.worker.indexer.BaseIndexerConfig;
import com.boyitech.logstream.core.worker.shipper.redis.RedisShipper;
import com.boyitech.logstream.core.worker.shipper.redis.RedisShipperConfig;
import com.boyitech.logstream.worker.indexer.MicrosoftWindowsAllV1Indexer;
import jdk.nashorn.internal.parser.JSONParser;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author juzheng
 * @Title: RedisWinToESTest
 * @date 2019/12/2 11:37 AM
 * @Description: 复制﻿boshi redis里windows数据进新的redis，再使用redisshipper从中读取并发送到es（高需要的测试类）
 */
public class RedisWinToESTest {

    private BaseIndexer Indexer;
    private List<String> list0;

    @Test
    public void testWrite(){
        String logtype="checkpoint-firewall";
        System.out.println("------准备进行测试，读取的是redis中："+logtype+"的数据------");
        Jedis jedis1 = new Jedis("172.17.250.200", 6379,1000000000);
        jedis1.auth("2b3c18c71d4172e698d26e2b99996eddc97f04ac343731a20516b1f1cebc7a17");
        jedis1.connect();
        jedis1.select(3);
        long llen = jedis1.llen(logtype);
        list0 = jedis1.lrange(logtype, 0, llen);
        jedis1.select(15);
        for(int i=0;i<llen;i++){
            jedis1.lpush(logtype,list0.get(i));
        }
        jedis1.close();
        System.out.println("---初始化---");

    }

    @Test
    public void test() throws InterruptedException {
        String shipper_config="{\"host\":\"172.17.250.200\",\"port\":6379,\"DBindex\":\"15\",\"keys\":[\"WinEvt\"],\"type\":\"0\",\"typeMessage\":\"xml\",\"moduleType\":\"redis\",\"index\":\"microsoft_windows_all_v1-022b-\",\"passwd\":\"2b3c18c71d4172e698d26e2b99996eddc97f04ac343731a20516b1f1cebc7a17\"}";
        RedisShipperConfig redisShipperConfig = new RedisShipperConfig(JSON.parseObject(shipper_config));
        RedisShipper redisShipper = new RedisShipper(redisShipperConfig);
        BaseCache cache = CacheFactory.createCache();
        redisShipper.setLv1Cache(cache);
        redisShipper.doStart();
        Event take = (Event) cache.take(1).get(0);
        System.out.println(take);
    }

}
