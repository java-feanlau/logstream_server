package com.boyitech.logstream.core.util.enums;

public enum MysqlLogLevel {

	DEFAULT(0,"default"), NOTE(3, "Note");

	private int level;
	private String name;

	private MysqlLogLevel(int level, String name) {
		this.level = level;
		this.name = name;
	}

	public static MysqlLogLevel of(String name) {
		for(MysqlLogLevel m : MysqlLogLevel.values()) {
			if(m.name.equals(name)) {
				return m;
			}
		}
		return DEFAULT;
	}

	public static MysqlLogLevel of(int level) {
		for(MysqlLogLevel m : MysqlLogLevel.values()) {
			if(m.level==level) {
				return m;
			}
		}
		return DEFAULT;
	}

	public int getLevel() {
		return level;
	}

	public String getName() {
		return name;
	}

}
