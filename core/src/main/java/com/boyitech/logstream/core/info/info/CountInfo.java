package com.boyitech.logstream.core.info.info;


import com.boyitech.logstream.core.info.queue.LimitQueue;

/**
 * @author Eric
 * @Title: CountInfo
 * @date 2019/3/19 13:45
 * @Description: TODO
 */
public class CountInfo {
    private long lastTime; //上一次记录总量的时间
    private double bacthTime;     //两次记录时间的间隔时间 秒为单位，精确到毫秒
    private long count = 0;    //解析的总数量
    private long bacthCount = 0;     //两次记录时间中积累的数量

    public CountInfo(long lastTime) {
        this.lastTime = lastTime;
    }

    //存500个 休眠一秒解析的数量和解析的时间
    private LimitQueue oneMinuteQueue = new LimitQueue();

    private LimitQueue fiveMinuteQueue = new LimitQueue();

    private LimitQueue fifteenMinuteQueue = new LimitQueue();

    public void addCountAndTime(long count) {
        long now = System.currentTimeMillis();
        bacthTime =(double) (now - lastTime) / 1000.0;
        this.bacthCount = count - this.count;
        this.count = count;
        oneMinuteQueue.offer(new BatchCountAndTimeInfo(bacthCount, now), 60);
        fiveMinuteQueue.offer(new BatchCountAndTimeInfo(bacthCount, now), 300);
        fifteenMinuteQueue.offer(new BatchCountAndTimeInfo(bacthCount, now), 900);
        lastTime = now;
    }

    public long getSpeed() {
        if(bacthTime == 0)
            return 0;
        long l = (long) (bacthCount / bacthTime);
        return l;
    }

    public long getOneSpeed(){
        return oneMinuteQueue.getSpeed();
    }

    public long getFiveSpeed(){
        return fiveMinuteQueue.getSpeed();
    }

    public long getFifteenSpeed(){
        return fifteenMinuteQueue.getSpeed();
    }



    public long getCount() {
        return count;
    }
}
