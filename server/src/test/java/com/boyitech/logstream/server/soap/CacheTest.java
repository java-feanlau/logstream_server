package com.boyitech.logstream.server.soap;

import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.server.BaseTest;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Eric
 * @Title: CacheTest
 * @date 2019/4/10 15:57
 * @Description: Cache的创建和销毁
 */
public class CacheTest extends BaseTest {

    @Test
    public void testCacheLv1(){
        //cacheLv1
        String cacheLv1 = instance.createCacheLv1();
        String lv1Reslut = instance.destroyCache(cacheLv1);
        Map<String, String> lv1Map = GsonHelper.fromJson(lv1Reslut);
        String lv1Status = lv1Map.get("soap_status");
        assertEquals("200",lv1Status);
        //cacheLv1
        String cacheLv2 = instance.createCacheLv2();
        String lv2Reslut = instance.destroyCache(cacheLv2);
        Map<String, String> lv2Map = GsonHelper.fromJson(lv2Reslut);
        String lv2Status = lv2Map.get("soap_status");
        assertEquals("200",lv2Status);
        //cacheLv1
        String cacheLv3 = instance.createCacheLv3();
        String lv3Reslut = instance.destroyCache(cacheLv3);
        Map<String, String> lv3Map = GsonHelper.fromJson(lv3Reslut);
        String lv3Status = lv3Map.get("soap_status");
        assertEquals("200",lv3Status);




    }
}
