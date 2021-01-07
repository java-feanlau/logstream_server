package com.boyitech.logstream.core.worker.shipper.file.utils;


import com.boyitech.logstream.core.setting.ClientSettings;
import com.boyitech.logstream.core.worker.shipper.file.info.FileOffsetInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Eric
 * @Title: OffsetInfoManage
 * @date 2018/11/28 16:20
 * @Description: 管理offset的写操作和file对应key的读取
 */
public class OffsetInfoManager implements Runnable {
    protected static final Logger LOGGER = LogManager.getLogger("worker");
    //    public static String ROOTPATH = System.getenv("APP_HOME") == null ? Paths.get(System.getProperty("user.dir")).toString() : System.getenv("APP_HOME");
    public static String ROOTPATH = ClientSettings.FILEOFFSETPATH.getValue();
    private static Map offsetMap;
    private static int time;
    private volatile boolean startFlag = true;
    private String offsetName;

    public OffsetInfoManager(String offsetName, Map offsetMap, int time) {
        this.offsetMap = offsetMap;
        this.time = time;
        this.offsetName = offsetName;
    }


    public Map getOffsetMap() throws Exception {
        FileInputStream freader;
        HashMap<String, FileOffsetInfo> map = null;
        try {
            //生产环境
            File file = Paths.get(ROOTPATH, "tmp", offsetName + ".txt").toFile();

            freader = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(freader);
            map = (HashMap<String, FileOffsetInfo>) objectInputStream.readObject();
            freader.close();
        } catch (FileNotFoundException e) {
            //  e.printStackTrace();
            LOGGER.debug("创建偏移量信息文件");
        } catch (Exception e) {
            throw e;
//            LOGGER.error("获取偏移量信息出错");
        }

        return map;

    }


    public void star() {
        startFlag = true;
    }

    public void stop() {
        startFlag = false;
    }

    //    public void deleteFile() {
//        //生产环境
////        File file = Paths.get(ROOTPATH, "tmp", offsetName + ".txt").toFile();
//        //测试环境
//        File file = Paths.get(System.getProperty("user.dir")+"\\"+offsetName+".txt").toFile();
//        file.delete();
//        LOGGER.debug(new Date() + " 删除偏移量文件：" + file);
//    }


    @Override
    public void run() {
        while (startFlag) {
            FileOutputStream outStream = null;
            ObjectOutputStream objectOutputStream = null;
            try {
                //生产环境
                File file = Paths.get(ROOTPATH, "tmp", offsetName + ".txt").toFile();
                //测试环境
//                File file = Paths.get(System.getProperty("user.dir")+"\\"+offsetName+".txt").toFile();
                outStream = new FileOutputStream(file);
//                outStream = new FileOutputStream(System.getProperty("user.dir")+"\\offsetMap.txt");
                objectOutputStream = new ObjectOutputStream(outStream);

                objectOutputStream.writeObject(offsetMap);

                objectOutputStream.close();
                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(time * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


    /**
     * @return java.lang.Long
     * @Author Eric Zheng
     * @Description 如果系统为linux则用日志inod来确定文件标识符
     * @Date 14:45 2018/11/29
     * @Param [file]
     **/
    public static Long getInode(File file) {


//        String property = System.getProperty("os.name");
//
//        if (property.startsWith("Linux")) {
//            Integer inodeNo = null;
//            try {
//                Path path = Paths.get(file.getPath());
//                BasicFileAttributes inode = Files.readAttributes(path, BasicFileAttributes.class);
//                Object StringMap = inode.fileKey();     //(dev=fd00,ino=203392031)
//                inodeNo = Integer.valueOf(StringMap.toString().split("=")[2].replace(")", ""));
//            } catch (IOException e) {
//
//                LOGGER.error(e);
//                 e.printStackTrace();
////                LOGGER.error("inodeManager:"+file+"文件没有找到");
//            }
//            return Long.valueOf(inodeNo);
//        } else if (property.startsWith("Windows")) {
//            return Long.valueOf(file.hashCode());
//        }else
//            return null;

        //linux和windows版本都以文件名字作为key
        return Long.valueOf(file.hashCode());


    }
}
