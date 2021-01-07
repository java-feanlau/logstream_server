package com.boyitech.logstream.core.worker.porter.elasticsearch_new;

import com.boyitech.logstream.core.cache.BaseCache;
import com.boyitech.logstream.core.factory.CacheFactory;
import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.manager.indexer.BaseIndexerManager;
import com.boyitech.logstream.core.setting.WorkerSettings;
import com.boyitech.logstream.core.worker.porter.BasePorter;
import com.boyitech.logstream.core.worker.porter.BasePorterConfig;

import com.boyitech.logstream.worker.indexer.ArrayVpnAllV1Indexer;
import com.boyitech.logstream.worker.indexer.NginxNginxErrorV1Indexer;
import com.boyitech.logstream.worker.indexer.NginxNginxSuccessV1Indexer;
import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.*;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

public class ElasticsearchPorter extends BasePorter {

    private RestHighLevelClient client;
    private final DateTimeFormatter dateTimepattern;
    private volatile AtomicBoolean pass = new AtomicBoolean(true);
    private ElasticsearchPorterConfig config;
    private BulkProcessor bulkProcessor;
    //index->logType
    private Map<String, String> indexLogTypeMap = new HashMap<>();
    //index->index_data
    private Map<String, String> IndexDataMap = new HashMap<>();

    //index->数量版本
    private Map<String, Integer> indexNumberVsersion = new HashMap<>();

    //index->数量
    private Map<String, Integer> indexCount = new HashMap<>();



    public ElasticsearchPorter(BasePorterConfig config) {
        super(config);
        this.config = (ElasticsearchPorterConfig) config;
//        dateTimepattern = DateTimeFormat.forPattern("YYYY.MM");
        dateTimepattern = DateTimeFormat.forPattern(this.config.getDateTimepattern());

    }

    public ElasticsearchPorter(String workerID, BasePorterConfig config) {
        super(workerID, config);
        this.config = (ElasticsearchPorterConfig) config;
//        dateTimepattern = DateTimeFormat.forPattern("YYYY.MM");
        dateTimepattern = DateTimeFormat.forPattern(this.config.getDateTimepattern());

    }


    @Override
    public boolean register() {
        String host = config.getIp();
        String port = config.getPort();
        client = new RestHighLevelClient(RestClient.builder(new HttpHost(host, Integer.parseInt(port), "http")));

        BulkProcessor.Listener listener = new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest request) {
                String description = request.getDescription();
                String[] indices = description.replaceAll("]", "").split("indices\\[")[1].split(", ");
                for (String index : indices) {
                    createIndexNotExist(index);
                }
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request,
                                  BulkResponse response) {
                pass.set(true);
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request,
                                  Throwable failure) {
                pass.set(false);
                int i = request.numberOfActions();
                System.out.println(i);
                System.out.println(failure.getCause().toString());
                List<DocWriteRequest<?>> requests = request.requests();
                requests.forEach(x -> {
                    IndexRequest indexRequest = (IndexRequest) x;
                    bulkProcessor.add(indexRequest);
                });
            }
        };

        BiConsumer<BulkRequest, ActionListener<BulkResponse>> bulkConsumer =
                (request, bulkListener) ->
                        client.bulkAsync(request, RequestOptions.DEFAULT, bulkListener);
        BulkProcessor.Builder builder =
                BulkProcessor.builder(bulkConsumer, listener);
        builder.setBulkActions(config.getMaxCountFlush()); //当有bulkProcessor有1000条数据的时候flush
        builder.setBulkSize(new ByteSizeValue(config.getMaxSizeFlush(), ByteSizeUnit.MB)); //当数据达到5M的时候flush
        builder.setConcurrentRequests(1); // 运行最大的并发数
        builder.setFlushInterval(TimeValue.timeValueSeconds(config.getMaxTimeFlush())); //5秒钟不管有多少条都刷新一次
//        builder.setBackoffPolicy(BackoffPolicy
//                .constantBackoff(TimeValue.timeValueSeconds(WorkerSettings.FAILURERETRYTIMES.getValue()), 3));
        builder.setBackoffPolicy(BackoffPolicy
                .noBackoff());


        bulkProcessor = builder.build();

        return true;
    }


    @Override
    public void execute() {
        if (pass.get() == true) {
            List<Event> needSendEvents = lv2Cache.poll(WorkerSettings.BATCHSIZE.getValue());
            finishEvent(needSendEvents);
            if (!needSendEvents.isEmpty()) {
                needSendEvents.forEach(event -> {
                    String index = event.getIndex();
                    String index_data_now = composeDateIndex(event);
                    String index_data_oid = IndexDataMap.get(index);
                    if (index_data_oid == null) {
                        //索引第一次来
                        indexNumberVsersion.put(index, 1);
                        indexCount.put(index, 1);
                        IndexDataMap.put(index, index_data_now);
                    } else if (index_data_oid != null && !index_data_oid.equals(index_data_now)) {
                        //日期改变了
                        indexNumberVsersion.put(index, 1);
                        indexCount.put(index, 1);
                        IndexDataMap.put(index, index_data_now);
                    }

                    Integer count = indexCount.get(index);
                    if (++count <= config.getOneIndexMaxCount()) {
                        indexCount.put(index, count);
                    } else {
                        indexCount.put(index, 1);
                        Integer integer = indexNumberVsersion.get(index);
                        indexNumberVsersion.put(index,++integer);
                    }
                    //日期没有改变



                    String finishIndex = index_data_now + "_n" + indexNumberVsersion.get(index);

                    if (!indexLogTypeMap.containsKey(finishIndex)) {
                        indexLogTypeMap.put(finishIndex, event.getLogType());  //todo 后续测试这比例是否有多线程异常。每一批次indexLogTypeMap都为空
                    }
                    IndexRequest indexRequest = new IndexRequest(finishIndex, event.getLogType());
                    indexRequest.source(event.getJsonMessage(), XContentType.JSON);
                    bulkProcessor.add(indexRequest);
                });
            }
        }


    }


    public void finishEvent(List<Event> events) {
        events.forEach(a -> {
            Map<String, Object> format = a.getFormat();
            format.put("Metafield_Type", a.getMetafieldCategory());
            format.put("Metafield_Category", a.getMetafieldCategory());
            format.put("Metafield_Subject", a.getMetafieldSubject());
            format.put("Metafield_Object", a.getMetafieldObject());
            format.put("Metafield_Loglevel", a.getMetafieldLoglevel());
            format.put("Metafield_Source", a.getMetafieldSource());
            format.put("Metafield_Description", a.getMetafieldDescription());
            if (a.getClientIP() != null) {
                format.put("ys_client_ip", a.getClientIP());
            }
        });
    }

    //索引不存在则创建
    public void createIndexNotExist(String index) {
        GetIndexRequest checkIndexExistsRequest = new GetIndexRequest(index);
        try {
            boolean exists = client.indices().exists(checkIndexExistsRequest, RequestOptions.DEFAULT);
            if (!exists) {
                CreateIndexRequest createIndexRequest = new CreateIndexRequest(index);
                createIndexRequest.settings(Settings.builder()
                        .put("index.number_of_shards", WorkerSettings.ESSHARDS.getValue())
                        .put("index.number_of_replicas", WorkerSettings.ESREPLICAS.getValue())
                );
                String mapping = BaseIndexerManager.getIndexMappingJson(indexLogTypeMap.get(index));
                createIndexRequest.mapping(
                        mapping,
                        XContentType.JSON);
                client.indices().create(createIndexRequest, RequestOptions.DEFAULT);

            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            addException(e.getMessage());
        } catch (IllegalAccessException e) {
            LOGGER.error(e.getMessage());
            addException(e.getMessage());
        } catch (InvocationTargetException e) {
            LOGGER.error(e.getMessage());
            addException(e.getMessage());
        } catch (NoSuchMethodException e) {
            LOGGER.error(e.getMessage());
            addException(e.getMessage());
        } finally {
            //不管存在还是不存在都将其删除，防止过多不用的数据。
            indexLogTypeMap.remove(index);
        }

    }


    /*
     * @Author Eric Zheng
     * @Description 构建索引
     * @Date 14:22 2019/8/13
     **/
    public String composeDateIndex(Event e) {

        StringBuffer sb = new StringBuffer();
        if (e.getIndex() != null && !e.getIndex().equals("")) {
            sb.append(e.getIndex());
        } else {
            sb.append("unknown-");
        }
        sb.append("_"+e.getTimestamp().toString(dateTimepattern));
        return sb.toString();
    }

    @Override
    public void tearDown() {
        try {
            client.close();
            bulkProcessor.awaitClose(5L, TimeUnit.SECONDS);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            LOGGER.error("Some requests have not been processed");
            e.printStackTrace();
        }
    }


    public static void main(String args[]) throws InterruptedException, IOException {
        HashMap<String, String> map = new HashMap<>();
        map.put("ip", "172.17.100.1");
        map.put("port", "9200");
        map.put("oneIndexMaxCount", "4");
//        map.put("dateTimepattern", "YYYY.MM.dd.HH.mm.ss");
        map.put("dateTimepattern", "YYYYMMddHHmm");
        map.put("moduleType", "elasticsearch");
        ElasticsearchPorterConfig config = new ElasticsearchPorterConfig(map);
        ElasticsearchPorter porter = new ElasticsearchPorter(config);
        BaseCache cache = CacheFactory.createCache();

        BaseIndexerManager.putMapping("nginx_nginx_success_v1", NginxNginxSuccessV1Indexer.class);
        BaseIndexerManager.putMapping("nginx_nginx_error_v1", NginxNginxErrorV1Indexer.class);
        BaseIndexerManager.putMapping("array_vpn_all_v1", ArrayVpnAllV1Indexer.class);
        porter.setLv2Cache(cache);
        porter.doStart();


        while (true){
            Thread.sleep(10000);
            Event event = new Event();
            event.setIndex("111111111111111111111");
            event.setLogType("nginx_nginx_success_v1");
            event.getFormat().put("a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            cache.put(event);
            Event event1 = new Event();
            event1.setIndex("22222222222222222222222");
            event1.setLogType("nginx_nginx_error_v1");
            event1.getFormat().put("b", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            cache.put(event1);
            Event event2 = new Event();
            event2.setIndex("333333333333333333333333");
            event2.setLogType("array_vpn_all_v1");
            event2.getFormat().put("b", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            cache.put(event2);
        }




    }
}
