package com.boyitech.logstream.core.util.enums;

import java.time.DateTimeException;

public enum Month {
    JANUARY(0, "Jan", "January"), FEBRUARY(1, "Feb", "February"), MARCH(2, "Mar", "March"), APRIL(3, "Apr", "April"),
    MAY(4, "May", "May"), JUNE(5, "Jun", "June"),JULY(6, "Jul", "July"), AUGUST(7, "Aug", "August"), SEPTEMBER(8, "Sep", "September"),
    OCTOBER(9, "Oct", "October"), NOVEMBER(10, "Nov", "November"), DECEMBER(11, "Dec", "December");

	private int node;
	private String simple;
	private String full;
	private static final Month[] ENUMS = Month.values();

	private Month(int node, String simple, String full) {
		this.node = node;
		this.simple = simple;
		this.full = full;
	}

	public static Month of(int month) {
		if (month < 1 || month > 12) {
            throw new DateTimeException("Invalid value for MonthOfYear: " + month);
        }
        return ENUMS[month - 1];
    }

	public static Month of(String simple) {
		for(Month m : Month.values()) {
			if(m.simple.equals(simple)) {
				return m;
			}
		}
		return null;
	}


	public static Month ofFull(String full) {
		for(Month m : Month.values()) {
			if(m.full.equals(full)) {
				return m;
			}
		}
		return null;
	}

	public int getValue() {
		return this.node+1;
	}

}
