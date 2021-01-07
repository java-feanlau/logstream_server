package com.boyitech.logstream.worker.indexer;


import com.alibaba.fastjson.JSONObject;
import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.GrokUtil;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.IndexerTimeUtils;
import com.boyitech.logstream.core.util.MetafieldHelper;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import com.boyitech.logstream.core.worker.indexer.BaseIndexerConfig;
import io.krakens.grok.api.Grok;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author: juzheng
 * @Title: TrendmicroImsaMailgatewayV1Indexer
 * @date: 2019-07-31T13:20:22.939
 * @Description: 1. 此indexer文件根据indexer通用模版创建
 * 2. 完成博世华域---趋势科技（亚信）邮件网关日志解析（syslog版本）（收到的是json格式的字符串，需要先解析）
 */
public class TrendmicroImsaMailgatewayV1Indexer extends BaseIndexer {
    private String[] patterns_msgtra_imss;
    private String[] patterns_01log_imss;
    private String[] patterns_01imsstasks;
    private String[] patterns_01smtpd;
    private String pattern_01smtpd_02connect_disconnect;
    private String pattern_01smtpd_02lost_too;
    private String[] patterns_01smtpd_02NOQUEUE;
    private String pattern_01smtpd_02proxy_accept;
    private String[] patterns_01smtpd_02other;
    private String[] patterns_01cleanup;
    private String[] patterns_01qmgr;
    private String pattern_01local;
    private String[] patterns_01smtp;
    private String[] patterns_01smtp_relay;
    private String[] patterns_01smtp_reply_infos;
    private String pattern_01tlsagent;
    private ArrayList<Grok>groks_patterns_01msgtra_imss;
    private ArrayList<Grok>groks_patterns_01log_imss;
    private ArrayList<Grok>groks_patterns_01imsstasks;
    private ArrayList<Grok> grok_patterns_01smtpd;
    private Grok grok_pattern_01smtpd_02connect_disconnect;
    private Grok grok_pattern_01smtpd_02lost_too;
    private ArrayList<Grok> grok_patterns_01smtpd_02NOQUEUE;
    private Grok grok_pattern_01smtpd_02proxy_accept;
    private ArrayList<Grok> grok_patterns_01smtpd_02other;
    private ArrayList<Grok> grok_patterns_01cleanup;
    private ArrayList<Grok> grok_patterns_01qmgr;
    private Grok grok_pattern_01local;
    private ArrayList<Grok> grok_patterns_01smtp;
    private ArrayList<Grok> grok_patterns_01smtp_relay;
    private ArrayList<Grok> grok_patterns_01smtp_reply_infos;
    private Grok grok_pattern_01tlsagent;
    private BaseIndexerConfig config;


    public TrendmicroImsaMailgatewayV1Indexer(BaseWorkerConfig config) {
        super(config);
        this.config= (BaseIndexerConfig) config;

    }

    public TrendmicroImsaMailgatewayV1Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
        this.config= (BaseIndexerConfig) config;

    }

    @Override
    public boolean register() {
        //此处注册正确的正则+grok
        patterns_msgtra_imss=new String[]{
                "%{WORD:transac}(\\s*)(?<time>%{YEAR}(\\s+)%{MONTH}(\\s+)%{MONTHDAY}(\\s+)%{TIME}(\\s+)%{ISO8601_TIMEZONE})(\\s+)(?<time2>%{YEAR}/%{MONTHNUM}/%{MONTHDAY}(\\s+)%{TIME}(\\s+)%{ISO8601_TIMEZONE})(\\s+)(?<time3>%{YEAR}(\\s+)%{MONTH}(\\s+)%{MONTHDAY}(\\s+)%{TIME}(\\s+)%{ISO8601_TIMEZONE})(\\s+)%{NOTSPACE}(\\s+)%{NOTSPACE}(\\s+)%{NOTSPACE}(\\s+)%{NOTSPACE}(\\s+)%{NOTSPACE:email1}(\\s+)%{NOTSPACE:email2}(\\s*)%{DATA:reply_name}(\\s*)%{IP:relay_server_ip}(\\s*)\\[%{IP:client_ip}\\]:%{NUMBER:relay_server_port}(\\s*)%{NOTSPACE:reply_dsn}(\\s*)%{NOTSPACE}(\\s*)%{NOTSPACE}(\\s*)\\[InternalId=%{NOTSPACE:internal_id},(\\s*)Hostname=%{NOTSPACE:hostname}\\](\\s*)%{GREEDYDATA}",
                "%{WORD:transac}(\\s*)(?<time>%{YEAR}(\\s+)%{MONTH}(\\s+)%{MONTHDAY}(\\s+)%{TIME}(\\s+)%{ISO8601_TIMEZONE})(\\s+)%{GREEDYDATA}",
                "%{WORD:transac}(\\s*)#null#\t#null#\t(?<time>%{YEAR}(\\s+)%{MONTH}(\\s+)%{MONTHDAY}(\\s+)%{TIME}(\\s+)%{ISO8601_TIMEZONE})(\\s+)%{GREEDYDATA}"
        };
        patterns_01log_imss=new String[]{
                "(?<time>%{YEAR}/%{MONTHNUM}/%{MONTHDAY} %{HOUR}:%{MINUTE}:%{SECOND}(\\s*)GMT%{ISO8601_TIMEZONE})(\\s+)\\[%{NUMBER:relay_server_port}:%{NOTSPACE:reply_code}\\](\\s+)%{GREEDYDATA:reply_message}"
        };

        patterns_01imsstasks=new String[]{
                "(?<time>%{YEAR}/%{MONTHNUM}/%{MONTHDAY} %{HOUR}:%{MINUTE}:%{SECOND}(\\s*)GMT%{ISO8601_TIMEZONE})(\\s+)\\[%{NUMBER:relay_server_port}:%{NOTSPACE:reply_code}\\](\\s+)%{GREEDYDATA:reply_message}"
        };

        patterns_01smtpd = new String[]{
                "^(%{NOTSPACE:start_flag}):(\\s)(%{GREEDYDATA:log_body})",
                "^(%{WORD:start_flag}):(\\s)(%{GREEDYDATA:log_body})",
                "^(%{NOTSPACE:start_flag})(\\s)(%{GREEDYDATA:log_body})"  //java补充
        };
        pattern_01smtpd_02connect_disconnect = "^from unknown\\[(%{IP:client_ip})\\]";
        pattern_01smtpd_02lost_too = "(%{DATA:abnormal_desc})(\\s+)after(\\s+)(%{WORD:abnormal_command})(\\s+)(\\(%{DATA:abnormal_size}\\))?(\\s*)from unknown\\[(%{IP:client_ip})\\]";
        patterns_01smtpd_02NOQUEUE = new String[]{
                "client=unknown\\[(%{IP:client_ip})\\]",
                "(%{WORD:oper_type})\\:(\\s+)(%{NOTSPACE:abnormal_command})(\\s+)from unknown\\[(%{IP:client_ip})\\]\\:(\\s+)(%{NUMBER:reply_code})(\\s+)(%{NOTSPACE:reply_dsn})(\\s+)\\<(%{NOTSPACE:abnormal_recepient})\\>\\:(\\s+)(%{DATA:abnormal_reason})\\;(\\s+)from=\\<(%{NOTSPACE:sender})\\>(\\s+)to=\\<(%{NOTSPACE:recepient})\\>(\\s+)proto=(%{NOTSPACE:protocol})(\\s+)helo=\\<(%{NOTSPACE:helo})\\>"
        };
        pattern_01smtpd_02proxy_accept = "(%{NOTSPACE:reply_info})\\:(\\s+)(%{NUMBER:reply_code})(\\s+)(%{NOTSPACE:reply_dsn})(\\s+)(%{NOTSPACE:reply_status})\\:(\\s+)queued as (%{WORD:abnormal_reason})\\;(\\s+)from=\\<(%{DATA:sender})\\>(\\s+)to=\\<(%{NOTSPACE:recepient})\\>(\\s+)proto=(%{NOTSPACE:protocol})(\\s+)helo=\\<(?<helo>\\S+)\\>";
        patterns_01smtpd_02other = new String[]{
                "^(%{NOTSPACE:queue_hash}):(\\s)client=unknown\\[(%{IP:client_ip})\\]",
                "^certificate verification (%{NOTSPACE:verification_status}) for unknown\\[(%{IP:relay_server_ip})\\]\\:(\\s+)untrusted issuer (%{NOTSPACE:untrusted_issuer})",
                "^certificate verification (%{NOTSPACE:verification_status}) for unknown\\[(%{IP:relay_server_ip})\\]\\:(\\s+)(%{NOTSPACE:certificate_type}) certificate",
                "^(%{DATA:abnormal_reason}) from unknown\\[(%{IP:relay_server_ip})\\]",
                "(%{WORD:oper_type})\\:(\\s+)(%{NOTSPACE:abnormal_command})\\:(\\s*)(%{DATA:abnormal_reason})\\:(\\s*)(%{GREEDYDATA:abnormal_details})"
        };

        patterns_01cleanup = new String[]{
                "^(%{NOTSPACE:queue_hash}):(\\s)message-id=(\\<|)(%{NOTSPACE:message_id})(\\>|)",
                "^(%{NOTSPACE:queue_hash}):(\\s)(%{WORD:oper_type})\\:(\\s+)header(\\s+)(%{NOTSPACE:header_field})\\:(\\s+)(%{NOTSPACE:used_tag})(\\s+)from unknown\\[(%{IP:client_ip})\\]\\;(\\s+)from=\\<(%{DATA:sender})\\>(\\s+)to=\\<(%{DATA:recepient})\\>(\\s+)proto=(%{WORD:protocol})(\\s+)helo=\\<(%{NOTSPACE:helo})\\>\\:(\\s+)(%{GREEDYDATA:prefix_message})",
                "^(%{NOTSPACE:queue_hash}):(\\s)(%{WORD:oper_type})\\:(\\s+)header(\\s)(%{NOTSPACE:header_field})\\:(\\s+)(%{IP:smtp_server})\\:(%{NUMBER:smtp_port}) from unknown\\[(%{IP:client_ip})\\]\\;(\\s+)from=\\<(%{NOTSPACE:sender})\\>(\\s+)to=\\<(%{NOTSPACE:recepient})\\>(\\s+)proto=(%{NOTSPACE:protocol})(\\s+)helo=\\<(%{NOTSPACE:helo})\\>\\:(\\s+)smtp\\:(?<smtp>\\S+)"
        };

        patterns_01qmgr = new String[]{
                "^(%{NOTSPACE:queue_hash}):(\\s)from=\\<(%{NOTSPACE:sender})\\>,(\\s)size=(?<size>\\d+),(\\s)nrcpt=(?<nrcpt>\\d+)(\\s+)\\(queue (%{WORD:queue_status})\\)",
                "^(%{NOTSPACE:queue_hash}):(\\s)(%{WORD:queue_status})",
                "Deferred Queue Event:Message=(%{NOTSPACE:queue_hash}),Action=(%{NOTSPACE:action_status}),From=(%{NOTSPACE:from_status}),To=(%{DATA:to_status})",
                "Deferred Queue Event:Message=(%{DATA:queue_hash}),Action=(%{DATA:action_status}),From=(%{DATA:from_status}),To=(%{GREEDYDATA:to_status})"
        };

        pattern_01local = "^(%{NOTSPACE:queue_hash}):(\\s)to=\\<(%{NOTSPACE:recepient})\\>\\,(\\s+)orig_to=\\<(%{NOTSPACE:orig_to})\\>\\,(\\s+)relay=(%{NOTSPACE:relay}),(\\s+)delay=(%{NOTSPACE:delay}),(\\s+)delays=(%{NOTSPACE:delay_smtpd})\\/(%{NOTSPACE:delay_cleanup})\\/(%{NOTSPACE:delay_qmgr})\\/(%{NOTSPACE:delay_local}),(\\s+)dsn=(%{NOTSPACE:reply_dsn}),(\\s+)status=(%{NOTSPACE:reply_status})(\\s+)\\((%{NOTSPACE:reply_info})\\)";

        patterns_01smtp = new String[]{
                "^(%{NOTSPACE:queue_hash}):(\\s)to=\\<(?<recepient>\\S+)\\>\\,(\\s+)relay=(?<relay>\\S+),(\\s+)delay=(?<delay>\\S+),(\\s+)delays=(%{NOTSPACE:delay_smtpd})\\/(%{NOTSPACE:delay_cleanup})\\/(%{NOTSPACE:delay_qmgr})\\/(%{NOTSPACE:delay_smtp}),(\\s+)dsn=(%{NOTSPACE:reply_dsn}),(\\s+)status=(%{NOTSPACE:reply_status})(\\s+)\\((%{GREEDYDATA:reply_infos})\\)",
                "^(%{NOTSPACE:queue_hash}):(\\s)Used TLS for (%{NOTSPACE:relay_server})\\[(%{NOTSPACE:relay_server_ip})\\]\\:(%{NOTSPACE:relay_server_port})",
                "^certificate verification (%{NOTSPACE:verification_status}) for (%{NOTSPACE:relay_server})\\[(%{IP:relay_server_ip})\\]\\:(%{NUMBER:relay_server_port})\\:(\\s+)untrusted issuer (%{NOTSPACE:untrusted_issuer})",
                "^certificate verification (%{NOTSPACE:verification_status}) for (%{NOTSPACE:relay_server})\\[(%{IP:relay_server_ip})\\]\\:(%{NUMBER:relay_server_port})\\:(\\s+)(%{NOTSPACE:certificate_type}) certificate",
                "connect to %{NOTSPACE:relay_server}\\[%{IP:relay_server_ip}\\]:%{NUMBER:relay_server_port}\\: %{GREEDYDATA:reply_infos}",
                "^(%{NOTSPACE:queue_hash}):(\\s+)%{DATA:reply_infos}:(\\s+)(%{DATA:verification_status})(\\s+)for(\\s+)(%{NOTSPACE:relay_server})\\[(%{IP:relay_server_ip})\\]:(%{NUMBER:relay_server_port})",
                "%{DATA:reply_infos}(\\s+)with(\\s+)(%{NOTSPACE:relay_server})\\[(%{IP:relay_server_ip})\\](\\s+)(%{GREEDYDATA:abnormal_reason})",
                "%{DATA:reply_infos}(%{NOTSPACE:relay_server}):(\\s*)(%{IP:relay_server_ip})",
                "^(%{NOTSPACE:queue_hash}):(\\s)host de-in%{NOTSPACE:relay_server}\\[%{IP:relay_server_ip}\\] said:(\\s*)%{GREEDYDATA:reply_infos}"
        };
        patterns_01smtp_relay = new String[]{
                "^(%{NOTSPACE:relay_server})\\[(%{IP:relay_server_ip})\\]\\:(%{NUMBER:relay_server_port})"
        };
        patterns_01smtp_reply_infos = new String[]{
                "^(%{NUMBER:reply_code})(\\s+)(%{NOTSPACE:reply_dsn})(\\s+)\\<(%{NOTSPACE:message_id})\\>(\\s+)\\[InternalId=(%{NOTSPACE:internal_id}),(\\s+)Hostname=(%{NOTSPACE:hostname})\\](\\s+)(%{GREEDYDATA:reply_info})",
                "^(%{NUMBER:reply_code})(\\s+)(%{NOTSPACE:reply_dsn})(\\s+)(%{NOTSPACE:reply_status})\\:(\\s+)queued as (%{GREEDYDATA:queue_hash})",
                "(%{NOTSPACE:relay_server})\\[(%{IP:relay_server_ip})\\]\\:(%{NUMBER:relay_server_port})\\:(\\s+)(%{GREEDYDATA:abnormal_reason})",
                "^(%{NUMBER:reply_code})(\\s+)(%{NOTSPACE:reply_dsn})(\\s+)(%{NOTSPACE:reply_name})(\\s+)%{GREEDYDATA:reply_message}",  //java新增
                "^(%{NUMBER:reply_code})(\\s+)(%{NOTSPACE:reply_status})",
                "^%{GREEDYDATA:abnormal_reason}"
        };

        pattern_01tlsagent = "(?<datetime>%{YEAR}/%{MONTHNUM}/%{MONTHDAY} %{TIME})(\\s+)GMT\\+08:00(\\s+)\\[(?<num1>\\d+)\\:(?<num2>\\d+)\\](\\s+)\\[(%{NOTSPACE:oper_type})\\]Connected from \\[(%{IP:client_ip})\\], sender is \\[(%{DATA:sender})\\], matched policy name is \\[(%{NOTSPACE:policy_name})\\],(\\s+)(%{NOTSPACE:policy_match_result})";

        groks_patterns_01msgtra_imss=GrokUtil.getGroks(patterns_msgtra_imss);
        groks_patterns_01log_imss=GrokUtil.getGroks(patterns_01log_imss);
        groks_patterns_01imsstasks=GrokUtil.getGroks(patterns_01imsstasks);
        grok_patterns_01smtpd = GrokUtil.getGroks(patterns_01smtpd);
        grok_pattern_01smtpd_02connect_disconnect = GrokUtil.getGrok(pattern_01smtpd_02connect_disconnect);
        grok_pattern_01smtpd_02lost_too = GrokUtil.getGrok(pattern_01smtpd_02lost_too);
        grok_patterns_01smtpd_02NOQUEUE = GrokUtil.getGroks(patterns_01smtpd_02NOQUEUE);
        grok_pattern_01smtpd_02proxy_accept = GrokUtil.getGrok(pattern_01smtpd_02proxy_accept);
        grok_patterns_01smtpd_02other = GrokUtil.getGroks(patterns_01smtpd_02other);
        grok_patterns_01cleanup = GrokUtil.getGroks(patterns_01cleanup);
        grok_patterns_01qmgr = GrokUtil.getGroks(patterns_01qmgr);
        grok_pattern_01local = GrokUtil.getGrok(pattern_01local);
        grok_patterns_01smtp = GrokUtil.getGroks(patterns_01smtp);
        grok_patterns_01smtp_relay = GrokUtil.getGroks(patterns_01smtp_relay);
        grok_patterns_01smtp_reply_infos = GrokUtil.getGroks(patterns_01smtp_reply_infos);
        grok_pattern_01tlsagent = GrokUtil.getGrok(pattern_01tlsagent);

        return true;

    }

    @Override
    public boolean format(Event event) {
        //最外层，格式化日志主体
        try {
            Map<String, Object> format = event.getFormat();
            String message = event.getMessage();//此次接收的syslog的json格式的字符串；
            if (GrokUtil.isJSONValid(message) == true) {
                JSONObject pa = JSONObject.parseObject(message);
                Map mapType=JSONObject.parseObject(message);
                message = pa.getString("message");
                String program = pa.getString("program");
                if (GrokUtil.isStringHasValue(program)) {
                    if (program.equals("msgtra.imss")){
                        Map<String,Object>mapByGrok00=GrokUtil.getMapByGroks(groks_patterns_01msgtra_imss,message);
                        //format=event.getFormat();
                        format.putAll(mapType);
                        format.putAll(mapByGrok00);
                        format.put("message", message);
                        if (mapByGrok00.get("flag") == null && mapByGrok00.get("flag") != "解析失败") {
                            event.setMetafieldLoglevel("1");
                        }
                    }
                    if(program.equals("imsstasks")){
                        Map<String,Object>mapByGrok00=GrokUtil.getMapByGroks(groks_patterns_01imsstasks,message);
                        //format=event.getFormat();
                        format.putAll(mapType);
                        format.putAll(mapByGrok00);
                        format.put("message", message);
                        if (mapByGrok00.get("flag") == null && mapByGrok00.get("flag") != "解析失败") {
                            event.setMetafieldLoglevel("1");
                        }
                    }
                    if(program.equals("log.imss")){
                        Map<String,Object>mapByGrok0=GrokUtil.getMapByGroks(groks_patterns_01log_imss,message);
                        //format=event.getFormat();
                        format.putAll(mapType);
                        format.putAll(mapByGrok0);
                        format.put("message", message);
                        if (mapByGrok0.get("flag") == null && mapByGrok0.get("flag") != "解析失败") {
                            event.setMetafieldLoglevel("1");
                        }
                    }
                    if (program.equals("postfix/smtpd")) {
                        Map<String, Object> mapByGroks1 = GrokUtil.getMapByGroks(grok_patterns_01smtpd, message);
                        format = event.getFormat();
                        format.putAll(mapType);
                        format.putAll(mapByGroks1);
                        format.put("message", message);

                        if (mapByGroks1.get("flag") == null && mapByGroks1.get("flag") != "解析失败") {
                            event.setMetafieldLoglevel("1");
                            String start_flag = String.valueOf(format.get("start_flag"));
                            if (start_flag.equals("connect") || start_flag.equals("disconnect")) {
                                String log_body = String.valueOf(format.get("log_body"));
                                Map<String, Object> mapByGroks2 = GrokUtil.getMap(grok_pattern_01smtpd_02connect_disconnect, log_body);
                                format.putAll(mapByGroks2);
                                if (mapByGroks2.get("flag") == null && mapByGroks2.get("flag") != "解析失败") {
                                    event.setMetafieldLoglevel("2");
                                }
                            } else if (start_flag.equals("lost") || start_flag.equals("too")) {
                                Map<String, Object> mapByGroks3 = GrokUtil.getMap(grok_pattern_01smtpd_02lost_too, message);
                                format.putAll(mapByGroks3);
                                if (mapByGroks3.get("flag") == null && mapByGroks3.get("flag") != "解析失败") {
                                    event.setMetafieldLoglevel("2");
                                }
                            } else {
                                if (start_flag.equals("NOQUEUE")) {
                                    String log_body = String.valueOf(format.get("log_body"));
                                    Map<String, Object> mapByGroks4 = GrokUtil.getMapByGroks(grok_patterns_01smtpd_02NOQUEUE, log_body);
                                    format.putAll(mapByGroks4);
                                    if (mapByGroks4.get("flag") == null && mapByGroks4.get("flag") != "解析失败") {
                                        event.setMetafieldLoglevel("2");
                                    }
                                } else if (start_flag.equals("proxy-accept")) {
                                    String log_body = String.valueOf(format.get("log_body"));
                                    Map<String, Object> mapByGroks5 = GrokUtil.getMap(grok_pattern_01smtpd_02proxy_accept, log_body);
                                    format.putAll(mapByGroks5);
                                    if (mapByGroks5.get("flag") == null && mapByGroks5.get("flag") != "解析失败") {
                                        event.setMetafieldLoglevel("2");
                                    }
                                } else {
                                    Map<String, Object> mapByGroks6 = GrokUtil.getMapByGroks(grok_patterns_01smtpd_02other, message);
                                    format.putAll(mapByGroks6);
                                    if (mapByGroks6.get("flag") == null && mapByGroks6.get("flag") != "解析失败") {
                                        event.setMetafieldLoglevel("2");
                                    }
                                }
                            }
                        }
                    }
                    if (program.equals("postfix/cleanup")) {
                        Map<String, Object> mapByGroks7 = GrokUtil.getMapByGroks(grok_patterns_01cleanup, message);
                        format = event.getFormat();
                        format.putAll(mapType);
                        format.putAll(mapByGroks7);
                        format.put("message", message);
                        if (mapByGroks7.get("flag") == null && mapByGroks7.get("flag") != "解析失败") {
                            event.setMetafieldLoglevel("1");
                        }
                    }
                    if (program.equals("postfix/qmgr")) {
                        Map<String, Object> mapByGroks8 = GrokUtil.getMapByGroks(grok_patterns_01qmgr, message);
                        format = event.getFormat();
                        format.putAll(mapType);
                        format.putAll(mapByGroks8);
                        format.put("message", message);
                        if (mapByGroks8.get("flag") == null && mapByGroks8.get("flag") != "解析失败") {
                            event.setMetafieldLoglevel("1");
                        }
                    }
                    if (program.equals("postfix/local")) {
                        Map<String, Object> mapByGroks9 = GrokUtil.getMap(grok_pattern_01local, message);
                        format = event.getFormat();
                        format.putAll(mapType);
                        format.putAll(mapByGroks9);
                        format.put("message", message);
                        if (mapByGroks9.get("flag") == null && mapByGroks9.get("flag") != "解析失败") {
                            event.setMetafieldLoglevel("1");
                        }
                    }
                    if (program.equals("postfix/smtp")) {
                        Map<String, Object> mapByGroks10 = GrokUtil.getMapByGroks(grok_patterns_01smtp, message);
                        format = event.getFormat();
                        format.putAll(mapType);
                        format.putAll(mapByGroks10);
                        format.put("message", message);
                        if (mapByGroks10.get("flag") == null && mapByGroks10.get("flag") != "解析失败") {
                            event.setMetafieldLoglevel("1");
                        }
                        String relay = String.valueOf(format.get("relay"));
                        if (GrokUtil.isStringHasValue(relay) && !relay.equals("none")) {
                            Map<String, Object> mapByGroks11 = GrokUtil.getMapByGroks(grok_patterns_01smtp_relay, relay);
                            format.putAll(mapByGroks11);
                            if (mapByGroks11.get("flag") == null && mapByGroks11.get("flag") != "解析失败") {
                                event.setMetafieldLoglevel("2");
                            }
                        }
                        String reply_infos = String.valueOf(format.get("reply_infos"));
                        if (GrokUtil.isStringHasValue(reply_infos)) {
                            Map<String, Object> mapByGroks12 = GrokUtil.getMapByGroks(grok_patterns_01smtp_reply_infos, reply_infos);
                            format.putAll(mapByGroks12);
                            if (mapByGroks12.get("flag") == null && mapByGroks12.get("flag") != "解析失败") {
                                event.setMetafieldLoglevel("2");
                            }
                        }
                    }
                    if (program.equals("tlsagent")) {
                        Map<String, Object> mapByGroks13 = GrokUtil.getMap(grok_pattern_01tlsagent, message);
                        format = event.getFormat();
                        format.putAll(mapType);
                        format.putAll(mapByGroks13);
                        format.put("message", message);
                        if (mapByGroks13.get("flag") == null && mapByGroks13.get("flag") != "解析失败") {
                            event.setMetafieldLoglevel("1");
                            String datetime = String.valueOf(format.get("datetime"));
                            if (GrokUtil.isStringHasValue(datetime)) {
                                datetime = IndexerTimeUtils.getISO8601Time(datetime, "yyyy/MM/dd HH:mm:ss");
                                format.put("datetime", datetime);
                            }
                        }
                    }
                }



            } else {
                LOGGER.error("message格式错误:" + message);
            }


            //格式化时间，样本：
            // IndexerTimeUtils.getISO8601Time2(format, "", "");


            if (!config.getIpFilter().equals("null")) {
                GrokUtil.filterGeoIP(config, format);
            } else {
                //使用GeoIP对src_ip、dst_ip或部分特殊字段处理，
                GrokUtil.setGeoIP2(format, "client_ip");
                GrokUtil.setGeoIP2(format, "relay_server_ip");
            }

            //字符串切割：
            String sender = String.valueOf(format.get("sender"));
            if (GrokUtil.isStringHasValue(sender)) {
                String senderStr[] = sender.split("@");
                String mail_user = senderStr[0];
                String mail_domain = senderStr[1];
                format.put("mail_user", mail_user);
                format.put("mail_domain", mail_domain);
            }

            String recepient = String.valueOf(format.get("recepient"));
            if (GrokUtil.isStringHasValue(recepient)) {
                String recepientStr[] = recepient.split("@");
                String mail_user = recepientStr[0];
                String mail_domain = recepientStr[1];
                format.put("mail_user", mail_user);
                format.put("mail_domain", mail_domain);
            }



            //格式化Metafield ，可能需要根基实际日志格式替换部分字段名如client_ip/src_ip、dst_ip
            format.put("format_level",event.getMetafieldLoglevel());

//            if (event.getLogType() != null) {
//                format.put("Metafield_description", event.getLogType());
//                format.put("Metafield_type", event.getLogType());
//            }
            if (event.getSource() != null) {
             //   format.put("Metafield_source", event.getSource());
                format.put("log_source", event.getSource());//增加来源设备标识；
            }
//            if (format.get("client_ip") != null) {
//                format.put("Metafield_object", format.get("client_ip"));
//            }
//            if (format.get("relay_server_ip") != null) {
//                format.put("Metafield_subject", format.get("relay_server_ip"));
//            } else {
//                format.put("Metafield_subject", event.getSource());
//            }
//            if (format.get("smtp_server") != null) {
//                format.put("Metafield_subject", format.get("smtp_server"));
//            }

//            if(format.get("client_ip") != null && format.get("dst_ip") != null){
//                format.put("ip_addr_pair", format.get("src_ip")+"=>"+format.get("dst_ip"));
//            }
            MetafieldHelper.setMetafield(event,"client_ip","smtp_server","relay_server_ip",format);
            if (format.get("flag") == "解析失败")
                return false;
        }
        catch (Exception e)
        {
            LOGGER.error(e);
        }
        return true;
    }

    @Override
    public void tearDown() {
    }

    //上传的Mapping，要在下面两处空格处加上对应的Mapping字段；
    public static Map getMapping() {
        //language=JSON
        String mapping = "{\"properties\":{"
                + "\"@timestamp\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
                + "\"received_at\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
                + "\"portedAt\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
                + "\"flag\":{\"type\":\"keyword\"},"
                + "\"message\":{\"type\":\"text\"},"
                + "\"@version\":{\"type\":\"keyword\"},"
                + "\"timestamp\":{\"type\":\"keyword\"},"
                + "\"tags\":{\"type\":\"keyword\"},"
                + "\"type\":{\"type\":\"keyword\"},"
                + "\"host\":{\"type\":\"keyword\"},"
                + "\"time\":{\"type\":\"keyword\"},"
                + "\"transac\":{\"type\":\"keyword\"},"
                + "\"log_class\":{\"type\":\"keyword\"},"
                + "\"program\":{\"type\":\"keyword\"},"
                + "\"format_level\":{\"type\":\"keyword\"},"
                + "\"facility\":{\"type\":\"keyword\"},"
                + "\"facility_label\":{\"type\":\"keyword\"},"
                + "\"severity\":{\"type\":\"keyword\"},"
                + "\"severity_label\":{\"type\":\"keyword\"},"
                + "\"pid\":{\"type\":\"keyword\"},"
                + "\"start_flag\":{\"type\":\"keyword\"},"
                + "\"log_body\":{\"type\":\"text\"},"
                + "\"client_ip\":{\"type\":\"ip\"},"
                + "\"abnormal_desc\":{\"type\":\"keyword\"},"
                + "\"abnormal_command\":{\"type\":\"keyword\"},"
                + "\"abnormal_size\":{\"type\":\"keyword\"},"
                + "\"oper_type\":{\"type\":\"keyword\"},"
                + "\"reply_code\":{\"type\":\"long\"},"
                + "\"reply_dsn\":{\"type\":\"keyword\"},"
                + "\"reply_name\":{\"type\":\"keyword\"},"
                + "\"reply_message\":{\"type\":\"keyword\"},"
                + "\"abnormal_recepient\":{\"type\":\"keyword\"},"
                + "\"abnormal_reason\":{\"type\":\"keyword\"},"
                + "\"abnormal_details\":{\"type\":\"keyword\"},"
                + "\"sender\":{\"type\":\"keyword\"},"
                + "\"recepient\":{\"type\":\"keyword\"},"
                + "\"trandisp\":{\"type\":\"keyword\"},"
                + "\"protocol\":{\"type\":\"keyword\"},"
                + "\"helo\":{\"type\":\"keyword\"},"
                + "\"reply_status\":{\"type\":\"keyword\"},"
                + "\"queue_hash\":{\"type\":\"keyword\"},"
                + "\"relay_server\":{\"type\":\"keyword\"},"
                + "\"relay_server_ip\":{\"type\":\"ip\"},"
                + "\"relay_server_port\":{\"type\":\"long\"},"
                + "\"verification_status\":{\"type\":\"keyword\"},"
                + "\"untrusted_issuer\":{\"type\":\"text\"},"
                + "\"certificate_type\":{\"type\":\"keyword\"},"
                + "\"message_id\":{\"type\":\"keyword\"},"
                + "\"header_field\":{\"type\":\"keyword\"},"
                + "\"used_tag\":{\"type\":\"keyword\"},"
                + "\"prefix_message\":{\"type\":\"text\"},"
                + "\"smtp_server\":{\"type\":\"ip\"},"
                + "\"smtp_port\":{\"type\":\"long\"},"
                + "\"size\":{\"type\":\"long\"},"
                + "\"nrcpt\":{\"type\":\"long\"},"
                + "\"queue_status\":{\"type\":\"keyword\"},"
                + "\"action_status\":{\"type\":\"keyword\"},"
                + "\"from_status\":{\"type\":\"keyword\"},"
                + "\"to_status\":{\"type\":\"keyword\"},"
                + "\"orig_to\":{\"type\":\"keyword\"},"
                + "\"relay\":{\"type\":\"keyword\"},"
                + "\"delay\":{\"type\":\"float\"},"
                + "\"delay_smtpd\":{\"type\":\"float\"},"
                + "\"delay_cleanup\":{\"type\":\"float\"},"
                + "\"delay_qmgr\":{\"type\":\"float\"},"
                + "\"delay_local\":{\"type\":\"float\"},"
                + "\"reply_info\":{\"type\":\"text\"},"
                + "\"delay_smtp\":{\"type\":\"float\"},"
                + "\"internal_id\":{\"type\":\"keyword\"},"
                + "\"hostname\":{\"type\":\"keyword\"},"
                + "\"datetime\":{\"type\":\"date\"},"
                + "\"num1\":{\"type\":\"keyword\"},"
                + "\"num2\":{\"type\":\"keyword\"},"
                + "\"mail_domain\":{\"type\":\"keyword\"},"
                + "\"mail_user\":{\"type\":\"keyword\"},"
                + "\"policy_name\":{\"type\":\"keyword\"},"
                + "\"policy_match_result\":{\"type\":\"keyword\"},"
                + "\"client_ip_geoip\": {"
                + "\"properties\": {"
                + "\"location\": {\"type\": \"geo_point\"},"
                + "\"city_name\": {\"type\": \"keyword\"},"
                + "\"continent_code\": {\"type\": \"keyword\"},"
                + "\"country_code2\": {\"type\": \"keyword\"},"
                + "\"country_code3\": {\"type\": \"keyword\"},"
                + "\"country_name\": {\"type\": \"keyword\"},"
                + "\"dma_code\": {\"type\": \"keyword\"},"
                + "\"ip\": {\"type\": \"ip\"},"
                + "\"latitude\": {\"type\": \"float\"},"
                + "\"longitude\": {\"type\": \"float\"},"
                + "\"postal_code\": {\"type\": \"keyword\"},"
                + "\"region_name\": {\"type\": \"keyword\"},"
                + "\"timezone\": {\"type\": \"keyword\"}"
                + "}"
                + "},"
                + "\"relay_server_ip_geoip\": {"
                + "\"properties\": {"
                + "\"location\": {\"type\": \"geo_point\"},"
                + "\"city_name\": {\"type\": \"keyword\"},"
                + "\"continent_code\": {\"type\": \"keyword\"},"
                + "\"country_code2\": {\"type\": \"keyword\"},"
                + "\"country_code3\": {\"type\": \"keyword\"},"
                + "\"country_name\": {\"type\": \"keyword\"},"
                + "\"dma_code\": {\"type\": \"keyword\"},"
                + "\"ip\": {\"type\": \"ip\"},"
                + "\"latitude\": {\"type\": \"float\"},"
                + "\"longitude\": {\"type\": \"float\"},"
                + "\"postal_code\": {\"type\": \"keyword\"},"
                + "\"region_name\": {\"type\": \"keyword\"},"
                + "\"time2\": {\"type\": \"keyword\"},"
                + "\"time3\": {\"type\": \"keyword\"},"
                + "\"email1\": {\"type\": \"keyword\"},"
                + "\"email2\": {\"type\": \"keyword\"},"
                + "\"timezone\": {\"type\": \"keyword\"}"
                + "}"
                + "},"
                + "\"Metafield_type\":{\"type\":\"keyword\"},"
                + "\"Metafield_category\":{\"type\":\"keyword\"},"
                + "\"Metafield_subject\":{\"type\":\"keyword\"},"
                + "\"Metafield_object\":{\"type\":\"keyword\"},"
                + "\"Metafield_loglevel\":{\"type\":\"keyword\"},"
                + "\"Metafield_source\":{\"type\":\"keyword\"},"
                + "\"Metafield_description\":{\"type\":\"keyword\", \"fields\": {\"raw\": {\"type\": \"keyword\"}}}"
                + "}"
                + "}";
        return GsonHelper.fromJson(mapping);
    }

    public static String getMappingString() {
        String mapping = "{\"properties\":{"
                + "\"@timestamp\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
                + "\"received_at\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
                + "\"portedAt\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
                + "\"flag\":{\"type\":\"keyword\"},"
                + "\"message\":{\"type\":\"text\"},"
                + "\"@version\":{\"type\":\"keyword\"},"
                + "\"timestamp\":{\"type\":\"keyword\"},"
                + "\"tags\":{\"type\":\"keyword\"},"
                + "\"type\":{\"type\":\"keyword\"},"
                + "\"host\":{\"type\":\"keyword\"},"
                + "\"time\":{\"type\":\"keyword\"},"
                + "\"transac\":{\"type\":\"keyword\"},"
                + "\"log_class\":{\"type\":\"keyword\"},"
                + "\"program\":{\"type\":\"keyword\"},"
                + "\"format_level\":{\"type\":\"keyword\"},"
                + "\"facility\":{\"type\":\"keyword\"},"
                + "\"facility_label\":{\"type\":\"keyword\"},"
                + "\"severity\":{\"type\":\"keyword\"},"
                + "\"severity_label\":{\"type\":\"keyword\"},"
                + "\"pid\":{\"type\":\"keyword\"},"
                + "\"start_flag\":{\"type\":\"keyword\"},"
                + "\"log_body\":{\"type\":\"text\"},"
                + "\"client_ip\":{\"type\":\"ip\"},"
                + "\"abnormal_desc\":{\"type\":\"keyword\"},"
                + "\"abnormal_command\":{\"type\":\"keyword\"},"
                + "\"abnormal_size\":{\"type\":\"keyword\"},"
                + "\"oper_type\":{\"type\":\"keyword\"},"
                + "\"reply_code\":{\"type\":\"long\"},"
                + "\"reply_dsn\":{\"type\":\"keyword\"},"
                + "\"reply_name\":{\"type\":\"keyword\"},"
                + "\"reply_message\":{\"type\":\"keyword\"},"
                + "\"abnormal_recepient\":{\"type\":\"keyword\"},"
                + "\"abnormal_reason\":{\"type\":\"keyword\"},"
                + "\"abnormal_details\":{\"type\":\"keyword\"},"
                + "\"sender\":{\"type\":\"keyword\"},"
                + "\"recepient\":{\"type\":\"keyword\"},"
                + "\"trandisp\":{\"type\":\"keyword\"},"
                + "\"protocol\":{\"type\":\"keyword\"},"
                + "\"helo\":{\"type\":\"keyword\"},"
                + "\"reply_status\":{\"type\":\"keyword\"},"
                + "\"queue_hash\":{\"type\":\"keyword\"},"
                + "\"relay_server\":{\"type\":\"keyword\"},"
                + "\"relay_server_ip\":{\"type\":\"ip\"},"
                + "\"relay_server_port\":{\"type\":\"long\"},"
                + "\"verification_status\":{\"type\":\"keyword\"},"
                + "\"untrusted_issuer\":{\"type\":\"text\"},"
                + "\"certificate_type\":{\"type\":\"keyword\"},"
                + "\"message_id\":{\"type\":\"keyword\"},"
                + "\"header_field\":{\"type\":\"keyword\"},"
                + "\"used_tag\":{\"type\":\"keyword\"},"
                + "\"prefix_message\":{\"type\":\"text\"},"
                + "\"smtp_server\":{\"type\":\"ip\"},"
                + "\"smtp_port\":{\"type\":\"long\"},"
                + "\"size\":{\"type\":\"long\"},"
                + "\"nrcpt\":{\"type\":\"long\"},"
                + "\"queue_status\":{\"type\":\"keyword\"},"
                + "\"action_status\":{\"type\":\"keyword\"},"
                + "\"from_status\":{\"type\":\"keyword\"},"
                + "\"to_status\":{\"type\":\"keyword\"},"
                + "\"orig_to\":{\"type\":\"keyword\"},"
                + "\"relay\":{\"type\":\"keyword\"},"
                + "\"delay\":{\"type\":\"float\"},"
                + "\"delay_smtpd\":{\"type\":\"float\"},"
                + "\"delay_cleanup\":{\"type\":\"float\"},"
                + "\"delay_qmgr\":{\"type\":\"float\"},"
                + "\"delay_local\":{\"type\":\"float\"},"
                + "\"reply_info\":{\"type\":\"text\"},"
                + "\"delay_smtp\":{\"type\":\"float\"},"
                + "\"internal_id\":{\"type\":\"keyword\"},"
                + "\"hostname\":{\"type\":\"keyword\"},"
                + "\"datetime\":{\"type\":\"date\"},"
                + "\"num1\":{\"type\":\"keyword\"},"
                + "\"num2\":{\"type\":\"keyword\"},"
                + "\"mail_domain\":{\"type\":\"keyword\"},"
                + "\"mail_user\":{\"type\":\"keyword\"},"
                + "\"policy_name\":{\"type\":\"keyword\"},"
                + "\"policy_match_result\":{\"type\":\"keyword\"},"
                + "\"client_ip_geoip\": {"
                + "\"properties\": {"
                + "\"location\": {\"type\": \"geo_point\"},"
                + "\"city_name\": {\"type\": \"keyword\"},"
                + "\"continent_code\": {\"type\": \"keyword\"},"
                + "\"country_code2\": {\"type\": \"keyword\"},"
                + "\"country_code3\": {\"type\": \"keyword\"},"
                + "\"country_name\": {\"type\": \"keyword\"},"
                + "\"dma_code\": {\"type\": \"keyword\"},"
                + "\"ip\": {\"type\": \"ip\"},"
                + "\"latitude\": {\"type\": \"float\"},"
                + "\"longitude\": {\"type\": \"float\"},"
                + "\"postal_code\": {\"type\": \"keyword\"},"
                + "\"region_name\": {\"type\": \"keyword\"},"
                + "\"timezone\": {\"type\": \"keyword\"}"
                + "}"
                + "},"
                + "\"relay_server_ip_geoip\": {"
                + "\"properties\": {"
                + "\"location\": {\"type\": \"geo_point\"},"
                + "\"city_name\": {\"type\": \"keyword\"},"
                + "\"continent_code\": {\"type\": \"keyword\"},"
                + "\"country_code2\": {\"type\": \"keyword\"},"
                + "\"country_code3\": {\"type\": \"keyword\"},"
                + "\"country_name\": {\"type\": \"keyword\"},"
                + "\"dma_code\": {\"type\": \"keyword\"},"
                + "\"ip\": {\"type\": \"ip\"},"
                + "\"latitude\": {\"type\": \"float\"},"
                + "\"longitude\": {\"type\": \"float\"},"
                + "\"postal_code\": {\"type\": \"keyword\"},"
                + "\"region_name\": {\"type\": \"keyword\"},"
                + "\"time2\": {\"type\": \"keyword\"},"
                + "\"time3\": {\"type\": \"keyword\"},"
                + "\"email1\": {\"type\": \"keyword\"},"
                + "\"email2\": {\"type\": \"keyword\"},"
                + "\"timezone\": {\"type\": \"keyword\"}"
                + "}"
                + "},"
                + "\"Metafield_type\":{\"type\":\"keyword\"},"
                + "\"Metafield_category\":{\"type\":\"keyword\"},"
                + "\"Metafield_subject\":{\"type\":\"keyword\"},"
                + "\"Metafield_object\":{\"type\":\"keyword\"},"
                + "\"Metafield_loglevel\":{\"type\":\"keyword\"},"
                + "\"Metafield_source\":{\"type\":\"keyword\"},"
                + "\"Metafield_description\":{\"type\":\"keyword\", \"fields\": {\"raw\": {\"type\": \"keyword\"}}}"
                + "}"
                + "}";
        return mapping;
    }
}