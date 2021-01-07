package com.boyitech.logstream.core.worker.shipper.nmap_xml;

import com.alibaba.fastjson.JSON;
import com.boyitech.logstream.core.worker.shipper.nmap_xml.info.Nmap;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @author Eric
 * @Title: Xml2JsonTest
 * @date 2019/1/25 13:36
 * @Description: TODO
 */
public class NmapAnalysis implements BaseXmlAnalysis{


    public List<String> analysis(File file) {
        List<String> strings = new ArrayList<>();
        //读取xml，封装对象
        SAXReader reader = new SAXReader();
        Document doc = null;
        try {
            doc = reader.read(file);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        Element root = doc.getRootElement();
        String version = root.attribute("version").getText();
        String arg = root.attribute("args").getText();
        Iterator<Element> hosts = root.elementIterator("host");
        while (hosts.hasNext()) {
            Element host = hosts.next();
            String starttime = host.attribute("starttime").getText() + "000";
            String endtime = host.attribute("endtime").getText() + "000";
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
            String formatStartTime = sdf.format(new Date(Long.valueOf(starttime)));
            String formatEndTime = sdf.format(new Date(Long.valueOf(endtime)));
            //status
            Element status = host.element("status");
            String hostState = status.attribute("state").getText();
            String reason = status.attribute("reason").getText();
            String reason_ttl = status.attribute("reason_ttl").getText();
            status.attribute("reason_ttl").getText();
            //address
            Iterator<Element> addresses = host.elementIterator("address");
            String macHost = null;
            String vendor = null;
            String ipv4Host = null;
            while (addresses.hasNext()) {
                Element address = addresses.next();
                String addrtype = address.attribute("addrtype").getText();
                if (addrtype.equals("ipv4")) {
                    ipv4Host = address.attribute("addr").getText();
                } else {
                    macHost = address.attribute("addr").getText();
                    vendor = address.attribute("vendor") != null ? address.attribute("vendor").getText() : null;
                }
            }

            Element runstats = root.element("runstats");
            Element finished = runstats.element("finished");
            String elapsed = finished.attribute("elapsed").getText();
            String summary = finished.attribute("summary").getText();
            String exit = finished.attribute("exit").getText();

            //ports
            Element portsAll = host.element("ports");
            Iterator<Element> ports = portsAll.elementIterator("port");
            while (ports.hasNext()) {
                Nmap nmap = new Nmap();
                Element port = ports.next();
                //  System.out.println(port);
                String protocol = port.attribute("protocol").getText();
                String portid = port.attribute("portid").getText();
                Element state = port.element("state");
                String portState = state.attribute("state").getText();
                String portreason = state.attribute("reason").getText();
                String portReasonTtl = state.attribute("reason_ttl").getText();
                Element service = port.element("service");
                String name = service.attribute("name").getText();
                String method = service.attribute("method").getText();
                String conf = service.attribute("conf").getText();
                String product = service.attribute("product") != null ? service.attribute("product").getText() : null;

                nmap.setProtocol(protocol);
                nmap.setPort(portid);
                nmap.setFormatEndTime(formatEndTime);
                nmap.setFormatStartTime(formatStartTime);
                nmap.setHostState(hostState);
                nmap.setReason(reason);
                nmap.setReasonTtl(reason_ttl);
                nmap.setMacHost(macHost);
                nmap.setVendor(vendor);
                nmap.setIpv4Host(ipv4Host);
                nmap.setVersion(version);
                nmap.setArg(arg);
                nmap.setPortState(portState);
                nmap.setPortreason(portreason);
                nmap.setPortReasonTtl(portReasonTtl);
                nmap.setName(name);
                nmap.setConf(conf);
                nmap.setMethod(method);
                nmap.setProduct(product);
                nmap.setElapsed(elapsed);
                nmap.setSummary(summary);
                nmap.setExit(exit);
                strings.add(JSON.toJSONString(nmap));
            }
        }
        return strings;
    }






}
