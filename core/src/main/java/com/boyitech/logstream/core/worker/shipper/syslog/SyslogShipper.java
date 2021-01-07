package com.boyitech.logstream.core.worker.shipper.syslog;

import com.boyitech.logstream.core.cache.BaseCache;
import com.boyitech.logstream.core.factory.CacheFactory;
import com.boyitech.logstream.core.factory.SingleListenerFactory;
import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.listener.BaseListener;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.worker.shipper.BaseListenerShipper;
import com.boyitech.logstream.core.worker.shipper.BaseShipperConfig;

import javax.management.InvalidAttributeValueException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * @Author Eric Zheng
 * @Description syslog使用的是udp,tcp
 * @Date 17:04 2019/8/1
 **/
public class SyslogShipper extends BaseListenerShipper {
    private SyslogShipperConfig conf;
    BaseListener listener;

    public SyslogShipper(BaseShipperConfig config) {
        super(config);
        this.conf = (SyslogShipperConfig) config;

    }

    public SyslogShipper(String porterID, BaseShipperConfig config) {
        super(porterID, config);
        this.conf = (SyslogShipperConfig) config;

    }

    @Override
    public void passMessage(Event event) {
        if (conf.isChangeIndex()) {
            event.setIndex(conf.getIndex());
        }
//        if (event.getInetInfo() != null) {
//            event.setMsgType(this.workerType()+"_"+event.getInetInfo().getProtocol());
//        }else {
            event.setMsgType(this.workerType());
       // }

        event.setMark(this.workerId);

        count.addAndGet(1);
        try {
            lv1Cache.put(event);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        System.out.println(event);
    }

    @Override
    public boolean isMultiline() {
        return conf.isMultiline();
    }

    @Override
    public Map<String, String> getMultilineConfig() {
        return conf.getMultilineRule();
    }

    @Override
    public int getPort() {
        return conf.getPort();
    }

    @Override
    public String getHost() {
        return conf.getHost();
    }

    @Override
    public List<String> getProtocol() {
        return conf.getProtocolList();
    }


    @Override
    public boolean register() {

        listener = SingleListenerFactory.getListenerInstance();
        filterRuleList = conf.getFilterRuleList();

        listener.registerShipper(this);


        return true;
    }

    @Override
    public void tearDown() {
        if (listener != null) {
            listener.unregisterShipper(this);
        }
    }

    @Override
    public void execute() throws InterruptedException, IOException {

    }

    @Override
    public String workerType() {
        return "syslog_listener";
    }

    public static void main(String args[]) throws InvalidAttributeValueException {

        String config = "{\"host\":\"172.17.20.81\",\"port\":\"514\",\"protocols\":[\"udp\",\"tcp\"],\"moduleType\":\"syslog\",\"index\":\"514\",\"filters\":[{}]}";
        Map<String, String> stringStringMap = GsonHelper.fromJson(config);
        SyslogShipperConfig conf = new SyslogShipperConfig(stringStringMap);
        SyslogShipper shipper = new SyslogShipper(conf);
        BaseCache cache = CacheFactory.createCache();
        shipper.setLv1Cache(cache);
        shipper.doStart();

        String config1 = "{\"host\":\"172.17.20.81\",\"port\":\"515\",\"protocols\":[\"udp\",\"tcp\"],\"moduleType\":\"syslog\",\"index\":\"515\",\"filters\":[{\"srcAddr\": \"172.17.20.48\"}]}";
        Map<String, String> stringStringMap1 = GsonHelper.fromJson(config1);
        SyslogShipperConfig conf1 = new SyslogShipperConfig(stringStringMap1);
        SyslogShipper shipper1 = new SyslogShipper(conf1);
        BaseCache cache1 = CacheFactory.createCache();
        shipper1.setLv1Cache(cache1);
        shipper1.doStart();

        String config2 = "{\"host\":\"172.17.20.81\",\"port\":\"516\",\"protocols\":[\"udp\",\"tcp\"],\"moduleType\":\"syslog\",\"index\":\"516\",\"filters\":[{}]}";
        Map<String, String> stringStringMap2 = GsonHelper.fromJson(config2);
        SyslogShipperConfig conf2 = new SyslogShipperConfig(stringStringMap2);
        SyslogShipper shipper2 = new SyslogShipper(conf2);
        BaseCache cache2 = CacheFactory.createCache();
        shipper2.setLv1Cache(cache2);
        shipper2.doStart();

        new Thread(() -> {
            while (true) {
                try {
                    System.out.println("514:" + cache.take(1));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        new Thread(() -> {
            while (true) {
                try {
                    System.out.println("514:" + cache1.take(1));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        new Thread(() -> {
            while (true) {
                try {
                    System.out.println("516:" + cache2.take(1));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();


//            shipper2.doDestroy();

    }
}