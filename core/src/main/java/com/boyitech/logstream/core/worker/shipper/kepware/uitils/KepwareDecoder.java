package com.boyitech.logstream.core.worker.shipper.kepware.uitils;

import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.worker.shipper.kepware.KepwareShipperConfig;
import com.boyitech.logstream.core.worker.shipper.kepware.info.KepwareInfo;


import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author Eric
 * @Title: PacketToNetflow
 * @date 2018/12/25 10:31
 * @Description: TODO
 */
public class KepwareDecoder {
    private static final int MESSAGE_FIRST_HEAD = 15;
    private final KepwareShipperConfig config;
    private List<String> eventList = new  ArrayList();


    public KepwareDecoder(byte[] inputStream, KepwareShipperConfig config) {
        this.config = config;
        analysis(ByteUtils.inversionBytes(inputStream));
    }

    private void analysis(byte[] inputStream) {
        short messageNum = ByteUtils.Bytes2ToShort(inputStream, 8);
        int index = MESSAGE_FIRST_HEAD;
        //16 第一条信息
        index++;
        for (short i = 0; i < messageNum; i++) {
            index += 2;
            String time = analysisTime(inputStream, index);
            index += 24;
            short logLevel = ByteUtils.Bytes2ToShort(inputStream, index);
            index += 4;
            short userLength = ByteUtils.Bytes2ToShort(inputStream, index);
            index += 4;
            byte[] userBytes = Arrays.copyOfRange(inputStream, index, index + 2 * userLength);
            String username = null;
            try {
                username = new String(userBytes, "unicode");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            index += 2 * userLength;
            short sourceLength = ByteUtils.Bytes2ToShort(inputStream, index);
            index += 4;
            byte[] sourceBytes = Arrays.copyOfRange(inputStream, index, index + 2 * sourceLength);
            String source = null;
            try {
                source = new String(sourceBytes, "unicode");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            index += 2 * sourceLength;
            short eventLength = ByteUtils.Bytes2ToShort(inputStream, index);
            index += 4;
            byte[] eventBytes = Arrays.copyOfRange(inputStream, index, index + 2 * eventLength);
            String event = null;
            try {
                event = new String(eventBytes, "unicode");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            index += 2 * eventLength;
            KepwareInfo kepwareInfo = new KepwareInfo();
            kepwareInfo.setTime(time);
            kepwareInfo.setUsername(username);
            kepwareInfo.setLogLevel(logLevel+"");
            kepwareInfo.setSource(source);
            kepwareInfo.setEvent(event);

            String kepwareInfojSON = GsonHelper.toJson(kepwareInfo);
            eventList.add(kepwareInfojSON);
//            System.out.println(kepwareInfojSON);

        }
    }

    /*
     * @Author Eric Zheng
     * @Description 将16个字节解析为时间 28/12/2018 12:35:48:89
     * @Date 10:22 2019/1/15
     * @Param [inputStream, index]
     * @return java.lang.String
     **/
    public String analysisTime(byte[] inputStream, int index) {
        short year = ByteUtils.Bytes2ToShort(inputStream, index);
        short month = ByteUtils.Bytes2ToShort(inputStream, index + 2);
        short day = ByteUtils.Bytes2ToShort(inputStream, index + 6);
        short hour = ByteUtils.Bytes2ToShort(inputStream, index + 8);
        short minute = ByteUtils.Bytes2ToShort(inputStream, index + 10);
        short second = ByteUtils.Bytes2ToShort(inputStream, index + 12);
        short millisecond = ByteUtils.Bytes2ToShort(inputStream, index + 14);

        String time = day + "/" + month + "/" + year + " " + hour + ":" + minute + ":" + second + ":" + millisecond;


        return time;
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();

    }

    public List<String> getEventList() {
        return eventList;
    }
}