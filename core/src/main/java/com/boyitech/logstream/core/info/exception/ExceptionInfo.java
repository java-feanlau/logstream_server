package com.boyitech.logstream.core.info.exception;

import java.util.Date;

/**
 * @author Eric
 * @Title: ExceptionInfo
 * @date 2019/7/1 10:59
 * @Description: TODO
 */
public class ExceptionInfo {
    //高：最后都是String
    private String errorDate;
    private String errorLevel;
    private String errorMessage;


    public ExceptionInfo(String errorLevel, String errorMessage) {
        errorDate = String.valueOf(new Date().getTime());
        this.errorLevel = errorLevel;
        this.errorMessage = errorMessage;
    }

    public String getErrorDate() {
        return errorDate;
    }

    public void setErrorDate(String errorDate) {
        this.errorDate = errorDate;
    }

    public String getErrorLevel() {
        return errorLevel;
    }

    public void setErrorLevel(String errorLevel) {
        this.errorLevel = errorLevel;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "\"errorDate\":\""+errorDate+"\",\n" +
                "\"errorLevel\":\""+errorLevel+"\",\n" +
                "\"errorMessage\":\""+errorMessage+"\"";
    }
}
