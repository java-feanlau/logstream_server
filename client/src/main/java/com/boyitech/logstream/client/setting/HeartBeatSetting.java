package com.boyitech.logstream.client.setting;

        import com.boyitech.logstream.core.setting.Setting;

/**
 * @author Eric
 * @Title: HeartBeatSetting
 * @date 2019/2/22 9:47
 * @Description: TODO
 */
public class HeartBeatSetting {
    public static final Setting<String> maxLoseHeartbeatNumber = Setting.stringSetting("client.heartbeat.maxLoseHeartbeatNumber", "10");
    public static final Setting<String> heartBeatTime = Setting.stringSetting("client.heartbeat.heartBeatTime", "5000");
    public static final Setting<String> updateConfigTime = Setting.stringSetting("client.heartbeat.updateConfigTime", "60000");


}
