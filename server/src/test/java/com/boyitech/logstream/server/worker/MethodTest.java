package com.boyitech.logstream.server.worker;

import com.boyitech.logstream.core.util.GeoIPHelper;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.IPv4Util;
import com.boyitech.logstream.core.util.IndexerTimeUtils;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.*;


/**
 * @author juzheng
 * @Title: MethodTest
 * @date 2019/7/15 9:48 AM
 * @Description:
 */
public class MethodTest {

    @Test
    //测试获得机器的ip
    public void getLocalIp() throws UnknownHostException {
        InetAddress inetAddress = InetAddress.getLocalHost();
        String ip = inetAddress.getHostAddress().toString();//获得本机Ip
        System.out.println(ip);

        String localIP = IPv4Util.getLocalHostLANAddress().toString().replace("/", "");
        System.out.println(localIP);

    }

    @Test
    //测试jdk8进行时间格式的转换
    public void testTime1(){
        //（新）将字符串转换为标准的ISO-8601的时间格式
        String timestamp = "20181212162923";
        if(timestamp!=null&&timestamp.trim().length()!=0) {
            LocalDateTime dt=LocalDateTime.now();
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                dt = LocalDateTime.parse(timestamp, formatter);
                ZoneOffset offset = ZoneOffset.of("+08:00");
                OffsetDateTime date = OffsetDateTime.of(dt, offset);

            }
            catch (DateTimeParseException ex) {
                ex.printStackTrace();
            }
            DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

        }

        //(旧)格式化时间
        String accessTime = "20181212162923";
        if (accessTime != null && accessTime.trim().length() != 0) {
            Date time = new Date();
            //2018-09-09 19:15:14
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
            try {
                time = sdf.parse(accessTime);
            } catch (ParseException e) {

            }
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
            accessTime = sdf2.format(time);
            System.out.println(accessTime);

        }

        //字符串-->>LocalDateTime
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse("2017-07-20 15:27:44", dateTimeFormatter);
        System.out.println("字符串转LocalDateTime: " + localDateTime);

        //LocalDateTime-->>字符串
        DateTimeFormatter dateTimeFormatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String dateString = dateTimeFormatter2.format(LocalDateTime.now());
        System.out.println("日期转字符串: " + dateString);

        //获取时区
        System.out.println(LocalDateTime.now(ZoneOffset.systemDefault()).atZone(ZoneOffset.systemDefault()).getOffset());
    }

    @Test
    public void testTime2(){
        DateTimeFormatter[] formatters = new DateTimeFormatter[]{
                // 直接使用常量创建DateTimeFormatter格式器
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ISO_LOCAL_TIME,
                DateTimeFormatter.ISO_LOCAL_DATE_TIME,
                // 使用本地化的不同风格来创建DateTimeFormatter格式器
                DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.MEDIUM),
                DateTimeFormatter.ofLocalizedTime(FormatStyle.LONG),
                // 根据模式字符串来创建DateTimeFormatter格式器
                DateTimeFormatter.ofPattern("Gyyyy%%MMM%%dd HH:mm:ss")
        };
        LocalDateTime date = LocalDateTime.now();
// 依次使用不同的格式器对LocalDateTime进行格式化
        for(int i = 0 ; i < formatters.length ; i++)
        {
            // 下面两行代码的作用相同
            System.out.println(date.format(formatters[i]));
           // System.out.println(formatters[i].format(date));
        }

    }

    @Test
    public void testTime3(){
        System.out.println( IndexerTimeUtils.getISO8601Time("Jul 15 12:09:37","yyyy MMM dd HH:mm:ss"));

    }

    /*
    * @Author juzheng
    * @Description 测试新的和旧的对于unix长整型时间戳的转换
    * @Date 10:12 AM 2019/7/29
    * @Param []
    * @return void
    */
    @Test
    public void testUnixTime(){
        //（旧）unix转换iso8601     1562102394--->>>2019/7/3 5:19:54 2019-07-03T05:19:54+08:00
        Long timestamp = Long.valueOf(1579052907)*1000L;
        Date time = new Date(Long.valueOf(timestamp));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        String unix_time=sdf.format(time);
        System.out.println(unix_time);


        // (新)
        Instant fromUnixTimestamp = Instant.ofEpochSecond(1579052907);
        String unix_time2=fromUnixTimestamp.atZone(ZoneId.of("+08:00")).toString();
        System.out.println(unix_time2);

        String unix_time3="1579052907.636378".split("\\.")[0];
        System.out.println(unix_time3);
        Instant fromUnixTimestamp3 = Instant.ofEpochSecond(Long.valueOf(unix_time3));
        String unix_time33=fromUnixTimestamp3.atZone(ZoneId.of("+08:00")).toString();
        System.out.println(unix_time33);


    }
    /*
    * @Author juzheng
    * @Description  测试geoip对各类网段的解析情况
    * @Date 2:56 PM 2019/7/30
    * @Param []
    * @return void
    */
    @Test
    public  void  testGeoip() throws IOException, GeoIp2Exception {
        System.out.println( GeoIPHelper.getInstance().getGeoIPInfo("172.19.100.20"));
    }

    /*
    * @Author juzheng
    * @Description just test map->json print
    * @Date 9:43 AM 2019/8/20
    * @Param
    * @return
    */
    @Test
    public void testMapPrintJSON(){
        Map about=new HashMap();
        Map about_details=new HashMap();
        about.put("about.logs.infos",about_details);
        about_details.put("org","apache");
        about_details.put("style","access");
        about_details.put("from","filebeat");
        about_details.put("from.version","7.3.0");
        System.out.println(GsonHelper.toJson(about));
        String s="\"risk_src\": {\"type\": \"ip\" }," +
                "\risk_dst\": {\"type\": \"ip\" }," +
                "\"risk_dvc\": {\"type\": \"ip\" }," +
                "\"risk_interestedIp\": {\"type\": \"ip\" }," +
                "\"risk_peerIp\": {\"type\": \"ip\" }," +
                "\"risk_aggregatedCnt\": {\"type\": \"integer\" }," +
                "\"risk_cnt\": {\"type\": \"integer\" }," +
                "\"risk_deviceRiskConfidenceLevel\": {\"type\": \"integer\" }," +
                "\"risk_dstPort\": {\"type\": \"integer\" }," +
                "\"risk_vLANId\": {\"type\": \"integer\" }";
        System.out.println(GsonHelper.toJson(s));
    }

    /*
    * @Author juzheng
    * @Description 测试去掉字符串前后引号
    * @Date 2:36 PM 2019/8/25
    * @Param
    * @return
    */
    @Test
    public void testNotyinhao(){
        String s="\"aa789\"";
        System.out.println(s);
        System.out.println(s.substring(0,1));
        System.out.println(s.substring(s.length()-1));
        if(s.substring(0,1).equals("\"")&&s.substring(s.length()-1).equals("\"")){
            s=s.replace(s.substring(0,1),"").replace(s.substring(s.length()-1),"");
        }
        System.out.println(s);

//        System.out.println(System.currentTimeMillis());
        // (新)（基于jdk1.8）
        Instant fromUnixTimestamp = Instant.ofEpochSecond(1567576635);
        //以下要申明时区，否则默认为UTC0时区
        String unix_time2=fromUnixTimestamp.atZone(ZoneId.of("+08:00")).toString();
        System.out.println(unix_time2);

        //（旧）unix转换iso8601基于jdk1.7     1562102394--->>>2019/7/3 5:19:54即 2019-07-03T05:19:54+08:00
       // Long timestamp = Long.valueOf(1567576635.555)*1000L; //*1000为了转换为毫秒
//        Date time = new Date(Long.valueOf(timestamp));
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
//        String unix_time=sdf.format(time);
//        System.out.println(unix_time);//结果：2019-07-03T05:19:54+08:00



    }

    @Test
    public void StringBufferToJson(){ List<Map> res = new ArrayList<Map>();
      StringBuffer contentBuffer = new StringBuffer();
      try {
          res = GsonHelper.formJson(contentBuffer.toString());
      }
      catch (Exception e){
          System.out.println(1);
      }
      if(res !=null){
          System.out.println(22);
          System.out.println(res.size());
      }
    }

    @Test
    public void getstring(){
//        String a="下载文件“FAQ_AS_CN.pdf”成功";
      //  String[]b=a.split("“");
//       String  b=a.substring(a.indexOf("“")+1,a.indexOf("”"));
//        System.out.println(b);
//        String[]c=b.split("\\.");
//        System.out.println(c.length);
//        String message="30,12/02/19,00:00:07,DNS 更新请求,172.16.20.48,BHSHC01968.boschhuayu-steering.com,,,0,6,,,,,,,,,0";
//        String[] msg=message.split(",");
//        for (String m:msg) {
//            System.out.println(m);
//        }
//        String hex_num="070C";
//        long dec_num = Long.parseLong(hex_num, 16);
//
//        String thread_id_0x16= "070c";
//        String thread_id=String.valueOf(Integer.parseInt(thread_id_0x16,16));
//        System.out.println(dec_num);
//        String result="{\"Metafield_object\":\"172.16.201.211\",\"Metafield_loglevel\":\"0\",\"@timestamp\":\"2020-02-02T02:28:06.610+08:00\",\"Metafield_type\":\"boyi_default_all_v1\",\"Metafield_subject\":\"172.16.201.211\",\"received_at\":\"2020-02-02T02:28:06.610+08:00\",\"Metafield_description\":\"弋搜采集日志\",\"ported_at\":\"2020-02-02T02:28:15.443+08:00\",\"message\":\"[2451][INFO] System backup success.\\n\",\"Metafield_source\":\"172.16.201.211\",\"Metafield_category\":\"redisShipper\"}\n";
//        for(int i=0;i<10000000;i++) {
//            Map map=GsonHelper.fromJson(result);
//        }
//        String s="[61857][INFO] Set iptables rule begin, access rule: [ncTIptablesInfo(service_port='8000', dest_net='', protocol='tcp', source_net=''), ncTIptablesInfo(service_port='80', dest_net='', protocol='tcp', source_net=''), ncTIptablesInfo(service_port='443', dest_net='', protocol='tcp', source_net=''), ncTIptablesInfo(service_port='9123', dest_net='', protocol='tcp', source_net=''), ncTIptablesInfo(service_port='9124', dest_net='', protocol='tcp', source_net=''), ncTIptablesInfo(service_port='9998', dest_net='', protocol='tcp', source_net=''), ncTIptablesInfo(service_port='9999', dest_net='', protocol='tcp', source_net=''), ncTIptablesInfo(service_port='5557', dest_net='', protocol='tcp', source_net=''), ncTIptablesInfo(service_port='9028', dest_net='', protocol='tcp', source_net=''), ncTIptablesInfo(service_port='9029', dest_net='', protocol='tcp', source_net=''), ncTIptablesInfo(service_port='22', dest_net='', protocol='tcp', source_net=''), ncTIptablesInfo(service_port='3320', dest_net='', protocol='tcp', source_n\\n";
//        String s="2020-02-04 15:18:02,Zhou Ping,信息,设置,172.16.154.101,F8-B1-56-DE-0C-61,将文件“Turning Friction_M_2020_000065.wwt”的密级设置为“非密”成功,父路径: AnyShare://Zhou Ping/2020/7047 914 291/000065_20.01.14,AnyShare-WinClient,{\\\"obj_id\\\":\\\"922DBB3E31CE4B1592BE09FBAC15417C\\\",\\\"user_account\\\":\\\"gc02399\\\"}\\n";
//        GrokCompiler grokCompiler = GrokCompiler.newInstance();
//        grokCompiler.registerDefaultPatterns();
//        grokCompiler.register("CHTIME", "%{YEAR}-%{MONTHNUM}-%{MONTHDAY} %{HOUR}:(?:[0-5][0-9]|[0-9]):%{SECOND}");
//        String pattern_1 = "%{CHTIME:datetime},%{DATA:user_name},%{DATA:log_level},%{DATA:action},%{DATA:ip},%{DATA:mac},%{DATA:action_desc},%{DATA:action_details},%{GREEDYDATA:user_agent},\\{%{GREEDYDATA:log_body}\\}";
//        Grok grok = grokCompiler.compile(pattern_1, true);
//        Match grokMatch = grok.match(s);
//        System.out.println(grokMatch.capture());
          String[] a=new String[2];
          Object[]b=a;
          a[0]="hi";
        System.out.println(b.getClass());
        System.out.println(Integer.parseInt("1"));


    }
}

