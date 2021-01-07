package com.boyitech.logstream.core.factory;

import com.boyitech.logstream.core.util.CharacterHelper;
import com.boyitech.logstream.core.worker.indexer.BaseIndexerConfig;
import com.boyitech.logstream.core.worker.porter.BasePorterConfig;
import com.boyitech.logstream.core.worker.porter.elasticsearch.ElasticsearchPorterConfig;
import com.boyitech.logstream.core.worker.porter.syslog.SyslogPorterConfig;
import com.boyitech.logstream.core.worker.shipper.BaseShipperConfig;

import java.lang.reflect.Constructor;
import java.util.Map;

//import com.boyi.logstream.core.worker.shipper.file.FileTailerConfig;

public class ConfigFactory {

    private static final String SHIPPERPACKAGE = "com.boyitech.logstream.core.worker.shipper";

    public static BaseShipperConfig buildShipperConfig(Map configMap) throws Exception {
        BaseShipperConfig config = null;
        String moduleType = null;
        try {
            moduleType = configMap.get("moduleType").toString().toLowerCase();
        } catch (NullPointerException e) {
            throw e;
        }
        Class c = null;
        try {
            String className = SHIPPERPACKAGE + "." + moduleType + "." + CharacterHelper.captureName(CharacterHelper.UnderlineToHump(moduleType)) + "ShipperConfig";
            c = Class.forName(className);
        } catch (ClassNotFoundException e) {
                throw e;

        }
        Constructor constructor = c.getConstructor(Map.class);
        try {
            config = (BaseShipperConfig) constructor.newInstance(configMap);
        }catch (Exception e){
            throw e;
        }

        return config;

//        try {
//            switch (moduleType.toLowerCase()) {
//                case "tcp_udp":
//                case "redis":
//                    config = new RedisShipperConfig(configMap);
//                    break;
//                case "file":
////				    config = new FileTailerConfig(configMap);
//                    config = new FileTailerConfig(configMap);
//                    break;
//                case "upload_file":
//                    config = new UploadFileConfig(configMap);
//                    break;
//                case "snmp_trap":
//                    config = new SNMPTrapConfig(configMap);
//                    break;
//                case "event_log":
//                    config = new EventLogShipperConfig(configMap);
//                    break;
//                case "netflow":
//                    config = new NetflowConfig(configMap);
//                    break;
//                case "syslog":
//                    config = new SyslogShipperConfig(configMap);
//                    break;
//                case "kepware":
//                    config = new KepwareConfig(configMap);
//                    break;
//
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }

    public static BaseIndexerConfig buildIndexerConfig(Map configMap) {
        BaseIndexerConfig config = null;

        return config;
    }

    public static BasePorterConfig buildPorterConfig(Map configMap) {
        BasePorterConfig config = null;
        try {
            switch ((String) configMap.get("moduleType")) {
                case "elasticsearch":
                    config = new ElasticsearchPorterConfig(configMap);
                    break;
                case "syslog":
                    config = new SyslogPorterConfig(configMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return config;
    }

//	public static BaseOutputConfig buildOutputConfig(Map c) {
//		BaseOutputConfig config = null;
//		try {
//			switch((String)c.get("type")) {
//			case "elasticsearch":
//				config = new ElasticsearchTransportPorterConfig(c);
//				break;
//			}
//		}catch(Exception e) {
//			e.printStackTrace();
//		}
//		return config;
//	}

}
