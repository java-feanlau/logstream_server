package com.boyitech.logstream.core.setting;

import com.boyitech.logstream.core.util.FilePathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.ElasticsearchParseException;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * @Author Eric Zheng
 * @Description 这个类只读配置文件并且存入到settings中。只需关注getSettings()和get(String key)即可
 * @Date 16:23 2019/3/7
 * @Param
 * @return
 **/
public class Settings {

	private static Map<String, String> settings = new HashMap<String, String>();

	//public static String grokPatternsPath = Paths.get(System.getProperty("user.dir"), "grok", "grokPatterns").toString();
	private static final String GLOBALSETTINGPATH = Paths.get(FilePathHelper.ROOTPATH, "config", "settings.conf").toString();
	private static final Logger LOGGER = LogManager.getLogger("main");
	//private static final boolean allowNullValues = true;

	public static void load(){
		//读取配置文件设置各全局变量
		File file = new File(GLOBALSETTINGPATH);
		Reader reader = null;
		StringBuffer sb = new StringBuffer();
		try {
			reader = new InputStreamReader(new FileInputStream(file));
			int tempchar;
			while ((tempchar = reader.read()) != -1) {
				if (((char) tempchar) != '\r') {
					sb.append((char) tempchar);
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			LOGGER.error(e);
			System.exit(0);
		} catch (IOException e) {
			LOGGER.error(e);
			System.exit(0);
		}
		try {
			settings = load(sb.toString());
		} catch (Exception e) {
			LOGGER.error(e);
		}
	}

    public static Map<String, String> load(String source) throws IOException {
        // It is safe to use EMPTY here because this never uses namedObject
        try (XContentParser parser = XContentFactory.xContent(contentType()).createParser(NamedXContentRegistry.EMPTY, null, source)) {
            return load(parser);
        }
    }

    private static Map<String, String> load(XContentParser jp) throws IOException {
        StringBuilder sb = new StringBuilder();
        Map<String, String> settings = new HashMap<>();
        List<String> path = new ArrayList<>();
        XContentParser.Token token = jp.nextToken();
        if (token == null) {
            return settings;
        }
        if (token != XContentParser.Token.START_OBJECT) {
            throw new ElasticsearchParseException("malformed, expected settings to start with 'object', instead was [{}]", token);
        }
        serializeObject(settings, sb, path, jp, null);

        // ensure we reached the end of the stream
        XContentParser.Token lastToken = null;
        try {
            while (!jp.isClosed() && (lastToken = jp.nextToken()) == null);
        } catch (Exception e) {
            throw new ElasticsearchParseException(
                    "malformed, expected end of settings but encountered additional content starting at line number: [{}], "
                            + "column number: [{}]",
                    e, jp.getTokenLocation().lineNumber, jp.getTokenLocation().columnNumber);
        }
        if (lastToken != null) {
            throw new ElasticsearchParseException(
                    "malformed, expected end of settings but encountered additional content starting at line number: [{}], "
                            + "column number: [{}]",
                    jp.getTokenLocation().lineNumber, jp.getTokenLocation().columnNumber);
        }

        return settings;
    }

    public static Map<String, String> getSettings() {
    	return settings;
    }

    //获取配置文件，也就是conf下setting.conf中的value
    public static String get(String key) {
    	return settings.get(key);
    }




    private static void serializeObject(Map<String, String> settings, StringBuilder sb, List<String> path, XContentParser parser,
            String objFieldName) throws IOException {
        if (objFieldName != null) {
            path.add(objFieldName);
        }

        String currentFieldName = null;
        XContentParser.Token token;
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.START_OBJECT) {
                serializeObject(settings, sb, path, parser, currentFieldName);
            } else if (token == XContentParser.Token.START_ARRAY) {
                serializeArray(settings, sb, path, parser, currentFieldName);
            } else if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (token == XContentParser.Token.VALUE_NULL) {
                serializeValue(settings, sb, path, parser, currentFieldName, true);
            } else {
                serializeValue(settings, sb, path, parser, currentFieldName, false);

            }
        }

        if (objFieldName != null) {
            path.remove(path.size() - 1);
        }
    }

    private static void serializeArray(Map<String, String> settings, StringBuilder sb, List<String> path,
    		XContentParser parser, String fieldName) throws IOException {
        XContentParser.Token token;
        int counter = 0;
        while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
            if (token == XContentParser.Token.START_OBJECT) {
                serializeObject(settings, sb, path, parser, fieldName + '.' + (counter++));
            } else if (token == XContentParser.Token.START_ARRAY) {
                serializeArray(settings, sb, path, parser, fieldName + '.' + (counter++));
            } else if (token == XContentParser.Token.FIELD_NAME) {
                fieldName = parser.currentName();
            } else if (token == XContentParser.Token.VALUE_NULL) {
                serializeValue(settings, sb, path, parser, fieldName + '.' + (counter++), true);
                // ignore
            } else {
                serializeValue(settings, sb, path, parser, fieldName + '.' + (counter++), false);
            }
        }
    }

    private static void serializeValue(Map<String, String> settings, StringBuilder sb, List<String> path, XContentParser parser, String fieldName,
            boolean isNull) throws IOException {
        sb.setLength(0);
        for (String pathEle : path) {
            sb.append(pathEle).append('.');
        }
        sb.append(fieldName);
        String key = sb.toString();
        String currentValue = isNull ? null : parser.text();

        if (settings.containsKey(key)) {
            throw new ElasticsearchParseException(
                    "duplicate settings key [{}] found at line number [{}], column number [{}], previous value [{}], current value [{}]",
                    key,
                    parser.getTokenLocation().lineNumber,
                    parser.getTokenLocation().columnNumber,
                    settings.get(key),
                    currentValue
            );
        }

//        if (currentValue == null && !allowNullValues) {
//            throw new ElasticsearchParseException(
//                    "null-valued setting found for key [{}] found at line number [{}], column number [{}]",
//                    key,
//                    parser.getTokenLocation().lineNumber,
//                    parser.getTokenLocation().columnNumber
//            );
//        }
        settings.put(key, currentValue);
    }

    public static XContentType contentType() {
        return XContentType.YAML;
    }
}
