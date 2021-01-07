package com.boyitech.logstream.core.util.filter_rule;

import java.util.Objects;

public class PortRule implements FilterRuleInterface {

	private String portS;
	private int portI;

	public PortRule(String port){
		if(!port.equals("") && port!=null){
			this.portI = Integer.parseInt(port);
			this.portS = port;
		}else{
			this.portI = 0;
			this.portS = "*";
		}
	}

	public boolean in(String port) {
		return portS.equals("*")? true : this.portS.equals(port);
	}

	public boolean in(int port) {
		return portI==0? true : this.portI == port;
	}

	public boolean in(byte[] port) {
		int p;
		try{
			p = port[0];
		}catch(Exception e){
			return false;
		}
		return portI==0? true : p==this.portI;
	}

	@Override
	public String toString(){
		return portS;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof PortRule)) return false;
		PortRule portRule = (PortRule) o;
		return portI == portRule.portI &&
				Objects.equals(portS, portRule.portS);
	}

	@Override
	public int hashCode() {
		return Objects.hash(portS, portI);
	}
}
