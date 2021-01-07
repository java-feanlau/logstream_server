package com.boyitech.logstream.worker.indexer;

import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.IndexHelper;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import org.joda.time.DateTime;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MicrosoftWindowsAllV1Indexer extends BaseIndexer{

	private DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    private DocumentBuilder dBuilder;
	private static final Map<String, String> xmlMap = new HashMap<String, String>();

	public MicrosoftWindowsAllV1Indexer(BaseWorkerConfig config) {
		super(config);
	}

	public MicrosoftWindowsAllV1Indexer(String indexerID, BaseWorkerConfig config) {
		super(indexerID, config);
	}

	@Override
	public boolean register() {
        try {
			dBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			LOGGER.error("EventLogIndexer register失败", e);
			return false;
		}
		return true;
	}

	@Override
	public void tearDown() {

	}

	@Override
	public boolean format(Event event) {

		String message = event.getMessage();
		Map formated = event.getFormat();
		if(message == null){
			formated.put("flag","解析失败，message为空");
			return false;
		}
		List<String> failure = new ArrayList<String>();
		formated.put("message", message);
		Document doc = null;
		InputStream is = new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8));
		try {
			doc = dBuilder.parse(is);
		} catch (SAXException | IOException e) {
			LOGGER.debug("解析 -- " + message + "-- 失败") ;
			return false;
		}
		Element root = doc.getDocumentElement();
		if(!"Event".equals(root.getNodeName())) {
			LOGGER.error("根节点错误： " + message);
			return false;
		}
		try {
			// 处理System节点
			NodeList targetSystemList = root.getElementsByTagName("System");
			Node systemNode = targetSystemList.item(0);
			formated = parseNode(null, systemNode, formated);
			// 处理RenderingInfo节点
			NodeList targetRenderingInfoList = root.getElementsByTagName("RenderingInfo");
			if(targetRenderingInfoList.getLength()>0) {
				Node renderingInfoNode = targetRenderingInfoList.item(0);
				formated = parseNode(null, renderingInfoNode, formated);
			}else {
				failure.add("no_RenderingInfo");
			}
			// 处理EventData节点
			NodeList targetEventDataList = root.getElementsByTagName("EventData");
			if(targetEventDataList.getLength()>0) {
				Node eventDataNode = targetEventDataList.item(0);
				formated = parseEventDataNode(eventDataNode, formated);
			}else {
				failure.add("no_EventData");
			}
			// 判断是否有UserData节点
			 //time:2:58 PM 2019/11/29新增：处理UserData节点
			NodeList targetUserDataList = root.getElementsByTagName("UserData");
			if(targetUserDataList.getLength()>0) {
				failure.add("has_UserData_Node");
				int length=targetUserDataList.item(0).getFirstChild().getChildNodes().getLength();
				for(int i=0;i<length;i++){
					formated.put("UserData."+targetUserDataList.item(0).getFirstChild().getChildNodes().item(i).getNodeName(),targetUserDataList.item(0).getFirstChild().getChildNodes().item(i).getTextContent());
				}
			}
		}catch (Exception e) {
			LOGGER.error(message, e);
			throw e;
		}
		if(formated.get("Computer")!=null){
			event.setMetafieldSource((String) formated.get("Computer"));
		}
		try {
			DateTime dt = DateTime.parse(formated.get("SystemTime").toString(), IndexHelper.EventLogDateTimeFormateer);
			formated.put("@timestamp", dt.toString());
		} catch(Exception e) {
			failure.add("timestamp_parse_failed");
			formated.put("@timestamp", event.getReceivedAt().toString());
		}
		if(formated.get("Level")!=null) {
			event.setMetafieldLoglevel((String) formated.get("Level"));
		}

		if(event.getClientIP()!=null){
			event.setMetafieldSubject(event.getClientIP());
			event.setMetafieldObject(event.getClientIP());
		}
		if(event.getMetafieldCategory()==null){
			event.setMetafieldCategory("Windows采集");
		}


		formated.put("tag", failure);
		return true;
	}

	public Map parseEventDataNode(Node node, Map result) {
		if(!node.getNodeName().equals("EventData")) {
			LOGGER.error("parseEventDataNode方法中nodeType必须为EventData");
			return null;
		}
		if(result==null)
			result = new HashMap();
		ArrayList<Map> list = new ArrayList<Map>();
		NodeList children = node.getChildNodes();
		for(int i=0;i<children.getLength();i++) {
			Map nodeInfo = new HashMap();
			Node child = children.item(i);
			if(child.getNodeName().equals("Data") || child.getNodeName().equals("Binary") || child.getNodeName().equals("ComplexData")) {
				// 获取属性节点
				NamedNodeMap attrs = child.getAttributes();
				for(int j=0;j<attrs.getLength();j++) {
					Node attr = attrs.item(j);
					nodeInfo.put("Event"+child.getNodeName()+attr.getNodeName(), attr.getNodeValue());
				}
				// 获取文本节点
				Node textNode = child.getFirstChild();
				if(textNode!=null)
					nodeInfo.put("Event"+child.getNodeName()+"Value", textNode.getNodeValue());
			}else {
				LOGGER.error("parseEventDataNode获取到错误的节点名称<" + child.getNodeName() + ">");
			}
			if(nodeInfo.size() > 0) {
                list.add(nodeInfo);
                if(String.valueOf(nodeInfo.get("EventDataName")).equals( "IpAddress")){
                    result.put("IpAddress",nodeInfo.get("EventDataValue"));
                }
				if(String.valueOf(nodeInfo.get("EventDataName")).equals( "ProcessId")){
					result.put("ProcessId",nodeInfo.get("EventDataValue"));
				}
            }
		}
		result.put("EventData", list);
		return result;
	}

	public Map parseNode(String prefix, Node node, Map result) {
		if(result==null)
			result = new HashMap();
		String name;
		if(prefix==null) {
			name = node.getNodeName();
		}else {
			name = prefix + "." + node.getNodeName();
		}
		// 如果node是文本节点
		if(node.getNodeType()==Node.TEXT_NODE) {
			specialPut(result, translateKey(prefix), node.getNodeValue());
			return result;
		}
		// 解析自身节点属性
		NamedNodeMap attributes = node.getAttributes();
		if(attributes!=null) {
			for(int i=0;i<attributes.getLength();i++) {
				Node attr = attributes.item(i);
				specialPut(result, translateKey(name + attr.getNodeName()), attr.getNodeValue());
			}
		}
		// 解析子节点
		NodeList childrenList = node.getChildNodes();
		int count = childrenList.getLength();
		for(int i=0;i<count;i++) {
			Node child = childrenList.item(i);
			parseNode(name, child, result);
		}
		return result;
	}

	private Map specialPut(Map map, String key, Object value) {
		if(map==null) {
			map = new HashMap();
		}
		if(map.containsKey(key)) {
			Object v = map.get(key);
			if(v.getClass().equals(ArrayList.class)) {
				ArrayList arrayList = (ArrayList) v ;
				arrayList.add(value);
			}else {
				ArrayList arrayList = new ArrayList();
				arrayList.add(v);
				arrayList.add(value);
				map.put(key, arrayList);
			}
		}else {
			map.put(key, value);
		}
		return map;
	}

	private static String translateKey(String key) {
		String k = key;
		if(xmlMap.containsKey(k)) {
			k = xmlMap.get(k);
		}else {
			System.out.println("unknown key : " + key);
		}
		return k==null? key : k;
	}

	public static Map getMapping() {
		String mapping = " {" +
				"        \"properties\" : {" +
				"          \"@timestamp\" : {" +
				"            \"type\" : \"date\"," +
				"            \"format\" : \"dateOptionalTime\"" +
				"          }," +
				"          \"Channel\" : {" +
				"            \"type\" : \"text\"," +
				"            \"fields\" : {" +
				"              \"keyword\" : {" +
				"                \"type\" : \"keyword\"," +
				"                \"ignore_above\" : 256" +
				"              }" +
				"            }" +
				"          }," +
				"          \"ChannelDescription\" : {" +
				"            \"type\" : \"text\"," +
				"            \"fields\" : {" +
				"              \"keyword\" : {" +
				"                \"type\" : \"keyword\"," +
				"                \"ignore_above\" : 256" +
				"              }" +
				"            }" +
				"          }," +
				"          \"Computer\" : {" +
				"            \"type\" : \"text\"," +
				"            \"fields\" : {" +
				"              \"keyword\" : {" +
				"                \"type\" : \"keyword\"," +
				"                \"ignore_above\" : 256" +
				"              }" +
				"            }" +
				"          }," +
				"          \"CorrelationActivityID\" : {" +
				"            \"type\" : \"text\"," +
				"            \"fields\" : {" +
				"              \"keyword\" : {" +
				"                \"type\" : \"keyword\"," +
				"                \"ignore_above\" : 256" +
				"              }" +
				"            }" +
				"          }," +
				"          \"EventData\" : {" +
				"            \"properties\" : {" +
				"              \"EventBinaryValue\" : {" +
				"                \"type\" : \"text\"," +
				"                \"fields\" : {" +
				"                  \"keyword\" : {" +
				"                    \"type\" : \"keyword\"," +
				"                    \"ignore_above\" : 256" +
				"                  }" +
				"                }" +
				"              }," +
				"              \"EventDataName\" : {" +
				"                \"type\" : \"text\"," +
				"                \"fields\" : {" +
				"                  \"keyword\" : {" +
				"                    \"type\" : \"keyword\"," +
				"                    \"ignore_above\" : 256" +
				"                  }" +
				"                }" +
				"              }," +
				"              \"EventDataValue\" : {" +
				"                \"type\" : \"text\"," +
				"                \"fields\" : {" +
				"                  \"keyword\" : {" +
				"                    \"type\" : \"keyword\"," +
				"                    \"ignore_above\" : 256" +
				"                  }" +
				"                }" +
				"              }" +
				"            }" +
				"          }," +
				"          \"EventID\" : {" +
				"            \"type\" : \"keyword\"," +
				"            \"fields\" : {" +
				"              \"keyword\" : {" +
				"                \"type\" : \"keyword\"," +
				"                \"ignore_above\" : 256" +
				"              }" +
				"            }" +
				"          }," +
				"          \"EventRecordID\" : {" +
				"            \"type\" : \"text\"," +
				"            \"fields\" : {" +
				"              \"keyword\" : {" +
				"                \"type\" : \"keyword\"," +
				"                \"ignore_above\" : 256" +
				"              }" +
				"            }" +
				"          }," +
				"          \"EvtMessage\" : {" +
				"            \"type\" : \"text\"," +
				"            \"fields\" : {" +
				"              \"keyword\" : {" +
				"                \"type\" : \"keyword\"," +
				"                \"ignore_above\" : 256" +
				"              }" +
				"            }" +
				"          }," +
				"          \"KeywordsDescription\" : {" +
				"            \"type\" : \"text\"," +
				"            \"fields\" : {" +
				"              \"keyword\" : {" +
				"                \"type\" : \"keyword\"," +
				"                \"ignore_above\" : 256" +
				"              }" +
				"            }" +
				"          }," +
				"          \"Level\" : {" +
				"            \"type\" : \"text\"," +
				"            \"fields\" : {" +
				"              \"keyword\" : {" +
				"                \"type\" : \"keyword\"," +
				"                \"ignore_above\" : 256" +
				"              }" +
				"            }" +
				"          }," +
				"          \"LevelDescription\" : {" +
				"            \"type\" : \"text\"," +
				"            \"fields\" : {" +
				"              \"keyword\" : {" +
				"                \"type\" : \"keyword\"," +
				"                \"ignore_above\" : 256" +
				"              }" +
				"            }" +
				"          }," +
				"          \"Opcode\" : {" +
				"            \"type\" : \"text\"," +
				"            \"fields\" : {" +
				"              \"keyword\" : {" +
				"                \"type\" : \"keyword\"," +
				"                \"ignore_above\" : 256" +
				"              }" +
				"            }" +
				"          }," +
				"          \"OpcodeDescription\" : {" +
				"            \"type\" : \"text\"," +
				"            \"fields\" : {" +
				"              \"keyword\" : {" +
				"                \"type\" : \"keyword\"," +
				"                \"ignore_above\" : 256" +
				"              }" +
				"            }" +
				"          }," +
				"          \"ProcessID\" : {" +
				"            \"type\" : \"text\"," +
				"            \"fields\" : {" +
				"              \"keyword\" : {" +
				"                \"type\" : \"keyword\"," +
				"                \"ignore_above\" : 256" +
				"              }" +
				"            }" +
				"          }," +
				"          \"ProviderDescription\" : {" +
				"            \"type\" : \"text\"," +
				"            \"fields\" : {" +
				"              \"keyword\" : {" +
				"                \"type\" : \"keyword\"," +
				"                \"ignore_above\" : 256" +
				"              }" +
				"            }" +
				"          }," +
				"          \"ProviderEventSourceName\" : {" +
				"            \"type\" : \"text\"," +
				"            \"fields\" : {" +
				"              \"keyword\" : {" +
				"                \"type\" : \"keyword\"," +
				"                \"ignore_above\" : 256" +
				"              }" +
				"            }" +
				"          }," +
				"          \"ProviderGuid\" : {" +
				"            \"type\" : \"text\"," +
				"            \"fields\" : {" +
				"              \"keyword\" : {" +
				"                \"type\" : \"keyword\"," +
				"                \"ignore_above\" : 256" +
				"              }" +
				"            }" +
				"          }," +
				"          \"ProviderName\" : {" +
				"            \"type\" : \"text\"," +
				"            \"fields\" : {" +
				"              \"keyword\" : {" +
				"                \"type\" : \"keyword\"," +
				"                \"ignore_above\" : 256" +
				"              }" +
				"            }" +
				"          }," +
				"          \"Qualifiers\" : {" +
				"            \"type\" : \"text\"," +
				"            \"fields\" : {" +
				"              \"keyword\" : {" +
				"                \"type\" : \"keyword\"," +
				"                \"ignore_above\" : 256" +
				"              }" +
				"            }" +
				"          }," +
				"          \"RenderingInfoCulture\" : {" +
				"            \"type\" : \"text\"," +
				"            \"fields\" : {" +
				"              \"keyword\" : {" +
				"                \"type\" : \"keyword\"," +
				"                \"ignore_above\" : 256" +
				"              }" +
				"            }" +
				"          }," +
				"          \"SystemTime\" : {" +
				"            \"type\" : \"date\"" +
				"          }," +
				"          \"Task\" : {" +
				"            \"type\" : \"text\"," +
				"            \"fields\" : {" +
				"              \"keyword\" : {" +
				"                \"type\" : \"keyword\"," +
				"                \"ignore_above\" : 256" +
				"              }" +
				"            }" +
				"          }," +
				"          \"TaskDescription\" : {" +
				"            \"type\" : \"text\"," +
				"            \"fields\" : {" +
				"              \"keyword\" : {" +
				"                \"type\" : \"keyword\"," +
				"                \"ignore_above\" : 256" +
				"              }" +
				"            }" +
				"          }," +
				"          \"ThreadID\" : {" +
				"            \"type\" : \"text\"," +
				"            \"fields\" : {" +
				"              \"keyword\" : {" +
				"                \"type\" : \"keyword\"," +
				"                \"ignore_above\" : 256" +
				"              }" +
				"            }" +
				"          }," +
				"          \"UserID\" : {" +
				"            \"type\" : \"text\"," +
				"            \"fields\" : {" +
				"              \"keyword\" : {" +
				"                \"type\" : \"keyword\"," +
				"                \"ignore_above\" : 256" +
				"              }" +
				"            }" +
				"          }," +
				"          \"Version\" : {" +
				"            \"type\" : \"text\"," +
				"            \"fields\" : {" +
				"              \"keyword\" : {" +
				"                \"type\" : \"keyword\"," +
				"                \"ignore_above\" : 256" +
				"              }" +
				"            }" +
				"          }," +
				"          \"flag\" : {" +
				"            \"type\" : \"text\"," +
				"            \"fields\" : {" +
				"              \"keyword\" : {" +
				"                \"type\" : \"keyword\"," +
				"                \"ignore_above\" : 256" +
				"              }" +
				"            }" +
				"          }," +
				"          \"keywords\" : {" +
				"            \"type\" : \"text\"," +
				"            \"fields\" : {" +
				"              \"keyword\" : {" +
				"                \"type\" : \"keyword\"," +
				"                \"ignore_above\" : 256" +
				"              }" +
				"            }" +
				"          }," +
				"          \"message\" : {" +
				"            \"type\" : \"text\"" +
				"          }," +
				"          \"portedAt\" : {" +
				"            \"type\" : \"date\"," +
				"            \"format\" : \"dateOptionalTime\"" +
				"          }," +
				"          \"ported_at\" : {" +
				"            \"type\" : \"date\"" +
				"          }," +
				"          \"receivedAt\" : {" +
				"            \"type\" : \"date\"," +
				"            \"format\" : \"dateOptionalTime\"" +
				"          }," +
				"          \"received_at\" : {" +
				"            \"type\" : \"date\"" +
				"          }," +
				"          \"tag\" : {" +
				"            \"type\" : \"text\"," +
				"            \"fields\" : {" +
				"              \"keyword\" : {" +
				"                \"type\" : \"keyword\"," +
				"                \"ignore_above\" : 256" +
				"              }" +
				"            }" +
				"          }," +
                "         \"IpAddress\":{\"type\":\"keyword\"}," +
				"         \"ProcessId\":{\"type\":\"keyword\"}," +
				"         \"Metafield_type\":{\"type\":\"keyword\"}," +
				"         \"Metafield_category\":{\"type\":\"keyword\"}," +
				"         \"Metafield_subject\":{\"type\":\"keyword\"}," +
				"         \"Metafield_object\":{\"type\":\"keyword\"}," +
				"         \"Metafield_loglevel\":{\"type\":\"keyword\"}," +
				"         \"Metafield_source\":{\"type\":\"keyword\"}," +
				"         \"Metafield_description\":{" +
				"                 \"type\":\"text\"," +
				"                 \"fields\": {" +
				"                     \"raw\": {" +
				"                       \"type\": \"keyword\"" +
				"}}" +
				"            }" +
				"        }" +
				"      }";
		return GsonHelper.fromJson(mapping);

	}

	public static String getType() {
		return "event_log";
	}

	static {
		// System
		xmlMap.put("System.ProviderName", "ProviderName");xmlMap.put("System.ProviderGuid", "ProviderGuid");
		xmlMap.put("System.ProviderEventSourceName", "ProviderEventSourceName");
		xmlMap.put("System.EventID", "EventID");xmlMap.put("System.EventIDQualifiers", "Qualifiers");
		xmlMap.put("System.Version", "Version");xmlMap.put("System.CorrelationActivityID", "CorrelationActivityID");
		xmlMap.put("System.Level", "Level");xmlMap.put("System.Task", "Task");
		xmlMap.put("System.Opcode", "Opcode");xmlMap.put("System.Keywords", "keywords");
		xmlMap.put("System.TimeCreatedSystemTime", "SystemTime");xmlMap.put("System.EventRecordID", "EventRecordID");
		xmlMap.put("System.Execution", "Execution");xmlMap.put("System.Correlation", "Correlation");
		xmlMap.put("System.ExecutionProcessID", "ProcessID");xmlMap.put("System.ExecutionThreadID", "ThreadID");
		xmlMap.put("System.Channel", "Channel");xmlMap.put("System.Computer", "Computer");
		xmlMap.put("System.SecurityUserID", "UserID");
		// EventData
		xmlMap.put("EventData.Data", "EventData");xmlMap.put("EventData.Binary", "EventBinary");
		//
		xmlMap.put("UserData", "UserData");
		// RenderInfo
		xmlMap.put("RenderingInfoCulture", "RenderingInfoCulture");xmlMap.put("RenderingInfo.Keywords.Keyword", "KeywordsDescription");
		xmlMap.put("RenderingInfo.Message", "EvtMessage");xmlMap.put("RenderingInfo.Level", "LevelDescription");
		xmlMap.put("RenderingInfo.Task", "TaskDescription");xmlMap.put("RenderingInfo.Opcode", "OpcodeDescription");
		xmlMap.put("RenderingInfo.Channel", "ChannelDescription");xmlMap.put("RenderingInfo.Provider", "ProviderDescription");
	}

}
