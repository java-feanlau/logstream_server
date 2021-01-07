package com.boyitech.logstream.worker.indexer;

import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.GrokUtil;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.IndexerTimeUtils;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import com.boyitech.logstream.core.worker.indexer.BaseIndexerConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * @author juzheng
 * @Title: WindowsDhcpAllV1Indexer
 * @date 2020/2/24 5:46 PM
 * @Description: 将提供的DHCP日志按照描述的需求正确格式化，满足后续数据分析需求
 */
public class WindowsDhcpAllV1Indexer extends BaseIndexer {
    private BaseIndexerConfig config;

    public WindowsDhcpAllV1Indexer(BaseWorkerConfig config) {
        super(config);
        this.config= (BaseIndexerConfig) config;

    }

    public WindowsDhcpAllV1Indexer(String indexerID, BaseWorkerConfig config) {
        super(indexerID, config);
        this.config= (BaseIndexerConfig) config;

    }

    @Override
    public boolean register() {
        return true;
    }

    @Override
    public boolean format(Event event) {

        Map descMap=new HashMap<String,String>();
        descMap.put("00","已启动日志。");
        descMap.put("01","已停止日志。");
        descMap.put("02","由于磁盘空间不足，日志被暂停。");
        descMap.put("10","已将一个新的 IP 地址租赁给一个客户端。");
        descMap.put("11","一个客户端已续订了一个租赁。");
        descMap.put("12","一个客户端已释放了一个租赁。");
        descMap.put("13","一个 IP 地址已在网络上被占用。");
        descMap.put("14","不能满足一个租赁请求，因为作用域的地址池已用尽。");
        descMap.put("15","一个租赁已被拒绝。");
        descMap.put("16","一个租赁已被删除。");
        descMap.put("17","一个租赁已到期，并且已到期租赁的 DNS 记录未被删除。");
        descMap.put("18","一个租赁已到期，并且 DNS 记录已被删除。");
        descMap.put("20","已将一个 BOOTP 地址租赁给一个客户端。");
        descMap.put("21","已将一个动态 BOOTP 地址租赁给一个客户端。");
        descMap.put("22","无法满足一个 BOOTP 请求，因为作用域的 BOOTP 地址池已用尽。");
        descMap.put("23","删除了一个 BOOTP IP 地址，因为检查到它不在使用中。");
        descMap.put("24","IP 地址清理操作已开始。");
        descMap.put("25","IP 地址清理统计。");
        descMap.put("30","命名 DNS 服务器的 DNS 更新请求。");
        descMap.put("31","DNS 更新失败。");
        descMap.put("32","DNS 更新成功。");
        descMap.put("33","由于 NAP 策略而丢弃数据包。");
        descMap.put("34","DNS 更新请求失败，因为超出了 DNS 更新请求队列限制。");
        descMap.put("35","DNS 更新请求失败。");
        descMap.put("36","数据包被丢弃，因为服务器具有故障转移备用角色或客户端 ID 的哈希值不匹配。");
        descMap.put("50","超过 50 以上的代码用于 Rogue 服务器检测信息。");

        Map QResultMap=new HashMap<String,String>();
        QResultMap.put("0","NoQuarantine");
        QResultMap.put("1","隔离");
        QResultMap.put("2","丢弃数据包");
        QResultMap.put("3","试用");
        QResultMap.put("6","无隔离信息");

        String message = event.getMessage();
        try {
            Map<String, Object> format = event.getFormat();
            String[] msgs=message.split(",");
            long msgs_len=msgs.length;
            format.put("message", message);
            format.put("log_type", "boyi-windows-dhcp");
            if(msgs_len==19){
               String datetime_str = msgs[1]+" "+msgs[2];
                format.put("datetime", datetime_str);
                format.put("EVENT_ID",msgs[0]);
                format.put("DATE",msgs[1]);
                format.put("TIME",msgs[2]);
                format.put("DESC",msgs[3]);
                format.put("IP",msgs[4]);
                format.put("HOSTNAME",msgs[5]);
                format.put("MAC",msgs[6]);
                format.put("USERNAME",msgs[7]);
                format.put("TransactionID",msgs[8]);
                format.put("QResult",msgs[9]);
                format.put("Probationtime",msgs[10]);
                format.put("CorrelationID",msgs[11]);
                format.put("Dhcid",msgs[12]);
                format.put("VendorClass_Hex",msgs[13]);
                format.put("VendorClass_ASCII",msgs[14]);
                format.put("UserClass_Hex",msgs[15]);
                format.put("UserClass_ASCII",msgs[16]);
                format.put("RelayAgentInformation",msgs[17]);
                format.put("DnsRegError",msgs[18]);
                format.put("ip_addr",msgs[4]);
                if(Integer.valueOf(msgs[0])>50){
                    format.put("event_desc","超过 50 以上的代码用于 Rogue 服务器检测信息。");
                }
                else {
                    String event_desc = (String) descMap.get(msgs[0]);
                    format.put("event_desc",event_desc);
                }

            }
            format.put("msgs_length", msgs_len);
            //格式化时间，样本：
            IndexerTimeUtils.getISO8601Time2(format, "datetime", "MM/dd/yy HH:mm:ss");
            format.put("datetime",format.get("@timestamp"));


            if (!config.getIpFilter().equals("null")) {
                GrokUtil.filterGeoIP(config, format);
            } else {
                //使用GeoIP对src_ip、dst_ip或部分特殊字段处理，
                GrokUtil.setGeoIP2(format, "IP");
            }
        }
        catch (Exception ex){
            LOGGER.error(ex+"message:"+message);
            addException(ex+"message:"+message);
            ex.printStackTrace();
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
                + "\"datetime\":{\"type\":\"keyword\"},"
                + "\"EVENT_ID\":{\"type\":\"integer\"},"
                + "\"DATE\":{\"type\":\"keyword\"},"
                + "\"TIME\":{\"type\":\"keyword\"},"
                + "\"DESC\":{\"type\":\"keyword\"},"
                + "\"IP\":{\"type\":\"keyword\"},"
                +"\"ip_addr\":{\"type\":\"keyword\",\"fields\":{\"ip\":{\"type\":\"ip\",\"ignore_malformed\":\"true\"}}},"
                + "\"HOSTNAME\":{\"type\":\"keyword\"},"
                + "\"MAC\":{\"type\":\"keyword\"},"
                + "\"USERNAME\":{\"type\":\"keyword\"},"
                + "\"TransactionID\":{\"type\":\"keyword\"},"
                + "\"QResult\":{\"type\":\"integer\"},"
                + "\"Probationtime\":{\"type\":\"keyword\"},"
                + "\"CorrelationID\":{\"type\":\"keyword\"},"
                + "\"Dhcid\":{\"type\":\"keyword\"},"
                + "\"VendorClass_Hex\":{\"type\":\"keyword\"},"
                + "\"VendorClass_ASCII\":{\"type\":\"keyword\"},"
                + "\"RelayAgentInformation\":{\"type\":\"keyword\"},"
                + "\"DnsRegError\":{\"type\":\"integer\"},"
                + "\"event_desc\":{\"type\":\"keyword\"},"
                + "\"msgs_length\":{\"type\":\"keyword\"},"
                + "\"log_type\":{\"type\":\"keyword\"},"
                + "\"IP_geoip\": {"
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
                + "\"Metafield_type\":{\"type\":\"keyword\"},"
                + "\"Metafield_category\":{\"type\":\"keyword\"},"
                + "\"Metafield_subject\":{\"type\":\"keyword\"},"
                + "\"Metafield_object\":{\"type\":\"keyword\"},"
                + "\"Metafield_loglevel\":{\"type\":\"keyword\"},"
                + "\"Metafield_source\":{\"type\":\"keyword\"},"
                + "\"Metafield_description\":{\"type\":\"text\", \"fields\": {\"raw\": {\"type\": \"keyword\"}}}"
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
                + "\"datetime\":{\"type\":\"keyword\"},"
                + "\"EVENT_ID\":{\"type\":\"integer\"},"
                + "\"DATE\":{\"type\":\"keyword\"},"
                + "\"TIME\":{\"type\":\"keyword\"},"
                + "\"DESC\":{\"type\":\"keyword\"},"
                + "\"IP\":{\"type\":\"keyword\"},"
                +"\"ip_addr\":{\"type\":\"keyword\",\"fields\":{\"ip\":{\"type\":\"ip\",\"ignore_malformed\":\"true\"}}},"
                + "\"HOSTNAME\":{\"type\":\"keyword\"},"
                + "\"MAC\":{\"type\":\"keyword\"},"
                + "\"USERNAME\":{\"type\":\"keyword\"},"
                + "\"TransactionID\":{\"type\":\"keyword\"},"
                + "\"QResult\":{\"type\":\"integer\"},"
                + "\"Probationtime\":{\"type\":\"keyword\"},"
                + "\"CorrelationID\":{\"type\":\"keyword\"},"
                + "\"Dhcid\":{\"type\":\"keyword\"},"
                + "\"VendorClass_Hex\":{\"type\":\"keyword\"},"
                + "\"VendorClass_ASCII\":{\"type\":\"keyword\"},"
                + "\"RelayAgentInformation\":{\"type\":\"keyword\"},"
                + "\"DnsRegError\":{\"type\":\"integer\"},"
                + "\"event_desc\":{\"type\":\"keyword\"},"
                + "\"msgs_length\":{\"type\":\"keyword\"},"
                + "\"log_type\":{\"type\":\"keyword\"},"
                + "\"IP_geoip\": {"
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
                + "\"Metafield_type\":{\"type\":\"keyword\"},"
                + "\"Metafield_category\":{\"type\":\"keyword\"},"
                + "\"Metafield_subject\":{\"type\":\"keyword\"},"
                + "\"Metafield_object\":{\"type\":\"keyword\"},"
                + "\"Metafield_loglevel\":{\"type\":\"keyword\"},"
                + "\"Metafield_source\":{\"type\":\"keyword\"},"
                + "\"Metafield_description\":{\"type\":\"text\", \"fields\": {\"raw\": {\"type\": \"keyword\"}}}"
                + "}"
                + "}";
        return mapping;
    }
}