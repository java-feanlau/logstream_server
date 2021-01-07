package com.boyitech.logstream.core.util;

import java.net.InetSocketAddress;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import com.boyitech.logstream.core.setting.MetricsSettings;
import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;

public class MetricHelper {

	private final MetricRegistry metrics;

    private static class MetricHolder {
        private static final MetricHelper INSTANCE = new MetricHelper();
    }

    private MetricHelper (){
    	metrics = new MetricRegistry();
    }

    public static final MetricRegistry getInstance() {
        return MetricHolder.INSTANCE.metrics;
    }

    public static Meter createMeter(String name) {
    	SortedMap<String,Meter> metersMap = MetricHelper.getInstance().getMeters();
    	for(String n : metersMap.keySet()) {
    		if(n.equals(name)) {//已存在就返回结果
    			return metersMap.get(n);
    		}
    	}
    	Meter meter = MetricHelper.getInstance().meter(name);//创建新的meter并返回
    	return meter;
    }

    public static Counter createCounter(String name) {
    	SortedMap<String,Counter> countersMap = MetricHelper.getInstance().getCounters();
    	for(String n : countersMap.keySet()) {
    		if(n.equals(name)) {//已存在就返回结果
    			return countersMap.get(n);
    		}
    	}
    	Counter counter = MetricHelper.getInstance().counter(name);
    	return counter;
    }

    public static void startConsoleReport() {
		ConsoleReporter reporter = ConsoleReporter.forRegistry(MetricHelper.getInstance())
			       .convertRatesTo(TimeUnit.SECONDS)
			       .convertDurationsTo(TimeUnit.MILLISECONDS)
			       .build();
		int seconds = MetricsSettings.CONSOLEINTERVAL.getValue();
		reporter.start(seconds, TimeUnit.MILLISECONDS);
    }

    public static void startGraphiteReport() {
    	String host = MetricsSettings.GRAPHITEHOST.getValue();
    	int port = MetricsSettings.GRAPHITEPORT.getValue();
    	final Graphite graphite = new Graphite(new InetSocketAddress(host, port));
    	final GraphiteReporter reporter = GraphiteReporter.forRegistry(MetricHelper.getInstance())
    			.prefixedWith("java.logStream")
    			.convertRatesTo(TimeUnit.SECONDS)
    			.convertDurationsTo(TimeUnit.MILLISECONDS)
    			.filter(MetricFilter.ALL)
    			.build(graphite);
    	int seconds = MetricsSettings.GRAPHITEINTERVAL.getValue();
    	reporter.start(seconds, TimeUnit.MILLISECONDS);
    }

}
