package yanry.lib.java.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author yanry
 *
 *         2014-5-12 上午10:11:30
 */
public class ConversionUtil {
	public static String intToIp(int ip) {
        StringBuilder sb = new StringBuilder();
		// 直接右移24位
		sb.append(String.valueOf((ip >>> 24)));
		sb.append(".");
		// 将高8位置0，然后右移16位
		sb.append(String.valueOf((ip & 0x00FFFFFF) >>> 16));
		sb.append(".");
		// 将高16位置0，然后右移8位
		sb.append(String.valueOf((ip & 0x0000FFFF) >>> 8));
		sb.append(".");
		// 将高24位置0
		sb.append(String.valueOf((ip & 0x000000FF)));
		return sb.toString();
	}

	public static long ipToInt(String ip) {
		if ("0:0:0:0:0:0:0:1".equals(ip)) {
			ip = "172.0.0.1";
		}
		int[] ipSection = new int[4];
		// 先找到IP地址字符串中.的位置
		int position1 = ip.indexOf(".");
		int position2 = ip.indexOf(".", position1 + 1);
		int position3 = ip.indexOf(".", position2 + 1);
		// 将每个.之间的字符串转换成整型
		ipSection[0] = Integer.parseInt(ip.substring(0, position1));
		ipSection[1] = Integer.parseInt(ip.substring(position1 + 1, position2));
		ipSection[2] = Integer.parseInt(ip.substring(position2 + 1, position3));
		ipSection[3] = Integer.parseInt(ip.substring(position3 + 1));
		return (ipSection[0] << 24) + (ipSection[1] << 16) + (ipSection[2] << 8) + ipSection[3];
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
