//package com.boyitech.logstream.core.test;
//
//import com.sun.jna.Library;
//import com.sun.jna.Memory;
//import com.sun.jna.Native;
//import com.sun.jna.Pointer;
//import com.sun.jna.WString;
//import com.sun.jna.platform.win32.WinDef;
//import com.sun.jna.platform.win32.WinNT.HANDLE;
//import com.sun.jna.ptr.IntByReference;
//
//public class WindowsEventTest {
//	public interface Kernel32 extends Library {
//		Kernel32 INSTANCE = (Kernel32) Native.loadLibrary("kernel32", Kernel32.class);
//		int GetLastError();
//		Pointer LocalFree(Pointer hMem);
//	}
//
//	public interface Wevtapi extends Library {
//		Wevtapi INSTANCE = (Wevtapi) Native.loadLibrary("wevtapi", Wevtapi.class);
//
//		public final int EvtQueryChannelPath = 0x1;
//		public final int EvtQueryFilePath = 0x2;
//		public final int EvtQueryForwardDirection = 0x100;
//		final int EvtQueryReverseDirection = 0x200;
//		public final int EvtQueryTolerateQueryErrors = 0x1000;
//
//		public final int EvtSeekRelativeToFirst = 1;
//		public final int EvtSeekRelativeToLast = 2;
//		public final int EvtSeekRelativeToCurrent = 3;
//		public final int EvtSeekRelativeToBookmark = 4;
//		public final int EvtSeekOriginMask = 7;
//		public final int EvtSeekStrict = 0x10000;
//
//		public final int EvtRenderEventValues = 0;
//		public final int EvtRenderEventXml = 1;
//		public final int EvtRenderBookmark = 2;
//
//		HANDLE EvtQuery(HANDLE Session, WString Path, WString Query, int Flags);
//
//		boolean EvtSeek(HANDLE ResultSet, WinDef.LONGLONG Position, HANDLE Bookmark, int Timeout, int Flags);
//
//		boolean EvtNext(HANDLE ResultSet, int EventArraySize, HANDLE[] EventArray, int Timeout, int Flags,
//				IntByReference Returned);
//
//		boolean EvtRender(HANDLE Context, HANDLE Fragment, int Flags, int BufferSize, Pointer Buffer,
//				IntByReference BufferUsed, IntByReference PropertyCount);
//
//		boolean EvtClose(HANDLE h);
//	}
//
//	public static void main(String[] args) {
//		String eventLog = new WindowsEventTest().getEventLog("<QueryList><Query Path='System'><Select Path='System'>*[System[(EventID!=100)]]</Select></Query></QueryList>");
//
//	}
//
//	private String getEventLog(String query) {
//		StringBuffer eventLog = new StringBuffer();
//		HANDLE hResults = null;
//		try {
//			WString wQuery = new WString(query);
//			hResults = Wevtapi.INSTANCE.EvtQuery(null, null, wQuery,
//					Wevtapi.INSTANCE.EvtQueryChannelPath | Wevtapi.INSTANCE.EvtQueryTolerateQueryErrors);
//			if (hResults == null)
//				throw new Exception("EvtQuery Error errorCode=" + getLastErrorWrapper());
//
//			boolean ret = Wevtapi.INSTANCE.EvtSeek(hResults, new WinDef.LONGLONG(0), null, 0,
//					Wevtapi.INSTANCE.EvtSeekRelativeToFirst);
//			if (!ret)
//				throw new Exception("EvtSeek Error errorCode=" + getLastErrorWrapper());
//
//			HANDLE[] handles = new HANDLE[100];
//			IntByReference intRef = new IntByReference(0);
//			while (true) {
//				if (!Wevtapi.INSTANCE.EvtNext(hResults, 100, handles, 0, 0, intRef)) {
//					if (getLastErrorWrapper() == 259)
//						break;
//					else
//						throw new Exception("EvtNext Error errorCode=" + getLastErrorWrapper());
//				} else {
//					for (int i = 0; i < intRef.getValue(); i++) {
//						eventLog.append(renderEvent(handles[i]));
//						handles[i] = null;
//					}
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			if (hResults != null)
//				Wevtapi.INSTANCE.EvtClose(hResults);
//		}
//		return eventLog.toString();
//	}
//
//	private String renderEvent(HANDLE h) throws Exception {
//		int bufferSize = 0;
//		Pointer buffer = null;
//		IntByReference bufferUsed = new IntByReference(0);
//		IntByReference propertyCount = new IntByReference(0);
//		if(!Wevtapi.INSTANCE.EvtRender(null, h, Wevtapi.INSTANCE.EvtRenderEventXml, bufferSize, buffer, bufferUsed, propertyCount)){
//			if(getLastErrorWrapper() == 122) {
//				bufferSize = bufferUsed.getValue();
//				buffer = new Memory(bufferSize);
//				if(buffer != null)
//					Wevtapi.INSTANCE.EvtRender(null, h, Wevtapi.INSTANCE.EvtRenderEventXml, bufferSize, buffer, bufferUsed, propertyCount);
//				else
//					throw new Exception("malloc Error errorCode=" + getLastErrorWrapper());
//			}
//			if(0 != getLastErrorWrapper())
//				throw new Exception("EvtRender Error errorCode=" + getLastErrorWrapper());
//		}
//
//		return buffer.getWideString(0);
//	}
//
//	private int getLastErrorWrapper() {
//		return Kernel32.INSTANCE.GetLastError();
//	}
//}