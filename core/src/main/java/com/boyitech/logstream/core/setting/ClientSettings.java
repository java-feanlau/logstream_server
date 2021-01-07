package com.boyitech.logstream.core.setting;

import com.boyitech.logstream.core.util.FilePathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class ClientSettings extends BaseSettings{
	private static final Path GLOBALSETTINGPATH = Paths.get(FilePathHelper.ROOTPATH, "config", "client.conf");
	private static final Path GLOBALSETTINGPATHTMP = Paths.get(FilePathHelper.ROOTPATH, "config", "client.conf.tmp");
	public static final Setting<Integer> NOCONFLICT = Setting.integerSetting("client.noconflict", 0); //0代表注册客户端
	public static final Setting<Integer> CONFLICT = Setting.integerSetting("client.noconflict", 1); //0代表冲突客户端
	public static final Setting<String> FILEOFFSETPATH = Setting.stringSetting("client.fileshipper.offset.path", Paths.get(System.getProperty("user.dir")).toString());
	private static final Logger LOGGER = LogManager.getLogger("main");

	private static String SERVERADDR;
	private static String CLIENTID;

	public static void load(){
		//读取配置文件设置各全�?变量
		File file = new File(GLOBALSETTINGPATH.toString());
		Reader reader = null;
		StringBuffer sb = new StringBuffer();
		try {
			reader = new InputStreamReader(new FileInputStream(file));
			int tempchar;
			while ((tempchar = reader.read()) != -1) {
				if (((char) tempchar) != '\r') {
					sb.append((char) tempchar);
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			LOGGER.error(e);
			System.exit(0);
		} catch (IOException e) {
			LOGGER.error(e);
			System.exit(0);
		}
		try {
			String[] configs = sb.toString().split("\n");
			CLIENTID = configs[0];
			SERVERADDR = configs[1];
			LOGGER.debug("客户端ClientID:" + CLIENTID);
			LOGGER.debug("服务端地址:" + SERVERADDR);
		} catch (Exception e) {
			LOGGER.error(e);
		}
	}

	public static void save() {
		String data = CLIENTID + "\n" + SERVERADDR;
		boolean flag = false;
		// 创建备份文件并写入配�?
		try {
			Files.write(GLOBALSETTINGPATHTMP, data.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			flag = true;
		} catch (IOException e) {
			LOGGER.error("客户端配置保存失�?", e);
			flag = false;
		}
		if(flag) {
			// 删除原配置并将tmp文件改名
			try {
				Files.copy(GLOBALSETTINGPATHTMP, GLOBALSETTINGPATH, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				LOGGER.error("客户端配置替换失�?", e);
			}
		}
	}

	public static String getClientID() {
		return CLIENTID;
	}

	public static void setClientID(String ClientID) {
		CLIENTID = ClientID;
		save();
	}

	public static String getServerAddr() {
		return SERVERADDR;
	}
}
