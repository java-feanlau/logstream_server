package com.boyitech.logstream.core.worker.shipper;

import com.boyitech.logstream.core.cache.BaseCache;
import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.filter_rule.FilterRule;
import com.boyitech.logstream.core.worker.BaseWorker;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public abstract class BaseListenerShipper extends BaseShipper {
    protected List<FilterRule> filterRuleList;

    public BaseListenerShipper(BaseShipperConfig config) {
        super(config);
    }

    public BaseListenerShipper(String porterID, BaseShipperConfig config) {
        super(porterID, config);
    }


    public List<FilterRule> getFilterRuleList() {
        return filterRuleList;
    }

    public abstract void passMessage(Event message);

    public abstract boolean isMultiline();

    public abstract Map getMultilineConfig();

    public abstract int getPort();
    public abstract String getHost();
    public abstract List<String> getProtocol();


}
