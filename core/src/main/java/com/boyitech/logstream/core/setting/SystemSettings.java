package com.boyitech.logstream.core.setting;

public class SystemSettings {

	public static final Setting<String> MODEL = Setting.stringSetting("system.operation.model", "default");

	public static final Setting<String> SOAPHOST = Setting.stringSetting("system.soap.host", null);
	public static final Setting<Integer> SOAPPORT = Setting.integerSetting("system.soap.port", 9433);


	public static final Setting<Integer> EXCEPTIONSlENGTH = Setting.integerSetting("system.worker.exceptions.size", 10);



	public static boolean isSoapEnable() {
		return SOAPHOST.getValue()!=null && SOAPPORT.getValue()>0;
	}
}
