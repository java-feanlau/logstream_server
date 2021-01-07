package com.boyitech.logstream.core.util;

import java.nio.file.Paths;

public class FilePathHelper {

	public static String ROOTPATH = System.getenv("APP_HOME")==null? Paths.get(System.getProperty("user.dir")).toString() : System.getenv("APP_HOME");

}
