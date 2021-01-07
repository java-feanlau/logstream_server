package com.boyitech.logstream.core.worker.shipper.event_log;

import java.util.ArrayList;
import java.util.Map;

import com.boyitech.logstream.core.worker.shipper.BaseShipperConfig;

public class EventLogShipperConfig extends BaseShipperConfig {

    private final ArrayList<String> eventTypes = new ArrayList<>();              //监听的目录


    public EventLogShipperConfig(Map config) {
        super(config);
        if (config.get("eventTypes") == null || "".equals(config.get("eventTypes"))) {
            eventTypes.add("Application");
            eventTypes.add("System");
            eventTypes.add("Security");
            eventTypes.add("ForwardedEvents");
        } else {
            ArrayList<String> eventTypes = (ArrayList<String>) config.get("eventTypes");
            this.eventTypes.addAll(eventTypes);
        }
    }

    public ArrayList<String> getEventTypes() {
        return eventTypes;
    }
}
