package com.boyitech.logstream.core.util;

import java.io.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * @author juzheng
 * @Title: IndexerTemplate
 * @date 2019/7/8 1:22 PM
 * @Description: 日志格式化Indexer代码主体框架一键生成的工具类
 */
public class IndexerTemplate {

    public static void main(String[] args) {
        String IndexerName = "Indexer";
        String TEMPLATE_PATH = "config";
        String CLASS_PATH = "core/src/main/java/com/boyitech/logstream/worker/indexer";
        System.out.print("请输入需要初始化的日志名并按回车结束（首字母须大写)(厂商+产品名+种类+版本):");
        Scanner scan = new Scanner(System.in);
        String read = scan.nextLine();
        IndexerName=read+IndexerName;
        // step1 创建freeMarker配置实例
        Configuration configuration = new Configuration();
        Writer out = null;
        try {
        // step2 获取模版路径
            configuration.setDirectoryForTemplateLoading(new File(TEMPLATE_PATH));
        // step3 创建数据模型
            Map<String, Object> dataMap = new HashMap<String, Object>();
            LocalDateTime localDateTime=LocalDateTime.now();
            String time=localDateTime.toString();
            dataMap.put("date", time);
            dataMap.put("className", IndexerName);
            dataMap.put("author",System.getProperty("user.name"));
        // step4 加载模版文件
            Template template = configuration.getTemplate("indexer.ftl");
        // step5 生成数据
            File docFile = new File(CLASS_PATH + "/" + IndexerName+".java");
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(docFile)));
        // step6 输出文件
            template.process(dataMap, out);
            System.out.println(IndexerName+".java 文件创建成功 !");
            }
            catch (Exception e) {
            e.printStackTrace();
            }
            finally {
                    try {
                        if (null != out) {
                            out.flush();
                        }
                    }
                    catch (Exception e2) {
                        e2.printStackTrace();
                    }
            }
    }
}