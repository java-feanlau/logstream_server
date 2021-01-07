package com.boyitech.logstream.server.worker.shipper;

import com.boyitech.logstream.core.cache.BaseCache;
import com.boyitech.logstream.core.factory.CacheFactory;
import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.setting.StatsSettings;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.shipper.redis.RedisShipper;
import com.boyitech.logstream.core.worker.shipper.redis.RedisShipperConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.UUID;

/**
 * @author Eric
 * @Title: RedisTest
 * @date 2019/4/11 14:47
 * @Description: TODO
 */
public class RedisTest {

    private  BaseCache cache;
    private String shipperConfig;
    private String valueOfInput1;
    private String valueOfInput2;
    private Jedis jedis;



    //判断redis的数据是否正常读取并且读取的数据库号是否是配置指定的数据库号
    @Test
    public void testRedisValue() throws InterruptedException {
        int DBindexer=(int)(Math.random()*15);
        jedis.select(DBindexer);
        jedis.lpush("users",valueOfInput1);
        //language=JSON
        shipperConfig = "{" +
                "\"moduleType\": \"redis\"," +
                "\"index\": \"text_redis\"," +
                "\"host\": \"172.17.30.10\"," +
                "\"port\": \"6379\"," +
                "\"passwd\": \"d115deabc61ac79fc179dd75efaf720f519d35b0ef7c2d473c74b32c4877eca6\"," +
                "\"keys\": [\"users\"]," +
                "\"DBindex\": \"" + DBindexer + "\"" +
                "}";

        RedisShipperConfig config = new RedisShipperConfig(GsonHelper.fromJson(shipperConfig));
        RedisShipper redisShipper = new RedisShipper(config);
        BaseCache cache = CacheFactory.createCache();
        redisShipper.setLv1Cache(cache);
        redisShipper.doStart();

        Thread.sleep(Integer.parseInt(StatsSettings.STATS_QUERY_SLEEP_TIME.getValue()));
        Event take = (Event) cache.take(1).get(0);

        Assert.assertTrue(take.getMessage().contains(valueOfInput1));

    }

    //多个key的情况下是否能正常读取
    @Test
    public void testRedisValues() throws InterruptedException {
        jedis.lpush("user1",valueOfInput1);
        jedis.lpush("user2",valueOfInput2);
        shipperConfig = "{" +
                "\"moduleType\": \"redis\"," +
                "\"index\": \"text_redis\"," +
                "\"host\": \"172.17.30.10\"," +
                "\"port\": \"6379\"," +
                "\"passwd\": \"d115deabc61ac79fc179dd75efaf720f519d35b0ef7c2d473c74b32c4877eca6\"," +
                "\"keys\": [\"user1\",\"user2\"]," +
                "\"DBindex\": \"0\"" +
                "}";


        RedisShipperConfig config = new RedisShipperConfig(GsonHelper.fromJson(shipperConfig));
        RedisShipper redisShipper = new RedisShipper(config);
        BaseCache cache = CacheFactory.createCache();
        redisShipper.setLv1Cache(cache);
        redisShipper.doStart();
        Thread.sleep(Integer.parseInt(StatsSettings.STATS_QUERY_SLEEP_TIME.getValue()));
        List take = cache.take(5);
        Assert.assertEquals(2,take.size());

    }

    @Before
    public void testPrepare() {
        cache = CacheFactory.createCache();
        valueOfInput1 = UUID.randomUUID().toString();
        valueOfInput2 = UUID.randomUUID().toString();
        jedis = new Jedis("172.17.30.10",6379);
        jedis.auth("d115deabc61ac79fc179dd75efaf720f519d35b0ef7c2d473c74b32c4877eca6");
        jedis.flushDB();
        // 向头添加元素，返回链表的长度

    }
}
