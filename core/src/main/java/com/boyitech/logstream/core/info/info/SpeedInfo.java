package com.boyitech.logstream.core.info.info;

/**
 * @author Eric
 * @Title: SpeedInfo
 * @date 2019/3/19 13:44
 * @Description: TODO
 */
public class SpeedInfo {
    private long speed = 0;
    private long oneMinuteSpeed = 0;
    private long fiveMinuteSpeed = 0;
    private long fifteenMinuteSpeed = 0;

    public void setAllSpeed(CountInfo countInfo){
        setSpeed(countInfo.getSpeed());
        setOneMinuteSpeed(countInfo.getOneSpeed());
        setFiveMinuteSpeed(countInfo.getFiveSpeed());
        setFifteenMinuteSpeed(countInfo.getFifteenSpeed());
    }

    public long getSpeed() {
        return speed;
    }

    public void setSpeed(long speed) {
        this.speed = speed;
    }

    public long getOneMinuteSpeed() {
        return oneMinuteSpeed;
    }

    public void setOneMinuteSpeed(long oneMinuteSpeed) {
        this.oneMinuteSpeed = oneMinuteSpeed;
    }

    public long getFiveMinuteSpeed() {
        return fiveMinuteSpeed;
    }

    public void setFiveMinuteSpeed(long fiveMinuteSpeed) {
        this.fiveMinuteSpeed = fiveMinuteSpeed;
    }

    public long getFifteenMinuteSpeed() {
        return fifteenMinuteSpeed;
    }

    public void setFifteenMinuteSpeed(long fifteenMinuteSpeed) {
        this.fifteenMinuteSpeed = fifteenMinuteSpeed;
    }

    @Override
    public String toString() {
        return "SpeedInfo{" +
                "speed=" + speed +
                ", fiveSpeed=" + fiveMinuteSpeed +
                ", fifteenMinuteSpeed=" + fifteenMinuteSpeed +
                '}';
    }
}
