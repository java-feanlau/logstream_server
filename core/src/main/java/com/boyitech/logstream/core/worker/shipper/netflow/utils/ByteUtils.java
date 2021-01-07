package com.boyitech.logstream.core.worker.shipper.netflow.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class ByteUtils {
	public static byte IntToByte(int i) {
		return (byte) i;
	}

	public static byte[] IntToBytes(int i) {
		byte abyte0[] = new byte[2];
		abyte0[1] = (byte) (0xff & i);
		abyte0[0] = (byte) ((0xff00 & i) >> 8);
		return abyte0;
	}

	public static byte[] IntToBytes4(int i) {
		byte abyte0[] = new byte[4];
		abyte0[3] = (byte) (0xff & i);
		abyte0[2] = (byte) ((0xff00 & i) >> 8);
		abyte0[1] = (byte) ((0xff0000 & i) >> 16);
		abyte0[0] = (byte) ((0xff000000 & i) >> 24);
		return abyte0;
	}

	public static byte[] LongToBytes8(long l) {
		byte abyte0[] = new byte[8];
		abyte0[7] = (byte) (int) (255L & l);
		abyte0[6] = (byte) (int) ((65280L & l) >> 8);
		abyte0[5] = (byte) (int) ((0xff0000L & l) >> 16);
		abyte0[4] = (byte) (int) ((0xff000000L & l) >> 24);
		abyte0[3] = (byte) (int) ((0xff00000000L & l) >> 32);
		abyte0[2] = (byte) (int) ((0xff0000000000L & l) >> 40);
		abyte0[1] = (byte) (int) ((0xff000000000000L & l) >> 48);
		abyte0[0] = (byte) (int) ((0xff00000000000000L & l) >> 56);
		return abyte0;
	}

	public static long Bytes8ToLong(byte abyte0[], int offset) {
		return (255L & (long) abyte0[offset]) << 56
				| (255L & (long) abyte0[offset + 1]) << 48
				| (255L & (long) abyte0[offset + 2]) << 40
				| (255L & (long) abyte0[offset + 3]) << 32
				| (255L & (long) abyte0[offset + 4]) << 24
				| (255L & (long) abyte0[offset + 5]) << 16
				| (255L & (long) abyte0[offset + 6]) << 8
				| (255L & (long) abyte0[offset + 7]);
	}

	public static void LongToBytes4(long l, byte abyte0[]) {
		abyte0[3] = (byte) (int) (255L & l);
		abyte0[2] = (byte) (int) ((65280L & l) >> 8);
		abyte0[1] = (byte) (int) ((0xff0000L & l) >> 16);
		abyte0[0] = (byte) (int) ((0xffffffffff000000L & l) >> 24);
	}

	public static void IntToBytes(int i, byte abyte0[]) {
		abyte0[1] = (byte) (0xff & i);
		abyte0[0] = (byte) ((0xff00 & i) >> 8);
	}

	public static void IntToBytes4(int i, byte abyte0[]) {
		abyte0[3] = (byte) (0xff & i);
		abyte0[2] = (byte) ((0xff00 & i) >> 8);
		abyte0[1] = (byte) ((0xff0000 & i) >> 16);
		abyte0[0] = (byte) (int) ((0xffffffffff000000L & (long) i) >> 24);
	}

	public static int Bytes4ToInt(byte abyte0[], int offset) {
		return (0xff & abyte0[offset]) << 24
				| (0xff & abyte0[offset + 1]) << 16
				| (0xff & abyte0[offset + 2]) << 8 | 0xff & abyte0[offset + 3];
	}

	public static long Bytes4ToLong(byte abyte0[], int offset) {
		return (255L & (long) abyte0[offset + 0]) << 24
				| (255L & (long) abyte0[offset + 1]) << 16
				| (255L & (long) abyte0[offset + 2]) << 8 | 255L
				& (long) abyte0[offset + 3];
	}

	static public final long byte2long(byte[] p, int off, int len) {
		long ret = 0;
		int done = off + len;
		for (int i = off; i < done; i++)
			ret = ((ret << 8) & 0xffffffff) + (p[i] & 0xff);

		return ret;
	}

	public static int int2long(byte[] p, int offset) {
		return (int) byte2long(p, offset, 2);
	}


	public static short Bytes2ToShort(byte abyte0[], int offset) {
		return (short) ((0xff & abyte0[offset]) << 8
						| (0xff & abyte0[offset + 1]));
	}


	public static String long2ip(long ipaddr) {
		long y = ipaddr % 256;
		long m = (ipaddr - y) / (256 * 256 * 256);
		long n = (ipaddr - 256 * 256 *256 * m - y) / (256 * 256);
		long x = (ipaddr - 256 * 256 *256 * m - 256 * 256 *n - y) / 256;
		return m + "." + n + "." + x + "." + y;
	}

	public static String long2time(long time){
		Date date = new Date(time);
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX"); //iso8601
		return sdf2.format(date);
	}


}
