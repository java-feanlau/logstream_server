package com.boyitech.logstream.core.setting;

public class MetricsSettings extends BaseSettings {

	public static final Setting<Integer> CONSOLEINTERVAL = Setting.integerSetting("metrics.console.interval", 0);

	public static final Setting<Integer> GRAPHITEINTERVAL = Setting.integerSetting("metrics.graphite.interval", 0);
	public static final Setting<String> GRAPHITEHOST = Setting.stringSetting("metrics.graphite.host", "172.17.30.10");
	public static final Setting<Integer> GRAPHITEPORT = Setting.integerSetting("metrics.graphite.port", 2003);

	public static boolean isGraphiteEnable() {
		return GRAPHITEINTERVAL.getValue()>0;
	}

	public static boolean isConsoleEnable() {
		return CONSOLEINTERVAL.getValue()>0;
	}

}
