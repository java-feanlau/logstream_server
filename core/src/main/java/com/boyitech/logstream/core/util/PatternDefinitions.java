package com.boyitech.logstream.core.util;

import io.krakens.grok.api.Grok;
import io.krakens.grok.api.GrokCompiler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

/**
 * @author juzheng
 * @Title: PatternDefinitions
 * @date 2019/8/20 1:47 PM
 * @Description:
 */
public class PatternDefinitions {
    protected static final Logger LOGGER = LogManager.getLogger("worker");

    public static ArrayList<Grok> getApacheGroks(String[] patterns1) {
        GrokCompiler grokCompiler = GrokCompiler.newInstance();
        grokCompiler.registerDefaultPatterns();
        grokCompiler.register("APACHE_TIME","%{DAY} %{MONTH} %{MONTHDAY} %{TIME} %{YEAR}");
        ArrayList<Grok> grokList = new ArrayList<>();
        for (String patterns : patterns1) {
            Grok grok = grokCompiler.compile(patterns, true);
            grokList.add(grok);
        }

        // 获取结果
        return grokList;
    }

    public static Grok getApacheGrok(String patterns) {
        GrokCompiler grokCompiler = GrokCompiler.newInstance();
        grokCompiler.registerDefaultPatterns();
        //time:11:04 AM 2019/8/20 filebeat
         grokCompiler.register("APACHE_TIME","%{DAY} %{MONTH} %{MONTHDAY} %{TIME} %{YEAR}");
        Grok grok = grokCompiler.compile(patterns, true);

        // 获取结果
        return grok;
    }
}
