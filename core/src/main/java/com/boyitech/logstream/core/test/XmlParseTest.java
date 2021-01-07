package com.boyitech.logstream.core.test;

import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.IndexHelper;
import org.joda.time.DateTime;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XmlParseTest {

	public static void main(String args[]) throws ParserConfigurationException, SAXException, IOException {
		ArrayList<String> list = new ArrayList();
		list.add("<Event xmlns='http://schemas.microsoft.com/win/2004/08/events/event'><System><Provider Name='rt640x64'/><EventID Qualifiers='32768'>1</EventID><Level>3</Level><Task>0</Task><Keywords>0x80000000000000</Keywords><TimeCreated SystemTime='2018-06-08T00:44:31.238135300Z'/><EventRecordID>749</EventRecordID><Channel>System</Channel><Computer>Night-15</Computer><Security/></System><EventData><Data>\\Device\\NDMP5</Data><Data>Realtek PCIe GBE Family Controller</Data><Binary>00000000020030000000000001000080000000000000000000000000000000000000000000000000</Binary></EventData><RenderingInfo Culture='zh-CN'><Message>Realtek PCIe GBE Family Controller is disconnected from network.</Message><Level>警告</Level><Task></Task><Opcode></Opcode><Channel></Channel><Provider></Provider><Keywords><Keyword>经典</Keyword></Keywords></RenderingInfo></Event>");
		list.add("<Event xmlns='http://schemas.microsoft.com/win/2004/08/events/event'><System><Provider Name='Microsoft-Windows-EventSystem' Guid='{899daace-4868-4295-afcd-9eb8fb497561}' EventSourceName='EventSystem'/><EventID Qualifiers='16384'>4625</EventID><Version>0</Version><Level>4</Level><Task>0</Task><Opcode>0</Opcode><Keywords>0x80000000000000</Keywords><TimeCreated SystemTime='2018-06-05T04:33:03.944725300Z'/><EventRecordID>1</EventRecordID><Correlation/><Execution ProcessID='0' ThreadID='0'/><Channel>Application</Channel><Computer>NIGHT-15</Computer><Security/></System><EventData><Data Name='param1'>86400</Data><Data Name='param2'>SuppressDuplicateDuration</Data><Data Name='param3'>Software\\Microsoft\\EventSystem\\EventLog</Data></EventData><RenderingInfo Culture='zh-CN'><Message>EventSystem 子系统正在取�? 86400 秒持续时间内重复的事件日志项。可以�?�过下列注册表项下名�? SuppressDuplicateDuration �? REG_DWORD 值控制取消超�?: HKLM\\Software\\Microsoft\\EventSystem\\EventLog�?</Message><Level>信息</Level><Task></Task><Opcode></Opcode><Channel></Channel><Provider>Microsoft-Windows-EventSystem</Provider><Keywords><Keyword>经典</Keyword></Keywords></RenderingInfo></Event>\r\n");
//		EventLogIndexer indexer = new EventLogIndexer(null);
//		for(String message : list) {
//			Event e = new Event();
//			e.setMessage(message);
//			indexer.register();
//			indexer.format(e);
//			System.out.println(e.getFormat());
//		}
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	    String message = list.get(1);
	    Document doc = null;
		File file = new File("C:\\Users\\Eric\\Desktop\\172.20.3.0_1.xml");
		FileInputStream fs = new FileInputStream(file);
		int available = fs.available();
		byte[] aByte =new byte[available];
		fs.read(aByte);
		InputStream is = new ByteArrayInputStream(aByte);
		doc = dBuilder.parse(is);

		Element root = doc.getDocumentElement();
		if(!"Event".equals(root.getNodeName())) {
			System.out.println("根节点错误： " + message);
		}
		Map formated = new HashMap();
		NodeList targetList = root.getElementsByTagName("System");
		System.out.println(targetList.getLength());
		Node systemNode = targetList.item(0);
		formated = parseNode(null, systemNode, null);

		 targetList = root.getElementsByTagName("EventData");
		Node event = targetList.item(0);
		formated = parseEventDataNode(event, formated);
		DateTime dt = DateTime.parse(formated.get("System.TimeCreatedSystemTime").toString(), IndexHelper.EventLogDateTimeFormateer);
		formated.put("@timestamp", dt.toString());
		System.out.println(GsonHelper.toJson(formated));
	}

	public static Map parseEventDataNode(Node node, Map result) {
		if(!node.getNodeName().equals("EventData")) {
			System.out.println("parseEventDataNode方法中nodeType必须为EventData");
			return null;
		}
		if(result==null)
			result = new HashMap();
		ArrayList<Map> list = new ArrayList<Map>();
		NodeList children = node.getChildNodes();
		System.out.println("子节点： " + children.getLength());
		for(int i=0;i<children.getLength();i++) {
			Map nodeInfo = new HashMap();
			Node child = children.item(i);
			System.out.println(child.getNodeName());
			if(child.getNodeName().equals("Data") || child.getNodeName().equals("Binary")) {
				// 获取属�?�节�?
				NamedNodeMap attrs = child.getAttributes();
				System.out.println("属�?�节点： " + attrs.getLength());
				for(int j=0;j<attrs.getLength();j++) {
					Node attr = attrs.item(j);
					nodeInfo.put("Event"+child.getNodeName()+attr.getNodeName(), attr.getNodeValue());
				}
				// 获取文本节点
				Node textNode = child.getFirstChild();
				nodeInfo.put("Event"+child.getNodeName()+"Value", textNode.getNodeValue());
			}else {
				System.out.println("parseEventDataNode获取到错误的节点名称<" + child.getNodeName() + ">");
			}
			list.add(nodeInfo);
		}
		result.put("EventData", list);
		return result;
	}

	public static Map parseNode(String prefix, Node node, Map result) {
		if(result==null)
			result = new HashMap();
		String name;
		if(prefix==null) {
			name = node.getNodeName();
		}else {
			name = prefix + "." + node.getNodeName();
		}
		// 如果node是文本节�?
		if(node.getNodeType()==Node.TEXT_NODE) {
			result.put(prefix, node.getNodeValue());
			return result;
		}
		// 解析自身节点属�??
		NamedNodeMap attributes = node.getAttributes();
		if(attributes!=null) {
			for(int i=0;i<attributes.getLength();i++) {
				Node attr = attributes.item(i);
				result.put(name + attr.getNodeName(), attr.getNodeValue());
			}
		}
		// 解析子节�?
		NodeList childrenList = node.getChildNodes();
		int count = childrenList.getLength();
		for(int i=0;i<count;i++) {
			Node child = childrenList.item(i);
			if(child.getNodeType()==Node.TEXT_NODE) {
				if(result.containsKey(name)) {
					Object value = result.get(name);
					if(value.getClass()==(ArrayList.class)) {
						List c = (List)value;
						c.add(child.getNodeValue());
					}else {
						List list = new ArrayList();
						list.add(value);
						list.add(child.getNodeValue());
						result.put(name, list);
					}
				}else {
					result.put(name, child.getNodeValue());
				}
			}else {
				result.putAll(parseNode(name, child, result));
			}
		}
		return result;
	}


}
