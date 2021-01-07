package com.boyitech.logstream.core.worker.shipper;

import com.boyitech.logstream.core.setting.Setting;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;

import java.util.Map;

public class BaseShipperConfig extends BaseWorkerConfig {
    protected String moduleType;
    protected boolean changeLogType = false;
    protected String index;
    protected boolean changeIndex = false;
    protected int batchSize;
    protected boolean multiline = false;
    protected Map<String, String> multilineRule = null;

    private static final Setting<Integer> DEFAULTBATCHSIZE = Setting.integerSetting("shipper.batch.size", 1000);

    public BaseShipperConfig(Map config) {
        super(config);
        if (config.get("moduleType") != null && config.get("moduleType") != "") {
            this.moduleType = config.get("moduleType").toString();
        } else {
            throw new RuntimeException("配置必须包含moduleType字段");
        }


        if (config.get("index") != null) {
            try {
                this.index = config.get("index").toString();
                this.changeIndex = true;
            } catch (Exception e) {
                LOGGER.debug("解析index字段失败", e);
                this.changeIndex = false;
            }
        } else {
            throw new RuntimeException("配置必须包含index字段");
        }
        if (config.get("batchSize") != null) {
            try {
                this.batchSize = Double.valueOf(config.get("batchSize").toString()).intValue();
            } catch (Exception e) {
                LOGGER.debug("解析batchSize字段失败,使用默认值", e);
                this.batchSize = DEFAULTBATCHSIZE.getValue();
            }
        } else {
            this.batchSize = DEFAULTBATCHSIZE.getValue();
        }
        if (config.get("multiline") != null) {
            try {
                this.multiline = Boolean.parseBoolean(config.get("multiline").toString());
            } catch (Exception e) {
                LOGGER.debug("解析multiline字段失败,使用默认值", e);
                this.multiline = false;
            }
        }
        if (isMultiline()) {
            //从配置中解析多行合并规则
            multilineRule = (Map) config.get("multiline_rule");
        }
    }


    public boolean isChangeLogType() {
        return changeLogType;
    }

    public String getIndex() {
        return index;
    }

    public boolean isChangeIndex() {
        return changeIndex;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public boolean isMultiline() {
        return multiline;
    }

    public Map<String, String> getMultilineRule() {
        return multilineRule;
    }

    public String getModuleType() {
        return moduleType;
    }

}
