package com.boyitech.logstream.core.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ua_parser.Client;
import ua_parser.Parser;

public class UserAgentParser {

	private static Parser uaParser;

	private static class UserAgentParserHolder {
		private static final UserAgentParser INSTANCE = new UserAgentParser();
	}

	public UserAgentParser() {
		try {
			uaParser = new Parser();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static final UserAgentParser getInstance() {
		return UserAgentParserHolder.INSTANCE;
	}

	public Map parse(String str) {
		Map ua = new HashMap();
		Client c = uaParser.parse(str);
		ua.put("device", c.device.family);
		ua.put("name", c.userAgent.family);
//		System.out.println("name:"+c.userAgent.family);
//		System.out.println("device:"+c.device.family);
		if(c.os!=null) {
			ua.put("os", c.os.family);
			ua.put("os_name", c.os.family);
			ua.put("os_major", c.os.major);
			ua.put("os_minor", c.os.minor);
//			System.out.println("os:"+c.os.family);
//			System.out.println("os_name:"+c.os.family);
//			System.out.println("os_major:"+c.os.major);
//			System.out.println("os_minor:"+c.os.minor);
		}
		if(c.userAgent!=null) {
			ua.put("major", c.userAgent.major);
			ua.put("minor", c.userAgent.minor);
			ua.put("patch", c.userAgent.patch);
			ua.put("build", c.userAgent.patch);
//			System.out.println("major:"+c.userAgent.major);
//			System.out.println("minor:"+c.userAgent.minor);
//			System.out.println("patch:"+c.userAgent.patch);
//			System.out.println("build:"+c.userAgent.patch);
		}
		return ua;
	}

}
