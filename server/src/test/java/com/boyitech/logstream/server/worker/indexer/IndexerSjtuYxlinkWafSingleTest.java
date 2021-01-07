package com.boyitech.logstream.server.worker.indexer;

import com.alibaba.fastjson.JSONObject;
import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import com.boyitech.logstream.core.worker.indexer.BaseIndexerConfig;
import com.boyitech.logstream.worker.indexer.DbappAuditAllV1Indexer;
import com.boyitech.logstream.worker.indexer.HuaweiUmaAllV1Indexer;
import com.boyitech.logstream.worker.indexer.YxlinkWafAllV1Indexer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author juzheng
 * @Title: IndexerSjtuYxlinkWafTxtTest
 * @date 2019/7/31 10:11 AM
 * @Description:  针对电机学院的waf日志样本做测试，
 */
public class IndexerSjtuYxlinkWafSingleTest {
    private BaseIndexer Indexer;

    @Before
    public void InitIndexer(){
        System.out.println("---初始化---");
        Map<String, String> map = new HashMap();
        map.put("logType", "1");
        BaseIndexerConfig baseIndexerConfig = new BaseIndexerConfig(map);
        //1.此处修改Indexer类型：如ApacheSuccessIndexer
        Indexer = new YxlinkWafAllV1Indexer(baseIndexerConfig);
        Indexer.register();
    }

    @Test
    public void indexerSingle() {
        System.out.println("---执行Indexer的测试---");
        List<String> list = new ArrayList<String>();
//        list.add("<137>WAF Jul01 10:38:39:ALERT#WAFSPLIT#WAF0HW12B806#WAFSPLIT#2019-7-1 10:38:39#WAFSPLIT#121.201.101.246#WAFSPLIT#65183#WAFSPLIT#172.20.6.20#WAFSPLIT#80#WAFSPLIT#禁止PHP加密webshell上传#WAFSPLIT#脚本上传#WAFSPLIT#高#WAFSPLIT#防止PHP加密的Webshell上传\n" +
//                "Webshell是一种脚本木马后门，黑客在入侵网站后，常常将这些后门文件放置在网站服务器的Web目录中，通过Web方式访问来控制网站服务器，包括上传/下载文件、查看数据1. 脚本程序中禁止脚本语言上传；\n" +
//                "2. 上传目录取消执行权限。#WAFSPLIT#1. 脚本程序中禁止脚本语言上传；\n" +
//                "2. 上传目录取消执行权限。#WAFSPLIT#拦截#WAFSPLIT#POST#WAFSPLIT#http://www.sdju.edu.cn/plus/90sec.php");
//        list.add("<137>WAF Jul01 10:39:00:ALERT#WAFSPLIT#WAF0HW12B806#WAFSPLIT#2019-7-1 10:39:0#WAFSPLIT#211.68.122.120#WAFSPLIT#40002#WAFSPLIT#172.20.6.20#WAFSPLIT#80#WAFSPLIT#可疑的文件包含#WAFSPLIT#信息泄露#WAFSPLIT#低#WAFSPLIT#攻击者企图包含远程文件\n" +
//                "服务器脚本通过包含函数包含文件时，由于脚本对要包含的文件来源过滤不严，可能导致包含一个关键文件，黑客可以查看关键文件或者执行精心构造的代码。#WAFSPLIT#1. 程序中包含的文件尽量避免动态拼接，如果确实需要，必须进行严格判断或过滤。#WAFSPLIT#拦截#WAFSPLIT#GET#WAFSPLIT#http://210.35.68.199/ans.pl?p=../../../../../usr/bin/id|&blah");
//        list.add("<137>WAF Jul01 08:01:48:ALERT#WAFSPLIT#WAF0HW12B806#WAFSPLIT#2019-7-1 8:1:48#WAFSPLIT#89.121.201.154#WAFSPLIT#37784#WAFSPLIT#172.20.6.20#WAFSPLIT#80#WAFSPLIT#禁止访问phpmyadmin页面#WAFSPLIT#信息泄露#WAFSPLIT#低#WAFSPLIT#可能会升级用户特权并通过 Web 应用程序获取管理许可权。可能会收集有关 Web 应用程序的敏感信息，如用户名、密码、机器名和/或敏感文件位置。#WAFSPLIT#1. 升级至 phpMyAdmin 的最新版本，位置如下：http://sourceforge.net/projects/phpmyadmin/；\n" +
//                " 2. 使用专业的数据库管理工具，如：Navicat for MySQL等。#WAFSPLIT#拦截#WAFSPLIT#GET#WAFSPLIT#http://101.230.228.66/xampp/phpmyadmin/index.php");
//        list.add("<137>WAF Jul01 08:02:08:ALERT#WAFSPLIT#WAF0HW12B806#WAFSPLIT#2019-7-1 8:2:8#WAFSPLIT#89.121.201.154#WAFSPLIT#4617#WAFSPLIT#172.20.6.20#WAFSPLIT#80#WAFSPLIT#禁止访问phpmyadmin页面#WAFSPLIT#信息泄露#WAFSPLIT#低#WAFSPLIT#可能会升级用户特权并通过 Web 应用程序获取管理许可权。可能会收集有关 Web 应用程序的敏感信息，如用户名、密码、机器名和/或敏感文件位置。#WAFSPLIT#1. 升级至 phpMyAdmin 的最新版本，位置如下：http://sourceforge.net/projects/phpmyadmin/；\n" +
//                " 2. 使用专业的数据库管理工具，如：Navicat for MySQL等。#WAFSPLIT#拦截#WAFSPLIT#GET#WAFSPLIT#http://101.230.228.66/phpMyadmin_bak/index.php");
//        list.add("<137>WAF Jul01 10:37:09:ALERT#WAFSPLIT#WAF0HW12B806#WAFSPLIT#2019-7-1 10:37:9#WAFSPLIT#172.17.103.191#WAFSPLIT#51655#WAFSPLIT#172.20.3.159#WAFSPLIT#80#WAFSPLIT#Bash远程命令执行漏洞(CVE-2014-6271)#WAFSPLIT#脚本执行#WAFSPLIT#中#WAFSPLIT#防止Bash远程命令执行漏洞(CVE-2014-6271)\\r\\nBash是Linux用户广泛使用的一款用于控制命令提示符工具，该漏洞被称为Bash bug或Shellshock。当用户正常访问时，只要shell是唤醒状态，这个漏洞就允许攻击者\\xE6\\x89临时解决方法：\\r\\n升级到最新版本。#WAFSPLIT#临时解决方法：\\r\\n升级到最新版本。#WAFSPLIT#检测#WAFSPLIT#GET#WAFSPLIT#**URL length exceeds the limit**");
//        list.add("<137>WAF Jul01 10:37:21:ALERT#WAFSPLIT#WAF0HW12B806#WAFSPLIT#2019-7-1 10:37:21#WAFSPLIT#172.17.22.214#WAFSPLIT#62979#WAFSPLIT#172.20.3.159#WAFSPLIT#80#WAFSPLIT#SQL注入(盲注关键字)#WAFSPLIT#SQL注入#WAFSPLIT#低#WAFSPLIT#黑客在SQL注入中经常使用一些盲注关键字\n" +
//                "当脚本程序使用输入内容来构造动态SQL语句以访问数据库时，会发生SQL注入攻击。#WAFSPLIT#1. 对用户输入的内容或参数进行严格的校验和过滤处理；\n" +
//                "2. 尽量不要动态拼接SQL语句；\n" +
//                "3. 将数据库的用户权限配置为低权限，如MSSQL中建议不要使用sa账户；\n" +
//                "4. 数据库中重要的信息（如管理员密码）必须加密存储。#WAFSPLIT#检测#WAFSPLIT#GET#WAFSPLIT#http://xtbg.sdju.edu.cn/select/selectUser1.do?type=user&participateInOffice=1&luserNoDis=null&tabs=simple,common,personal&listDeactivedUser=undefined");
//        list.add("<137>WAF Jul01 10:28:00:ALERT#WAFSPLIT#WAF0HW12B806#WAFSPLIT#2019-7-1 10:28:0#WAFSPLIT#172.17.22.214#WAFSPLIT#49411#WAFSPLIT#172.20.3.159#WAFSPLIT#80#WAFSPLIT#SQL注入(盲注关键字)#WAFSPLIT#SQL注入#WAFSPLIT#低#WAFSPLIT#黑客在SQL注入中经常使用一些盲注关键字\n" +
//                "当脚本程序使用输入内容来构造动态SQL语句以访问数据库时，会发生SQL注入攻击。#WAFSPLIT#1. 对用户输入的内容或参数进行严格的校验和过滤处理；\n" +
//                "2. 尽量不要动态拼接SQL语句；\n" +
//                "3. 将数据库的用户权限配置为低权限，如MSSQL中建议不要使用sa账户；\n" +
//                "4. 数据库中重要的信息（如管理员密码）必须加密存储。#WAFSPLIT#检测#WAFSPLIT#GET#WAFSPLIT#http://xtbg.sdju.edu.cn/select/inquires.do?selectType=user&userFirstName=%E6%9D%8E%E6%A2%A6%E8%BE%BE&luserNoDis=null&participateInOffice=1&listDeactivedUser=undefined");
//        list.add("<137>WAF Jul01 10:28:01:ALERT#WAFSPLIT#WAF0HW12B806#WAFSPLIT#2019-7-1 10:28:1#WAFSPLIT#172.17.22.214#WAFSPLIT#49411#WAFSPLIT#172.20.3.159#WAFSPLIT#80#WAFSPLIT#SQL注入(盲注关键字)#WAFSPLIT#SQL注入#WAFSPLIT#低#WAFSPLIT#黑客在SQL注入中经常使用一些盲注关键字\n" +
//                "当脚本程序使用输入内容来构造动态SQL语句以访问数据库时，会发生SQL注入攻击。#WAFSPLIT#1. 对用户输入的内容或参数进行严格的校验和过滤处理；\n" +
//                "2. 尽量不要动态拼接SQL语句；\n" +
//                "3. 将数据库的用户权限配置为低权限，如MSSQL中建议不要使用sa账户；\n" +
//                "4. 数据库中重要的信息（如管理员密码）必须加密存储。#WAFSPLIT#检测#WAFSPLIT#GET#WAFSPLIT#http://xtbg.sdju.edu.cn/select/inquires.do?selectType=user&userFirstName=%E6%9D%8E%E6%A2%A6%E8%BE%BE&luserNoDis=null&participateInOffice=1&listDeactivedUser=undefined");
//        list.add("<137>WAF Jul01 08:04:55:ALERT#WAFSPLIT#WAF0HW12B806#WAFSPLIT#2019-7-1 8:4:55#WAFSPLIT#89.121.201.154#WAFSPLIT#16491#WAFSPLIT#172.20.6.20#WAFSPLIT#80#WAFSPLIT#禁止访问phpmyadmin页面#WAFSPLIT#信息泄露#WAFSPLIT#低#WAFSPLIT#可能会升级用户特权并通过 Web 应用程序获取管理许可权。可能会收集有关 Web 应用程序的敏感信息，如用户名、密码、机器名和/或敏感文件位置。#WAFSPLIT#1. 升级至 phpMyAdmin 的最新版本，位置如下：http://sourceforge.net/projects/phpmyadmin/；\n" +
//                " 2. 使用专业的数据库管理工具，如：Navicat for MySQL等。#WAFSPLIT#拦截#WAFSPLIT#GET#WAFSPLIT#http://101.230.228.66/phpmyadmin/phpmyadmin/index.php");
//        list.add("<137>WAF Jul01 08:09:54:ALERT#WAFSPLIT#WAF0HW12B806#WAFSPLIT#2019-7-1 8:9:54#WAFSPLIT#89.121.201.154#WAFSPLIT#56134#WAFSPLIT#172.20.6.20#WAFSPLIT#80#WAFSPLIT#禁止访问phpmyadmin页面#WAFSPLIT#信息泄露#WAFSPLIT#低#WAFSPLIT#可能会升级用户特权并通过 Web 应用程序获取管理许可权。可能会收集有关 Web 应用程序的敏感信息，如用户名、密码、机器名和/或敏感文件位置。#WAFSPLIT#1. 升级至 phpMyAdmin 的最新版本，位置如下：http://sourceforge.net/projects/phpmyadmin/；\n" +
//                " 2. 使用专业的数据库管理工具，如：Navicat for MySQL等。#WAFSPLIT#拦截#WAFSPLIT#GET#WAFSPLIT#http://101.230.228.66/phpMyAdminhf/index.php");
//        list.add("<137>WAF Jul01 08:15:38:ALERT#WAFSPLIT#WAF0HW12B806#WAFSPLIT#2019-7-1 8:15:38#WAFSPLIT#154.223.180.18#WAFSPLIT#2746#WAFSPLIT#172.20.6.20#WAFSPLIT#80#WAFSPLIT#FCKeditor编辑器存在connector文件#WAFSPLIT#脚本上传#WAFSPLIT#中#WAFSPLIT#FCKeditor编辑器是一款非常流行的在线编辑工具，在大中型网站使用尤其广泛，但攻击者可以利用connector文件上传Webshell#WAFSPLIT#建议删除FCKeditor编辑器里的connector文件或者直接用本设备进行防护。#WAFSPLIT#拦截#WAFSPLIT#GET#WAFSPLIT#http://dtxy.sdju.edu.cn/FCKeditor/editor/filemanager/connectors/aspx/connector.aspx");
//        list.add("<137>WAF Jul01 09:43:40:ALERT#WAFSPLIT#WAF0HW12B806#WAFSPLIT#2019-7-1 9:43:40#WAFSPLIT#59.47.72.163#WAFSPLIT#59104#WAFSPLIT#172.20.6.20#WAFSPLIT#80#WAFSPLIT#FCKeditor编辑器存在connector文件#WAFSPLIT#脚本上传#WAFSPLIT#中#WAFSPLIT#FCKeditor编辑器是一款非常流行的在线编辑工具，在大中型网站使用尤其广泛，但攻击者可以利用connector文件上传Webshell#WAFSPLIT#建议删除FCKeditor编辑器里的connector文件或者直接用本设备进行防护。#WAFSPLIT#拦截#WAFSPLIT#POST#WAFSPLIT#http://yjs.sdju.edu.cn/FCKeditor/editor/filemanager/connectors/asp/connector.asp?Command=FileUpload&Type=File&CurrentFolder=%2F");
        list.add("<129>WAF Dec01 22:09:23:ALERT#WAFSPLIT#WAF0HW0CB507#WAFSPLIT#2018-12-1 22:9:23#WAFSPLIT#180.97.215.41#WAFSPLIT#52967#WAFSPLIT#219.220.243.217#WAFSPLIT#80#WAFSPLIT#上传目录存在脚本文件#WAFSPLIT#脚本执行#WAFSPLIT#中#WAFSPLIT#防止访问如upload/1.php等在网站上传和备份目录中的脚本文件，黑客一般利用上传、备份和图片目录具有写权限的特性来写入脚本木马" +
                "Webshell是一种脚本木马后门，黑客在入侵网站后，常常将这些1. 根据入侵记录的URL，查找和删除对应的Webshell文件；" +
                "2. 对网站进行全面检查，清理后门，修补漏洞；" +
                "3. 配置网站时可写的目录不要赋予执行权限，可执行的目录不要赋予写入权限。#WAFSPLIT#1. 根据入侵记录的URL，查找和删除对应的Webshell文件；" +
                "2. 对网站进行全面检查，清理后门，修补漏洞；" +
                "3. 配置网站时可写的目录不要赋予执行权限，可执行的目录不要赋予写入权限。#WAFSPLIT#检测#WAFSPLIT#POST#WAFSPLIT#http://jwxt.sbs.edu.cn//uploadfile/userfiles/media/confg.inc.php");
        for(String s : list) {
            Event e = new Event();
            e.setMessage(s);
            Indexer.format(e);
            System.out.println(e.getJsonMessage());
        }
    }

    @After
    public void AfterOne(){
        System.out.println("---测试结束---");
    }
}

