package com.boyitech.logstream.server.test.indexer;


import com.boyitech.logstream.core.info.Event;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ReadSource {

   // private final static  String  FILEPATH = "C:\\Users\\Eric\\Desktop\\aptTest.txt";
   private final static  String  FILEPATH = "/Users/juzheng/Downloads/IndexFile_Test_Out.txt";
    public static List readFileToEvent() {
        List<Event> eventsList = new ArrayList<Event>();
        try {
            BufferedReader br = new BufferedReader(new FileReader( new File(FILEPATH)));
            String s = null;
            while ((s = br.readLine()) != null) {
//               System.out.println(s);
                Event event = new Event();
                event.setMessage(s);
                eventsList.add(event);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return eventsList;
    }

    public static void writeFile(List<Event> list) {
        List<Event> eventsList = new ArrayList<>();
        try {
            BufferedWriter br = new BufferedWriter(new FileWriter( new File(FILEPATH)));
            for (Event event : list) {
                br.write(event.getJsonMessage()+"\r");
                //br.write(event.getMessage()+"\r");
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 读取文件，将jspn中的message提取出来
     * @param path
     * @return
     */
/*    public static List<Event> readJsonToGetMes(String path){
        List<Event> eventsList = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader( new File(path)));
            String s = null;
            while ((s = br.readLine()) != null) {
                Event event = new Event();
                //Map<String, Object> map = GrokUtil.getMap("(?<aaa>(?<=\"message\"=>\").*?(?=\"))", s);
                   Map<String, Object> map = GrokUtil.getMap("(?<aaa>(?<=\"message\"=>\").*?(?=\",))", s);
                event.setMessage((String) map.get("aaa"));
                eventsList.add(event);

            }
            br.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return eventsList;
    }*/


    /**
     * 读取文件，将message提取出来
     * @param path
     * @return
     */
    public static List<Event> readMessage(String path){
        List<Event> eventsList = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(path)));
            String s = null;
            while ((s = br.readLine()) != null) {
                Event event = new Event();
                event.setMessage(s);
                event.setSource("1.1.1.1");
                eventsList.add(event);
            }
            br.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return eventsList;
    }

    //一次性读取文件所有内容
    public static List<Event> readToString(String fileName) {
        String encoding = "UTF-8";
        File file = new File(fileName);
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            List<Event> eventsList = new ArrayList<>();
            Event event = new Event();
            String s = new String(filecontent,encoding);
            event.setMessage(s);
            event.setSource("1.1.1.1");
            eventsList.add(event);
            return  eventsList;

        } catch (Exception e) {
            e.printStackTrace();
            return  null;
        }
    }



    public static void main(String[] args) {
       List<Event> events=readMessage("/Users/juzheng/Downloads/apachSuccess.txt");
        for (Event event : events) {
            System.out.println(event.getMessage());

        }

    }

}
