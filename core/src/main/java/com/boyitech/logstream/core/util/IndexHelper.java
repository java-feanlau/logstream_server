package com.boyitech.logstream.core.util;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import com.boyitech.logstream.core.worker.indexer.FormatException;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class IndexHelper {

	public final static Pattern Quotation = Pattern.compile("\".*?\"");
	public final static Pattern GreedyQuotation = Pattern.compile("\".*\"");
	public final static Pattern Brackets = Pattern.compile("^\\[.*?\\]");

	public final static Pattern TimePattern = Pattern.compile("^\\d{2}:\\d{2}:\\d{2}.\\d{3}");
	public final static Pattern DateTimePattern = Pattern.compile("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.\\d{3}");
	public final static Pattern SimpleDateTimePattern = Pattern.compile("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
	public final static Pattern NginxDateTimePattern = Pattern.compile("^\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2}");
	public final static Pattern NGINXACCESSDATETIME = Pattern.compile("^\\[\\d{2}/\\w{3}/\\d{4}:\\d{2}:\\d{2}:\\d{2} \\+\\d{4}\\]"); // [18/May/2018:10:46:10 +0800]
	public final static Pattern APACHEACCESSDATETIME = Pattern.compile("^\\[\\d{1,2}/\\w{3}/\\d{4}:\\d{2}:\\d{2}:\\d{2} \\+\\d{4}\\]"); // [14/May/2018:10:00:07 +0800]
	public final static Pattern SYSLOG3164APPNAMEWITHPROCID = Pattern.compile("^\\S*?\\[\\d+\\]\\:");
	public final static Pattern SYSLOG3164APPNAME = Pattern.compile("^\\S*?:");
	public final static DateTimeFormatter MysqlTimeFormatter = DateTimeFormat.forPattern("YY-MM-dd HH:mm:ss");
	public final static DateTimeFormatter SimpleDateTimeFormatter = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss");
	public final static DateTimeFormatter SimpleDateTimeFormatterWithZone = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss Z");
	public final static DateTimeFormatter DateTimeFormatter = DateTimeFormat.forPattern("YYYY-MM-dd'T'HH:mm:ss.SSS'Z'");
	public final static DateTimeFormatter NginxErrorDateTimeFormatter = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss");
	public final static DateTimeFormatter NginxAccessDateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy:HH:mm:ss Z"); // 18/May/2018:10:46:10 +0800
	public final static DateTimeFormatter Apache22AccessDateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy:HH:mm:ss Z");
	public final static DateTimeFormatter Apache22ErrorDateTimeFormatter = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss");
	public final static DateTimeFormatter OracleDateTimeFormateer = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
	public final static DateTimeFormatter DateTimeFormatterWithUTC = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss.SSS").withZoneUTC();
	public final static DateTimeFormatter EventLogDateTimeFormateer = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'").withZoneUTC();

	public final static Set<String> MONTHS = new HashSet<String>();

	static {
		MONTHS.add("Jan");MONTHS.add("Feb");MONTHS.add("Mar");MONTHS.add("Apr");MONTHS.add("May");MONTHS.add("Jun");
		MONTHS.add("Jul");MONTHS.add("Aug");MONTHS.add("Sep");MONTHS.add("Oct");MONTHS.add("Nov");MONTHS.add("Dec");
	}

	public static String[] nextSplitBySingleSpace(String str) throws FormatException {
		if(str==null || str.equals("")) {
			throw new FormatException("日志格式错误");
		}
		String[] result = new String[2];
		str = str.trim();
		int i = str.indexOf(" ");
		if(i>0) {
			result[0] = str.substring(0, i);
			result[1] = str.substring(i + 1);
		}else {
			result[0] = str;
			result[1] = "";
		}

		return result;
	}

	public static String getHTTPThreadLevel(String method, String category) {
//		1/2/3/4类的Get/Post/head  低危
//		5类的Get/Post/head  中危
//		1/2/3类的options connet trace 中危（不常用的method）
//		4/5类不常用的method 高危
//		所有put delete patch都是高危
		switch(method) {
		case "GET":
		case "HEAD":
		case "POST":
			switch(category) {
			case "消息":
			case "成功":
			case "重定向":
			case "客户端错误":
				return "低危";
			case "服务器错误":
			case "无法识别":
				return "中危";
			default:
				return "高危";
			}
		case "PUT":
		case "DELETE":
		case "PATCH":
			return "高危";
		case "TRACE":
		case "OPTIONS":
		case "CONNECT":
			switch(category) {
			case "消息":
			case "成功":
			case "重定向":
			case "无法识别":
				return "中危";
			case "客户端错误":
			case "服务器错误":
				return "高危";
			default:
				return "高危";
			}
		default:
			return "高危";
		}
	}

	public static String getHTTPCategory(String level) {
		int l = 0;
		try {
			l = Integer.parseInt(level);
		}catch(Exception e) {
			return "异常";
		}
		if(l>=100 && l<=199) {
			return "消息";
		}else if(l>=200 && l<=299) {
			return "成功";
		}else if(l>=300 && l<=399) {
			return "重定向";
		}else if(l>=400 && l<=499) {
			return "客户端错误";
		}else if(l>=500 && l<=599) {
			return "服务器错误";
		}else {
			return "无法识别";
		}
	}
}
