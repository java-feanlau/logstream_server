package com.boyitech.logstream.core.test;


/**
 * @author Eric
 * @Title: upOrDownTest
 * @date 2019/1/24 13:47
 * @Description: TODO
 */
public class upOrDownTest {
    public static void main(String args[]) {
        StringBuffer sb = new StringBuffer();
        String moduleType = "file";
        for (int i = 0; i < moduleType.length(); i++) {
            char c = moduleType.charAt(i);
            if (i == 0) {
                sb.append(Character.toUpperCase(c));
            }else if(c == '_'){
                i++;
                c = moduleType.charAt(i);
                sb.append(Character.toUpperCase(c));
            }else {
                sb.append(c);
            }
        }
        sb.append("Shipper");
        System.out.println(sb);
    }
}
