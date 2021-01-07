package com.boyitech.logstream.core.util.filter_rule;

public interface FilterRuleInterface {
	public boolean in(String ipOrPort);

	public boolean in(int ipOrPort);

	public boolean in(byte[] ipOrPort);

}
