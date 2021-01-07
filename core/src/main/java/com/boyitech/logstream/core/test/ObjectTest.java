package com.boyitech.logstream.core.test;

import com.boyitech.logstream.core.util.CharacterHelper;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.shipper.BaseShipper;
import com.boyitech.logstream.core.worker.shipper.BaseShipperConfig;
import com.boyitech.logstream.core.worker.shipper.file.FileShipper;
import com.boyitech.logstream.core.worker.shipper.file.FileShipperConfig;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Eric
 * @Title: ObjectTest
 * @date 2019/1/29 10:08
 * @Description: TODO
 */
public class ObjectTest {
    private static final String SHIPPERPACKAGE = "com.boyitech.logstream.core.worker.shipper";
    private static final String INDEXERPACKAGE = "com.boyitech.logstream.worker.indexer";

    public static void main(String args[]) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        String className = SHIPPERPACKAGE + "." + "file" + "." + CharacterHelper.captureName(CharacterHelper.UnderlineToHump("file")) + "Shipper";
        Class c = Class.forName(className);
        System.out.println(className);
        Constructor constructor = c.getConstructor(BaseWorkerConfig.class);
        Map config = new HashMap<>();
        config.put("moduleType","file");
        BaseShipperConfig cong = new FileShipperConfig(config);
        BaseShipper indexer = (BaseShipper) constructor.newInstance(cong);

        FileShipper fileShipper = (FileShipper) indexer;




    }
}
