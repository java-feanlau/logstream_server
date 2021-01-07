package com.boyitech.logstream.core.worker.porter.elasticsearch;

import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.manager.indexer.BaseIndexerManager;
import com.boyitech.logstream.core.setting.WorkerSettings;
import com.boyitech.logstream.core.util.GrokUtil;
import com.boyitech.logstream.core.util.IPv4Util;
import com.boyitech.logstream.core.worker.porter.BasePorter;
import com.boyitech.logstream.core.worker.porter.BasePorterConfig;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
//import com.boyi.logstream.server.manager.YSLogServerManager;

public class ElasticsearchPorter extends BasePorter {

    private TransportClient client;
    private final DateTimeFormatter dateTimepattern;
    private int retryTimes;
    private List<Event> retryList = new ArrayList<Event>();

    public boolean result = false;  //for test
    private ElasticsearchPorterConfig config;

    public ElasticsearchPorter(BasePorterConfig config) {
        super(config);
        this.config = (ElasticsearchPorterConfig) config;
        dateTimepattern = DateTimeFormat.forPattern("YYYY.MM");
        retryTimes = WorkerSettings.FAILURERETRYTIMES.getValue();
    }

    public ElasticsearchPorter(String workerID, BasePorterConfig config) {
        super(workerID, config);
        this.config = (ElasticsearchPorterConfig) config;
        dateTimepattern = DateTimeFormat.forPattern("YYYY.MM");
        retryTimes = WorkerSettings.FAILURERETRYTIMES.getValue();
    }


    @Override
    public boolean register() {
        // 配置与es集群通讯的节点
        Settings settings = Settings.builder()
//                .put("client.transport.sniff", true)
                .put("client.transport.ignore_cluster_name", true).build();
//                .put("cluster.name", "ys_es").build();
        client = new PreBuiltTransportClient(settings);

        String host = config.getIp();
        String port = config.getPort();

        try {
            client.addTransportAddress(new TransportAddress(
                    InetAddress.getByAddress(IPv4Util.ipToBytesByInet(host)), Integer.parseInt(port)));
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            LOGGER.debug("elasticsearch注册绑定的ip端口为：" + host + ":" + port);
        }

    }

    @Override
    public void run() {
        List<Event> retryList = new ArrayList<Event>();
        while (runSignal) {
            try {
                retryList = execute(retryList);
            } catch (Exception e) {
                this.recordException("", e);
                LOGGER.error("porter error ： ", e);
            }
        }
        tearDown();
        if (countDownLatch != null) {
            countDownLatch.countDown();
            LOGGER.info(Thread.currentThread().getName() + "退出");
        }
    }



    public void finishEvent(List<Event> events){
        events.forEach(a->{
            Map<String, Object> format = a.getFormat();
            format.put("Metafield_type",a.getMetafieldType());
            format.put("Metafield_category",a.getMetafieldCategory());
            format.put("Metafield_subject",a.getMetafieldSubject());
            format.put("Metafield_object",a.getMetafieldObject());
            format.put("Metafield_loglevel",a.getMetafieldLoglevel());
            format.put("Metafield_source",a.getMetafieldSource());
            format.put("Metafield_description",a.getMetafieldDescription());
            if(a.getClientIP()!=null){
                format.put("ys_client_ip",a.getClientIP());
            }
        });
    }

    private List<Event> execute(List<Event> retryList) throws InterruptedException {
        if (lv2Cache.size() != 0) {
            List<Event> eventList = (List<Event>) lv2Cache.poll(WorkerSettings.BATCHSIZE.getValue() - retryList.size());
            finishEvent(eventList);
            // 将新的输出和上一次失败的队列合并
            eventList.addAll(retryList);
            return flush(eventList);
        } else {
            return flush(retryList);
        }
    }

    @Override
    public void execute() throws InterruptedException {
    }

    @Override
    public void tearDown() {
        try {
            flush(retryList).size();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    public static boolean checkConfig() {
        // TODO Auto-generated method stub
        return false;
    }

    public List<Event> flush(List<Event> outputList) throws InterruptedException {


        if (outputList == null)
            return null;
        List<Event> tmp = outputList;
        List<Event> retry = new ArrayList<Event>();
        if (!outputList.isEmpty()) {
        //   confirmIndex(outputList.get(0));
            List<Event> outputList2=new ArrayList<>();
            List indexer_date_list=outputList.stream().map((e) -> {
                return e.getEsIndex();
            }).distinct().collect(Collectors.toList());

            for (Object indexer_date:indexer_date_list) {
                outputList2.addAll(outputList.stream().filter((e) -> e.getEsIndex() == (int)indexer_date).limit(1).collect(Collectors.toList()));
            }
            for (Event e:outputList2) {
                confirmIndex(e);
            }

            // 构建bulk
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            DateTime now = DateTime.now();
            //将这一批次的event封装到bulkRequest
            for (Event e : outputList) {
                // 根据event的tiamstamp拼接完整的索引名称
                String completeIndex = composeIndex(e);
                e.setPortedAt(now);
                bulkRequest.add(client.prepareIndex(completeIndex, e.getLogType()).setSource(e.getJsonMessage(),
                        XContentType.JSON));
                count.addAndGet(1);
            }
            BulkResponse bulkResponse = null;
            boolean bulkSuccess = false;
            do {
                try {
                    bulkResponse = bulkRequest.get();
                    bulkSuccess = true;
                } catch (NoNodeAvailableException nnae) {
                    bulkSuccess = false;
                    LOGGER.warn(nnae.getMessage());
                    Thread.sleep(WorkerSettings.RECONNECTINTERVAL.getValue());
                }
            } while (!bulkSuccess);
            int failed = 0;
            if (bulkResponse.hasFailures()) {
                for (BulkItemResponse res : bulkResponse.getItems()) {
                    failed++;
                    count.decrementAndGet();
                    // logstash的es插件里的几个状态码
                    // RETRYABLE_CODES = [429, 503]
                    // DLQ_CODES = [400, 404]
                    // SUCCESS_CODES = [200, 201]
                    // CONFLICT_CODE = 409
                    Event e;
                    if (res.getFailure() == null) {
                        continue;
                    }
                    switch (res.getFailure().getStatus().getStatus()) {
                        case 200:
                            // es成功接收的数据则进行转发
                            e = tmp.get(res.getItemId());
                            result = true;
                            if (this.lv3Cache != null) {
                                this.forwarding(e);
                            }
                            break;
                        case 404:
                            confirmIndex(tmp.get(res.getItemId()));
                            e = tmp.get(res.getItemId());
                            e.increaseRetry();
                            retry.add(e);
                            break;
                        case 429:
                        case 503:
                            e = tmp.get(res.getItemId());
                            e.increaseRetry();
                            retry.add(e);
                            break;
                        default:
                            e = tmp.get(res.getItemId());
                            LOGGER.error("es porter error:" + res.getFailure().getStatus().getStatus()
                                    + ": " + res.getFailureMessage() + "\n" + e.getMessage());
                            break;
                    }
                }
            } else {
                result = true;
                // 没有失败则转发所有则判断有lv3cache
                if (this.lv3Cache != null) {
                    this.forwarding(tmp);
                }
            }

        }
        return retry;
    }


    private void forwarding(List<Event> list) {
        for (Event e : list) {
            forwarding(e);
        }
    }

    private void forwarding(Event event) {
        try {
            lv3Cache.put(event);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private boolean confirmIndex(Event e) {
        String completeIndex = composeIndex(e);
        synchronized (this.getClass()) {
            if (!indexExists(completeIndex)) {
                try {
                    createIndex(e);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
            }
        }
        return indexExists(completeIndex);
    }

    /**
     * 查询es中索引是否已经存在
     *
     * @return
     */
    private boolean indexExists(String completeIndex) {
        IndicesExistsRequest indexExists = new IndicesExistsRequest(completeIndex);
        return client.admin().indices().exists(indexExists).actionGet().isExists();
    }

    /**
     * 在es中创建索引及对应的mapping
     *
     * @return
     * @throws Exception
     */
    private boolean createIndex(Event e) throws Exception {
        String completeIndex = this.composeIndex(e);
        // 获取index和mapping，向manager查找索引相关信息
        String type = e.getLogType();
        Map mapping = BaseIndexerManager.getIndexMapping(e.getLogType());

        CreateIndexRequest request = new CreateIndexRequest(completeIndex);
        //todo 配置文件
        request.settings(Settings.builder()
                .put("index.number_of_shards", e.getEsShards())
                .put("index.number_of_replicas", e.getEsReplicas())
        );


        LOGGER.info("创建索引：" + completeIndex);
        // 创建索引成功
        if (client.admin().indices().create(request).get().isAcknowledged()) {

            PutMappingRequest putMappingRequest = new PutMappingRequest(completeIndex).type(type).source(mapping);


            LOGGER.info("上传mapping：" + mapping.toString());
            if (client.admin().indices().putMapping(putMappingRequest).actionGet().isAcknowledged()) {
                LOGGER.info("索引" + completeIndex + "创建成功");
                return true;
            } else {
                LOGGER.info("索引" + completeIndex + "创建失败");
                return false;
            }
        } else {
            return false;
        }
    }

    public String composeIndex(Event e) {
        StringBuffer sb = new StringBuffer();
        if (e.getIndex() != null && !e.getIndex().equals("")) {
            sb.append(e.getIndex());
        } else {
            sb.append("unknown-");
        }

        if(GrokUtil.isStringHasValue(String.valueOf(e.getFormat().get("@timestamp")))){
            DateTime dt=new DateTime(e.getFormat().get("@timestamp"));
            sb.append(dt.toString(dateTimepattern));
        }
        else
        {
            sb.append(e.getReceivedAt().toString(dateTimepattern));
        }
        return sb.toString();
    }
}
