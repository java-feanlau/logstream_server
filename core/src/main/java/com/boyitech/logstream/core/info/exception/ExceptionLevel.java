package com.boyitech.logstream.core.info.exception;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.spi.StandardLevel;

/**
 * @author Eric
 * @Title: ExceptionLevel
 * @date 2019/7/1 11:37
 * @Description: TODO
 */
public final class ExceptionLevel {
    public static final String MAJOR; //一般
    public static final String CRITICAL; //严重
    public static final String BLOCKER; //奔溃


    static {
        MAJOR = "major";
        CRITICAL = "critical";
        BLOCKER = "blocker";
    }
}
