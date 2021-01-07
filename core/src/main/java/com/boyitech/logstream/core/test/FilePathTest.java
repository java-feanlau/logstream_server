package com.boyitech.logstream.core.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.StringJoiner;

import org.apache.commons.io.FilenameUtils;
import org.codehaus.plexus.util.DirectoryScanner;

public class FilePathTest {

	public static void main(String args[]) {
		String fullPath = "D:\\test\\**/*.txt";
		String[] strs = fullPath.split("/|\\\\");
		boolean f = true;
		StringJoiner base = new StringJoiner(File.separator);
		StringJoiner wildcard = new StringJoiner(File.separator);
		for(String s : strs) {
			if(f && !(s.contains("*")||s.contains("?"))) {
				base.add(s);
			}else {
				f = false;
				wildcard.add(s);
			}
		}
		DirectoryScanner scanner = new DirectoryScanner();
		scanner.setIncludes(new String[]{wildcard.toString()});
		scanner.setBasedir(base.toString());
		scanner.setCaseSensitive(false);
		scanner.scan();
		String[] files = scanner.getIncludedFiles();
		for(String p : files) {
			Path path = Paths.get(base.toString(), p);
			BasicFileAttributes attr;
			try {
				attr = Files.readAttributes(path, BasicFileAttributes.class);
				Object fileKey = attr.fileKey();
			    String s = fileKey.toString();
			    String inode = s.substring(s.indexOf("ino=") + 4, s.indexOf(")"));
			    System.out.println(inode + " : " + path.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

}
