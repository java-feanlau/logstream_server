package com.boyitech.logstream.core.worker.shipper.event_log;

import com.boyitech.logstream.core.cache.BaseCache;
import com.boyitech.logstream.core.factory.CacheFactory;
import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.FilePathHelper;
import com.boyitech.logstream.core.util.os.OSinfo;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.indexer.BaseIndexerConfig;
import com.boyitech.logstream.core.worker.shipper.BaseShipper;
import com.boyitech.logstream.core.worker.BaseWorker;
import com.boyitech.logstream.core.worker.shipper.BaseShipperConfig;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.Winevt.EVT_FORMAT_MESSAGE_FLAGS;
import com.sun.jna.platform.win32.Winevt.EVT_HANDLE;
import com.sun.jna.ptr.IntByReference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.*;

public class EventLogShipper extends BaseShipper {

    protected Queue<String> channelQueue;
    protected EventLogShipperConfig config;
    protected int bookmarkSaveCycle = 1;
    protected int batchSize = 500;

    private final String bookmarkPath;

    private ExecutorService executor;

    public EventLogShipper(BaseShipperConfig config) {
        super(config);
        this.config = (EventLogShipperConfig) config;
        this.bookmarkPath = Paths.get(FilePathHelper.ROOTPATH, "tmp").toString();
    }

    public EventLogShipper(String shipperID, BaseShipperConfig config) {
        super(shipperID, config);
        this.config = (EventLogShipperConfig) config;
        this.bookmarkPath = Paths.get(FilePathHelper.ROOTPATH, "tmp").toString();
    }


    @Override
    public boolean register() {
        // 判断操作系统是否是windows，如果不是则失败
        if (!OSinfo.isWindows()) {
            throw new RuntimeException("event log采集任务只能在windows操作系统上运行");
        }
        // TODO 从config中获取需要处理的channel列表
        // 目前只采集固定的三种日志
        ArrayList<String> channelArray = config.getEventTypes();
        channelQueue = new ArrayBlockingQueue<>(channelArray.size());
        boolean flag = false;

        //订阅了多少频道，就会启动多少个线程
        this.threadsNum = channelArray.size();

        for (String channel : channelArray) {
            flag = channelQueue.add(channel);
        }
        //自定义，当队列大小满的时候，等待插入。否则书签瞬间就记录到末尾了。
        executor = new ThreadPoolExecutor(
                5, 5,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(2000), // 未处理的任务的等待队列
                (r, executor) -> {
                    try {
                        executor.getQueue().put(r);
                    } catch (InterruptedException e) {
                        throw new RejectedExecutionException("interrupted", e);
                    }
                }
        );

        return flag;
    }

    @Override
    public void execute()  {
//        new Thread(() -> {
//            long befor = 0;
//            while (true) {
//                Long test = this.count.get();
//                System.out.println(test - befor);
//                befor = test;
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//
//        }).start();
        String channel = this.obtainChannel();
        if (channel == null) {
            BaseWorker.LOGGER.debug("当前线程没有获取到需要订阅的频道，任务结束");
            return;
        }
        // 创建eventHandle
        HANDLE handle = Kernel32.INSTANCE.CreateEvent(null, true, true, null);
        if (handle == null) {
            Win32Exception e = new Win32Exception(Kernel32.INSTANCE.GetLastError());
            this.recordException("", e);
            throw e;
        }
        EVT_HANDLE eventHandle = new EVT_HANDLE(handle.getPointer());
        // 订阅
        EVT_HANDLE bookmark = Wevtapi.INSTANCE.EvtCreateBookmark(this.readBookmark(channel));
        EVT_HANDLE hSubscription;
        EVT_HANDLE lastHandle = null;
        //书签存在的时候
        if (bookmark != null) {
            hSubscription = Wevtapi.INSTANCE.EvtSubscribe(null, eventHandle, channel, "*",
                    bookmark, null, null, Winevt.EVT_SUBSCRIBE_FLAGS.EvtSubscribeStartAfterBookmark);
        } else {
            //书签不存在
            // bookmark为null，记录异常信息
            Win32Exception e = new Win32Exception(Kernel32.INSTANCE.GetLastError());
            this.recordException("", e);
            BaseWorker.LOGGER.error("bookmark为null", e);
            hSubscription = Wevtapi.INSTANCE.EvtSubscribe(null, eventHandle, channel, "*",
                    null, null, null, Winevt.EVT_SUBSCRIBE_FLAGS.EvtSubscribeStartAtOldestRecord);
        }
        if (hSubscription == null) {
            // 订阅失败，任务结束
            System.out.println(Kernel32.INSTANCE.GetLastError());
            Win32Exception e = new Win32Exception(Kernel32.INSTANCE.GetLastError());
            this.recordException("", e);
            this.returnChannel(channel);
            if (bookmark != null)
                Wevtapi.INSTANCE.EvtClose(bookmark);
            if (handle != null)
                Kernel32.INSTANCE.CloseHandle(handle);
            throw e;
        }
        int collectorCycle = 0;
        try {
            //开始采集
            while (super.runSignal) {
                switch (Kernel32.INSTANCE.WaitForSingleObject(eventHandle, 2000)) {
                    case Kernel32.WAIT_OBJECT_0:
                        // 正常唤醒
                        EVT_HANDLE[] eventArray = new EVT_HANDLE[batchSize];
                        IntByReference Returned = new IntByReference();    //下一次请求还需要还需要多大的长度
                        boolean flag = false;
                        while (!flag) {
                            boolean b_process_handle = true;
                            boolean readble = Wevtapi.INSTANCE.EvtNext(hSubscription, batchSize, eventArray, Kernel32.INFINITE, 0, Returned);
                            if (!readble) {
                                int status = Kernel32.INSTANCE.GetLastError();
                                if (status == Kernel32.ERROR_NO_MORE_ITEMS || (status == Kernel32.ERROR_INVALID_OPERATION && Returned.getValue() == 0)) {
                                    // 这两种情况均视作没有日志被读取到，等待下一次唤醒
                                    b_process_handle = false;
                                    flag = true;
                                } else if (status == Kernel32.RPC_S_INVALID_BOUND) {
                                    if (Returned.getValue() == eventArray.length) {
                                        //虽然报错RPC_S_INVALID_BOUND，但仍然返回了请求的记录，则无视该错误，记录这些句柄
                                        flag = true;
                                    } else {
                                        //报错RPC_S_INVALID_BOUND，返回记录数并不恰好等于请求的数量
                                        if (Returned.getValue() == 0) {
                                            b_process_handle = false;
                                        }
                                        flag = true;
                                    }
                                } else {
                                    // 其他错误，不处理日志
                                    Win32Exception e = new Win32Exception(Kernel32.INSTANCE.GetLastError());
                                    this.recordException("", e);

                                    BaseWorker.LOGGER.error("windows event log频道<" + channel + ">发生错误,status code: " + status);
                                    b_process_handle = false;
                                    flag = true;
                                }
                            }
                            if (b_process_handle) {
                                collectorCycle++;
                                if (collectorCycle >= this.bookmarkSaveCycle) {
                                    // 保存书签
                                    lastHandle = eventArray[Returned.getValue() - 1];
                                    this.saveBookmark(channel, lastHandle);
                                    collectorCycle = 0;
                                }
                                // 读取日志信息，构建event对象并存入lv1缓存
                                List<Event> eventList = new ArrayList<Event>();
                                int size = Returned.getValue();
                                for (int i = 0; i < size; i++) {
                                    EVT_HANDLE evt_handle = eventArray[i];
                                    executor.submit(()->{
                                        Event e = new Event();
                                        e.setMessage(getMessageFromEVT_HANDLE(evt_handle));
                                        if (this.mark != null)
                                            e.setMark(this.mark);
                                        if (config.isChangeIndex())
                                            e.setIndex(config.getIndex());
                                        count.addAndGet(1);
                                        try {
                                            lv1Cache.put(e);
                                        } catch (InterruptedException e1) {
                                            e1.printStackTrace();
                                        }
//                                        System.out.println(count.get());
                                    });
                                }
//                                while (eventList.size() == size) {
//                                    count.addAndGet(size);
//                                    lv1Cache.put(eventList);
//                                    Thread.sleep(0);
//                                }
                            }
                        }
                        break;
                    case Kernel32.WAIT_TIMEOUT:
                        // 超时则结束当前循环直接进入下一次
                        break;
                    case Kernel32.WAIT_FAILED:
                        // 等待失败，记录异常
                        Exception e = new Win32Exception(Kernel32.INSTANCE.GetLastError());
                        BaseWorker.LOGGER.error("EvtShipper WaitForSingleObject failed", e);
                        this.recordException("", e);
                        break;
                    case Kernel32.WAIT_ABANDONED:
                        BaseWorker.LOGGER.error("EvtShipper WaitForSingleObject WAIT_ABANDONED");
                        break;
                    default:
                        BaseWorker.LOGGER.error("EvtShipper WaitForSingleObject get unexpect result");
                }
            }
        } finally {
            // execute执行完毕之前
            this.returnChannel(channel);
            if (hSubscription != null)
                Wevtapi.INSTANCE.EvtClose(hSubscription);
            if (bookmark != null)
                Wevtapi.INSTANCE.EvtClose(bookmark);
            if (lastHandle != null)
                Wevtapi.INSTANCE.EvtClose(lastHandle);
            if (handle != null)
                Kernel32.INSTANCE.CloseHandle(handle);
        }
    }

    @Override
    public void tearDown() {

    }


    protected String obtainChannel() {
        return channelQueue.poll();
    }

    protected boolean returnChannel(String channel) {
        return channelQueue.offer(channel);
    }

    protected String readBookmark(String channel) {
        Path bookmarkFilePath = Paths.get(this.bookmarkPath, channel);
        if (Files.exists(bookmarkFilePath)) {
            try {
                String xmlStr = new String(Files.readAllBytes(bookmarkFilePath), StandardCharsets.UTF_8);
                return xmlStr;
            } catch (IOException e) {
                BaseWorker.LOGGER.error("获取频道<" + channel + ">的书签文件失败，文件路径：" + bookmarkFilePath.toString());
                return null;
            }
        } else {
            return null;
        }
    }

    protected boolean saveBookmark(String channel, EVT_HANDLE handle) {
        int dwBufferSize = 1000;
        EVT_HANDLE bookmark = null;
        Pointer pBookmarkXml = null;
        try {
            bookmark = Wevtapi.INSTANCE.EvtCreateBookmark(null);
            if (bookmark == null) {
                Exception e = new Win32Exception(Kernel32.INSTANCE.GetLastError());
                this.recordException("", e);
                BaseWorker.LOGGER.debug("EvtCreateBookmark失败", e);
                return false;
            }
            if (!Wevtapi.INSTANCE.EvtUpdateBookmark(bookmark, handle)) {
                Exception e = new Win32Exception(Kernel32.INSTANCE.GetLastError());
                this.recordException("", e);
                BaseWorker.LOGGER.debug("EvtUpdateBookmark失败", e);
                //return false;
            }
            IntByReference bufferUsed = new IntByReference();
            IntByReference propertyCount = new IntByReference();
            pBookmarkXml = new Memory(dwBufferSize);
            if (Wevtapi.INSTANCE.EvtRender(null, bookmark, Winevt.EVT_RENDER_FLAGS.EvtRenderBookmark, dwBufferSize, pBookmarkXml, bufferUsed, propertyCount)) {

            } else {
                Exception e = new Win32Exception(Kernel32.INSTANCE.GetLastError());
                this.recordException("", e);
                BaseWorker.LOGGER.debug("saveBookmark失败", e);
                return false;
            }
            Path bookmarkFilePath = Paths.get(this.bookmarkPath, channel.replaceAll("/","-"));
            BaseWorker.LOGGER.trace("书签内容：\n" + pBookmarkXml.getWideString(0));
            try {
                Files.write(bookmarkFilePath, pBookmarkXml.getWideString(0).getBytes(), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
            } catch (IOException e) {
                BaseWorker.LOGGER.error("将书签内容保存到文件时发生异常", e);
                return false;
            }
            return true;
        } finally {
            if (bookmark != null)
                Wevtapi.INSTANCE.EvtClose(bookmark);
            if (pBookmarkXml != null)
                pBookmarkXml.clear(dwBufferSize);
        }
    }

    protected String getMessageFromEVT_HANDLE(EVT_HANDLE h) {
        IntByReference status = new IntByReference();
        EVT_HANDLE hSystemContext = Wevtapi.INSTANCE.EvtCreateRenderContext(0, null, Winevt.EVT_RENDER_CONTEXT_FLAGS.EvtRenderContextSystem);
        if (hSystemContext == null) {
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }
        Pointer pRenderedValues = null;
        int dwBufferSize = 0;  //真实的读取大小
        IntByReference bufferUsed = new IntByReference();
        IntByReference propertyCount = new IntByReference();
        if (!Wevtapi.INSTANCE.EvtRender(hSystemContext, h, Winevt.EVT_RENDER_FLAGS.EvtRenderEventValues, dwBufferSize, pRenderedValues, bufferUsed, propertyCount)) {
            if (W32Errors.ERROR_INSUFFICIENT_BUFFER != Kernel32.INSTANCE.GetLastError()) {
                Win32Exception win32Exception = new Win32Exception(Kernel32.INSTANCE.GetLastError());
                BaseWorker.LOGGER.debug(W32Errors.ERROR_INSUFFICIENT_BUFFER+"第一次EvtRender发生异常:"+win32Exception.getMessage());
                throw win32Exception;
            }
            dwBufferSize = bufferUsed.getValue();
            pRenderedValues = new Memory(dwBufferSize);
            if (!Wevtapi.INSTANCE.EvtRender(hSystemContext, h, Winevt.EVT_RENDER_FLAGS.EvtRenderEventValues,
                    dwBufferSize, pRenderedValues, bufferUsed, propertyCount)) {
                Win32Exception e = new Win32Exception(Kernel32.INSTANCE.GetLastError());
                BaseWorker.LOGGER.error("日志渲染EvtRender出现异常", e);
                throw e;
            }
            Winevt.EVT_VARIANT tmp = new Winevt.EVT_VARIANT();
            tmp.use(pRenderedValues);
            tmp.read();
            EVT_HANDLE hMetadata = null;
            try {
                hMetadata = Wevtapi.INSTANCE.EvtOpenPublisherMetadata(null, tmp.getValue().toString(), null, 0, 0);
            } catch (Exception e) {
                BaseWorker.LOGGER.error("EvtOpenPublisherMetadata发生异常", e);
            }
            if (hMetadata == null) {
                //LOGGER.debug("hMetadata为null，未格式化日志 - " + tmp.getValue().toString());
            }
            String pwsMessage = getMessageString(hMetadata, h, status);
            if (pwsMessage != null) {
                if (status.getValue() != 0) {
                    BaseWorker.LOGGER.debug("格式化字符串非空,status非零");
                }
            } else {
                if (status.getValue() != 0)
                    BaseWorker.LOGGER.debug("无法正确格式化日志,status非零:\n", pwsMessage);
            }
//            System.out.println("--------" + pwsMessage + "------------");.
            if (pRenderedValues != null) {
                long peer = Pointer.nativeValue(pRenderedValues);
                Native.free(peer);//手动释放内存
                Pointer.nativeValue(pRenderedValues, 0);//避免Memory对象被GC时重复执行Nativ.free()方法
            }
            if (h != null) {
                Wevtapi.INSTANCE.EvtClose(h);
            }
            if (hSystemContext != null) {
                Wevtapi.INSTANCE.EvtClose(hSystemContext);
            }
            if (hMetadata != null) {
                Wevtapi.INSTANCE.EvtClose(hMetadata);
            }
            return pwsMessage == null ? null : pwsMessage.trim();
        } else {
            BaseWorker.LOGGER.debug("EvtRender首次调用返回TRUE,未格式化日志");
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }
    }

    protected static String getMessageString(EVT_HANDLE hMetadata, EVT_HANDLE hEvent, IntByReference status) {
        // TODO 优化各种处理失败的情况
        char[] buffer = null;
        IntByReference dwBufferUsed = new IntByReference();
        int dwBufferSize = 0;
        int _status = 0;
        if (!Wevtapi.INSTANCE.EvtFormatMessage(hMetadata, hEvent, 0, 0, null, EVT_FORMAT_MESSAGE_FLAGS.EvtFormatMessageXml, dwBufferSize, buffer, dwBufferUsed)) {
            _status = Kernel32.INSTANCE.GetLastError();
            if (_status == W32Errors.ERROR_INSUFFICIENT_BUFFER || (hMetadata == null && _status == W32Errors.ERROR_INVALID_PARAMETER)) {
                _status = 0;
                dwBufferSize = dwBufferUsed.getValue() + 1;
                buffer = new char[dwBufferSize];
                Wevtapi.INSTANCE.EvtFormatMessage(hMetadata, hEvent, 0, 0, null, EVT_FORMAT_MESSAGE_FLAGS.EvtFormatMessageXml, dwBufferSize, buffer, dwBufferUsed);
                buffer[dwBufferUsed.getValue() - 1] = '\0';
            } else {
                if (buffer != null)
                    BaseWorker.LOGGER.error("其他异常编号：" + Kernel32.INSTANCE.GetLastError(), new Win32Exception(Kernel32.INSTANCE.GetLastError()));
            }
        }
        status = new IntByReference(_status);
        String str = null;
        if (buffer != null) {
            str = new String(buffer);
        }
        return str;
    }

    public static void main(String args[]) throws InterruptedException {
        Map configMap = new HashMap<>();
        configMap.put("moduleType", "event_log");
        configMap.put("index", "event_log");
        ArrayList<Object> objects = new ArrayList<>();
        objects.add("Application");
        configMap.put("eventTypes",objects );
        EventLogShipper shipper = new EventLogShipper(new EventLogShipperConfig(configMap));
        BaseCache cache = CacheFactory.createCache();
        shipper.setRunSignal(true);
        shipper.setLv1Cache(cache);
        shipper.doStart();

//        Map configMap1 = new HashMap<>();
//        configMap1.put("logType", "win_event");
//        WinEventIndexer winEventIndexer = new WinEventIndexer(new BaseIndexerConfig(configMap1));
//        winEventIndexer.setLv1Cache(cache);
//        winEventIndexer.doStart();
        while (true){
            System.out.println(cache.take(1));
        }
//        WinEventIndexer winEventIndexer = new WinEventIndexer();
    }

}
