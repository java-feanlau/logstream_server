package com.boyitech.logstream.core.info.eneity;

import java.util.HashMap;
import java.util.Map;

/**
 * @author juzheng
 * @Title: IndexerInfosResult
 * @date 2019/8/16 10:05 AM
 * @Description:  返回Indexer基本信息的实体类
 */
public class IndexerInfosResult {
    public String vendor;
    public String application_name;
    public String indexer_version;
    public String log_style;
    public String[] tags;

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getApplication_name() {
        return application_name;
    }

    public void setApplication_name(String application_name) {
        this.application_name = application_name;
    }

    public String getIndexer_version() {
        return indexer_version;
    }

    public void setIndexer_version(String indexer_version) {
        this.indexer_version = indexer_version;
    }

    public String getLog_style() {
        return log_style;
    }

    public void setLog_style(String log_style) {
        this.log_style = log_style;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }




}
