package com.boyitech.logstream.core.info.info;

/**
 * @author Eric
 * @Title: SecondCountAndTime
 * @date 2019/3/22 17:15
 * @Description: TODO
 */
public class BatchCountAndTimeInfo {
    private Long count;
    private Long time;    //这一批次记录的时间

    public BatchCountAndTimeInfo(Long count, Long time) {
        this.count = count;
        this.time = time;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}
