package com.boyitech.logstream.core.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ApacheIndexerTest {
	public static void main(String args[]) {

		String timestamp ="12/Mar/2019:11:07:26 +0800";
		if (timestamp != null && timestamp.trim().length() != 0) {
			Date time = new Date();
			//2018-09-09 19:15:14
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z", Locale.US);
			try {
				time = sdf.parse(timestamp);
			} catch (ParseException e) {

			}
			SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
			timestamp = sdf2.format(time);
			System.out.println(timestamp);
		}
	}
}
