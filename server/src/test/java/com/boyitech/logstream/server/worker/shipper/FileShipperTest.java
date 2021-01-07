package com.boyitech.logstream.server.worker.shipper;

import com.boyitech.logstream.core.cache.BaseCache;
import com.boyitech.logstream.core.factory.CacheFactory;
import com.boyitech.logstream.core.setting.StatsSettings;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.shipper.file.FileShipper;
import com.boyitech.logstream.core.worker.shipper.file.FileShipperConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

/**
 * @author Eric
 * @Title: FileShipperTest
 * @date 2019/4/10 16:59
 * @Description: TODO
 */
public class FileShipperTest  {

    private Map shipperConfig;
    private String fileName;
    private String valueOfInput;

    @Before
    public void testPrepare() {
        //language=JSON
        String shipperConfig = "{" +
             "\"moduleType\": \"file\"," +
                "\"index\": \"text_ys\"," +
                "\"readPath\": [\"/Users/juzheng/Downloads/工作文件夹/商学院弋搜Indexer新增201907\"],"+
                "\"fileNameMatch\": \"sbs_dns*\"," +
                "\"threadPollMax\": \"1\"," +
                "\"ignoreOld\": \"false\"," +
                "\"ignoreFileOfTime\": \"86400\"," +
                "\"saveOffsetTime\": \"5\"," +
                "\"encoding\": \"utf8\"," +
                "\"secondOfRead\": \"5\"" +
                "}";
        this.shipperConfig = GsonHelper.fromJson(shipperConfig);
        fileName = "C:\\Users\\Eric\\Desktop\\testShipper\\testFileShipper.txt";
        valueOfInput = UUID.randomUUID().toString();
    }

    @After
    public void deleteOffsetFile(){

        File file = Paths.get(Paths.get(System.getProperty("user.dir")).toString(),
                "tmp", "d75e680e49f7dbc886abf776da592c77.txt").toFile();
        file.delete();
    }

    //判断fileshipper采集的文件与内容是否无误
    @Test
    public void testFileValue() throws FileNotFoundException, UnsupportedEncodingException, InterruptedException {
//        PrintWriter writer = new PrintWriter(fileName, "UTF-8");
//        writer.println(valueOfInput);
//        writer.close();

        FileShipperConfig config = new FileShipperConfig(shipperConfig);
        FileShipper fileShipper = new FileShipper(config);
        BaseCache cache = CacheFactory.createCache();
        fileShipper.setLv1Cache(cache);
        fileShipper.doStart();

        Thread.sleep(Integer.parseInt(StatsSettings.STATS_QUERY_SLEEP_TIME.getValue()));
//
//        Event take = (Event) cache.take(1).get(0);
//        Assert.assertTrue(take.getMessage() .equals(valueOfInput) );
    }

    //判断fileshipper销毁能否释放资源
    @Test
    public void testFileStartStopAndDestroy() throws FileNotFoundException, UnsupportedEncodingException, InterruptedException {
        PrintWriter writer = new PrintWriter(fileName, "UTF-8");
        writer.println(valueOfInput);
        writer.close();


        FileShipperConfig config = new FileShipperConfig(shipperConfig);
        FileShipper fileShipper = new FileShipper(config);
        BaseCache cache = CacheFactory.createCache();
        fileShipper.setLv1Cache(cache);
        fileShipper.doStart();
        Assert.assertTrue(fileShipper.isAlive());
        fileShipper.doDestroy();
        Thread.sleep(5000);
        Assert.assertTrue(!fileShipper.isAlive());

    }



}
