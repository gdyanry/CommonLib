package yanry.lib.java.util;

import java.io.UnsupportedEncodingException;

/**
 * @author yanry
 * <p>
 * 2014-5-5 下午5:41:03
 */
public class HexUtil {

    /**
     * 字节数组转化成16进制表示的字符串
     *
     * @param joint 连接字符串
     * @param bytes 待转化字节数组
     * @return
     */
    public static String bytesToHex(String joint, byte... bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            if (joint != null && joint.length() > 0 && sb.length() > 0) {
                sb.append(joint);
            }
            sb.append(byteToHex(b));
        }
        return sb.toString();
    }

    private static String byteToHex(byte b) {
        String strHex = Integer.toHexString(b & 0xFF);
        // 每个字节由两个字符表示，位数不够，高位补0
        return (strHex.length() == 1) ? "0" + strHex : strHex;
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
                sb.append(byteToHex(b));
                needSpace = true;
                if (++counter % width == 0) {
                    sb.append('\n');
                }
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println(String.format("%02x", Math.round(0xff * 0.98f)));
        byte b = Byte.MIN_VALUE;
        System.out.println(Integer.toHexString(b));
        System.out.println(Integer.toHexString(b & 0xFF));
    }
}
