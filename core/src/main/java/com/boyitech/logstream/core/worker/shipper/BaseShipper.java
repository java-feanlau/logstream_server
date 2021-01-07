package com.boyitech.logstream.core.worker.shipper;

import com.boyitech.logstream.core.cache.BaseCache;
import com.boyitech.logstream.core.worker.BaseWorker;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;

import java.util.concurrent.atomic.AtomicLong;

public abstract class BaseShipper extends BaseWorker {

    protected BaseCache lv1Cache;


    private final String moudleType;

    protected AtomicLong count = new AtomicLong();

    public BaseShipper(BaseShipperConfig config) {
        super(config);
        moudleType = config.getModuleType();
        LOGGER.debug("初始化shipper");
    }


    public BaseShipper(String porterID, BaseShipperConfig config) {
        super(porterID, config);
        moudleType = config.getModuleType();
        LOGGER.debug("初始化shipper");
    }

    public BaseCache getLv1Cache() {
        return lv1Cache;
    }

    public void setLv1Cache(BaseCache lv1Cache) {
        this.lv1Cache = lv1Cache;
    }

    @Override
    public String workerType() {
        return "shipper";
    }

    public AtomicLong getCount() {
        return count;
    }

    public String getMoudleType() {
        return moudleType;
    }

}
