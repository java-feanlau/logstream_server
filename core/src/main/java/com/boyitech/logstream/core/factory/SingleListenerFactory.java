package com.boyitech.logstream.core.factory;

import com.boyitech.logstream.core.listener.GatewayListener;

/**
 * @author Eric
 * @Title: ListenerFactory
 * @date 2019/8/1 11:28
 * @Description:
 */
public class SingleListenerFactory {


    public static class GatewayListenerHolder{
        public static GatewayListener listener = new GatewayListener();
    }

    public static GatewayListener getListenerInstance(){
        return GatewayListenerHolder.listener;
    }

}
