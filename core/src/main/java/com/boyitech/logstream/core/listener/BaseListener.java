package com.boyitech.logstream.core.listener;

import com.boyitech.logstream.core.worker.shipper.BaseListenerShipper;
import com.boyitech.logstream.core.worker.shipper.BaseShipper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Eric
 * @Title: BaseListener
 * @date 2019/8/1 10:38
 * @Description: TODO
 */
public abstract class BaseListener {

    protected static final Logger LOGGER = LogManager.getLogger("main");

    public abstract boolean registerShipper(BaseListenerShipper shipper);

    public abstract boolean unregisterShipper(BaseListenerShipper shipper);
}
