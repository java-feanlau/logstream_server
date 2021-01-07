package com.boyitech.logstream.core.setting;

public class CacheSettings extends BaseSettings {

	public static final Setting<Integer> MEMORYCACHESIZE = Setting.integerSetting("cache.memory.size", 100000);


	public static final Setting<Integer> GLOBALCACHESIZE = Setting.integerSetting("cache.global.size", 100000);

	public static final Setting<Integer> GEOIPCACHESIZE = Setting.integerSetting("cache.geoip.size", 200000);


}