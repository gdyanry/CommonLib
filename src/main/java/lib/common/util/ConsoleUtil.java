/**
 *
 */
package lib.common.util;

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

    public static void execCommand(Process process, Consumer<String> outputHandler) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            outputHandler.accept(line);
        }
    }
}
