/**
 * 
 */
package lib.common.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author yanry
 *
 *         2014-5-12 上午10:11:30
 */
public class ConversionUtil {
	public static String longToIp(long longIp) {
		StringBuffer sb = new StringBuffer("");
		// 直接右移24位
		sb.append(String.valueOf((longIp >>> 24)));
		sb.append(".");
		// 将高8位置0，然后右移16位
		sb.append(String.valueOf((longIp & 0x00FFFFFF) >>> 16));
		sb.append(".");
		// 将高16位置0，然后右移8位
		sb.append(String.valueOf((longIp & 0x0000FFFF) >>> 8));
		sb.append(".");
		// 将高24位置0
		sb.append(String.valueOf((longIp & 0x000000FF)));
		return sb.toString();
	}

	public static long ipToLong(String strIp) {
		if ("0:0:0:0:0:0:0:1".equals(strIp)) {
			strIp = "172.0.0.1";
		}
		long[] ip = new long[4];
		// 先找到IP地址字符串中.的位置
		int position1 = strIp.indexOf(".");
		int position2 = strIp.indexOf(".", position1 + 1);
		int position3 = strIp.indexOf(".", position2 + 1);
		// 将每个.之间的字符串转换成整型
		ip[0] = Long.parseLong(strIp.substring(0, position1));
		ip[1] = Long.parseLong(strIp.substring(position1 + 1, position2));
		ip[2] = Long.parseLong(strIp.substring(position2 + 1, position3));
		ip[3] = Long.parseLong(strIp.substring(position3 + 1));
		return (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3];
	}

	public static byte[] intToByteArray(int value, ByteOrder bo) {
		ByteBuffer converter = ByteBuffer.allocate(4);
		converter.order(bo == null ? ByteOrder.nativeOrder() : bo);
		converter.putInt(value);
		return converter.array();
	}

	public static int byteArrayToInt(byte[] valueBuf, int offset, ByteOrder bo) {
		ByteBuffer converter = ByteBuffer.wrap(valueBuf);
		converter.order(bo == null ? ByteOrder.nativeOrder() : bo);
		return converter.getInt(offset);
	}

	public static byte[] concatArrays(byte[]... arrays)
	{
		int len = 0;
		for (byte[] a : arrays)
		{
			len += a.length;
		}
		byte[] b = new byte[len];
		int offs = 0;
		for (byte[] a : arrays)
		{
			System.arraycopy(a, 0, b, offs, a.length);
			offs += a.length;
		}
		return b;
	}
}
