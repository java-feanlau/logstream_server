package com.boyitech.logstream.server.worker.porter;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;



/**
 * @author juzheng
 * @Title: ESPortTest
 * @date 2019/12/2 5:22 PM
 * @Description:
 */
public class ESPortTest {
    @Test
    public void test(){
       StringBuffer sb = new StringBuffer();
        DateTimeFormatter fomatter1 = DateTimeFormat.forPattern("YYYY-MM");
        DateTime dt=new DateTime("2019-12-02T02:20:01.217573600Z");
        sb.append(dt.toString(fomatter1));
        System.out.println(sb);
    }
}
