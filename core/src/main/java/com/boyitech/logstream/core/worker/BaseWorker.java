package com.boyitech.logstream.core.worker;

import com.boyitech.logstream.core.info.exception.ExceptionInfo;
import com.boyitech.logstream.core.info.exception.ExceptionLevel;
import com.boyitech.logstream.core.setting.SystemSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

public abstract class BaseWorker implements Worker, Runnable {

    protected static final Logger LOGGER = LogManager.getLogger("worker");
    protected final String workerId;
    protected String mark = null; //标识
    protected int threadsNum; //线程
    protected CountDownLatch countDownLatch;
    protected volatile boolean runSignal = false;
    private ExceptionInfo[] lastExceptions = new ExceptionInfo[SystemSettings.EXCEPTIONSlENGTH.getValue()];
    protected final Set<WorkerThread> threads = new HashSet<>();
    protected final ReentrantLock threadListLock = new ReentrantLock(false);

    public BaseWorker(BaseWorkerConfig config) {
        workerId = UUID.randomUUID().toString();
        if (config != null) {
            this.threadsNum = config.getThreadNum();
        } else {
            this.threadsNum = 1;
        }
    }

    public BaseWorker(String worerId, BaseWorkerConfig config) {
        this.workerId = worerId;
        if (config != null) {
            this.threadsNum = config.getThreadNum();
        } else {
            this.threadsNum = 1;
        }
        lastExceptions = new ExceptionInfo[SystemSettings.EXCEPTIONSlENGTH.getValue()];
    }

    //启动 indexer，shipper，porter执行的方法
    @Override
    public boolean doStart() {
        threadListLock.lock();
        try {
            // 如果worker正在运行或当前线程数小于1，返回false
            if (this.isAlive() || threadsNum < 1) {
                return false;
            }
            // register,进行初始化
            try {
                if (!register()) {
                    return false;
                }
            } catch (Exception e) {
                this.recordException("worker初始化发生异常", e);
                return false;
            }
            threads.clear();
            this.runSignal = true;
            // 创建新的队列和线�??
            for (int i = 0; i < threadsNum; i++) {
                WorkerThread thread = createThread();
                threads.add(thread);
                thread.start();
            }
            return true;
        } catch (Exception e) {
            LOGGER.error("worker异常", e);
            return false;
        } finally {
            threadListLock.unlock();
        }
    }

    /*
     * @Author Eric Zheng
     * @Description 目前来说暂停和销毁所做的职责都是相同的。差别在于数据库和缓存，这部分操作交友manager完成
     * @Date 10:23 2019/3/15
     **/
    public boolean doStop(CountDownLatch countDownLatch) {
        return doDestroy(countDownLatch);
    }


    public boolean doDestroy(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
        if (!this.isAlive()) {
            long count = countDownLatch.getCount();
            for (long i = 0; i < count; i++) {
                countDownLatch.countDown();
            }

        }
        return doDestroy();
    }

    @Override
    public boolean doStop() {
        tearDown();
        this.runSignal = false;
        return true;
    }

    @Override
    public boolean doDestroy() {
        tearDown();
        this.runSignal = false;
        return true;
    }

    @Override
    public boolean isAlive() {
        threadListLock.lock();
        if (threads == null || threads.size() == 0) {
            threadListLock.unlock();
            return false;
        } else {
            boolean flag = false;
            for (Thread t : threads) {
                if (t.isAlive()) {
                    flag = true;
                    break;
                }
            }
            threadListLock.unlock();
            return flag;
        }
    }

    @Override
    public boolean isDone() {
        return false;
    }

    //实现runnable接口
    public void run() {
        while (runSignal && ((WorkerThread) Thread.currentThread()).isRun()) {
            try {
                execute();
                Thread.sleep(0);
            } catch (InterruptedException e) {
                LOGGER.warn(Thread.currentThread().getName() + "异常");
                break;
            } catch (Exception e) {
                LOGGER.error(Thread.currentThread().getName() + " exception in run():", e);
                this.recordException("", e);
                break;
            }
        }
        if (countDownLatch != null) {
            countDownLatch.countDown();
        }
        LOGGER.debug(Thread.currentThread().getName() + "结束运行");
    }


    protected WorkerThread createThread() {
        WorkerThread thread = new WorkerThread(this);      //this为当前runnable对象，即BaseWorker对象，这样创建的线程会执行上面实现的run方法
        thread.setName(this.workerType() + "  " + this.workerId + "  " + thread.getName());
        LOGGER.info("创建线程 " + thread.getName());
        return thread;
    }

    @Override
    public boolean forceStop() {
        for (Thread t : threads) {
            t.interrupt();
        }
        return true;
    }

    @Override
    public void recordException(String msg, Exception e) {
        if (msg.equals("") || msg == null) {
            this.addException(e.getMessage());
            return;
        }
        // TODO Auto-generated method stub
        LOGGER.error(msg + ":" + e);
        this.addException(msg + ":" + e.getMessage());
    }

    public void addException(String errerMessage) {
        addException(ExceptionLevel.MAJOR, errerMessage);
    }


    public void addException(String errerLevel, String exception) {
        if(exception !=null){
            exception = exception.replaceAll(":", "");
            exception = exception.replaceAll("/", "");  //有这两个字符，客户端发送过来会报错。辣鸡gson
        }

        int length = lastExceptions.length;
        for (int i = 1; i < length; i++) {
            lastExceptions[length - i] = lastExceptions[length - i - 1];
        }
        ExceptionInfo exceptionInfo = new ExceptionInfo(errerLevel, exception);
        lastExceptions[0] = exceptionInfo;
    }


    public int getThreadsNum() {
        return threadsNum;
    }


    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    public void setCountDownLatch(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    public boolean isRunSignal() {
        return runSignal;
    }

    public void setRunSignal(boolean runSignal) {
        this.runSignal = runSignal;
    }


    public ExceptionInfo[] getLastExceptions() {
        return lastExceptions;
    }


    public String getWorkerId() {
        return workerId;
    }


    public String workerType() {
        return "base";
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public boolean register() {
        return true;
    }

    @Override
    public boolean doRestart() {
        return false;
    }
}
