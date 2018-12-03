/**
 *
 */
package lib.common.util.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.function.Consumer;

/**
 * @author yanry
 * <p>
 * 2015年1月16日 下午2:48:32
 */
public class ConsoleUtil {

    public static StringBuilder appendColorSequences(StringBuilder stringBuilder, Color color, boolean isForeground) {
        return stringBuilder.append('\033').append('[').append(isForeground ? '3' : '4').append(color.code).append('m');
    }

    public static StringBuilder getColorSequences(Color color, boolean isForeground) {
        return appendColorSequences(new StringBuilder(), color, isForeground);
    }

    public static void execCommand(Process process, Consumer<String> outputHandler) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            outputHandler.accept(line);
        }
    }

    public static StringBuilder appendStackTrace(StringBuilder builder, StackTraceElement e) {
        return builder.append("    at ").append(e.getClassName()).append('.').append(e.getMethodName())
                .append('(').append(e.getFileName()).append(':').append(e.getLineNumber()).append(')');
    }

    public static void query(QueryPrompt prompt, QueryResult result) {
        Scanner scanner = new Scanner(System.in);
        int repeatTimes = 0;
        while (!query(prompt, result, scanner, repeatTimes)) {
            repeatTimes++;
        }
        scanner.close();
    }

    private static boolean query(QueryPrompt prompt, QueryResult result, Scanner scanner, int repeatTimes) {
        String hint = prompt.getPrompt(repeatTimes);
        System.out.println(hint);
        String read = scanner.nextLine();
        return result.isValidResult(read);
    }
}
