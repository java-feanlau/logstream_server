package com.boyitech.logstream.server;

import com.boyitech.logstream.core.cache.BaseCache;
import com.boyitech.logstream.core.factory.CacheFactory;
import com.boyitech.logstream.core.setting.Settings;
import com.boyitech.logstream.core.setting.SystemSettings;
import com.boyitech.logstream.core.util.FilePathHelper;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.ShutdownHandler;
import com.boyitech.logstream.core.util.os.OSinfo;
import com.boyitech.logstream.core.worker.indexer.BaseIndexerConfig;
import com.boyitech.logstream.core.worker.porter.redis.RedisPorter;
import com.boyitech.logstream.core.worker.porter.redis.RedisPorterConfig;
import com.boyitech.logstream.core.worker.shipper.event_log.EventLogShipper;
import com.boyitech.logstream.core.worker.shipper.event_log.EventLogShipperConfig;
import com.boyitech.logstream.server.factory.SingleManagerFactory;
import com.boyitech.logstream.server.manager.ServerManagerApi;
import com.boyitech.logstream.server.manager.ServerManager;
import com.boyitech.logstream.server.restful.RestfulServer;
import com.boyitech.logstream.core.setting.RestfulServerSetting;
import com.boyitech.logstream.worker.indexer.MicrosoftWindowsAllV1Indexer;
import org.apache.commons.cli.*;
import org.apache.cxf.configuration.jsse.TLSServerParameters;
import org.apache.cxf.configuration.security.ClientAuthentication;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.service.invoker.BeanInvoker;
import org.apache.cxf.transport.http_jetty.JettyHTTPServerEngineFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import sun.misc.Signal;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("restriction")
public class ServerApp {

    static final Logger LOGGER = LogManager.getLogger("main");
    static Options options;
    static final String SERVICENAME = "boyi-log_stream_server";

    public static void main(String[] args) throws InterruptedException {
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
        // 解析输入的参数，目前还没啥用
       // options(args);
        // 初始化核心程序
        initCore();
//        initCoreTest();   //如果测试的话，需要将后续代码全部注释掉
        // 初始化Soap api
        initSoap();
//        // 初始化Restful API
        initRestfulServer();

        // 注册系统中断
        Signal.handle(new Signal("INT"), new ShutdownHandler(SingleManagerFactory.getServerManager()));
        Signal.handle(new Signal("TERM"), new ShutdownHandler(SingleManagerFactory.getServerManager()));


    }

    public static void initCore() throws InterruptedException {

        ServerManager serverManager = SingleManagerFactory.getServerManager();
    }

    public static void initCoreTest() throws InterruptedException {
        Map map = loadFileTest(null);

        BaseCache cache1 = CacheFactory.createCache();
        BaseCache cache2 = CacheFactory.createCache();
        Object host = map.get("host").toString();
        Object port = map.get("port").toString();
        Object passwd = map.get("passwd").toString();
        Object keys = map.get("keys").toString();
        Object DBindex = map.get("DBindex").toString();
        Object eventTypes = map.get("eventTypes");


        Map shipperMap = new HashMap<>();
        shipperMap.put("moduleType", "event_log");
        shipperMap.put("eventTypes", eventTypes);
//        shipperMap.put("index","microsoft_windows_all_v1-hhhh");
        EventLogShipper shipper = new EventLogShipper(new EventLogShipperConfig(shipperMap));

//        Map indexerMap = new HashMap<>();
//        indexerMap.put("logType","microsoft_windows_all_v1");
//        MicrosoftWindowsAllV1Indexer indexer = new MicrosoftWindowsAllV1Indexer(new BaseIndexerConfig(indexerMap));

        Map porterMap = new HashMap<>();
        porterMap.put("host", host);
        porterMap.put("port", port);
        porterMap.put("passwd", passwd);
        porterMap.put("keys", keys);
        porterMap.put("DBindex", DBindex);
        porterMap.put("moduleType", "redis");
        RedisPorter porter = new RedisPorter(new RedisPorterConfig(porterMap));

        shipper.setLv1Cache(cache1);
//        indexer.setLv1Cache(cache1);
//        indexer.setLv2Cache(cache2);
        porter.setLv2Cache(cache1);

        porter.doStart();
//        indexer.doStart();
        shipper.doStart();

    }

    public static void initSoap() {
        if (SystemSettings.isSoapEnable()) {
            try {
                String host = SystemSettings.SOAPHOST.getValue();
                int port = SystemSettings.SOAPPORT.getValue();
                String address = "https://" + host + ":" + port + "/logProcessor";
//                String address = "http://" + host + ":" + port + "/logProcessor";
                JaxWsServerFactoryBean sf = new JaxWsServerFactoryBean();
                sf.setServiceClass(ServerManagerApi.class);
                sf.setAddress(address);

                ServerManager implementor = SingleManagerFactory.getServerManager();
                sf.getServiceFactory().setInvoker(new BeanInvoker(implementor));

                sf = configureSSLOnTheServer(sf, port);
                Server server = sf.create();
                String endpoint = server.getEndpoint().getEndpointInfo().getAddress();
                LOGGER.info("Server started at " + endpoint);
            } catch (Exception e) {
                LOGGER.error("Soap初始化失败", e);
            }
        }
    }

    private static void initRestfulServer() {
        // 创建客户端管理进程并启动
        try {
            RestfulServer server = new RestfulServer(RestfulServerSetting.HOST.getValue(), RestfulServerSetting.PORT.getValue(), true);
        } catch (Exception e) {
            LOGGER.error("创建网络服务接口失败", e);
        }
    }

    private static JaxWsServerFactoryBean configureSSLOnTheServer(JaxWsServerFactoryBean sf, int port) {
        try {
            TLSServerParameters tlsParams = new TLSServerParameters();
            ClientAuthentication clientAuthentication = new ClientAuthentication();
            //开启客户端认证
            clientAuthentication.setRequired(true);
            tlsParams.setClientAuthentication(clientAuthentication);
            KeyStore keyStore = KeyStore.getInstance("JKS");
            String password = "logprocessor";
            File truststore = new File(Paths.get(FilePathHelper.ROOTPATH, "ssl", "privatestore.jks").toString());
            keyStore.load(new FileInputStream(truststore), password.toCharArray());
            KeyManagerFactory keyFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyFactory.init(keyStore, password.toCharArray());
            KeyManager[] km = keyFactory.getKeyManagers();
            tlsParams.setKeyManagers(km);

            truststore = new File(Paths.get(FilePathHelper.ROOTPATH, "ssl", "privatestore.jks").toString());
            keyStore.load(new FileInputStream(truststore), password.toCharArray());
            TrustManagerFactory trustFactory = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustFactory.init(keyStore);
            TrustManager[] tm = trustFactory.getTrustManagers();
            tlsParams.setTrustManagers(tm);

            JettyHTTPServerEngineFactory factory = new JettyHTTPServerEngineFactory();
            factory.setTLSServerParametersForPort(port, tlsParams);
        } catch (KeyStoreException kse) {
            LOGGER.error("Security configuration failed with the following: " + kse.getCause());
        } catch (NoSuchAlgorithmException nsa) {
            LOGGER.error("Security configuration failed with the following: " + nsa.getCause());
        } catch (FileNotFoundException fnfe) {
            LOGGER.error("Security configuration failed with the following: " + fnfe.getCause());
        } catch (UnrecoverableKeyException uke) {
            LOGGER.error("Security configuration failed with the following: " + uke.getCause());
        } catch (CertificateException ce) {
            LOGGER.error("Security configuration failed with the following: " + ce.getCause());
        } catch (GeneralSecurityException gse) {
            LOGGER.error("Security configuration failed with the following: " + gse.getCause());
        } catch (IOException ioe) {
            LOGGER.error("Security configuration failed with the following: " + ioe.getCause());
        }
        return sf;
    }

    private static void options(String args[]) {
        if(args.length == 0){
            return;
        }
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
        if (!cmd.hasOption("run")) {
            System.exit(0);
        } else {
            return;
        }
    }

    public static Map loadFileTest(String path) {
        // 加载配置文件
        File file = null;
        try {
            if (path != null) {
                file = new File(path);
            } else {
                file = new File(Paths.get(FilePathHelper.ROOTPATH, "config", "logprocessor.conf").toString());
            }
        } catch (Exception e) {
            LOGGER.error("没有提供日志流配置文件，系统退出");
            System.exit(0);
        }
        Reader reader = null;
        StringBuffer sb = new StringBuffer();
        try {
            reader = new InputStreamReader(new FileInputStream(file));
            // 加载jar内部文件需要使用getResourceAsStream()方法
            // reader = new
            // InputStreamReader(App.class.getResourceAsStream("/logStreamRedis.conf"));
            int tempchar;
            while ((tempchar = reader.read()) != -1) {
                if (((char) tempchar) != '\r') {
                    sb.append((char) tempchar);
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("找不到日志流配置文件，系统退出");
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("日志流配置文件读取发生异常，系统退出");
            System.exit(0);
        }
        String config = sb.toString();
        Map stringObjectMap = GsonHelper.fromJson(config);
        return stringObjectMap;
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
            System.err.println("安装失败");
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
            System.err.println("卸载失败");
        }
        System.exit(exitCode);
    }

    public static int installOnWindows() {
        int exitCode = 1;
        Runtime run = Runtime.getRuntime();
        String app = OSinfo.isX86() ? "prunsrv32.exe" : "prunsrv64.exe";
        try {
            StringBuffer buff = new StringBuffer();
            buff.append(app);
            buff.append(" //IS//" + SERVICENAME);
            buff.append(" --Description=日志数据采集服务程序");
            buff.append(" --Install=\"" + Paths.get(FilePathHelper.ROOTPATH, "bin", app).toString() + "\"");
            buff.append(" --Startup=auto");
//			buff.append(" --Jvm=\"C:\\Program Files\\Java\\jdk1.8.0_131\\jre\\bin\\server\\jvm.dll\"");
            buff.append(" --Jvm=auto");
            buff.append(" --LogPath\"=" + Paths.get(FilePathHelper.ROOTPATH, "logs").toString() + "\"");
            buff.append(" --Classpath\"=" + Paths.get(FilePathHelper.ROOTPATH, "lib").toString() + "\\*" + "\"");
            buff.append(" --StartMode=jvm --StartMethod=start --StartClass=com.boyitech.logstream.LogStreamServerApp ++StartParams='-run'"
                    + " --StopMode=jvm --StopMethod=stop --StopClass=com.boyitech.logstream.LogStreamServerApp");
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
        File file = new File(Paths.get("/", "etc", "systemd", "system", SERVICENAME + ".service").toString());
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
            bufferedWriter.write("Description=" + SERVICENAME + "\n");
            bufferedWriter.write("After=network.target\n\n");

            bufferedWriter.write("[Service]\n");
            bufferedWriter.write("Type=simple\n");
            bufferedWriter.write("User=root\n");
            bufferedWriter.write("Group=root\n");
            bufferedWriter.write("EnvironmentFile=-/etc/default/" + SERVICENAME + "\n");
            bufferedWriter
                    .write("ExecStart=" + Paths.get(FilePathHelper.ROOTPATH, "bin", "server") + " -run\n"); // 启动程序的可执行文件
            bufferedWriter.write("WorkingDirectory=/YSApp/log_stream/\n\n");
            bufferedWriter.write("LimitMEMLOCK=infinity\n\n");
            bufferedWriter.write("LimitNOFILE=65536\n\n");
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
            Process p = run.exec("systemctl enable " + SERVICENAME + ".service");
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
        ServerApp.main(args);
    }

    public static void stop(String args[]) {
        Signal.raise(new Signal("INT"));
    }

}
