package com.boyitech.logstream.core.worker.shipper.nmap_xml;

import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.shipper.BaseShipper;
import com.boyitech.logstream.core.worker.shipper.BaseShipperConfig;
import com.boyitech.logstream.core.worker.shipper.file.utils.OffsetInfoManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//import com.boyi.logstream.core.worker.shipper.filev1.utils.MultilineStateMachine;

/**
 * @author ZhengHao
 * @Title: Shipper
 * @date 2018/11/22 13:30
 * @Description: TODO
 */
public class NmapXmlShipper extends BaseShipper {
    private ArrayList<Event> cache = new ArrayList<Event>(100000);
    private static final String XMLRPACKAGE = "com.boyitech.logstream.core.worker.shipper.nmap_xml";
    private static final String RAF_MODE = "rw";
    private NmapXmlShipperConfig config;
    private String readPath;               //监听的目录
    private Integer threadPollMax;        //线程池的最大值
    private Integer MaxLineSize;          //一行的最大字节数
    private String encoding;              //编码格式
    private int secondOfRead;
    LinkedHashMap<File, Object> needReadFiles = new LinkedHashMap<>();  //存放改动文件的集合，之后用线程去消费
    //监听
    private WatchService watcher;
    private Map<WatchKey, Path> keys;
    private ExecutorService threadPool;   //线程池

    protected static final Logger LOGGER = LogManager.getLogger("worker");
    BaseXmlAnalysis analysis = null;

    /**
     * @param
     * @return
     * @Author Eric Zheng
     * @Description 读取配置文件，将配置文件信息保存到config,并且传到成员变量当中
     * @Date 13:37 2018/11/22
     * @Param []
     */
    public NmapXmlShipper(BaseShipperConfig config) {
        super(config);
        this.config = (NmapXmlShipperConfig) config;
        analysis = new NmapAnalysis();
    }

    public NmapXmlShipper(String shipperId, BaseShipperConfig config) {
        super(shipperId, config);
        this.config = (NmapXmlShipperConfig) config;
        analysis = new NmapAnalysis();
    }

    @Override
    public boolean register() {
        //加载属性文件
        Map<String, String> multilineRule = config.getMultilineRule();
        readPath = config.getReadPath();
        threadPollMax = config.getThreadPollMax();
        MaxLineSize = config.getMaxLineSize();
        encoding = config.getEncoding();
        threadPool = Executors.newFixedThreadPool(threadPollMax);
        secondOfRead = config.getSecondOfRead();


        //注册watcher，监听目录
        try {
            this.watcher = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            e.printStackTrace();
            this.addException("nmapXmlShipper启动失败，请重新注册："+e);
            return false;
        }
        keys = new HashMap<WatchKey, Path>();
        registerWatch();
        processEvents();

        return true;
    }

    @Override
    public void tearDown() {

    }

    /**
     * @return void
     * @Author Eric Zheng
     * @Description 读操作，进来的每一个文件都创建一个线程调度器来进行读取，每隔几秒读一次，并将偏移量和修改时间写入文件当中
     * 当日志多长时间没有修改的时候关闭此文件的调度
     * @Date 14:19 2018/11/23
     * @Param [file]
     **/
    @Override
    public void execute() {
        while (true) {
            if (!needReadFiles.isEmpty()) {
                Set<File> files = needReadFiles.keySet();
                Iterator<File> iterator = files.iterator();
                File needReadFile = iterator.next();
                needReadFiles.remove(needReadFile);
                //在线程启动之前就将文件读取出来，保证线程安全
                threadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        //从队列中读取需要读取的文件
                        //如果文件不为空
                        if (needReadFile.exists()) {
                            try {

//                                String module = config.getXmlType();
//                                String className = XMLRPACKAGE + "." + CharacterHelper.captureName(CharacterHelper.UnderlineToHump(module)) + "Analysis";
//                                Class c = Class.forName(className);
//                                Constructor constructor = c.getConstructor();
//                                BaseXmlAnalysis analysis = (BaseXmlAnalysis)constructor.newInstance();

                                List<String> resultOfList = analysis.analysis(needReadFile);
                                for (String out : resultOfList) {
                                    Event e = new Event();
                                    e.setMessage(out);
                                    e.setKey(needReadFile.getPath());
                                    e.setSource(needReadFile.getPath());
//                                    if (mark != null)
//                                        e.setMark(mark);
                                    if (config.isChangeIndex())
                                        e.setIndex(config.getIndex());
                                    count.addAndGet(1);
                                    lv1Cache.add(e);
                                }


                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            LOGGER.debug("'" + needReadFile.getName() + "'为老文件，但是文件大小小于上次记录，重新读取");
                        }
                    }
                });
            } else {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @return void
     * @Author Eric Zheng
     * @Description 监听指定目录
     * @Date 14:12 2018/11/22
     * @Param
     **/
    public void registerWatch() {
        Path dir = Paths.get(readPath);
        WatchKey key = null;
        try {
            key = dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY);
        } catch (IOException e) {
            e.printStackTrace();
        }
        keys.put(key, dir);
    }


    /**
     * @return void
     * @Author Eric Zheng
     * @Description 监听目录是否有新的文件生成
     * @Date 14:06 2018/11/23
     * @Param []
     **/

    void processEvents() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    WatchKey key;
                    try {
                        key = watcher.take();
                    } catch (InterruptedException x) {
                        return;
                    }
                    Path dir = keys.get(key);
                    if (dir == null) {
                        System.err.println("操作未识别");
                        continue;
                    }
                    try {
                        //积累触发事件
                        Thread.sleep(secondOfRead * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        String fileName = event.context().toString();
                        String newFileName = keys.get(key) + "/" + fileName;
                        File newFile = new File(newFileName);
                        Long inode = OffsetInfoManager.getInode(newFile);
                        // 事件可能丢失或遗弃
                        if (kind == StandardWatchEventKinds.OVERFLOW) {
                            continue;
                        }

                        if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                            if (!needReadFiles.containsKey(newFile)) {
                                needReadFiles.put(newFile, null);
                            }
                        }
                    }
                    key.reset();
                }
            }
        }).start();

    }


    public static void main(String args[]) throws IOException, InterruptedException {

        Map map = new HashMap();
        map.put("readPath", "C:\\Users\\Eric\\Desktop\\xml");
        map.put("moduleType", "nmap_xml");
        map.put("xmlType", "nmap");

        NmapXmlShipperConfig fileTailerConfig = new NmapXmlShipperConfig(map);
        NmapXmlShipper fileShipper = new NmapXmlShipper(fileTailerConfig);

        fileShipper.register();
        fileShipper.execute();
    }


}
