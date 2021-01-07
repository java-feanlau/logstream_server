//package com.boyitech.logstream.core.test;
//
//import org.joda.time.DateTime;
//
//import com.sun.jna.platform.win32.Advapi32Util.EventLogIterator;
//import com.sun.jna.platform.win32.Advapi32Util.EventLogRecord;
//
//public class JNATest {
//
//	public static void main(String args[]) {
//		EventLogIterator iter = new EventLogIterator("application");
//        int count = 0;
//        while (iter.hasNext()) {
//            EventLogRecord record = iter.next();
////            if (record.getData() == null) {
////                continue;
////            }
//            count++;
////            System.out.println(record.getRecordNumber()
////                    + " Event Time: " + new Date(record.getRecord().TimeGenerated.longValue() * 1000L).toString()
////                    + ": Event ID: " + record.getEventId()
////                    + ", Event Type: " + record.getType()
////                    + ", Event Data: " + record.getData()
////                    + ", Event Source: " + record.getSource());
//            System.out.println(new DateTime(record.getRecord().TimeGenerated.longValue()*1000).toString() + "  "
//            		+ record.getRecord().EventID.shortValue() + "  " + record.getRecordNumber() + "  " + record.getSource() + "  "
//            		+ record.getStatusCode() + "  " + record.getType()  + "  "
//            		+ record.getRecord().EventCategory + "  "
//            		+ toString(record.getStrings()));
//            if (count > 10) {
//                break;
//            }
//        }
//	}
//
//	public static String toString(String strs[]) {
//		if(strs==null)
//			return "NULL";
//		StringBuffer sb = new StringBuffer();
//		for(String s : strs) {
//			sb.append(s);
//			sb.append(" ");
//		}
//		return sb.toString();
//	}
//
//}
