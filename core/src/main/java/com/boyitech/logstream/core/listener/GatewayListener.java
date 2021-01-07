package com.boyitech.logstream.core.listener;


import com.boyitech.logstream.core.listener.tcp.TcpHandler;
import com.boyitech.logstream.core.listener.udp.UdpHandler;

import com.boyitech.logstream.core.util.filter_rule.FilterRule;
import com.boyitech.logstream.core.worker.shipper.BaseListenerShipper;
import io.netty.bootstrap.AbstractBootstrap;
import io.netty.channel.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Eric
 * @Title: Listener
 * @date 2019/7/25 15:15
 * @Description: TODO
 */
public class GatewayListener extends BaseListener {
    private ConcurrentHashMap<FilterRule, ArrayList<BaseListenerShipper>> register = new ConcurrentHashMap<>();
    private static Map<String, AbstractBootstrap> moudle2ListenerType = new HashMap<>();
    private Map<String, Channel> hostPortProtocol2Channel = new ConcurrentHashMap<>();
    private Map<Channel, Integer> useChannelNumber = new ConcurrentHashMap<>();


    {
        moudle2ListenerType.put("udp", SingleNettyFactory.createUDPServerBootStrap(new UdpHandler(register)));
        moudle2ListenerType.put("tcp", SingleNettyFactory.createTCPServerBootStrap(new TcpHandler(register)));
    }

    /*
     * @Author Eric Zheng
     * @Description 注册基于udp，tcp的监听shipper
     * @Date 17:01 2019/8/1
     **/
    @Override
    public synchronized boolean registerShipper(BaseListenerShipper shipper) {

        String host = shipper.getHost();
        int port = shipper.getPort();
        List<String> protocols = shipper.getProtocol();
        List<FilterRule> filterRuleList = shipper.getFilterRuleList();
        filterRuleList.forEach(filterRule -> {
            ArrayList<BaseListenerShipper> listenerShippers = register.get(filterRule);
            if (listenerShippers == null) {
                ArrayList<BaseListenerShipper> newListenerShippers = new ArrayList<>();
                newListenerShippers.add(shipper);
                register.put(filterRule, newListenerShippers);
            } else {
                if (!listenerShippers.contains(shipper)) {
                    listenerShippers.add(shipper);
                }
                register.put(filterRule, listenerShippers);
            }
        });

        for (String protocol : protocols) {
            try {
                AbstractBootstrap abstractBootstrap = moudle2ListenerType.get(protocol);
                String host_port_protocol = host + "_" + port + "_" + protocol;
                Channel channel = hostPortProtocol2Channel.get(host_port_protocol);
                if (channel == null) {
                    channel = abstractBootstrap.bind(port).sync().channel();
                    hostPortProtocol2Channel.put(host_port_protocol, channel);
                }
                int number = 0;
                if (useChannelNumber.containsKey(channel)) {
                    number = useChannelNumber.get(channel);
                }
                useChannelNumber.put(channel, ++number);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        LOGGER.info(shipper.getWorkerId() + "成功在listener的注册,并且注册了" + filterRuleList.size() + "条过滤规则");

        return true;
    }

    @Override
    public synchronized boolean unregisterShipper(BaseListenerShipper shipper) {
        String host = shipper.getHost();
        int port = shipper.getPort();
        List<FilterRule> filterRuleList = shipper.getFilterRuleList();

        for (FilterRule filterRule : filterRuleList) {
            ArrayList<BaseListenerShipper> baseListenerShippers = register.get(filterRule);
            if (baseListenerShippers == null) {
                continue;
            }
            baseListenerShippers.remove(shipper);
            if (baseListenerShippers.size() == 0) {
                register.remove(filterRule);
            }
        }

        List<String> protocols = shipper.getProtocol();
        for (String protocol : protocols) {
            String host_port_protocol = host + "_" + port + "_" + protocol;

            Channel removeChannel = hostPortProtocol2Channel.get(host_port_protocol);
            if (removeChannel == null) {
                LOGGER.info("关闭" + shipper.getWorkerId() + "在listener的注册");
                continue;
            }
            Integer integer = useChannelNumber.get(removeChannel);
            if (integer <= 1) {
                hostPortProtocol2Channel.remove(host_port_protocol);
                useChannelNumber.remove(removeChannel);
                removeChannel.close();
            } else {
                useChannelNumber.put(removeChannel, --integer);
            }

        }
        LOGGER.info("取消" + shipper.getWorkerId() + "在listener的注册");
        return true;
    }


}





