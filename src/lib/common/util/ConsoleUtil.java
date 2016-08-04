/**
 * 
 */
package lib.common.util;

/**
 * @author yanry
 *
 *         2015年1月16日 下午2:48:32
 */
public class ConsoleUtil {

	public static void debug(Class<?> tag, String msg) {
		System.out.println(getLog(tag, msg));
	}

	public static void error(Class<?> tag, String msg) {
		System.err.println(getLog(tag, msg));
	}

	private static String getLog(Class<?> tag, String msg) {
		return String.format("[%tF %<tT(%s) - %s] %s", System.currentTimeMillis(), Thread.currentThread().getName(),
				StringUtil.getLogTag(tag), msg);
	}

}
