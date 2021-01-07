package com.boyitech.logstream.core.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.boyitech.logstream.core.util.filter_rule.IpRangeRule;
import com.boyitech.logstream.core.worker.indexer.BaseIndexerConfig;
import io.krakens.grok.api.Grok;
import io.krakens.grok.api.GrokCompiler;
import io.krakens.grok.api.Match;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


public class GrokUtil {

    protected static final Logger LOGGER = LogManager.getLogger("worker");


    /**
     * @return java.lang.String
     * @Author Eric Zheng
     * @Description 传入tomcat日志pattern输出对应的grokPattern
     * @Date 16:13 2018/11/1
     * @Param [pattern] 写在tomcat上的日志格式pattern：%h %l %u %t "%r" %s "%{Referer}i" "%{User-Agent}i"
     **/
    public static String TomcatGrok(String pattern) throws IOException {
        //加载属性文件
        Properties prop = new Properties();
        InputStream in = GrokUtil.class.getClassLoader().getResourceAsStream("tomcatPattern.properties");
        prop.load(in);
        Iterator<String> it = prop.stringPropertyNames().iterator();
        //将pattern切分从配置文件中读取
        pattern = pattern.replace("&quot;", "");
        System.out.println("以这个字段来切分：" + prop.getProperty("split"));
        String[] split = pattern.split(prop.getProperty("split"));
        StringBuffer sb = new StringBuffer();
        for (String s : split) {
            // System.out.println(s);
            sb.append(prop.getProperty(s) + " ");
        }
        in.close();
        System.out.println(sb.toString().trim());

        return sb.toString().trim();
    }


    /**
     * @return java.lang.String
     * @Author Eric Zheng
     * @Description
     * @Date 11:19 2018/11/15
     * @Param [pattern]
     **/
    public static String NginxGrok(String pattern) {
        //加载属性文件
        Properties prop = new Properties();

        InputStream in = GrokUtil.class.getClassLoader().getResourceAsStream("nginxPattern.properties");
        StringBuffer sb = new StringBuffer();
        try {
            prop.load(in);
            Iterator<String> it = prop.stringPropertyNames().iterator();
            //将pattern切分从配置文件中读取
            pattern = pattern.replace("&quot;", "");
            // System.out.println("以这个字段来切分：" + prop.getProperty("split"));
            String[] split = pattern.split(prop.getProperty("split"));
            for (String s : split) {
                // System.out.println(s);
                sb.append(prop.getProperty(s) + " ");
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }


        //System.out.println(sb.toString().trim());

        return sb.toString().trim();
    }


    /**
     * @return java.lang.String
     * @Author Eric Zheng
     * @Description
     * @Date 11:44 2018/11/19
     * @Param [pattern]
     **/
    public static String webLogAndIisGrok(String pattern) throws IOException {
        //加载属性文件
        Properties prop = new Properties();
        InputStream in = GrokUtil.class.getClassLoader().getResourceAsStream("w3cPattern.properties");
        prop.load(in);
        Iterator<String> it = prop.stringPropertyNames().iterator();
        //将pattern切分从配置文件中读取
        pattern = pattern.replace("&quot;", "");
        //System.out.println("以这个字段来切分：" + prop.getProperty("split"));
        String[] split = pattern.split(prop.getProperty("split"));
        StringBuffer sb = new StringBuffer();
        for (String s : split) {
            // System.out.println(s);
            sb.append(prop.getProperty(s) + " ");
        }
        in.close();
        System.out.println(sb.toString().trim());

        return sb.toString().trim();
    }


    /**
     * @return void
     * @Author Eric Zheng
     * @Description 记录ip详细情况
     * @Date 9:31 2018/12/11
     * @Param [map]
     **/
    public static void setGeoIP(Map formated) {
        String src_ip = (String) formated.get("src_ip");
        String dst_ip = (String) formated.get("dst_ip");

        if (src_ip != null && src_ip.trim().length() != 0) {
            try {
                Map geoIPInfo = GeoIPHelper.getInstance().getGeoIPInfo(src_ip);
                formated.put("src_ip_geoip", geoIPInfo);
            } catch (Exception e) {
                // e.printStackTrace();
            }
        }
        if (dst_ip != null && dst_ip.trim().length() != 0) {
            try {
                Map geoIPInfo = GeoIPHelper.getInstance().getGeoIPInfo(dst_ip);
                formated.put("dst_ip_geoip", geoIPInfo);
            } catch (Exception e) {
                // e.printStackTrace();
            }
        }
    }


    /*
    * @Author juzheng
    * @Description 处理ip内网过滤需求
    * @Date 9:20 AM 2019/8/8
    * @Param [config] indexer的配置
    * @return java.lang.String
    * ex:
    * {
{
    "logType": "array_vpn_all_v1",
    "ipFilter": {
        "src_ip": [
            "192.100.0.0/24",
            "172.16.0.0/30"
        ],
        "dst_ip": [
            "192.100.0.0/24",
            "172.16.0.0/30"
        ]
    }
}   */
    public static boolean filterGeoIP(BaseIndexerConfig config, Map formated) {

        String ipFilter = String.valueOf(config.getIpFilter());
        if (ipFilter != null && !ipFilter.trim().equals("") && !ipFilter.equals("null")&&ipFilter.length()>14) {
            try {
                int i = 0;
                Map mapIpFilter = JSONObject.parseObject(ipFilter);

                Map map = new HashMap<String, Integer>();
                for (Object key : mapIpFilter.keySet()) {
                    map.put(key, 1);
                }
                for (Object key : mapIpFilter.keySet()) {
                    i++;
                    String ipKey = key.toString();
                    JSONArray ipValues = (JSONArray) mapIpFilter.get(key);
                    if (ipValues.size() > 0) {
                        for (int j = 0; j < ipValues.size(); j++) {
                            IpRangeRule ipRangeRule=new IpRangeRule(ipValues.get(j).toString());
                            String ipRealValue = String.valueOf(formated.get(ipKey));
                            if(GrokUtil.isStringHasValue(ipRealValue)==true&&IPv4Util.isIPv4(ipRealValue)==true){
                                if (ipRangeRule.in(ipRealValue)==true){
                                    map.replace(ipKey,0);
                                }
                            }
                        }
                    } else {
                        return false;
                    }
                    if (i == mapIpFilter.size()) {
                        for (Object k : map.keySet()) {
                            if (map.get(k).equals(1)) {
                                String ipValue = String.valueOf(formated.get(k));
                                if (ipValue != null && !ipValue.trim().equals("") && !ipValue.equals("null")) {
                                    try {
                                        Map geoIPInfo = GeoIPHelper.getInstance().getGeoIPInfo(ipValue);
                                        formated.put(k + "_geoip", geoIPInfo);
                                    } catch (Exception e) {
                                        // e.printStackTrace();
                                        LOGGER.error("GeoIP解析失败！");
                                    }
                                }
                            }
                        }
                        return true;
                    }
                }

            } catch (Exception ex) {
                LOGGER.error("IP过滤失败！ipFilter:" + ipFilter);
                return false;
            }
        } else {
            LOGGER.error("IP过滤失败！过滤规则为空！ipFilter:" + ipFilter);
            return false;
        }

        return false;
    }

    /**
     * @return void
     * @Author juzheng
     * @Description 可解析指定ip的geoip信息;
     * @Date 17:21 2019/07/17
     * @Param [map]
     **/
    public static void setGeoIP2(Map formated, String ipKey) {
        String ipValue = String.valueOf(formated.get(ipKey));
        if (ipValue != null && !ipValue.trim().equals("") && !ipValue.equals("null")&&ipValue!="null") {
            try {
                Map geoIPInfo = GeoIPHelper.getInstance().getGeoIPInfo(ipValue);
                formated.put(ipKey + "_geoip", geoIPInfo);
            } catch (Exception e) {
                // e.printStackTrace();
                LOGGER.error("GeoIP解析失败！");
            }
        }

    }

    /**
     * @return boolean
     * @Author juzheng
     * @Description 判断日志某个字段是否是null/""/"null"，没得就返回false，有值就是true
     * @Date 10:37 AM 2019/7/18
     * @Param [field]：日志的字段值
     */
    public static boolean isStringHasValue(String field) {
        if (field != null && !field.trim().equals("") && !field.equals("null")&&field!="null")
            return true;
        return false;
    }

    /*
    * @Author juzheng
    * @Description 替换字符前后的引号
    * @Date 10:23 AM 2019/8/26
    * @Param [s]
    * @return java.lang.String
    */
    public static String setStringValue(String s){
        if(s.substring(0,1).equals("\"")&&s.substring(s.length()-1).equals("\"")){
            s=s.replace(s.substring(0,1),"").replace(s.substring(s.length()-1),"");
        }
        return s;
    }

    /*
     * @Author juzheng
     * @Description 判断传过来的字符串是否为json格式的字符串
     * @Date 1:24 PM 2019/8/1
     * @Param [m]
     * @return boolean
     */
    public final static boolean isJSONValid(String m) {
        try {
            JSONObject.parseObject(m);
        } catch (JSONException ex) {
           // LOGGER.error(ex);
            try {
                JSONObject.parseArray(m);
            } catch (JSONException ex1) {
               // LOGGER.error(ex);
               // LOGGER.error(ex1);
                return false;
            }
        }
        return true;
    }


    public static ArrayList<Grok> getGroks(String[] patterns1) {
        GrokCompiler grokCompiler = GrokCompiler.newInstance();
        grokCompiler.registerDefaultPatterns();
        //2018-10-24 10:32:42
        grokCompiler.register("CHTIME", "%{YEAR}-%{MONTHNUM}-%{MONTHDAY} %{HOUR}:(?:[0-5][0-9]|[0-9]):%{SECOND}");

        ArrayList<Grok> grokList = new ArrayList<>();
        for (String patterns : patterns1) {
            Grok grok = grokCompiler.compile(patterns, true);
            grokList.add(grok);
        }

        // 获取结果
        return grokList;
    }


    public static Grok getGrok(String patterns) {
        GrokCompiler grokCompiler = GrokCompiler.newInstance();
        grokCompiler.registerDefaultPatterns();
        //2018-10-24 10:32:42
        grokCompiler.register("CHTIME", "%{YEAR}-%{MONTHNUM}-%{MONTHDAY} %{HOUR}:(?:[0-5][0-9]|[0-9]):%{SECOND}");

        Grok grok = grokCompiler.compile(patterns, true);

        // 获取结果
        return grok;
    }

    //多条匹配规则时候使用该方法，防止错误的日志打印
    public static Map<String, Object> getMaps(Grok grok, String logMsg) {

        // 通过match()方法进行匹配, 对log进行解析, 按照指定的格式进行输出
        Match grokMatch = grok.match(logMsg);
        if (grokMatch.capture().isEmpty()) {
            if (grokMatch.isNull()) {
                HashMap<String, Object> flgs = new HashMap<String, Object>();
                flgs.put("flag", "解析失败");
                return flgs;
            }
        }
        // 获取结果
        return grokMatch.capture();
    }

    public static Map<String, Object> getMap(Grok grok, String logMsg) {

        // 通过match()方法进行匹配, 对log进行解析, 按照指定的格式进行输出
        Match grokMatch = grok.match(logMsg);
        if (grokMatch.capture().isEmpty()) {
            if (grokMatch.isNull()) {
                HashMap<String, Object> flgs = new HashMap<String, Object>();
                flgs.put("flag", "解析失败");
                LOGGER.error("日志解析失败 patterns:" + grok.getOriginalGrokPattern() + " message:" + logMsg);
                return flgs;
            }
        }
        // 获取结果
        return grokMatch.capture();
    }

    public static Map<String, Object> getMapByGroks(List<Grok> groks, String logMsg) {

        Map<String, Object> map = null;
        String patterns = null;
        for (int i = 0; i < groks.size(); i++) {
            map = getMaps(groks.get(i), logMsg);
            if (map.get("flag") != null && map.get("flag").equals("解析失败")) {
                if (i != groks.size() - 1) {
                    map.clear();
                } else {
                    patterns = groks.get(i).getOriginalGrokPattern();
                }
            } else {
                patterns = groks.get(i).getOriginalGrokPattern();
                break;
            }
        }

        if ((map.get("flag") != null && map.get("flag").equals("解析失败"))) {
            LOGGER.error("日志解析失败 patterns:" + patterns + " message:" + logMsg);
        }
        // 获取结果
        return map;
    }

    public static String formTime(String time, String befor, String agter) {


        Date timeBefor = new Date();
        //2018-09-09 19:15:14
        SimpleDateFormat sdf = new SimpleDateFormat(befor, Locale.US);
        try {
            timeBefor = sdf.parse(time);
        } catch (ParseException e) {
            LOGGER.error("@timestamp时间格式化出错");
        }
        SimpleDateFormat sdf2 = new SimpleDateFormat(agter);
        time = sdf2.format(timeBefor);
        return time;

    }


}
