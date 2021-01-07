package com.boyitech.logstream.core.worker.shipper.kepware;


import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.shipper.BaseShipper;
import com.boyitech.logstream.core.worker.shipper.BaseShipperConfig;
import com.boyitech.logstream.core.worker.shipper.kepware.uitils.KepwareDecoder;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.io.IOUtils.EOF;

/**
 * @author Eric
 * @Title: KepwareShipper
 * @date 2019/1/14 15:49
 * @Description: TODO
 */
public class KepwareShipper extends BaseShipper {
    private final static double VERSION = 4.5;
    public static String ROOTPATH = System.getenv("APP_HOME")==null? Paths.get(System.getProperty("user.dir")).toString() : System.getenv("APP_HOME");
    private KepwareShipperConfig config;
    private static String readPath;
    private List<String> eventList;            //每次读取后将所有日志存储到这个集合中
    private String saveEvent;           //每次读取后将最后一条存储
    private static volatile boolean flag;
    private static int secondOfRead;


    //创建文件输入流对象


    public KepwareShipper(BaseShipperConfig config) {
        super(config);
        this.config = (KepwareShipperConfig) config;

    }

    public KepwareShipper(String shipperID,BaseShipperConfig config) {
        super(shipperID,config);
        this.config = (KepwareShipperConfig) config;

    }


    public boolean register() {
        eventList = new ArrayList();
        readPath = config.getReadPath();
        secondOfRead = config.getSecondOfRead();
        flag = true;
        //加载偏移量文件
        try {
//          freader = new FileInputStream("C:\\Users\\Eric\\Desktop\\testMap.txt");
//            FileInputStream freader = new FileInputStream(System.getProperty("user.dir") + "/kepwareoffset.txt");
            //生产环境
           // File file = Paths.get(ROOTPATH, "tmp", "kepwareoffset.txt").toFile();
            //测试环境
            File file = Paths.get(ROOTPATH, "kepwareoffset.txt").toFile();
            FileInputStream freader = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(freader));
            saveEvent = reader.readLine(); // 读取第一行
          //  System.out.println(saveEvent);
            reader.close();
            freader.close();

        } catch (FileNotFoundException e) {
            //  e.printStackTrace();
            LOGGER.debug(e);
            this.addException("kepwareShipper启动失败，请重新注册");
            return false;
        } catch (Exception e) {
            LOGGER.error(e);
            this.addException("kepwareShipper启动失败启动失败，请重新注册");
            return false;
        }


        return true;
    }


    @Override
    public void execute() {

        try {
            read();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void tearDown() {

    }


    /*
     * @Author Eric Zheng
     * @Description
     * @Date 14:55 2019/1/15
     * @Param []
     * @return void
     **/
    public void read() throws Exception {
        FileInputStream fis = null;

        while (flag) {

            //将读取到文件转存储到byte数组 参数为文件的字节长度
            try {
                fis = new FileInputStream(readPath);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            byte[] inputStream = new byte[fis.available()];
            while (fis.read(inputStream) != EOF) {
                //传入到解析器解析
                KepwareDecoder kepwareDecoder = new KepwareDecoder(inputStream, config);
                eventList = kepwareDecoder.getEventList();
                //当saveEvent不为空的时候，从saveEvent后面继续读
                if (saveEvent != null && !saveEvent.trim().equals("")) {
                    int needReadIndex = eventList.indexOf(saveEvent);
                    if (needReadIndex != -1) {
                        if (needReadIndex + 1 != eventList.size()) {
                            List<String> stringList = eventList.subList(needReadIndex, eventList.size());
                            packEventAndPutCache(stringList);
                        }
                    }
                    //当saveEvent为空的时候，读取所有
                } else {
                    packEventAndPutCache(eventList);
                }
                saveEvent = eventList.get(eventList.size() - 1);
            }
            //每次读取后将最后一行存储到文件中
//            FileOutputStream outStream = new FileOutputStream(new File(System.getProperty("user.dir") + "\\kepwareoffset.txt"));

            //生产环境
           // File file = Paths.get(ROOTPATH, "tmp", "kepwareoffset.txt").toFile();
            //测试环境
            File file = Paths.get(ROOTPATH, "kepwareoffset.txt").toFile();

            FileOutputStream outStream = new FileOutputStream(file);
            outStream.write(saveEvent.getBytes());

            fis.close();  //关闭流对象
            Thread.sleep(secondOfRead * 1000);

        }

    }

    /*
     * @Author Eric Zheng
     * @Description 包装成event
     * @Date 10:13 2019/1/18
     * @Param [eventList]
     * @return void
     **/
    private void packEventAndPutCache(List<String> eventList) throws InterruptedException {
        for (String s : eventList) {
            Event e = new Event();
            e.setMessage(s);
            e.setKey(readPath);
            e.setSource(readPath);
            if (this.mark != null)
                e.setMark(this.mark);
            if (config.isChangeIndex())
                e.setIndex(config.getIndex());
            count.addAndGet(1);
            lv1Cache.put(e);
        }
    }


    public static void main(String args[]) {
        Map<String, String> mapConf = new HashMap();
        mapConf.put("readPath", "C:\\Users\\Eric\\Desktop\\test.log");
        mapConf.put("moduleType", "kepware_shipper");

        KepwareShipperConfig conf = new KepwareShipperConfig(mapConf);
        KepwareShipper kepwareShipper = new KepwareShipper(conf);
        kepwareShipper.register();
        kepwareShipper.execute();

    }

}
