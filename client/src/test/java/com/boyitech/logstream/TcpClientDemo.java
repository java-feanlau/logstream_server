package com.boyitech.logstream;

import java.io.*;
import java.net.Socket;

class TcpClientDemo {
    public static void main(String[] args) throws IOException {
        //创建Socket服务指定目的主机和端口
        Socket s = new Socket("172.17.100.100", 1025);
        while (true) {
            System.out.println("发信息:");
            BufferedReader ins = new BufferedReader(new InputStreamReader(System.in));
            String str = ins.readLine();
            //为了发送数据    获取Socket中的输出流
            OutputStream out = s.getOutputStream();
            out.write(str.getBytes());
            if ("886".equals(str)) {
                System.out.println("bye");
                s.close();
                System.exit(0);
            }
            //获取数据
            System.out.println("等待回复中:");
            InputStream in = s.getInputStream();
            byte[] data = new byte[1024];
            int len = in.read(data);
            System.out.println(new String(data, 0, len));
        }
    }
}