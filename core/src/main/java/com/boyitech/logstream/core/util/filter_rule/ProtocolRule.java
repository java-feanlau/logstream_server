package com.boyitech.logstream.core.util.filter_rule;

import java.util.Objects;

public class ProtocolRule implements FilterRuleInterface {

	private String protocol;

	public ProtocolRule(String protocol){
		this.protocol = protocol;
	}

	public boolean in(String protocol) {
		return protocol.equals("*")? true : this.protocol.equals(protocol);
	}

	public boolean in(int protocol) {
		return false;
	}

	public boolean in(byte[] ipOrPort) {
		return false;
	}

	@Override
	public String toString(){
		return protocol;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ProtocolRule)) return false;
		ProtocolRule that = (ProtocolRule) o;
		return Objects.equals(protocol, that.protocol);
	}

	@Override
	public int hashCode() {
		return Objects.hash(protocol);
	}
}
