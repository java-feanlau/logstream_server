package com.boyitech.logstream.core.util;

import org.apache.commons.lang.StringUtils;

/**
 * @author juzheng
 * @Title: MonthHelper
 * @date 2019/7/22 1:32 PM
 * @Description:
 */
public class MonthHelper {
    public static final String replaceChineseMonth(String message){
        if(message.contains("一月")){
            message=message.replace("一月","Jan");
        }
        if(message.contains("二月")){
            message=message.replace("二月","Feb");
        }
        if(message.contains("三月")){
            message=message.replace("三月","Mar");
        }
        if(message.contains("四月")){
            message=message.replace("四月","Apr");
        }
        if(message.contains("五月")){
            message=message.replace("五月","May");
        }
        if(message.contains("六月")){
            message=message.replace("六月","Jun");
        }
        if(message.contains("七月")){
            message=message.replace("七月","Jul");
        }
        if(message.contains("八月")){
            message=message.replace("八月","Aug");
        }
        if(message.contains("九月")){
            message=message.replace("九月","Sep");
        }
        if(message.contains("十月")){
            message=message.replace("十月","Oct");
        }
        if(message.contains("十一月")){
            message=message.replace("十一月","Nov");
        }
        if(message.contains("十二月")){
            message=message.replace("十二月","Dec");
        }

        return  message;
    }
}
