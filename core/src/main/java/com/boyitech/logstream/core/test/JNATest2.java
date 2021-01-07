//package com.boyitech.logstream.core.test;
//
//import java.io.UnsupportedEncodingException;
//import java.nio.ByteBuffer;
//import java.nio.CharBuffer;
//import java.util.Arrays;
//
//import com.sun.jna.Memory;
//import com.sun.jna.Pointer;
//import com.sun.jna.platform.win32.Kernel32;
//import com.sun.jna.platform.win32.W32Errors;
//import com.sun.jna.platform.win32.Win32Exception;
//import com.sun.jna.platform.win32.WinNT;
//import com.sun.jna.platform.win32.WinNT.EVENTLOGRECORD;
//import com.sun.jna.platform.win32.WinNT.HANDLE;
//import com.sun.jna.ptr.IntByReference;
//
//public class JNATest2 {
//
//	public static void main(String args[]) throws UnsupportedEncodingException {
//		HANDLE h = com.sun.jna.platform.win32.Advapi32.INSTANCE.OpenEventLog(null, "application");
//		IntByReference pnBytesRead = new IntByReference();
//		IntByReference pnMinNumberOfBytesNeeded = new IntByReference();
//
//		IntByReference pOldestRecord = new IntByReference();
//		com.sun.jna.platform.win32.Advapi32.INSTANCE.GetOldestEventLogRecord(h, pOldestRecord);
//		int dwRecord = pOldestRecord.getValue();
//		System.out.println("OLD: " + dwRecord);
//		IntByReference pRecordCount = new IntByReference();
//		com.sun.jna.platform.win32.Advapi32.INSTANCE.GetNumberOfEventLogRecords(h, pRecordCount);
//		int dwRecordCnt = pRecordCount.getValue();
//
//
//		System.out.println("CNT: " + dwRecordCnt);
//
//		int bufSize = 0x7ffff; // (r.size()) * 2048;
//		Memory buffer = new Memory(bufSize);
//		int rc = 0;
//		int cnt = 0;
//		if (Kernel32.INSTANCE.GetLastError() != 0)
//			throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
//		while (com.sun.jna.platform.win32.Advapi32.INSTANCE.ReadEventLog(h, WinNT.EVENTLOG_SEEK_READ /*| WinNT.EVENTLOG_SEQUENTIAL_READ*/
//				| WinNT.EVENTLOG_FORWARDS_READ /*| WinNT.EVENTLOG_BACKWARDS_READ*/
//				, dwRecord, buffer, bufSize, pnBytesRead, pnMinNumberOfBytesNeeded)) {
//			System.out.println("------------------------------------------------");
//			rc = Kernel32.INSTANCE.GetLastError();
//			if (rc == W32Errors.ERROR_INSUFFICIENT_BUFFER) {
//				throw new Win32Exception(rc);
//			}
//
//			int dwRead = pnBytesRead.getValue();
//			Pointer pevlr = buffer;
//
//			while (dwRead > 0) {
//				cnt++;
//				EVENTLOGRECORD record = new EVENTLOGRECORD(pevlr);
//
//				System.out.println("------------------------------------------------------------");
//				System.out.println(cnt + ". " + dwRecord + " Event ID: " + record.EventID.shortValue() + " SID: "
//						+ record.UserSidLength);
//
//				dwRecord++;
//
//				// WCHAR SourceName[]
//				// WCHAR Computername[]
//				{
//					ByteBuffer names = pevlr.getByteBuffer(record.size(),
//							(record.UserSidLength.intValue() != 0 ? record.UserSidOffset.intValue()
//									: record.StringOffset.intValue()) - record.size());
//					names.position(0);
//					CharBuffer namesBuf = names.asCharBuffer();
//					String[] splits = namesBuf.toString().split("\0");
//					System.out.println("SOURCE NAME: " + splits[0]);
//					System.out.println("COMPUTER NAME: " + splits[1]);
//				}
//				// SID UserSid
//				if (record.UserSidLength.intValue() != 0) {
//					ByteBuffer sid = pevlr.getByteBuffer(record.UserSidOffset.intValue(),
//							record.UserSidLength.intValue());
//					sid.position(0);
//					// CharBuffer sidBuf = sid.asCharBuffer();
//					byte[] dst = new byte[record.UserSidLength.intValue()];
//					sid.get(dst);
//					System.out.println("SID: " + Arrays.toString(dst));
//				} else {
//					System.out.println("SID: N/A");
//				}
//				// WCHAR Strings[]
//				{
//					ByteBuffer strings = pevlr.getByteBuffer(record.StringOffset.intValue(),
//							record.DataOffset.intValue() - record.StringOffset.intValue());
//					strings.position(0);
//					CharBuffer stringsBuf = strings.asCharBuffer();
//					System.out.println("STRINGS[" + record.NumStrings.intValue() + "]: \n" + stringsBuf.toString());
//				}
//				// BYTE Data[]
//				{
//					ByteBuffer data = pevlr.getByteBuffer(record.DataOffset.intValue(), record.DataLength.intValue());
//					data.position(0);
//					CharBuffer dataBuf = data.asCharBuffer();
//					System.out.println("DATA: \n" + new String(dataBuf.toString().getBytes("GB2312")));
//				}
//				// CHAR Pad[]
//				// DWORD Length;
//
//				dwRead -= record.Length.intValue();
//				pevlr = pevlr.share(record.Length.intValue());
//			}
//		}
//	}
//
//}
