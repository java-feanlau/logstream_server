package com.boyitech.logstream.core.factory;

import com.boyitech.logstream.core.util.CharacterHelper;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import com.boyitech.logstream.core.worker.indexer.BaseIndexerConfig;
import com.boyitech.logstream.core.worker.porter.BasePorter;
import com.boyitech.logstream.core.worker.porter.BasePorterConfig;
import com.boyitech.logstream.core.worker.shipper.BaseShipper;
import com.boyitech.logstream.core.worker.shipper.BaseShipperConfig;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

//import com.boyi.logstream.core.worker.shipper.file.FileTailerShipper;

public class WorkerFactory {

    private static final String INDEXERPACKAGE = "com.boyitech.logstream.worker.indexer";
    private static final String SHIPPERPACKAGE = "com.boyitech.logstream.core.worker.shipper";
    private static final String PORTERPACKAGE = "com.boyitech.logstream.core.worker.porter";

    public static BaseShipper createShipperWorker(BaseShipperConfig config) throws Exception {
        BaseShipper worker = null;
        Class c = null;
        String module = config.getModuleType();
        try {
            String className = SHIPPERPACKAGE + "." + module + "." + CharacterHelper.captureName(CharacterHelper.UnderlineToHump(module)) + "Shipper";
            c = Class.forName(className);
        } catch (ClassNotFoundException e) {
                throw e;

        }
        Constructor constructor = c.getConstructor(BaseShipperConfig.class);
        worker = (BaseShipper) constructor.newInstance(config);

        return worker;

    }

    public static BaseShipper createShipperWorker(String workerID, BaseShipperConfig config) throws Exception {
        BaseShipper worker = null;
        try {
            String module = config.getModuleType();
            String className = SHIPPERPACKAGE + "." + module + "." + CharacterHelper.captureName(CharacterHelper.UnderlineToHump(module)) + "Shipper";
            Class c = Class.forName(className);
            Constructor constructor = c.getConstructor(String.class, BaseShipperConfig.class);
            worker = (BaseShipper) constructor.newInstance(workerID, config);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("无法获取配置中对应类型的worker");
        }
        return worker;
    }

    public static BaseIndexer createIndexerWorker(BaseIndexerConfig config) throws NoSuchMethodException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        String logType = config.getLogType();
        String className = INDEXERPACKAGE + "." + CharacterHelper.captureName(CharacterHelper.UnderlineToHump(logType)) + "Indexer";
        Class c = Class.forName(className);
        Constructor constructor = c.getConstructor(BaseWorkerConfig.class);
        BaseIndexer indexer = (BaseIndexer) constructor.newInstance(config);
        return indexer;
    }

    public static BaseIndexer createIndexerWorker(String workerID, BaseIndexerConfig config) throws NoSuchMethodException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        String logType = config.getLogType();
        String className = INDEXERPACKAGE + "." + CharacterHelper.captureName(CharacterHelper.UnderlineToHump(logType)) + "Indexer";
        Class c = Class.forName(className);
        Constructor constructor = c.getConstructor(String.class, BaseWorkerConfig.class);
        BaseIndexer indexer = (BaseIndexer) constructor.newInstance(workerID, config);
        return indexer;
    }


    public static BasePorter createPorterWorker(BasePorterConfig config) throws Exception {
        BasePorter worker = null;
        try {
            String module = config.getModuleType();
            String className = PORTERPACKAGE + "." + module + "." + CharacterHelper.captureName(CharacterHelper.UnderlineToHump(module)) + "Porter";
            Class c = Class.forName(className);
            Constructor constructor = c.getConstructor(BasePorterConfig.class);
            worker = (BasePorter) constructor.newInstance(config);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("无法获取配置中对应类型的worker");
        }
        return worker;
    }

    public static BasePorter createPorterWorker(String workerID, BasePorterConfig config) throws Exception {
        BasePorter worker = null;
        try {
            String module = config.getModuleType();
            String className = PORTERPACKAGE + "." + module + "." + CharacterHelper.captureName(CharacterHelper.UnderlineToHump(module)) + "Porter";
            Class c = Class.forName(className);
            Constructor constructor = c.getConstructor(String.class, BasePorterConfig.class);
            worker = (BasePorter) constructor.newInstance(workerID, config);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("无法获取配置中对应类型的worker");
        }
        return worker;
    }
}
