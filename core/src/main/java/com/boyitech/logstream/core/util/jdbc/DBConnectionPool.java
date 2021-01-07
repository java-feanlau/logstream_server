package com.boyitech.logstream.core.util.jdbc;

import com.boyitech.logstream.core.setting.BaseSettings;
import com.boyitech.logstream.core.setting.MysqlSettings;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据库连接对象
 * Created by yuandl on 2016-12-16.
 */
public class DBConnectionPool extends BaseSettings {
    private static volatile DBConnectionPool dbConnection;
    private ComboPooledDataSource cpds;

    /**
     * 在构造函数初始化的时候获取数据库连接
     */
    private DBConnectionPool() {
        try {

            /**数据库连接池对象**/
            cpds = new ComboPooledDataSource();

            /**设置数据库连接驱动**/
            cpds.setDriverClass(MysqlSettings.JDBC_DRIVERCLASSNAME.getValue());
            /**设置数据库连接地址jdbc:mysql://172.17.250.213:3306/mydb?serverTimezone=UTC**/
            cpds.setJdbcUrl("jdbc:mysql://"+MysqlSettings.JDBC_HOST.getValue()
                    +":"+MysqlSettings.JDBC_PORT.getValue()+"/"+MysqlSettings.JDBC_DATABASE.getValue());
            /**设置数据库连接用户名**/
            cpds.setUser(MysqlSettings.JDBC_USERNAME.getValue());
            /**设置数据库连接密码**/
            cpds.setPassword(MysqlSettings.JDBC_PASSWORD.getValue());

            /**初始化时创建的连接数,应在minPoolSize与maxPoolSize之间取值.默认为3**/
//            cpds.setInitialPoolSize(3);
            cpds.setInitialPoolSize(1);
            /**连接池中保留的最大连接数据.默认为10**/
            cpds.setMaxPoolSize(5);
            /**当连接池中的连接用完时，C3PO一次性创建新的连接数目;**/
            cpds.setAcquireIncrement(1);
            /**隔多少秒检查所有连接池中的空闲连接,默认为0表示不检查;**/
            cpds.setIdleConnectionTestPeriod(60);
            /**最大空闲时间,超过空闲时间的连接将被丢弃.为0或负数据则永不丢弃.默认为0;**/
            cpds.setMaxIdleTime(300);

            /**因性能消耗大请只在需要的时候使用它。如果设为true那么在每个connection提交的
             时候都将校验其有效性。建议使用idleConnectionTestPeriod或automaticTestTable
             等方法来提升连接测试的性能。Default: false**/
            cpds.setTestConnectionOnCheckout(false);

            /**如果设为true那么在取得连接的同时将校验连接的有效性。Default: false **/
            cpds.setTestConnectionOnCheckin(false);
            /**定义在从数据库获取新的连接失败后重复尝试获取的次数，默认为30;**/
            cpds.setAcquireRetryAttempts(5);
            /**两次连接中间隔时间默认为1000毫秒**/
            cpds.setAcquireRetryDelay(1000);
            /** 获取连接失败将会引起所有等待获取连接的线程异常,
             但是数据源仍有效的保留,并在下次调用getConnection()的时候继续尝试获取连接.如果设为true,
             那么尝试获取连接失败后该数据源将申明已经断开并永久关闭.默认为false**/
            cpds.setBreakAfterAcquireFailure(false);

        } catch (PropertyVetoException e) {
            LOGGER.error(e);
        }finally {
//            try {
//                System.out.println("aaa");
//                Connection connection = cpds.getConnection();
//                Statement statement = connection.createStatement();
//                statement.executeUpdate("Create Database If Not Exists yslm_logstream11 Character Set UTF8");
//                LOGGER.error("数据库"+MysqlSettings.JDBC_DATABASE.getValue()+"创建成功或已存在");
//            } catch (SQLException e) {
//                LOGGER.error("创建数据库"+MysqlSettings.JDBC_DATABASE.getValue()+"失败,"+e);
//            }

        }
    }

    /**
     * 获取数据库连接对象，单例
     *
     * @return
     */
    public static DBConnectionPool getInstance() {
        if (dbConnection == null) {
            synchronized (DBConnectionPool.class) {
                if (dbConnection == null) {
                    dbConnection = new DBConnectionPool();
                }
            }
        }
        return dbConnection;
    }

    /**
     * 获取数据库连接
     *
     * @return 数据库连接
     */
    public final synchronized Connection getConnection() throws SQLException {
        return cpds.getConnection();
    }

    /**
     * finalize()方法是在垃圾收集器删除对象之前对这个对象调用的。
     *
     * @throws Throwable
     */
    protected void finalize() throws Throwable {
        DataSources.destroy(cpds);
        super.finalize();
    }

    public static void main(String args[]){
        System.out.println(MysqlSettings.JDBC_DRIVERCLASSNAME.getValue());
        DBConnectionPool dbConnectionPool = new DBConnectionPool();
    }
}