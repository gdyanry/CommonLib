package yanry.lib.java.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

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
     * @return result hex text
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
            append(sb, b);
        }
        return sb.toString();
    }

    private static void append(StringBuilder sb, byte b) {
        String strHex = Integer.toHexString(b & 0xFF);
        if (strHex.length() == 1) {
            // 每个字节由两个字符表示，位数不够，高位补0
            sb.append('0');
        }
        sb.append(strHex);
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
                append(sb, b);
                needSpace = true;
                if (++counter % width == 0) {
                    sb.append('\n');
                }
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        System.out.printf("%02x%n", Math.round(0xff * 0.98f));
        System.out.println(FileUtil.getMD5(new File("C:\\Users\\56916\\Downloads\\ySK9GRBpt_D4uHyYKsz3")));

//        System.out.println(Integer.toHexString(b));
//        System.out.println(Integer.toHexString(b & 0xFF));
    }
}
