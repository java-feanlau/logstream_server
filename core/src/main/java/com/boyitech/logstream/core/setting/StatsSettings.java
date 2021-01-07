package com.boyitech.logstream.core.setting;

public class StatsSettings extends BaseSettings {

	public static final Setting<String> STATS_QUERY_SLEEP_TIME = Setting.stringSetting("status.query.sleep.mtime", "5000");
	public static final Setting<String> STATS_COUNT_SLEEP_TIME = Setting.stringSetting("status.count.sleep.mtime", "5000");


	public static final Setting<String> GRAPHITE_HOST = Setting.stringSetting("graphite.host", "172.17.30.10");
	public static final Setting<Integer> GRAPHITE_PORT = Setting.integerSetting("graphite.port", 2003);


}
