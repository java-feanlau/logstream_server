package com.boyitech.logstream.client.info;

public class Version {
	public static  String VERSION = "0.1.1";
	public volatile static double CASVERSION = 0;

	public static void setCASVERSION(double CASVERSION) {
		Version.CASVERSION = CASVERSION;
	}
}
