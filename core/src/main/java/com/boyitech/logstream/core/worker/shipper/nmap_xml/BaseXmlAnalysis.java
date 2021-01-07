package com.boyitech.logstream.core.worker.shipper.nmap_xml;

import java.io.File;
import java.util.List;

/**
 * @author Eric
 * @Title: BaseXmlAnalysis
 * @date 2019/1/28 17:06
 * @Description: TODO
 */
public interface BaseXmlAnalysis {
    public List<String> analysis(File file);
}
