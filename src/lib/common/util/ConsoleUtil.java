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
	public static void debug(String msg) {
		debug(ConsoleUtil.class, msg);
	}

	public static void error(String msg) {
		error(ConsoleUtil.class, msg);
	}

	public static void debug(Class<?> loggerClass, String msg) {
		System.out.println(getLog(loggerClass, msg));
	}

	public static void error(Class<?> loggerClass, String msg) {
		System.err.println(getLog(loggerClass, msg));
	}

	public static String getLog(Class<?> loggerClass, String msg) {
		if (loggerClass == null) {
			loggerClass = ConsoleUtil.class;
		}
		boolean isLogger = false;
		StackTraceElement[] frames = Thread.currentThread().getStackTrace();
		for (StackTraceElement f : frames) {
			if (f.getClassName().equals(loggerClass.getName())) {
				isLogger = true;
			} else if (isLogger) {
				String fileName = f.getFileName();
				fileName = fileName.substring(0, fileName.lastIndexOf("."));
				return String.format("[%s:%s.%s(line%s)] %s", Thread.currentThread().getName(), fileName, f.getMethodName(), f.getLineNumber(), msg);
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
