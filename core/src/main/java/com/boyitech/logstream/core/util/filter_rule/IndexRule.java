package com.boyitech.logstream.core.util.filter_rule;

public class IndexRule implements FilterRuleInterface {

	private String index;

	public IndexRule(String index){
		this.index = index;
	}

	public boolean in(String ipOrPort) {
		return false;
	}

	public boolean in(byte[] ipOrPort) {
		return false;
	}

	public boolean same(FilterRuleInterface rule) {
		if(rule instanceof IndexRule){
			IndexRule tmp = (IndexRule) rule;
			return this.index==tmp.getIndex();
		}else{
			return false;
		}
	}

	public boolean in(int ipOrPort) {
		return false;
	}

	public String getIndex() {
		return index;
	}

}
