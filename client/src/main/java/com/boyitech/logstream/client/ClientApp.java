package com.boyitech.logstream.client;

import com.boyitech.logstream.client.factory.ManagerFactory;
import com.boyitech.logstream.client.rest.heartbeat.HeartBeatCreator;
import com.boyitech.logstream.core.setting.ClientSettings;
import com.boyitech.logstream.core.setting.Settings;
import com.boyitech.logstream.core.util.FilePathHelper;
import com.boyitech.logstream.core.util.ShutdownHandler;
import com.boyitech.logstream.core.util.os.OSinfo;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import sun.misc.Signal;

import javax.net.ssl.SSLException;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Paths;

public class ClientApp {

	static final Logger LOGGER = LogManager.getLogger("main");
	static Options options;
	static final String CLIENTNAME = "boyi-log_stream_client";

	public static void main(String[] args) throws InterruptedException {


		//ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);
		// 开发环境里设置了这个就不会提示找不到配置
		// 但是在linux中依然会提示没找到配置
		System.setProperty("log4j.configurationFile",
				Paths.get(FilePathHelper.ROOTPATH, "config", "log4j2.properties").toString());
		System.setProperty("es.set.netty.runtime.available.processors", "false");// 程序自身的netty和es5.0的netty配置会有冲突

		// 通过log4j的接口设置log4j.configurationFile的默认路径
		LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
		loggerContext.setConfigLocation(Paths.get(FilePathHelper.ROOTPATH, "config", "log4j2.properties").toUri());
		LOGGER.info("设置log4j的配置文件为" + Paths.get(FilePathHelper.ROOTPATH, "config", "log4j2.properties").toString());
		// 加载配置
		Settings.load();
		// 加载客户端配置(uuid和服务器ip地址)
		ClientSettings.load();
		// 解析输入的参数，目前还没啥用
//		options(args);
		// 初始化核心程序
		initCore();
		// 创建心跳处理组件
		initHeartBeat();
		// 注册系统中断
		Signal.handle(new Signal("INT"), new ShutdownHandler(ManagerFactory.getClientManager()));
		Signal.handle(new Signal("TERM"), new ShutdownHandler(ManagerFactory.getClientManager()));
	}

	public static void initCore() {
		ManagerFactory.getClientManager();
	}

	private static void options(String args[]) {
		options = new Options();
		options.addOption("h", false, "显示帮助信息");
		options.addOption("f", true, "文件");
		options.addOption("install", false, "注册系统服务并设置自动启动");
		options.addOption("run", false, "运行");

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			LOGGER.error("无法解析的命令行参数，系统退出", e);
			System.exit(0);
		}
		if (cmd.hasOption("h")) {
			System.err.println("帮助信息：然而并没有帮助信息");
			System.exit(0);
		}
		if (cmd.hasOption("install")) {
			install();
		}
		if (!cmd.hasOption("run")){
			System.exit(0);
		}else {
			return;
		}
	}
	public static void initHeartBeat() {
		try {
			// 创建发送心跳的对象并放入单独线程运行
			HeartBeatCreator creater = new HeartBeatCreator(ClientSettings.getServerAddr());
			new Thread(creater).start();
		} catch (SSLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}


	public static void install() {
		int exitCode = 1;
		if (OSinfo.getOSname().toString() == "Linux") {
			exitCode = installOnLinux();
		} else if (OSinfo.getOSname().toString() == "Windows") {
			exitCode = installOnWindows();
		}
		if (exitCode == 0) {
			System.out.println("安装成功");
		} else {
			System.out.println("安装失败");
		}
		System.exit(exitCode);
	}

	public static void uninstall() {
		int exitCode = 1;
		if (OSinfo.getOSname().toString() == "Linux") {
			exitCode = uninstallOnLinux();
		} else if (OSinfo.getOSname().toString() == "Windows") {
			exitCode = uninstallOnWindows();
		}
		if (exitCode == 0) {
			System.out.println("卸载成功");
		} else {
			System.out.println("卸载失败");
		}
		System.exit(exitCode);
	}

	public static int installOnWindows() {
		int exitCode = 1;
		Runtime run = Runtime.getRuntime();
		String app = OSinfo.isX86()? "prunsrv32.exe" : "prunsrv64.exe";
		try {
			StringBuffer buff = new StringBuffer();
			buff.append(app);
			buff.append(" //IS//" + CLIENTNAME);
			buff.append(" --Description=日志数据采集服务程序");
			buff.append(" --Install=\"" + Paths.get(FilePathHelper.ROOTPATH, "bin", app).toString() + "\"");
			buff.append(" --Startup=auto");
//			buff.append(" --Jvm=\"C:\\Program Files\\Java\\jdk1.8.0_131\\jre\\bin\\server\\jvm.dll\"");
			buff.append(" --Jvm=auto");
			buff.append(" --LogPath\"=" + Paths.get(FilePathHelper.ROOTPATH, "logs").toString() + "\"");
			buff.append(" --Classpath\"=" + Paths.get(FilePathHelper.ROOTPATH, "lib").toString() + "\\*" + "\"");
			buff.append(" --StartMode=jvm --StartMethod=start --StartClass=com.boyitech.logstream.client.LogStreamClientApp ++StartParams='-run'"
					+ " --StopMode=jvm --StopMethod=stop --StopClass=com.boyitech.logstream.client.LogStreamClientApp");
			buff.append(" --StdOutput=auto");
			buff.append(" --StdError=auto");
			buff.append(" ++Environment='APP_HOME=" + FilePathHelper.ROOTPATH + "'");
			LOGGER.debug("执行系统命令： " + buff.toString());
			Process p = run.exec(buff.toString());// 启动另一个进程来执行命令
			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
			String lineStr;
			while ((lineStr = inBr.readLine()) != null)
				// 获得命令执行后在控制台的输出信息
				System.out.println(lineStr);// 打印输出信息
			// 检查命令是否执行失败。
			p.waitFor();
			exitCode = p.exitValue();
			inBr.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return exitCode;
	}

	public static int installOnLinux() {
		int exitCode = 1;
		File file = new File(Paths.get("/", "etc", "systemd", "system", CLIENTNAME + ".service").toString());
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				System.err.println("创建服务文件失败");
				LOGGER.error("创建服务文件失败", e);
				return 1;
			}
		}
		try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file.getPath(), false))) {
			bufferedWriter.write("[Unit]\n");
			bufferedWriter.write("Description="+ CLIENTNAME +"\n");
			bufferedWriter.write("After=network.target\n\n");

			bufferedWriter.write("[Service]\n");
			bufferedWriter.write("Type=simple\n");
			bufferedWriter.write("User=root\n");
			bufferedWriter.write("Group=root\n");
			bufferedWriter.write("EnvironmentFile=-/etc/default/"+ CLIENTNAME+"\n");
			bufferedWriter
					.write("ExecStart=" + Paths.get(FilePathHelper.ROOTPATH, "bin", "log_stream_client") + " -run\n"); // 启动程序的可执行文件
			bufferedWriter.write("Restart=no\n\n");

			bufferedWriter.write("[Install]\n");
			bufferedWriter.write("WantedBy=multi-user.target\n");
		} catch (IOException e) {
			System.out.println("注册服务失败");
			LOGGER.error("注册服务失败", e);
			return 1;
		}
		file.setExecutable(true);
		Runtime run = Runtime.getRuntime();
		try {
			Process p = run.exec("systemctl enable "+ CLIENTNAME +".service");
			p.waitFor();
			exitCode = p.exitValue();
		} catch (IOException | InterruptedException e) {
			System.out.println("设置自启动失败");
			LOGGER.error("设置自启动失败", e);
		}
		return exitCode;
	}

	public static int uninstallOnLinux() {
		return 0;
	}

	public static int uninstallOnWindows() {
		return 0;
	}

	public static void start(String args[]) throws InterruptedException {
		ClientApp.main(args);
	}

	public static void stop(String args[]) {
		Signal.raise(new Signal("INT"));
	}

}
