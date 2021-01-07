package com.boyitech.logstream.core.worker.indexer;

import com.boyitech.logstream.core.cache.BaseCache;
import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.GrokUtil;
import com.boyitech.logstream.core.worker.BaseWorker;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public abstract class BaseIndexer extends BaseWorker {

    protected BaseCache lv1Cache;
    protected BaseCache lv2Cache;

    protected AtomicLong countLong = new AtomicLong();
    protected AtomicLong failed =new AtomicLong();

    protected String logType;

    protected String ipFilter;
    protected DateTimeFormatter dateTimepattern = DateTimeFormat.forPattern("YYYYMM");

    public BaseIndexer(BaseWorkerConfig config) {
        super(config);
        this.logType = config.getLogType();
        this.ipFilter=config.getIpfilter();
        LOGGER.debug("初始化indexer");
    }

    public BaseIndexer(String indexerID,BaseWorkerConfig config) {
        super(indexerID,config);
        this.logType = config.getLogType();
        this.ipFilter=config.getIpfilter();
        LOGGER.debug("初始化indexer");
    }

    @Override
    public void execute() throws InterruptedException {
        List<Event> eventList = lv1Cache.poll(1000);
        int failNum = 0;
        for (Event e : eventList) {
            try {
                e.setLogType(logType);
                e.setMetafieldType(logType);
                if (!format(e)) {
                    failNum++;
                }
            } catch (Exception exc) {
                Map<String, Object> format = e.getFormat();
                if (format.get("message") == null) {
                    format.put("message", e.getMessage());
                }
            }
            if(GrokUtil.isStringHasValue(String.valueOf(e.getFormat().get("@timestamp")))){
                DateTime dt=new DateTime(e.getFormat().get("@timestamp"));
                e.setEsIndex(Integer.valueOf(dt.toString(dateTimepattern)));
            }
            else
            {
                e.setEsIndex(Integer.valueOf((e.getReceivedAt().toString(dateTimepattern))));
            }

        }
        countLong.addAndGet(eventList.size());
        failed.addAndGet(failNum);
        lv2Cache.put(eventList);

    }


    public abstract boolean format(Event event);

    public BaseCache getLv1Cache() {
        return lv1Cache;
    }

    public void setLv1Cache(BaseCache lv1Cache) {
        this.lv1Cache = lv1Cache;
    }

    public BaseCache getLv2Cache() {
        return lv2Cache;
    }

    public void setLv2Cache(BaseCache lv2Cache) {
        this.lv2Cache = lv2Cache;
    }


    @Override
    public String workerType() {
        return "indexer";
    }

    public AtomicLong getCountLong() {
        return countLong;
    }

    public AtomicLong getFailed() {
        return failed;
    }
}
