package com.boyitech.logstream.core.util;

import javax.management.InvalidAttributeValueException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author michael <br>
 * blog: http://www.micmiu.com
 * mail: sjsky007@gmail.com
 */
public class IPv4Util {

    private final static int INADDRSZ = 4;

    /**
     * 把IP地址转化为字节数组
     *
     * @param ipAddr
     * @return byte[]
     */
    public static byte[] ipToBytesByInet(String ipAddr) {
        try {
            return InetAddress.getByName(ipAddr).getAddress();
        } catch (Exception e) {
            throw new IllegalArgumentException(ipAddr + " is invalid IP");
        }
    }

    /**
     * 把IP地址转化为int
     *
     * @param ipAddr
     * @return int
     */
    public static byte[] ipToBytesByReg(String ipAddr) {
        byte[] ret = new byte[4];
        try {
            String[] ipArr = ipAddr.split("\\.");
            ret[0] = (byte) (Integer.parseInt(ipArr[0]) & 0xFF);
            ret[1] = (byte) (Integer.parseInt(ipArr[1]) & 0xFF);
            ret[2] = (byte) (Integer.parseInt(ipArr[2]) & 0xFF);
            ret[3] = (byte) (Integer.parseInt(ipArr[3]) & 0xFF);
            return ret;
        } catch (Exception e) {
            throw new IllegalArgumentException(ipAddr + " is invalid IP");
        }

    }

    /**
     * 字节数组转化为IP
     *
     * @param bytes
     * @return int
     */
    public static String bytesToIp(byte[] bytes) {
        return new StringBuffer().append(bytes[0] & 0xFF).append(".").append(
                bytes[1] & 0xFF).append(".").append(bytes[2] & 0xFF)
                .append(".").append(bytes[3] & 0xFF).toString();
    }

    /**
     * 根据位运算把 byte[] -> int
     *
     * @param bytes
     * @return int
     */
    public static int bytesToInt(byte[] bytes) {
        int addr = bytes[3] & 0xFF;
        addr |= ((bytes[2] << 8) & 0xFF00);
        addr |= ((bytes[1] << 16) & 0xFF0000);
        addr |= ((bytes[0] << 24) & 0xFF000000);
        return addr;
    }

    public static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();//need flip
        return buffer.getLong();
    }

    /**
     * 把IP地址转化为int
     *
     * @param ipAddr
     * @return int
     */
    public static int ipToInt(String ipAddr) {
        try {
            return bytesToInt(ipToBytesByInet(ipAddr));
        } catch (Exception e) {
            throw new IllegalArgumentException(ipAddr + " is invalid IP");
        }
    }

    /*
     * @Author Eric Zheng
     * @Description 把ip转换为long
     * @Date 17:03 2019/7/16
     **/
    public static long ipToLong(String strIp) {
        long[] ip = new long[4];
        //先找到IP地址字符串中.的位置
        int position1 = strIp.indexOf(".");
        int position2 = strIp.indexOf(".", position1 + 1);
        int position3 = strIp.indexOf(".", position2 + 1);
        //将每个.之间的字符串转换成整型
        ip[0] = Long.parseLong(strIp.substring(0, position1));
        ip[1] = Long.parseLong(strIp.substring(position1 + 1, position2));
        ip[2] = Long.parseLong(strIp.substring(position2 + 1, position3));
        ip[3] = Long.parseLong(strIp.substring(position3 + 1));
        return (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3];
    }

    /**
     * ipInt -> byte[]
     *
     * @param ipInt
     * @return byte[]
     */
    public static byte[] intToBytes(int ipInt) {
        byte[] ipAddr = new byte[INADDRSZ];
        ipAddr[0] = (byte) ((ipInt >>> 24) & 0xFF);
        ipAddr[1] = (byte) ((ipInt >>> 16) & 0xFF);
        ipAddr[2] = (byte) ((ipInt >>> 8) & 0xFF);
        ipAddr[3] = (byte) (ipInt & 0xFF);
        return ipAddr;
    }

    /**
     * 把int->ip地址
     *
     * @param ipInt
     * @return String
     */
    public static String intToIp(int ipInt) {
        return new StringBuilder().append(((ipInt >> 24) & 0xff)).append(".")
                .append((ipInt >> 16) & 0xff).append(".").append(
                        (ipInt >> 8) & 0xff).append(".").append((ipInt & 0xff))
                .toString();
    }

    /**
     * 把192.168.1.1/24 转化为int数组范围
     *
     * @param ipAndMask
     * @return int[]
     */
    public static int[] getIPIntScope(String ipAndMask) {
        String[] ipArr = ipAndMask.split("/");
        if (ipArr.length != 2) {
            throw new IllegalArgumentException("invalid ipAndMask with: "
                    + ipAndMask);
        }
        int netMask = Integer.valueOf(ipArr[1].trim());
        if (netMask < 0 || netMask > 31) {
            throw new IllegalArgumentException("invalid ipAndMask with: "
                    + ipAndMask);
        }
        int ipInt = IPv4Util.ipToInt(ipArr[0]);
        int netIP = ipInt & (0xFFFFFFFF << (32 - netMask));
        int hostScope = (0xFFFFFFFF >>> netMask);
        return new int[]{netIP, netIP + hostScope};
    }

    /**
     * 把192.168.1.1/24 转化为IP数组范围
     *
     * @param ipAndMask
     * @return String[]
     */
    public static String[] getIPAddrScope(String ipAndMask) {
        int[] ipIntArr = IPv4Util.getIPIntScope(ipAndMask);
        return new String[]{IPv4Util.intToIp(ipIntArr[0]),
                IPv4Util.intToIp(ipIntArr[1])};
    }

    /**
     * 根据IP 子网掩码（192.168.1.1 255.255.255.0）转化为IP段
     *
     * @param ipAddr ipAddr
     * @param mask   mask
     * @return int[]
     */
    public static int[] getIPIntScope(String ipAddr, String mask) {
        int ipInt;
        int netMaskInt = 0, ipcount = 0;
        try {
            ipInt = IPv4Util.ipToInt(ipAddr);
            if (null == mask || "".equals(mask)) {
                return new int[]{ipInt, ipInt};
            }
            netMaskInt = IPv4Util.ipToInt(mask);
            ipcount = IPv4Util.ipToInt("255.255.255.255") - netMaskInt;
            int netIP = ipInt & netMaskInt;
            int hostScope = netIP + ipcount;
            return new int[]{netIP, hostScope};
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid ip scope express  ip:"
                    + ipAddr + "  mask:" + mask);
        }
    }

    /**
     * 根据IP 子网掩码（192.168.1.1 255.255.255.0）转化为IP段
     *
     * @param ipAddr ipAddr
     * @param mask   mask
     * @return String[]
     */
    public static String[] getIPStrScope(String ipAddr, String mask) {
        int[] ipIntArr = IPv4Util.getIPIntScope(ipAddr, mask);
        return new String[]{IPv4Util.intToIp(ipIntArr[0]),
                IPv4Util.intToIp(ipIntArr[0])};
    }


    /*
     * @Author Eric Zheng
     * @Description 获取当前机器的网络ip
     * @Date 13:39 2019/6/26
     **/
    public static InetAddress getLocalHostLANAddress() {
        try {
            InetAddress candidateAddress = null;
            // 遍历所有的网络接口
            for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements(); ) {
                NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                // 在所有的接口下再遍历IP
                for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                    InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {// 排除loopback类型地址
                        if (inetAddr.isSiteLocalAddress()) {
                            // 如果是site-local地址，就是它了
                            return inetAddr;
                        } else if (candidateAddress == null) {
                            // site-local类型的地址未被发现，先记录候选地址
                            candidateAddress = inetAddr;
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                return candidateAddress;
            }
            // 如果没有发现 non-loopback地址.只能用最次选的方案
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            return jdkSuppliedAddress;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
    * @Author juzheng
    * @Description 判断字符串是否为IPv4
    * @Date 2:13 PM 2019/8/15
    * @Param [addr]
    * @return boolean
    */
    public static boolean isIPv4(String addr)
    {
        if(addr.length() < 7 || addr.length() > 15 || "".equals(addr))
        {
            return false;
        }
        /**
         * 判断IP格式和范围
         */
       // String rexp = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
        String rexp="(?<![0-9])(?:(?:25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})[.](?:25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})[.](?:25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})[.](?:25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2}))(?![0-9])";

        Pattern pat = Pattern.compile(rexp);

        Matcher mat = pat.matcher(addr);
        Boolean aBoolean = mat.matches();

        return aBoolean;
    }

    /*
    * @Author juzheng
    * @Description 判断是否是正确的ip地址段的格式，既可以是~也可以是/
    * @Date 3:50 PM 2019/8/15
    * @Param []
    * @return boolean
    */
    public static boolean isIPv4s(String ipRange){
        if (ipRange.contains("~") && ipRange.contains("/")) {
            return false;
        } else if (ipRange.equals("*") || ipRange.equals("0.0.0.0")) {
            return true;
        } else if (ipRange.contains("~")) {
            String[] ips = ipRange.split("~");
            return  (isIPv4(ips[0])&&isIPv4(ips[0]));

        } else if (ipRange.contains("/")) {
            String minIpAddres = IPv4Util.getMinIpAddres(ipRange);
            String maxIpAddres = IPv4Util.getMaxIpAddres(ipRange);
            return (isIPv4(minIpAddres)&&isIPv4(maxIpAddres));
        } else {
            return isIPv4(ipRange);
        }
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String ipAddr = "255.127.127.127";

        byte[] bytearr = IPv4Util.ipToBytesByInet(ipAddr);

        StringBuffer byteStr = new StringBuffer();

        for (byte b : bytearr) {
            if (byteStr.length() == 0) {
                byteStr.append(b);
            } else {
                byteStr.append("," + b);
            }
        }
        System.out.println("IP: " + ipAddr + " ByInet --> byte[]: [ " + byteStr
                + " ]");

        bytearr = IPv4Util.ipToBytesByReg(ipAddr);
        byteStr = new StringBuffer();

        for (byte b : bytearr) {
            if (byteStr.length() == 0) {
                byteStr.append(b);
            } else {
                byteStr.append("," + b);
            }
        }
        System.out.println("IP: " + ipAddr + " ByReg  --> byte[]: [ " + byteStr
                + " ]");

        System.out.println("byte[]: " + byteStr + " --> IP: "
                + IPv4Util.bytesToIp(bytearr));

        //int ipInt = IPv4Util.ipToInt(ipAddr);
        int ipInt = 0;

        System.out.println("IP: " + ipAddr + "  --> int: " + ipInt);

        System.out.println("int: " + ipInt + " --> IP: "
                + IPv4Util.intToIp(ipInt));

        String ipAndMask = "172.17.20.81/24";

        int[] ipscope = IPv4Util.getIPIntScope(ipAndMask);
        System.out.println(ipAndMask + " --> int地址段：[ " + ipscope[0] + ","
                + ipscope[1] + " ]");

        System.out.println(ipAndMask + " --> IP 地址段：[ "
                + IPv4Util.intToIp(ipscope[0]) + ","
                + IPv4Util.intToIp(ipscope[1]) + " ]");

        String ipAddr1 = "192.168.1.1", ipMask1 = "255.255.0.0";

        int[] ipscope1 = IPv4Util.getIPIntScope(ipAddr1, ipMask1);
        System.out.println(ipAddr1 + " , " + ipMask1 + "  --> int地址段 ：[ "
                + ipscope1[0] + "," + ipscope1[1] + " ]");

        System.out.println(ipAddr1 + " , " + ipMask1 + "  --> IP地址段 ：[ "
                + IPv4Util.intToIp(ipscope1[0]) + ","
                + IPv4Util.intToIp(ipscope1[1]) + " ]");

        System.out.println( IPv4Util.ipToLong("192.100.0.0"));
        System.out.println( IPv4Util.ipToLong("192.100.255.255"));

        System.out.println("src=192.100.73.169  dst=102.220.249.2");
        String min="192.100.0.0/24";
        int[] ipscopemin = IPv4Util.getIPIntScope(min);
        System.out.println("IP地址段 ：[ "
                + IPv4Util.intToIp(ipscopemin[0]) + ","
                + IPv4Util.intToIp(ipscopemin[1]) + " ]");

        String max="172.16.0.0/30";
        int[] ipscopemax = IPv4Util.getIPIntScope(max);
        System.out.println( "IP地址段 ：[ "
                + IPv4Util.intToIp(ipscopemax[0]) + ","
                + IPv4Util.intToIp(ipscopemax[1]) + " ]");

        System.out.println(isIPv4("101.132.139.188"));
        System.out.println(isIPv4("1.139.188"));
        System.out.println(isIPv4("1.13.139.444"));
        System.out.println(isIPv4("101.132.139.188"));
        System.out.println(isIPv4("101.12.19.188"));
        System.out.println(isIPv4("101.132.139.200"));
        System.out.println(isIPv4("101.132.139.299"));
        System.out.println(isIPv4("101.132.139.fedsa"));
        System.out.println(isIPv4("101.132.139.1vdcs88"));
        System.out.println(isIPv4("101139.188"));

    }



  /*
   * @Author Eric Zheng
   * @Description 获取掩码，传入的是数字 例如24
   * @Date 9:59 2019/8/14
   **/
    public static String getMask(int maskLength){
        int binaryMask = 0xFFFFFFFF << (32 - maskLength);
        StringBuffer sb = new StringBuffer();
        for(int shift=24;shift>0;shift-=8){
            sb.append(Integer.toString((binaryMask>>>shift)&0xFF));
            sb.append(".");
        }
        sb.append(Integer.toString(binaryMask&0xFF));
        return sb.toString();
    }

    /*
     * @Author Eric Zheng
     * @Description 获取网络ID，即也是CIDR表示的最小IP
     * @Date 10:24 2019/8/14
     **/
    public static String getMinIpAddres(String ipCidr){
        String[] ipMaskLen = ipCidr.split("\\/");
        String mask = getMask(Integer.parseInt(ipMaskLen[1]));
        String[] ips = ipMaskLen[0].split("\\.");
        String[] masks = mask.split("\\.");
        StringBuffer sb = new StringBuffer();
        for(int i=0;i<4;i++){
            sb.append(Integer.parseInt(ips[i])&Integer.parseInt(masks[i]));
            if(i!=3){
                sb.append(".");
            }
        }
        return sb.toString();
    }

    public static String getMaxIpAddres(String netIp){
        String[] split = netIp.split("\\/");
        String netId = getMinIpAddres(netIp);
        int num = Integer.parseInt(split[1]);
        int binaryMask = 0xFFFFFFFF << (32 - num);
        binaryMask = binaryMask ^ 0xFFFFFFFF;
        StringBuffer sb = new StringBuffer(15);
        for(int shift=24;shift>0;shift-=8){
            sb.append(Integer.toString((binaryMask>>>shift)&0xFF));
            sb.append(".");
        }
        sb.append(Integer.toString(binaryMask&0xFF));
        String maskReverse = sb.toString();

        String[] netIdArray = netId.split("\\.");
        String[] maskRevertArray = maskReverse.split("\\.");
        StringBuffer sb1 = new StringBuffer();
        for(int i=0,len=netIdArray.length;i<len;i++){
            sb1.append(Integer.parseInt(netIdArray[i])+Integer.parseInt(maskRevertArray[i]));
            if(i!=len-1){
                sb1.append(".");
            }
        }
        return sb1.toString();
    }
}
