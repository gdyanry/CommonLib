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

	public static String capitalize(String word) {
		String firstLetter = word.substring(0, 1);
		return word.replaceFirst(firstLetter, firstLetter.toUpperCase());
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
		List<Integer> list = new ArrayList<Integer>(strArr.length);
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
		return HexUtil.bytesToHex(md.digest(input.getBytes(charset)));
	}

	public static String generateFixedLengthNumber(Random r, int length) {
		return String.format("%0" + length + "d", r.nextInt((int) Math.pow(10, length)));
	}

}
