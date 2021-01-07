package com.boyitech.logstream.core.setting;

public class RestfulServerSetting {
	public static final Setting<String> HOST = Setting.stringSetting("server.restful.host", "0.0.0.0");
	public static final Setting<Integer> PORT = Setting.integerSetting("server.restful.port", 6666);
}
