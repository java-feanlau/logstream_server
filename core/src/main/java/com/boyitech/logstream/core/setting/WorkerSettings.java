package com.boyitech.logstream.core.setting;

public class WorkerSettings extends BaseSettings {

	public static final Setting<Integer> BATCHSIZE = Setting.integerSetting("porter.batch.size", 1000);
	public static final Setting<Integer> FAILURERETRYTIMES = Setting.integerSetting("porter.failure.retry_times", 2);
	public static final Setting<Integer> RECONNECTINTERVAL = Setting.integerSetting("porter.reconnet.interval", 1000);

	public static final Setting<Integer> ESSHARDS = Setting.integerSetting("porter.es.shards", 12);
	public static final Setting<Integer> ESREPLICAS = Setting.integerSetting("porter.es.replicas", 0);


	public static final Setting<Integer> REDISSHIPPERCONNECTMAXTIME = Setting.integerSetting("shipper.redis.wait.time", 30000);



}
