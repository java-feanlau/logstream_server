package com.boyitech.logstream.core.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author juzheng
 * @Title: IndexerTimeUtils
 * @date 2019/7/17 10:21 AM
 * @Description: 基于jdk8的java.time包的indexer时间格式化的工具类
 */
public class IndexerTimeUtils {
    protected static final Logger LOGGER = LogManager.getLogger("worker");

    /*
    * @Author juzheng
    * @Description  时间转换工具类
    * @Date 10:31 AM 2019/7/29
    * @Param [timestamp, formatter]
    * @return java.lang.String
    */
    public static String getISO8601Time(String timestamp,String formatter){
        //timestamp=Year.now()+" "+timestamp;
        if(timestamp!=null&&timestamp.trim().length()!=0&&formatter!=null&&formatter.trim().length()!=0) {
            try {
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(formatter,Locale.US);
                LocalDateTime dt = LocalDateTime.parse(timestamp, dateTimeFormatter);
                ZoneOffset offset = ZoneOffset.of("+08:00");
                OffsetDateTime date = OffsetDateTime.of(dt,offset);
                return String.valueOf(date);
            }
            catch (DateTimeParseException ex) {
                //ex.printStackTrace();
            }
        }
        return "";
    }

    /*
    * @Author juzheng
    * @Description   时间转换工具类，常用,
    * @Date 10:31 AM 2019/7/29
    * @Param [format, timeKey, formatter]
    * @return void
    */
    public static void getISO8601Time2(Map format, String timeKey,String formatter){
        String timestamp=String.valueOf(format.get(timeKey));
        OffsetDateTime date=OffsetDateTime.now();
        if(GrokUtil.isStringHasValue(timestamp)&&formatter!=null&&formatter.trim().length()!=0) {
            try {
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(formatter,Locale.US);
                LocalDateTime dt = LocalDateTime.parse(timestamp, dateTimeFormatter);
                ZoneOffset offset = ZoneOffset.of("+08:00");
                date = OffsetDateTime.of(dt,offset);

            }
            catch (DateTimeParseException ex) {
                LOGGER.error(ex);
               // ex.printStackTrace();
            }
        }
        format.put("@timestamp", String.valueOf(date));

    }
    /*
    * @Author juzheng
    * @Description  时间格式化工具类，与2相比形参是value；
    * @Date 4:54 PM 2019/7/30
    * @Param [format, timeValue, formatter]
    * @return void
    */
    public static void getISO8601Time3(Map format, String timeValue,String formatter){
        String timestamp=timeValue;
        OffsetDateTime date=OffsetDateTime.now();
        if(timestamp!=null&&timestamp.trim().length()!=0&&formatter!=null&&formatter.trim().length()!=0) {
            try {
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(formatter,Locale.US);
                LocalDateTime dt = LocalDateTime.parse(timestamp, dateTimeFormatter);
                ZoneOffset offset = ZoneOffset.of("+08:00");
                date = OffsetDateTime.of(dt,offset);

            }
            catch (DateTimeParseException ex) {
                LOGGER.error(ex);
                // ex.printStackTrace();
            }
        }
        format.put("@timestamp", String.valueOf(date));

    }
    /*
    * @Author juzheng
    * @Description  将unix时间戳转换为iso-8601格式的时间格式化工具类
    * @Date 10:44 AM 2019/7/29
    * @Param [format, unixTimeKey, ISOTimeKey]
    * @return void
    */
    public static void getISO8601TimeFromUnixTime(Map format,String unixTimeKey,String ISOTimeKey){
        String timestamp=String.valueOf(format.get(unixTimeKey));
        OffsetDateTime date=OffsetDateTime.now();
        if(timestamp!=null&&timestamp.trim().length()!=0) {
            try {
                Instant fromUnixTimestamp = Instant.ofEpochSecond(Long.parseLong(timestamp));
                date = fromUnixTimestamp.atOffset(ZoneOffset.of("+08:00"));
            }
            catch (Exception ex){
                LOGGER.error(ex);
            }
        }
        format.put(ISOTimeKey,String.valueOf(date));
    }

    public static void main(String[] args) {
        //test
        String timestamp="20181212162923";
        String formatter="yyyyMMddHHmmss";
       // System.out.println(getISO8601Time(timestamp,formatter));
        Map f=new HashMap();
        getISO8601Time2(f,"","");
        //2019-07-17T10:51:52+08:00
        //2018-12-12T16:29:23+08:00
    }
}
