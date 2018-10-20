package lib.common.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StringUtil {

    public static String getLogTag(Class<?> c) {
        String name = c.getSimpleName();
        if (name.length() > 0) {
            return name;
        } else if (c.getEnclosingConstructor() != null) {
            return getLogTag(c.getEnclosingConstructor().getDeclaringClass());
        } else if (c.getEnclosingMethod() != null) {
            return getLogTag(c.getEnclosingMethod().getDeclaringClass());
        } else {
            return getLogTag(c.getSuperclass());
        }
    }

    public static String getClassName(Object o) {
        String c = o.getClass().getSimpleName();
        if (c.length() == 0) {
            return o.getClass().getSuperclass().getSimpleName();
        }
        return c;
    }

    public static String setFirstLetterCase(String word, boolean upper) {
        String firstLetter = word.substring(0, 1);
        return word.replaceFirst(firstLetter, upper ? firstLetter.toUpperCase() : firstLetter.toLowerCase());
    }

    public static String getExceptionStackTrace(Exception e) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        e.printStackTrace(new PrintWriter(out, true));
        return out.toString();
    }

    public static <T> String arrayToString(String separator, T[] array) {
        StringBuilder sb = new StringBuilder();
        if (array != null && array.length > 0) {
            for (T item : array) {
                sb.append(item).append(separator);
            }
            sb.delete(sb.lastIndexOf(separator), sb.length());
        }
        return sb.toString();
    }

    public static List<Integer> stringToIntList(String srcStr, String separator) {
        if (srcStr == null || srcStr.length() == 0 || srcStr.equals("null")) {
            return null;
        }
        String[] strArr = srcStr.split(separator + "+");
        List<Integer> list = new ArrayList<>(strArr.length);
        for (String s : strArr) {
            list.add(Integer.valueOf(s));
        }
        return list;
    }

    public static <T> String listToString(List<T> list, String separator) {
        if (list != null) {
            return arrayToString(separator, list.toArray());
        }
        return null;
    }

    public static String encrypt(String input, String charset, String algorithm)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        return encrypt(input, charset, md);
    }

    public static String encrypt(String input, String charset, MessageDigest md) throws UnsupportedEncodingException {
        return HexUtil.bytesToHex(md.digest(input.getBytes(charset)));
    }

    public static String generateFixedLengthNumber(Random r, int length) {
        return String.format("%0" + length + "d", r.nextInt((int) Math.pow(10, length)));
    }

    private static void appendSpace(StringBuilder stringBuilder, int level) {
        for (int i = 0; i < level; i++) {
            stringBuilder.append('\t');
        }
    }

    public static String formatJson(String raw) {
        if (raw == null) {
            return null;
        }
        int level = 0;
        StringBuilder stringBuilder = new StringBuilder();
        char newLine = '\n';
        boolean isInString = false;
        char lastChar = 0;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (level > 0 && newLine == stringBuilder.charAt(stringBuilder.length() - 1)) {
                appendSpace(stringBuilder, level);
            }
            switch (c) {
                case '{':
                case '[':
                    stringBuilder.append(c).append(newLine);
                    level++;
                    break;
                case ',':
                    stringBuilder.append(c);
                    if (!isInString) {
                        stringBuilder.append(newLine);
                    }
                    break;
                case '}':
                    if (lastChar != '{') {
                        stringBuilder.append(newLine);
                    }
                    level--;
                    appendSpace(stringBuilder, level);
                    stringBuilder.append(c);
                    break;
                case ']':
                    if (lastChar != '[') {
                        stringBuilder.append(newLine);
                    }
                    level--;
                    appendSpace(stringBuilder, level);
                    stringBuilder.append(c);
                    break;
                case '"':
                    isInString = !isInString;
                    stringBuilder.append(c);
                    break;
                default:
                    stringBuilder.append(c);
                    break;
            }
            lastChar = c;
        }
        return stringBuilder.toString();
    }
}
