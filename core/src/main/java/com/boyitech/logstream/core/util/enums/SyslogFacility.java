package com.boyitech.logstream.core.util.enums;

import java.time.DateTimeException;

public enum SyslogFacility {

	KERNEL(0, "kernel messages"), USERLEVEL(1, "user-level messages"), MAILSYSTEM(2, "mail system"),
	SYSTEMDAEMONS(3, "system daemons"), SECURITY_AUTHORIZATION(4, "security/authorization messages"),
	SYSLOGD(5, "messages generated internally by syslogd"), LINEPRINTER(6, "line printer subsystem"),
	NETWORKNEWS(7, "network news subsystem"), UUCP(8, "UUCP subsystem"), CLOCK(9, "clock daemon"),
	SECURITY_AUTHORIZATION2(10, "security/authorization messages"), FTP(11, "FTP daemon"), NTP(12, "NTP subsystem"),
	LOGAUDIT(13, "log audit"), LOGALERT(14, "log alert"), CLOCK2(15, "clock daemon"), LOCALUSE0(16, "local use 0"),
	LOCALUSE1(17, "local use 1"), LOCALUSE2(18, "local use 2"), LOCALUSE3(19, "local use 3"), LOCALUSE4(20, "local use 4"),
	LOCALUSE5(21, "local use 5"), LOCALUSE6(22, "local use 6"), LOCALUSE7(23, "local use 7");

    // 成员变量
    private String facility;
    private int code;
    // 构造方法
    private SyslogFacility(int code, String facility) {
        this.facility = facility;
        this.code = code;
    }

    public static String getFacility(int code) {
		if (code < 0 || code > 23) {
            throw new DateTimeException("Invalid value for facility: " + code);
        }
    	for(SyslogFacility f : SyslogFacility.values()) {
    		if(f.code == code)
    			return f.facility;
    	}
    	return null;
    }

}
