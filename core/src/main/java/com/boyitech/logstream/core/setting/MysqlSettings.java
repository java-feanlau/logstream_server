package com.boyitech.logstream.core.setting;

/**
 * @author Eric
 * @Title: MysqlSettings
        * @date 2019/3/8 14:02
        * @Description: TODO
        */
public class MysqlSettings {
    public static final Setting<String> JDBC_DRIVERCLASSNAME = Setting.stringSetting("jdbc.driverClassName", "com.mysql.jdbc.Driver");
    public static final Setting<String> JDBC_HOST = Setting.stringSetting("jdbc.host","172.17.30.10");
    public static final Setting<String> JDBC_PORT = Setting.stringSetting("jdbc.port", "3306");
    public static final Setting<String> JDBC_USERNAME = Setting.stringSetting("jdbc.username", "root");
    public static final Setting<String> JDBC_PASSWORD = Setting.stringSetting("jdbc.password", "Boyipass..");
    public static final Setting<String> JDBC_DATABASE = Setting.stringSetting("jdbc.database", "yslm_logstream");
//    public static final Setting<String> JDBC_INITIALPOOLSIZE = Setting.stringSetting("jdbc.pool.initiaSize", "1");
//    public static final Setting<String> JDBC_MAXPOOLSIZE = Setting.stringSetting("jdbc.pool.maxSize", "5");
//    public static final Setting<String> JDBC_MAXIDLETIME = Setting.stringSetting("jdbc.pool.MaxIdeleteTime", "300");

}
