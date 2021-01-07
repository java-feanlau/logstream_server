package com.boyitech.logstream.server.test.soap;

import javax.jws.WebService;

@WebService
public interface OrderProcess {

    public String processOrder(Order order);
}