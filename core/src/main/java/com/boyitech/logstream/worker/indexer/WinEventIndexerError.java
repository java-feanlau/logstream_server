//package com.boyitech.logstream.worker.indexer;
//
//import com.boyitech.logstream.core.info.Event;
//import com.boyitech.logstream.core.util.GsonHelper;
//import com.boyitech.logstream.core.worker.BaseWorkerConfig;
//import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
//import org.dom4j.*;
//
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.*;
//
///**
// * @author juzheng
// * @Title: WinEventIndexer
// * @date 2019-3-14 9:31
// * @Description:博世/windows日志格式化，日志经logstash预处理，格式为xml,使用dom4j包处理
// */
//public class WinEventIndexerError extends BaseIndexer {
//
//    private  static Document doc;
//    public WinEventIndexerError(BaseWorkerConfig config) {
//        super(config);
//    }
//
//    public WinEventIndexerError(String indexerID, BaseWorkerConfig config) {
//        super(indexerID, config);
//    }
//
//    @Override
//    public boolean register() {
//        doc=null;
//        return true;
//    }
//
//    @Override
//    public void tearDown() {
//    }
//
//    @Override
//    public boolean format(Event event) {
//        String messageJson = event.getMessage();  //此处message返回的是一个字符串格式的xml日志；
//        Map messageMap = GsonHelper.fromJson(messageJson);
//        String message=messageMap.get("xml").toString();
//        Map<String, Object> format = event.getFormat();
//        format.put("message", message);
//        if (message == null) {
//            format.put("flag", "解析失败，message为空");
//            return false;
//        }
//        if (message != null) {
//            try {
//                doc = DocumentHelper.parseText(message);
//            } catch (DocumentException e) {
//                e.printStackTrace();
//            }
//            Element root = doc.getRootElement();//获得根结点Event
//            Iterator it = root.elementIterator();//遍历根结点Event
//            while (it.hasNext())
//            {
//                Element element = (Element) it.next();
//                if (element.getName()=="System"){
//                    Iterator<Element> eleSystem = element.elementIterator();
//                    while (eleSystem.hasNext())
//                    {
//                        Element e = eleSystem.next();
//                        String eName=e.getName();
//                        switch (eName)
//                        {
//                            case "Provider":
//                                Element Provider = element.element("Provider");
//                                String ProviderName="";
//                                String ProviderGuid="";
//                                Iterator attrIt = e.attributeIterator();
//                                while (attrIt.hasNext()) {
//                                    Attribute a  = (Attribute) attrIt.next();
//                                    if (a.getName()=="Name")
//                                    {
//                                        ProviderName = Provider.attribute("Name").getText();
//                                        format.put("ProviderName",ProviderName);
//                                    }
//                                    if(a.getName()=="Guid")
//                                    {
//                                        ProviderGuid = Provider.attribute("Guid").getText();
//                                        format.put("ProviderGuid",ProviderGuid);
//                                    }
//                                }
//                                break;
//                            case "EventID":
//                                String EventID=e.getText();
//                                format.put("EventID",EventID);
//                                break;
//                            case "Version":
//                                String EventVersion=e.getText();
//                                format.put("EventVersion",EventVersion);
//                                break;
//                            case "Level":
//                                String Level=e.getText();
//                                format.put("Level",Level);
//                                break;
//                            case "Task":
//                                String Task=e.getText();
//                                format.put("Task",Task);
//                                break;
//                            case "Opcode":
//                                String Opcode=e.getText();
//                                format.put("Opcode",Opcode);
//                                break;
//                            case "Keywords":
//                                String Keywords=e.getText();
//                                format.put("Keywords",Keywords);
//                                break;
//                            case "TimeCreated":
//                                //2018-12-04T06:10:46.419565700Z
//                                Element eTimeCreated = element.element("TimeCreated");
//                                String timestamp = eTimeCreated.attribute("SystemTime").getText().split("\\.")[0];
//                                if (timestamp != null && timestamp.trim().length() != 0) {
//                                    Date time = new Date();
//                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
//                                    try {
//                                        time=sdf.parse(timestamp);
//                                    } catch (ParseException ex) {
//                                        LOGGER.error("@timestamp时间格式化出错");
//                                    }
//                                    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
//                                    timestamp = sdf2.format(time);
//                                }
//                                String TimeCreated=timestamp;
//                                format.put("TimeCreated",TimeCreated);
//                                format.put("@timestamp", TimeCreated);
//                                break;
//                            case "EventRecordID":
//                                String EventRecordID=e.getText();
//                                format.put("EventRecordID",EventRecordID);
//                                break;
//                            case "Correlation":
//                                Element Correlation = element.element("Correlation");
//                                String ActivityID;
//                                if(Correlation.attribute("ActivityID")!=null) {
//                                    ActivityID = Correlation.attribute("ActivityID").getText();
//                                    format.put("ActivityID",ActivityID);
//                                }
//                                break;
//                            case "Execution":
//                                Element Execution = element.element("Execution");
//                                String ProcessID = Execution.attribute("ProcessID").getText();
//                                String ThreadID = Execution.attribute("ThreadID").getText();
//                                format.put("ProcessID",ProcessID);
//                                format.put("ThreadID",ThreadID);
//                                break;
//                            case "Channel":
//                                String Channel=e.getText();
//                                format.put("Channel",Channel);
//                                break;
//                            case "Computer":
//                                String Computer=e.getText();
//                                format.put("Computer",Computer);
//                                break;
//                            case "Security":
//                                Element Security = element.element("Security");
//                                String UserID;
//                                if(Security.attribute("UserID")!=null) {
//                                    UserID = Security.attribute("UserID").getText();
//                                    format.put("UserID",UserID);
//                                }
//                                break;
//                        }
//
//
//                    }
//                }
//                if (element.getName()=="UserData"){
//                    String UserData = element.getText();  //???
//                    format.put("UserData",UserData);
//                }
//                if (element.getName()=="EventData"){
//                    String EventDataXml = element.getText();
//                    if (EventDataXml!="")
//                    format.put("EventDataXml",EventDataXml);
//                    Iterator<Element> eleEve = element.elementIterator();
//                    List list_names=new ArrayList();
//                    List list_values=new ArrayList();
//                    while (eleEve.hasNext())
//                    {
//                        Element e = eleEve.next();
//                        String eName=e.getName();
//                        switch (eName)
//                        {
//                            case "Data":
//                                String EventDataValues;
//                                String EventDataNames="";
//                                int i=0;
//                                if(e.getText()!=null) {
//                                    EventDataValues = e.getText();
//                                    list_values.add(EventDataValues);
//                                    if (e.attribute("Name")!=null) {
//                                        EventDataNames = e.attribute("Name").getText();
//                                        list_names.add(EventDataNames);
//                                    }
//                                    format.put("EventDataValues",list_values);
//                                    format.put("EventDataNames",list_names);
//                                }
//                                break;
//                            case "Binary":
//                                String EventDataBinary=e.getText();
//                                format.put("EventDataBinary",EventDataBinary);
//                                break;
//                        }
//                    }
//                }
//                if (element.getName()=="RenderingInfo"){
//                    Element RenderingInfo = element.element("RenderingInfo");
//                    String EvtRenderCulture = RenderingInfo.attribute("Culture").getText();
//                    format.put("EvtRenderCulture",EvtRenderCulture);
//
//                    Iterator<Element> eleRender =element.elementIterator();
//                    while (eleRender.hasNext()){
//                        Element e = eleRender.next();
//                        String eName=e.getName();
//                        switch (eName)
//                        {
//                            case "Level":
//                                String EvtLevel=e.getText();
//                                format.put("EvtLevel",EvtLevel);
//                                break;
//                            case "Task":
//                                String EvtTask=e.getText();
//                                format.put("EvtTask",EvtTask);
//                                break;
//                            case "Opcode":
//                                String EvtOpcode=e.getText();
//                                format.put("EvtOpcode",EvtOpcode);
//                                break;
//                            case "Channel":
//                                String EvtChannel=e.getText();
//                                format.put("EvtChannel",EvtChannel);
//                                break;
//                            case "Provider":
//                                String EvtProvider=e.getText();
//                                format.put("EvtProvider",EvtProvider);
//                                break;
//                            case "Keywords":
//                                Element eKeyword=e.element("Keyword"); //???
//                                String  Keyword=eKeyword.getText();
//                                format.put("Keyword",Keyword);
//                                break;
//                            case "Message":
//                                String EvtMessage=e.getText();
//                                format.put("EvtMessage",EvtMessage);
//
//                        }
//                    }
//                }
//            }
//        }
//        //格式化Metafield
//        event.setMetafieldLoglevel("1");
//        if (event.getLogType() != null) {
//            format.put("Metafield_description", event.getLogType());
//            format.put("Metafield_type", event.getLogType());
//        }
//        if (event.getSource() != null) {
//            format.put("Metafield_source", event.getSource());
//        }
//
//        if (format.get("c_ip") != null) {
//            format.put("Metafield_object", format.get("c_ip"));
//        }
//        if (format.get("s_ip") != null) {
//            format.put("Metafield_subject", format.get("s_ip"));
//        }else {
//            format.put("Metafield_subject", event.getSource());
//        }
//
//        if (format.get("flag") == "解析失败")
//            return false;
//        return true;
//    }
//
//    public static Map getMapping() {
//        String mapping = "{\"properties\":{"
//                + "\"@timestamp\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
//                + "\"received_at\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
//                + "\"portedAt\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
//                + "\"flag\":{\"type\":\"keyword\"},"
//                + "\"message\":{\"type\":\"text\"},"
//                + "\"ProviderName\":{\"type\":\"keyword\"},"
//                + "\"ProviderGuid\":{\"type\":\"keyword\"},"
//                + "\"EventID\":{\"type\":\"keyword\"},"
//                + "\"EventVersion\":{\"type\":\"keyword\"},"
//                + "\"Level\":{\"type\":\"keyword\"},"
//                + "\"Task\":{\"type\":\"keyword\"},"
//                + "\"Opcode\":{\"type\":\"keyword\"},"
//                + "\"Keywords\":{\"type\":\"keyword\"},"
//                + "\"TimeCreated\":{\"type\":\"keyword\"},"
//                + "\"EventRecordID\":{\"type\":\"keyword\"},"
//                + "\"ActivityID\":{\"type\":\"keyword\"},"
//                + "\"ProcessID\":{\"type\":\"keyword\"},"
//                + "\"ThreadID\":{\"type\":\"keyword\"},"
//                + "\"Channel\":{\"type\":\"keyword\"},"
//                + "\"Computer\":{\"type\":\"keyword\"},"
//                + "\"UserID\":{\"type\":\"keyword\"},"
//                + "\"UserData\":{\"type\":\"keyword\"},"
//                + "\"EventDataXml\":{\"type\":\"keyword\"},"
//                + "\"EventDataValues\":{\"type\":\"keyword\"},"
//                + "\"EventDataBinary\":{\"type\":\"keyword\"},"
//                + "\"EventDataNames\":{\"type\":\"keyword\"},"
//                + "\"EvtRenderCulture\":{\"type\":\"keyword\"},"
//                + "\"EvtLevel\":{\"type\":\"keyword\"},"
//                + "\"EvtTask\":{\"type\":\"keyword\"},"
//                + "\"EvtOpcode\":{\"type\":\"keyword\"},"
//                + "\"EvtChannel\":{\"type\":\"keyword\"},"
//                + "\"EvtProvider\":{\"type\":\"keyword\"},"
//                + "\"EvtKeywords\":{\"type\":\"keyword\"},"
//                + "\"EvtMessage\":{\"type\":\"keyword\"},"
//                + "\"Metafield_type\":{\"type\":\"keyword\"},"
//                + "\"Metafield_category\":{\"type\":\"keyword\"},"
//                + "\"Metafield_subject\":{\"type\":\"keyword\"},"
//                + "\"Metafield_object\":{\"type\":\"keyword\"},"
//                + "\"Metafield_loglevel\":{\"type\":\"keyword\"},"
//                + "\"Metafield_source\":{\"type\":\"keyword\"},"
//                + "\"Metafield_description\":{\"type\":\"text\", \"fields\": {\"raw\": {\"type\": \"keyword\"}}}"
//                + "}"
//                + "}";
//        return GsonHelper.fromJson(mapping);
//    }
//
//    public static String getMappingString() {
//        String mapping = "{\"properties\":{"
//                + "\"@timestamp\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
//                + "\"received_at\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
//                + "\"portedAt\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
//                + "\"flag\":{\"type\":\"keyword\"},"
//                + "\"message\":{\"type\":\"text\"},"
//                + "\"ProviderName\":{\"type\":\"keyword\"},"
//                + "\"ProviderGuid\":{\"type\":\"keyword\"},"
//                + "\"EventID\":{\"type\":\"keyword\"},"
//                + "\"EventVersion\":{\"type\":\"keyword\"},"
//                + "\"Level\":{\"type\":\"keyword\"},"
//                + "\"Task\":{\"type\":\"keyword\"},"
//                + "\"Opcode\":{\"type\":\"keyword\"},"
//                + "\"Keywords\":{\"type\":\"keyword\"},"
//                + "\"TimeCreated\":{\"type\":\"keyword\"},"
//                + "\"EventRecordID\":{\"type\":\"keyword\"},"
//                + "\"ActivityID\":{\"type\":\"keyword\"},"
//                + "\"ProcessID\":{\"type\":\"keyword\"},"
//                + "\"ThreadID\":{\"type\":\"keyword\"},"
//                + "\"Channel\":{\"type\":\"keyword\"},"
//                + "\"Computer\":{\"type\":\"keyword\"},"
//                + "\"UserID\":{\"type\":\"keyword\"},"
//                + "\"UserData\":{\"type\":\"keyword\"},"
//                + "\"EventDataXml\":{\"type\":\"keyword\"},"
//                + "\"EventDataValues\":{\"type\":\"keyword\"},"
//                + "\"EventDataBinary\":{\"type\":\"keyword\"},"
//                + "\"EventDataNames\":{\"type\":\"keyword\"},"
//                + "\"EvtRenderCulture\":{\"type\":\"keyword\"},"
//                + "\"EvtLevel\":{\"type\":\"keyword\"},"
//                + "\"EvtTask\":{\"type\":\"keyword\"},"
//                + "\"EvtOpcode\":{\"type\":\"keyword\"},"
//                + "\"EvtChannel\":{\"type\":\"keyword\"},"
//                + "\"EvtProvider\":{\"type\":\"keyword\"},"
//                + "\"EvtKeywords\":{\"type\":\"keyword\"},"
//                + "\"EvtMessage\":{\"type\":\"keyword\"},"
//                + "\"Metafield_type\":{\"type\":\"keyword\"},"
//                + "\"Metafield_category\":{\"type\":\"keyword\"},"
//                + "\"Metafield_subject\":{\"type\":\"keyword\"},"
//                + "\"Metafield_object\":{\"type\":\"keyword\"},"
//                + "\"Metafield_loglevel\":{\"type\":\"keyword\"},"
//                + "\"Metafield_source\":{\"type\":\"keyword\"},"
//                + "\"Metafield_description\":{\"type\":\"text\", \"fields\": {\"raw\": {\"type\": \"keyword\"}}}"
//                + "}"
//                + "}";
//        return mapping;
//    }
//
//}
