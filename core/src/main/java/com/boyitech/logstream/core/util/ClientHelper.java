package com.boyitech.logstream.core.util;

import com.boyitech.logstream.core.util.os.OSinfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;
import java.util.UUID;

public class ClientHelper {

	static final Logger LOGGER = LogManager.getLogger("main");

	public static final String MACHINECODE;

	static {
		MACHINECODE = getMachineFingerPrint();
	}

	public static String getTestFingerPrint(){
		return UUID.randomUUID().toString();
	}

	public static String getMachineFingerPrint() {
		String fingerPrint = null;
//		String PROCESSOR_IDENTIFIER = System.getenv("PROCESSOR_IDENTIFIER");
//		String PROCESSOR_REVISION = System.getenv("PROCESSOR_REVISION");
//		String NUMBER_OF_PROCESSORS = System.getenv("NUMBER_OF_PROCESSORS");
		StringBuffer buffer = new StringBuffer();
//		buffer.append(PROCESSOR_IDENTIFIER);
//		buffer.append(PROCESSOR_REVISION);
//		buffer.append(NUMBER_OF_PROCESSORS);
		if (OSinfo.getOSname().toString() == "Linux") {
			buffer.append(getLinuxSerialNumber());
		} else {
			buffer.append(getWindowsSerialNumber());
		}
//		for (FileStore store : FileSystems.getDefault().getFileStores()) {
//			try {
//				buffer.append(store.getAttribute("volume:vsn"));
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
//			BASE64Encoder base64en = new BASE64Encoder();
//			System.out.println("fingerPrint : "+ buffer.toString());
//			fingerPrint = base64en.encode(md5.digest(buffer.toString().getBytes("utf-8")));

			Base64.Encoder encoder = Base64.getEncoder();
			fingerPrint = encoder.encodeToString(md5.digest(buffer.toString().getBytes("utf-8")));

		}catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			System.err.println("生成机器码失败，系统退出");
			System.exit(1);
		}
		LOGGER.info("生成指纹："+fingerPrint);
		return fingerPrint;
//		return "aaaa";
	}

	public static final String getWindowsSerialNumber() {
		String sn = null;

		OutputStream os = null;
		InputStream is = null;
		Runtime runtime = Runtime.getRuntime();
		Process process = null;
		try {
			process = runtime.exec(new String[] { "wmic", "bios", "get", "serialnumber" });
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		os = process.getOutputStream();
		is = process.getInputStream();
		try {
			os.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Scanner sc = new Scanner(is);
		try {
			while (sc.hasNext()) {
				String next = sc.next();
				if ("SerialNumber".equals(next)) {
					sn = sc.next().trim();
					break;
				}
			}
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		if (sn == null) {
			throw new RuntimeException("Cannot find computer SN");
		}
		return sn;
	}

	public static final String getLinuxSerialNumber() {
		String sn = null;
		if (sn == null) {
			sn = readDmidecode();
		}
		if (sn == null) {
			sn = readLshal();
		}
		if (sn == null) {
			throw new RuntimeException("Cannot find computer SN");
		}
		return sn;
	}

	private static BufferedReader read(String command) {
		OutputStream os = null;
		InputStream is = null;
		Runtime runtime = Runtime.getRuntime();
		Process process = null;
		try {
			process = runtime.exec(command.split(" "));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		os = process.getOutputStream();
		is = process.getInputStream();
		try {
			os.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return new BufferedReader(new InputStreamReader(is));
	}

	private static String readDmidecode() {
		String line = null;
		String marker = "Serial Number:";
		BufferedReader br = null;
		String sn = null;
		try {
			br = read("dmidecode -t system");
			while ((line = br.readLine()) != null) {
				if (line.indexOf(marker) != -1) {
					sn = line.split(marker)[1].trim();
					break;
				}
			}
			return sn;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	private static String readLshal() {
		String line = null;
		String marker = "system.hardware.serial =";
		BufferedReader br = null;
		String sn = null;
		try {
			br = read("lshal");
			while ((line = br.readLine()) != null) {
				if (line.indexOf(marker) != -1) {
					sn = line.split(marker)[1].replaceAll("\\(string\\)|(\\')", "").trim();
					break;
				}
			}
			return sn;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
}
