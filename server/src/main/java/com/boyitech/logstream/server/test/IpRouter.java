package com.boyitech.logstream.server.test;

import com.boyitech.logstream.core.util.IPv4Util;

public class IpRouter {
    /**最大IP的256进制LONG值的KEY*/
    public static final String CIDR_MAX_IP = "CIDR_MAX_IP";
    /**最小IP的256进制LONG值的KEY*/
    public static final String CIDR_MIN_IP = "CIDR_MIN_IP";


    /**
     * 构造方法
     */


    public IpRouter(){
    }


    /*
     * 获取网络ID，即也是CIDR表示的最小IP
     * @param ipCidr CIDR法表示的IP，例如：172.16.0.0/12
     * @return 网络ID，即也是CIDR表示的最小IP
     */
    private String getNetworkId(String ipCidr){
        String[] ipMaskLen = ipCidr.split("\\/");
        String mask = this.getMask(Integer.parseInt(ipMaskLen[1]));
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
    /*
     * 获取掩码
     * @param maskLength 网络ID位数
     * @return
     */
    private String getMask(int maskLength){
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
     * 获取IP最大值
     * @param netId 网络ID
     * @param maskReverse 掩码反码
     * @return
     */
    private String getMaxIpAddres(String netId,String maskReverse){
        String[] netIdArray = netId.split("\\.");
        String[] maskRevertArray = maskReverse.split("\\.");
        StringBuffer sb = new StringBuffer();
        for(int i=0,len=netIdArray.length;i<len;i++){
            sb.append(Integer.parseInt(netIdArray[i])+Integer.parseInt(maskRevertArray[i]));
            if(i!=len-1){
                sb.append(".");
            }
        }
        return sb.toString();
    }

    private String getNewMaxIpAddres(String netIp){
        String[] split = netIp.split("\\/");
        String netId = getNetworkId(netIp);
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
    /*
     * 获取掩码整型数组
     * @param maskLength 网络ID位数
     * @return
     */
    private int[] getmaskArray(int maskLength){
        int binaryMask = 0xFFFFFFFF << (32 - maskLength);
        int[] mask = new int[4];
        for(int shift=24,k=0;shift>0;shift-=8){
            mask[k] = (binaryMask>>>shift)& 0xFF;
            k++;
        }
        mask[3] = binaryMask & 0xFF;
        return mask;
    }
    /*
     * 获取掩码的反码
     * @param maskLength 网络ID位数
     * @return
     */
    private String getMaskRevert(int maskLength){
        int binaryMask = 0xFFFFFFFF << (32 - maskLength);
        binaryMask = binaryMask ^ 0xFFFFFFFF;
        StringBuffer sb = new StringBuffer(15);
        for(int shift=24;shift>0;shift-=8){
            sb.append(Integer.toString((binaryMask>>>shift)&0xFF));
            sb.append(".");
        }
        sb.append(Integer.toString(binaryMask&0xFF));
        return sb.toString();
    }
    /*
     * IP地址转换为一个256进制的long整数
     * @param ip
     * @return
     */
    private long ipToNumeric(String ip){
        String[] ips = ip.split("\\.");
        Long[] ipLong = new Long[4];
        for(int i=0,len=ips.length;i<len;i++){
            ipLong[i] = Long.parseLong(ips[i]);
        }
        long result = ipLong[3] & 0xFF;
        result |= ((ipLong[2]<<8)) & 0xFF00;
        result |= ((ipLong[1]<<16)) & 0xFF0000;
        result |= ((ipLong[0]<<24)) & 0xFF000000;
        return result;
    }


    private void print(Object obj){
        System.out.println(obj);
    }
    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

        IpRouter router = new IpRouter();

        String ipCidr = "172.28.64.5/22";
        int num = 22;

        String mask = router.getMask(num);
        router.print("掩码:["+mask+"]");

        String networkId = router.getNetworkId(ipCidr);
        router.print("网络ID（最小IP）:["+networkId+"]");

        String maskRevert = router.getMaskRevert(num);
        router.print("掩码:["+mask+"]"+"的反码:["+maskRevert+"]");

        String maxIpAddres = router.getMaxIpAddres(networkId, maskRevert);
        router.print("最大IP地址:["+maxIpAddres+"]");

        long minIpValue = router.ipToNumeric(networkId);
        long maxIpValue = router.ipToNumeric(maxIpAddres);
        router.print("["+ipCidr+"]IP 256进制值范围为["+minIpValue+","+maxIpValue+"]");
        System.out.println(IPv4Util.getMaxIpAddres("172.28.64.5/22"));
        System.out.println(IPv4Util.getMinIpAddres("172.28.64.5/22"));

    }

}