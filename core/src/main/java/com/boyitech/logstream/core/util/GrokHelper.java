package com.boyitech.logstream.core.util;

import io.krakens.grok.api.GrokCompiler;

public class GrokHelper {
	private static GrokCompiler grokCompiler = GrokCompiler.newInstance();

	static {
		grokCompiler.registerDefaultPatterns();
	}

	public static GrokCompiler getGrokCompiler() {
		return grokCompiler;
	}


}
