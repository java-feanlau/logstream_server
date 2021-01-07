package com.boyitech.logstream.server.worker.indexer;

import com.alibaba.fastjson.JSONObject;
import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import com.boyitech.logstream.core.worker.indexer.BaseIndexerConfig;
import com.boyitech.logstream.worker.indexer.SangforAccesscontrolAllV1Indexer;
import com.boyitech.logstream.worker.indexer.TrendmicroImsaMailgatewayV1Indexer;
import io.krakens.grok.api.Grok;
import io.krakens.grok.api.GrokCompiler;
import io.krakens.grok.api.Match;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author juzheng
 * @Title: IndexerBoshiACAllTest
 * @date 2019/8/5 2:26 PM
 * @Description: 博世华域----深信服AC日志解析的测试类
 */
public class IndexerBoshiACAllTest {
    /*
     * @Author juzheng
     * @Description   测试dom4j解析xml格式的字符串；
     * @Date 9:31 AM 2019/8/6
     * @Param []
     * @return void
     */
    @Test
    public void testDom4jXml() {
/**
 *
 <?xml version="1.0" encoding="utf-8"?>
 <_d>
 <_f n="private_type">1</_f>
 <_f n="is_webapp">0</_f>
 <_f n="line_no">0</_f>
 <_f n="dealed_line_no">线路1</_f>
 <_f n="protocol">6</_f>
 <_f n="detail">访问网站:ctldl.windowsupdate.com,匹配URL组：操作系统升级，该操作被拒绝</_f>
 <_f n="urldata">ctldl.windowsupdate.com/msdownload/update/v3/static/trustedr/en/disallowedcertstl.cab?2aaea8d41edbbe58</_f>
 <_f n="policy">Default Deny Policy for all employee</_f>
 <_f n="url">ctldl.windowsupdate.com/msdownload/update/v3/static/trustedr/en/disallowedcertstl.cab?2aaea8d41edbbe58</_f>
 <_f n="host">ctldl.windowsupdate.com</_f>
 <_f n="termtype">未知类型</_f>
 </_d>
 */
        Document doc;
        String testXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<_d>\n  <_f n=\"private_type\">1</_f>\n  <_f n=\"is_webapp\">0</_f>\n  <_f n=\"line_no\">0</_f>\n  <_f n=\"dealed_line_no\">线路1</_f>\n  <_f n=\"protocol\">6</_f>\n  <_f n=\"detail\">访问网站:ctldl.windowsupdate.com,匹配URL组：操作系统升级，该操作被拒绝</_f>\n  <_f n=\"urldata\">ctldl.windowsupdate.com/msdownload/update/v3/static/trustedr/en/disallowedcertstl.cab?2aaea8d41edbbe58</_f>\n  <_f n=\"policy\">Default Deny Policy for all employee</_f>\n  <_f n=\"url\">ctldl.windowsupdate.com/msdownload/update/v3/static/trustedr/en/disallowedcertstl.cab?2aaea8d41edbbe58</_f>\n  <_f n=\"host\">ctldl.windowsupdate.com</_f>\n  <_f n=\"termtype\">未知类型</_f>\n</_d>";
        try {
            doc = DocumentHelper.parseText(testXml);
            Element root = doc.getRootElement();//获得根结点Event
            System.out.println(root.getName());
            Iterator rootIt = root.elementIterator();//遍历根结点Event
            while (rootIt.hasNext()) {
                Element element = (Element) rootIt.next();
//                System.out.println("getName "+element.getName());//节点名 _f；
//                System.out.println("getText "+element.getText());//节点内容的值 1；
//                System.out.println("getData "+element.getData());//节点内容的值 1；
//                System.out.println("getXPathResult(0) "+element.getXPathResult(0));//org.dom4j.tree.DefaultText@67b64c45 [Text: "1"]
//                System.out.println("attribute(0) "+element.attribute(0));//org.dom4j.tree.DefaultAttribute@4411d970 [Attribute: name n value "private_type"]
//                System.out.println("getNamespace "+element.getNamespace());//org.dom4j.Namespace@babe [Namespace: prefix  mapped to URI ""]
//                System.out.println("getQName "+element.getQName());//org.dom4j.QName@be7 [name: _f namespace: "org.dom4j.Namespace@babe [Namespace: prefix  mapped to URI ""]"]
//                System.out.println("getStringValue "+element.getStringValue());//1
//                System.out.println("getTextTrim "+element.getTextTrim());//1
//                System.out.println("getDocument "+element.getDocument());//org.dom4j.tree.DefaultDocument@6442b0a6 [Document: name null]
//                System.out.println("getNodeType "+element.getNodeType());//1 猜测是节点层级
//                System.out.println("getNodeTypeName "+element.getNodeTypeName());//Element
//                System.out.println("getParent "+element.getParent());// org.dom4j.tree.DefaultElement@60f82f98 [Element: <_d attributes: []/>]
//                System.out.println("getPath "+element.getPath());///_d/_f
                //  System.out.println(element.attribute("n").getValue());
                System.out.println(element.attribute(0).getValue() + ":" + element.getText());//可获得节点名的值
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }


    /*
     * @Author juzheng
     * @Description  模拟从文件中读取一行json格式的字符串来解析
     * @Date 11:27 AM 2019/8/6
     * @Param []
     * @return void
     */
    @Test
    public void testTxtLog() throws InterruptedException {
        Map<String, String> map = new HashMap();
        map.put("logType", "1");
        BaseIndexerConfig baseIndexerConfig = new BaseIndexerConfig(map);
        //1.此处修改Indexer类型：如AheSuccessIndexer
        BaseIndexer Indexer = new SangforAccesscontrolAllV1Indexer(baseIndexerConfig);
        //2.此处修改日志样本的txt文件的路径：
        String FILEPATH = "/Users/juzheng/Downloads/工作文件夹/ys3.2boshi sangfor2019.01.29/index_0/type_1/data_0.json";
        Indexer.register();

        System.out.println("---执行Indexer的测试---");
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(FILEPATH)));
            String s = new String();
            int count = 0;
            while ((s = br.readLine()) != null) {
                count++;
                Event e = new Event();
                e.setMessage(s);
                Indexer.format(e);
                System.out.println(e.getJsonMessage());
                JSONObject pa = JSONObject.parseObject(e.getJsonMessage());
            }
            br.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testGrok(){
        /* Create a new grokCompiler instance */
        GrokCompiler grokCompiler = GrokCompiler.newInstance();
        grokCompiler.registerDefaultPatterns();

        /* Grok pattern to compile, here httpd logs */
        final Grok grok = grokCompiler.compile("%{COMBINEDAPACHELOG}");

        /* Line of log to match */
        String log = "112.169.19.192 - - [06/Mar/2013:01:36:30 +0900] \"GET / HTTP/1.1\" 200 44346 \"-\" \"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.152 Safari/537.22\"";

        Match gm = grok.match(log);

        /* Get the map with matches */
        final Map<String, Object> capture = gm.capture();
        System.out.println(JSONObject.toJSON(capture));
    }
}
