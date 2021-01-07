package com.boyitech.logstream.core.util.filter_rule;

public class EmptyRule implements FilterRuleInterface {

	public boolean in(String ipOrPort) {
		return true;
	}

	public boolean in(int ipOrPort) {
		return true;
	}

	public boolean in(byte[] ipOrPort) {
		return true;
	}

	public String toString(){
		return "*";
	}

	public boolean same(FilterRuleInterface rule) {
		if(rule instanceof EmptyRule){
			return true;
		}else{
			return false;
		}
	}


	@Override
	public int hashCode() {
		return "EmptyRule".hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return true;
	}
}
