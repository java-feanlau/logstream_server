package com.boyitech.logstream.server;

import com.boyitech.logstream.server.manager.ServerManager;

/**
 * @author Eric
 * @Title: baseTest
 * @date 2019/4/9 17:05
 * @Description: TODO
 */
public class BaseTest {

    protected ServerManager instance;

    public BaseTest(){
        // 加载配置
//        Settings.load();
        instance = new ServerManager();
    }

}
