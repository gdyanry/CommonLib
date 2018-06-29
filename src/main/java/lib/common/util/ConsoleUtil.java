/**
 *
 */
package lib.common.util;

import java.util.Scanner;

/**
 * @author yanry
 * <p>
 * 2015年1月16日 下午2:48:32
 */
public class ConsoleUtil {
    public static void debug(String msg, Object... args) {
        debug(0, msg, args);
    }

    public static void error(String msg, Object... args) {
        error(0, msg, args);
    }

    public static void debug(int depthOffset, String msg, Object... args) {
        System.out.println(getLog(depthOffset, msg, args));
    }

    public static void error(int depthOffset, String msg, Object... args) {
        System.err.println(getLog(depthOffset, msg, args));
    }

    public static String getLog(int depthOffset, String msg, Object... args) {
        if (args.length > 0) {
            msg = String.format(msg, args);
        }
        StackTraceElement[] frames = Thread.currentThread().getStackTrace();
        if (frames.length > depthOffset + 1) {
            StackTraceElement f = frames[1 + depthOffset];
            String fileName = f.getFileName();
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
            return String.format("[%s:%s.%s] %s", Thread.currentThread().getName(), fileName, f.getLineNumber(), msg);
        }
        return String.format("[%s] %s", Thread.currentThread().getName(), msg);
    }

    private static Scanner scanner = new Scanner(System.in);

    public static String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }
}
