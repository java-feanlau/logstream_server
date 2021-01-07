package com.boyitech.logstream.core.util.enums;

public enum LogLevel {

	DEFAULT(0,"无"),LOW(1, "低"), MIDDLE(2, "中"), HIGH(3,"高");

	private int level;
	private String name;

	private LogLevel(int level, String name) {
		this.level = level;
		this.name = name;
	}

	public static LogLevel of(String name) {
		for(LogLevel m : LogLevel.values()) {
			if(m.name.equals(name)) {
				return m;
			}
		}
		return null;
	}

	public int getLevel() {
		return level;
	}

	public String getName() {
		return name;
	}

}
