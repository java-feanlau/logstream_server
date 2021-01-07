package com.boyitech.logstream.core.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.codehaus.plexus.util.DirectoryScanner;
import org.joda.time.DateTime;

public class FakeData {

	public static void main(String args[]) {
//		String wildcardPath = "D:\\work\\log_sample\\apache\\access_log";
//		final String path = wildcardPath;
//		List<String> l = new ArrayList<String>();
//		if (path == null || path.isEmpty()) {
//		} else {
//			if (!wildcardPath.contains("\\*") && !wildcardPath.contains("\\?")
//					&& !Files.isDirectory(Paths.get(wildcardPath))) { // 如果不包含�?�配符，则直接返回路�?
//				l.add(wildcardPath);
//			} else {
//				String[] strs = path.split("/|\\\\");
//				boolean f = true;
//				StringJoiner base = new StringJoiner(File.separator);
//				StringJoiner wildcard = new StringJoiner(File.separator);
//				for (String s : strs) {
//					if (f && !(s.contains("*") || s.contains("?"))) {
//						base.add(s);
//					} else {
//						f = false;
//						wildcard.add(s);
//					}
//				}
//				DirectoryScanner scanner = new DirectoryScanner();
//				scanner.setIncludes(new String[] { wildcard.toString() });
//				scanner.setBasedir(base.toString());
//				scanner.setCaseSensitive(false);
//				scanner.scan();
//				String[] files = scanner.getIncludedFiles();
//				for (String p : files) {
//					l.add(Paths.get(base.toString(), p).toAbsolutePath().toString());
//				}
//			}
//		}
//		for (String p : l) {
//			try {
//				File originFile = new File(p);
//				File newFile = new File(Paths.get(originFile.getParent(), originFile.getName()+"_fake").toString());
//				InputStreamReader read = new InputStreamReader(new FileInputStream(originFile), "utf8");
//				BufferedReader bufferedReader = new BufferedReader(read);
//				String lineTxt = null;
//
////				while ((lineTxt = bufferedReader.readLine()) != null) {
////					System.out.println(lineTxt);
////				}
//
//				bufferedReader.close();
//				read.close();
//			}catch(Exception e) {
//				e.printStackTrace();
//			}
//		}

		boolean s = Boolean.parseBoolean("sb");
		System.out.println(s);

	}

	public static String replaceTime(String originStr, DateTime newTime) {
		String newString = originStr;

		return newString;
	}

}
