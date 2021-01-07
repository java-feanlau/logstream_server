package com.boyitech.logstream.core.util.filter_rule;

import javax.management.InvalidAttributeValueException;

import com.boyitech.logstream.core.util.IPv4Util;

import java.util.Objects;

public class IpRangeRule implements FilterRuleInterface {

    private long begin;
    private long end;
    private boolean all = false;

    public IpRangeRule(String ipRange) throws InvalidAttributeValueException, RuntimeException {
        if (ipRange.contains("~") && ipRange.contains("/")) {
            throw new InvalidAttributeValueException();
        } else if (ipRange.equals("*") || ipRange.equals("0.0.0.0")) {
            this.all = true;
        } else if (ipRange.contains("~")) {
            String[] ips = ipRange.split("~");
            this.begin = IPv4Util.ipToLong(ips[0]);
            this.end = IPv4Util.ipToLong(ips[1]);
        } else if (ipRange.contains("/")) {
            String minIpAddres = IPv4Util.getMinIpAddres(ipRange);
            String maxIpAddres = IPv4Util.getMaxIpAddres(ipRange);
            this.begin = IPv4Util.ipToLong(minIpAddres);
            this.end = IPv4Util.ipToLong(maxIpAddres);
        } else {
            this.begin = IPv4Util.ipToLong(ipRange);
            this.end = IPv4Util.ipToLong(ipRange);
        }
    }

    public IpRangeRule(String begin, String end) {
        this.begin = IPv4Util.ipToLong(begin);
        this.end = IPv4Util.ipToLong(end);
    }

    public boolean in(int ip) {
        return all ? true : (ip - begin >= 0 && end - ip <= 0);
    }

    public boolean in(String ip) {
        if (ip == null || ip.equals("")) {
            return false;
        }
        if (all) {
            return true;
        }
        long tmp = IPv4Util.ipToLong(ip);
        return (tmp - begin >= 0 && tmp - end <= 0);
    }

    public boolean in(byte[] ip) {
        if (all) {
            return true;
        }
        long tmp = IPv4Util.bytesToLong(ip);
        return (tmp - begin >= 0 && tmp - end <= 0);
    }


    public boolean same(FilterRuleInterface rule) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IpRangeRule)) return false;
        IpRangeRule that = (IpRangeRule) o;

        if (all == that.all == true) {
            return true;
        } else {
            return begin == that.begin &&
                    end == that.end;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(begin, end);
    }


    //	@Override
//	public boolean equals(Object o) {
//		if (this == o) return true;
//		if (o == null || getClass() != o.getClass()) return false;
//		IpRangeRule that = (IpRangeRule) o;
//		return begin == that.begin &&
//				end == that.end &&
//				all == that.all;
//	}
//
//	@Override
//	public int hashCode() {
//		return Objects.hash(begin, end, all);
//	}
}
