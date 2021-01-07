package com.boyitech.logstream.core.util.enums;

import java.time.DateTimeException;

public enum SyslogSeverity {
	Emergency(0, "Emergency","system is unusable"), Alert(1, "Alert", "action must be taken immediately"),
	Critical(2, "Critical", "critical conditions"), Error(3, "Error", "error conditions"),
	Warning(4, "Warning", "warning conditions"), Notice(5, "Notice", "normal but significant condition"),
	Informational(6, "Informational", "informational messages"), Debug(7, "Debug", "debug-level messages");

	private int code;
	private String severity;
	private String description;

	private SyslogSeverity(int code, String severity, String description) {
		this.code = code;
		this.severity = severity;
		this.description = description;
	}

	public static String getSeverity(int code) {
		if (code < 0 || code > 7) {
            throw new DateTimeException("Invalid value for severity: " + code);
        }
		for(SyslogSeverity s : SyslogSeverity.values()) {
			if(s.code==code) {
				return s.severity;
			}
		}
		return null;
	}

}
