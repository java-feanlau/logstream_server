package com.boyitech.logstream.core.manager.porter;

import com.boyitech.logstream.core.cache.BaseCache;
import com.boyitech.logstream.core.manager.WorkerManager;
import com.boyitech.logstream.core.worker.porter.BasePorter;

public interface PorterManager extends WorkerManager {

    /**
     * 根据配置创建新的porter
     * @param config
     * @return
     */
   public BasePorter createPorter(String config, BaseCache lv2cache, BaseCache lv3cache);

    /**
     * 根据配置更新已有porter
     * @param config
     * @return
     */
 //   public BasePorter updatePorter(BaseLogStreamConfig config);

}
