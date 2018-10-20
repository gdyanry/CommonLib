/**
 * 
 */
package lib.common.util;

import java.io.UnsupportedEncodingException;

/**
 * @author yanry
 * 
 *         2014-5-5 下午5:41:03
 */
public class HexUtil {

	public static String bytesToHex(byte... bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}
	
	public static String charsetHex(String input, String charset, int width)
			throws UnsupportedEncodingException {
		char[] charArr = input.toCharArray();
		StringBuilder sb = new StringBuilder(charset).append('\n');
		int counter = 0;
		for (char c : charArr) {
			sb.append('(').append(c).append(')');
			boolean needSpace = false;
			byte[] byteArr = String.valueOf(c).getBytes(charset);
			for (byte b : byteArr) {
				if (needSpace) {
					sb.append("   ");
				}
				sb.append(String.format("%02x", b));
				needSpace = true;
				if (++counter % width == 0) {
					sb.append('\n');
				}
			}
		}
		return sb.toString();
	}

	public static void main(String[] args) {
        System.out.println(String.format("%02x", Math.round(0xff * 0.97f)));
	}
}
