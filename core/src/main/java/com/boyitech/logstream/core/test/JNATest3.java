//package com.boyitech.logstream.core.test;
//
//import java.nio.file.Path;
//import java.nio.file.Paths;
//
//import com.boyitech.logstream.core.util.FilePathHelper;
//import com.sun.jna.Memory;
//import com.sun.jna.Pointer;
//import com.sun.jna.platform.win32.Kernel32;
//import com.sun.jna.platform.win32.Wevtapi;
//import com.sun.jna.platform.win32.Win32Exception;
//import com.sun.jna.platform.win32.WinNT.HANDLE;
//import com.sun.jna.platform.win32.Winevt;
//import com.sun.jna.platform.win32.Winevt.EVT_FORMAT_MESSAGE_FLAGS;
//import com.sun.jna.platform.win32.Winevt.EVT_HANDLE;
//import com.sun.jna.ptr.IntByReference;
//
//public class JNATest3 {
//
//	static String bookmarkPath = Paths.get(FilePathHelper.ROOTPATH, "tmp").toString();
//
//	public static void main(String args[]) {
//
//		//HANDLE handle = Kernel32.INSTANCE.CreateEvent(null, true, true, null);
////		EVT_HANDLE bkmkhndl = Wevtapi.INSTANCE.EvtCreateBookmark("");
////		if (Kernel32.INSTANCE.GetLastError() != 0) {
////			System.out.println(Kernel32.INSTANCE.GetLastError());
////			throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
////		}
//		HANDLE handle = Kernel32.INSTANCE.CreateEvent(null, true, true, null);
//		if (handle == null) {
//			System.out.println(Kernel32.INSTANCE.GetLastError());
//			throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
//		}
//		EVT_HANDLE eventHandle = new EVT_HANDLE(handle.getPointer());
//		EVT_HANDLE hSubscription = Wevtapi.INSTANCE.EvtSubscribe(null, eventHandle, "Application", "*",
//				null, null, null, Winevt.EVT_SUBSCRIBE_FLAGS.EvtSubscribeStartAtOldestRecord);
//		if (hSubscription == null) {
//			System.out.println(Kernel32.INSTANCE.GetLastError());
//			throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
//		}
//		EVT_HANDLE[] eventArray = new EVT_HANDLE[20];
//		IntByReference Returned = new IntByReference();
//		if(Wevtapi.INSTANCE.EvtNext(hSubscription, 20, eventArray, Integer.MAX_VALUE, 0, Returned)) {
//			for(EVT_HANDLE h : eventArray) {
//				IntByReference s = new IntByReference();
//				EVT_HANDLE renderContextHandle = Wevtapi.INSTANCE.EvtCreateRenderContext(0, null, Winevt.EVT_RENDER_CONTEXT_FLAGS.EvtRenderContextSystem);
//				if(renderContextHandle!=null) {
//					Pointer pRenderedValues = null;
//					int dwBufferSize = 0;
//					IntByReference bufferUsed = new IntByReference();
//					IntByReference propertyCount = new IntByReference();
//					if(!Wevtapi.INSTANCE.EvtRender(renderContextHandle, h, Winevt.EVT_RENDER_FLAGS.EvtRenderEventValues,
//							dwBufferSize, pRenderedValues, bufferUsed, propertyCount)) {
//						dwBufferSize = bufferUsed.getValue();
//						pRenderedValues = new Memory(dwBufferSize);
//						if(pRenderedValues!=null) {
//							Wevtapi.INSTANCE.EvtRender(renderContextHandle, h, Winevt.EVT_RENDER_FLAGS.EvtRenderEventValues,
//									dwBufferSize, pRenderedValues, bufferUsed, propertyCount);
//							Winevt.EVT_VARIANT tmp = new Winevt.EVT_VARIANT();
//							tmp.use(pRenderedValues);
//							tmp.read();
//							System.out.println(tmp.getVariantType());
//							System.out.println("value: " + tmp.getValue());
//							EVT_HANDLE hMetadata = Wevtapi.INSTANCE.EvtOpenPublisherMetadata(null, tmp.getValue().toString(), null, 0, 0);
//							IntByReference status = new IntByReference();
//							if(hMetadata!=null) {
//								String pwsMessage = GetMessageString(hMetadata, h, status);
//								System.out.println(pwsMessage.trim());
//							}else {
//								System.out.println("failed");
//								String pwsMessage = GetMessageString(hMetadata, h, status);
//								System.out.println(pwsMessage.trim());
//								//throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
//							}
//						}
//					}else {
//						System.out.println(Kernel32.INSTANCE.GetLastError());
//						System.out.println(bufferUsed.getValue());
//						throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
//					}
//					getMessageStringFixed(null, h, s);
//				}else {
//					throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
//				}
//
//			}
//			saveBookmark("Application", eventArray[Returned.getValue()-1]);
//		}
//
//	}
//
//	protected static boolean saveBookmark(String channel, EVT_HANDLE handle) {
//		int dwBufferSize = 65535;
//		EVT_HANDLE bookmark = null;
//		Pointer pBookmarkXml = null;
//		try {
//			bookmark = Wevtapi.INSTANCE.EvtCreateBookmark(null);
//			if(bookmark==null) {
//				Exception e = new Win32Exception(Kernel32.INSTANCE.GetLastError());
//				System.out.println("EvtCreateBookmark失败");
//				return false;
//			}
//			if(!Wevtapi.INSTANCE.EvtUpdateBookmark(bookmark, handle)) {
//				Exception e = new Win32Exception(Kernel32.INSTANCE.GetLastError());
//				System.out.println("EvtUpdateBookmark失败");
//				//return false;
//			}
//			IntByReference bufferUsed = new IntByReference();
//			IntByReference propertyCount = new IntByReference();
//			pBookmarkXml = new Memory(dwBufferSize);
//			if(Wevtapi.INSTANCE.EvtRender(null, bookmark, Winevt.EVT_RENDER_FLAGS.EvtRenderBookmark, dwBufferSize, pBookmarkXml, bufferUsed, propertyCount)) {
//
//			}else {
//				Exception e = new Win32Exception(Kernel32.INSTANCE.GetLastError());
//				System.out.println("saveBookmark失败");
//				return false;
//			}
//			Path bookmarkFilePath = Paths.get(bookmarkPath, channel);
//			System.out.println("书签内容：\n" + pBookmarkXml.getWideString(0));
////			try {
////				Files.write(bookmarkFilePath, pBookmarkXml, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
////			} catch (IOException e) {
////				System.out.println("将书签内容保存到文件时发生异�?");
////				return false;
////			}
//			return true;
//		}finally {
//			if(bookmark!=null)
//				Wevtapi.INSTANCE.EvtClose(bookmark);
//			if(pBookmarkXml!=null)
//				pBookmarkXml.clear(dwBufferSize);
//		}
//	}
//
//	public static String getMessageStringFixed(EVT_HANDLE hMetadata, EVT_HANDLE hEvent, IntByReference status) {
//		char[] buffer = new char[65535];
//		boolean result = Wevtapi.INSTANCE.EvtFormatMessage(hMetadata, hEvent, 0, 0, null, EVT_FORMAT_MESSAGE_FLAGS.EvtFormatMessageXml, 65535, buffer, status);
//		String str = new String(buffer);
//		return str;
//	}
//
//	public static String GetMessageString(EVT_HANDLE hMetadata, EVT_HANDLE hEvent, IntByReference status) {
//		char[] buffer = new char[65535];
//		boolean result = Wevtapi.INSTANCE.EvtFormatMessage(hMetadata, hEvent, 0, 0, null, EVT_FORMAT_MESSAGE_FLAGS.EvtFormatMessageXml, 65535, buffer, status);
//		String str = new String(buffer);
//		return str;
//	}
//
//}
