package network.piranya.platform.node.utilities;

import java.nio.ByteBuffer;

public abstract class BitUtils {
	
	public static long intsToLong(int _1, int _2) {
		return (long)_1 << 32 | _2 & 0xFFFFFFFFL;
	}
	
	public static int shortsToInt(short _1, short _2) {
		return (_1 << 16) | _2;
	}
	
	public static int[] to2Ints(long n) {
		int _1 = (int)(n >> 32);
        int _2 = (int)n;
		return new int[] { _1, _2 };
	}
	
	public static short[] to2Shorts(int n) {
		return new short[] { (short)(n >> 16), (short) n };
	}
	
	public static byte[] toByteArray(int value) {
		return ByteBuffer.allocate(4).putInt(value).array();
	}
	
	public static int intFromByteArray(byte[] bytes, int offset) {
		return ByteBuffer.wrap(bytes, offset, 4).getInt();
	}
	
	public static long longFromByteArray(byte[] bytes, int offset) {
		return ByteBuffer.wrap(bytes, offset, 8).getLong();
	}
	
	public static short shortFromByteArray(byte[] bytes, int offset) {
		return ByteBuffer.wrap(bytes, offset, 2).getShort();
	}
	
	
	private BitUtils() { }
	
}
