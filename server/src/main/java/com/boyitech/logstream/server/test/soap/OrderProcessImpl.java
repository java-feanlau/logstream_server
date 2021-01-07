package com.boyitech.logstream.server.test.soap;

/**
 * @author Eric
 * @Title: OrderProcessImpl
 * @date 2019/7/15 13:42
 * @Description: TODO
 */
public class OrderProcessImpl implements  OrderProcess {
    @Override
    public String processOrder(Order order) {
        System.out.println("123");
        return null;
    }
}
