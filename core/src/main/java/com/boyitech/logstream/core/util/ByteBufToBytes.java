package com.boyitech.logstream.core.util;

import io.netty.buffer.ByteBuf;

public class ByteBufToBytes {
    /**
     * 将ByteBuf转换为byte[]
     * @param datas
     * @return
     */
    public static byte[] read(ByteBuf datas) {
        byte[] bytes = new byte[datas.readableBytes()];// 创建byte[]
        datas.readBytes(bytes);// 将ByteBuf转换为byte[]
        datas.release();
        return bytes;
    }
}
