package com.boyitech.logstream.core.info.queue;



import com.boyitech.logstream.core.info.info.BatchCountAndTimeInfo;

import java.util.LinkedList;

public class LimitQueue {


    private  LinkedList<BatchCountAndTimeInfo>  queue = new LinkedList<BatchCountAndTimeInfo>();


    /**
     * 入列：当队头元素的时间超过限定时间（秒）的时候，把队头的元素poll掉，
     */
    public synchronized  void offer(BatchCountAndTimeInfo e, int time) {
//        if (queue.size() == 0){
//            queue.offer(e);
//            return;
//        }
//        while (((e.getTime() - queue.getFirst().getTime()) / 1000 > time) ) {
//            queue.poll();
//        }
//        queue.offer(e);
        while (queue.size() !=0 && ((e.getTime() - queue.getFirst().getTime()) / 1000 > time) ) {
            queue.poll();
        }
        queue.offer(e);
    }

    public BatchCountAndTimeInfo get(int position) {
        return queue.get(position);
    }

    public BatchCountAndTimeInfo getLast() {
        return queue.getLast();
    }

    public BatchCountAndTimeInfo getFirst() {
        return queue.getFirst();
    }

    public synchronized long getSpeed() {
        if(queue.size() == 0 || queue.size() == 1){
            return 0;
        }
        long count = 0;
        double time = this.getTime();
        for (BatchCountAndTimeInfo batchCountAndTimeInfo : queue) {
            count += batchCountAndTimeInfo.getCount();
        }
        long l = (long) (count / time);
        return l;
    }

    public double getTime(){
        double time = queue.getLast().getTime() - queue.getFirst().getTime();
        time = time /1000;
        return time;
    }

    public int size() {
        return queue.size();
    }

}
