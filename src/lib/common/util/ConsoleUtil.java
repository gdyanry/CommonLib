/**
 * 
 */
package lib.common.util;

import java.util.Scanner;

/**
 * @author yanry
 *
 *         2015年1月16日 下午2:48:32
 */
public class ConsoleUtil {
	public static void debug(String msg, Object... args) {
		debug(ConsoleUtil.class, msg, args);
	}

	public static void error(String msg, Object... args) {
		error(ConsoleUtil.class, msg, args);
	}

	public static void debug(Class<?> loggerClass, String msg, Object... args) {
		System.out.println(getLog(loggerClass, msg, args));
	}

	public static void error(Class<?> loggerClass, String msg, Object... args) {
		System.err.println(getLog(loggerClass, msg, args));
	}

	public static String getLog(Class<?> loggerClass, String msg, Object... args) {
		if (loggerClass == null) {
			loggerClass = ConsoleUtil.class;
		}
		if (args.length > 0) {
			msg = String.format(msg, args);
		}
		boolean isLogger = false;
		StackTraceElement[] frames = Thread.currentThread().getStackTrace();
		for (StackTraceElement f : frames) {
			if (f.getClassName().equals(loggerClass.getName())) {
				isLogger = true;
			} else if (isLogger) {
				String fileName = f.getFileName();
				fileName = fileName.substring(0, fileName.lastIndexOf("."));
				return String.format("[%s][%s] %s", Thread.currentThread().getName(), fileName, msg);
			}
		}
		return String.format("[%s] %s", Thread.currentThread().getName(), msg);
	}

    private static Scanner scanner = new Scanner(System.in);

    public static String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }
}
