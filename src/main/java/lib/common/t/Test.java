package lib.common.t;

import lib.common.entity.SimpleInfoHandler;
import lib.common.model.log.ConsoleHandler;
import lib.common.model.log.LogFormatter;
import lib.common.model.log.LogLevel;
import lib.common.model.log.Logger;
import lib.common.util.*;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author yanry
 * <p>
 * 2015年2月11日 上午9:34:13
 */
public class Test {

    public static void main(String[] args) throws IOException {
        System.out.println(IOUtil.getAppRelativeFile("").getAbsolutePath());
        System.out.println();
        byte[] arr = {-122, 1, 0, 0};
        System.out.println(ConversionUtil.byteArrayToInt(arr, 0, null));
        System.out.println(Arrays.toString(ConversionUtil.intToByteArray(36868, null)));

        System.out.println(49 >> 2);
        System.out.println(Math.pow(2, 6));
        System.out.println(StringUtil.generateFixedLengthNumber(new Random(), 10));

        System.out.println(1 << 2);

        try {
            System.out.println(HexUtil.charsetHex("中文", "utf-8", 20));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println(~1);
        System.out.println(~0);
        System.out.println(String.format("%tF", 1466776535000L));
        System.out.println(String.format("%tR", 1466776535000L));
        System.out.println(String.format("%tT  %1$s", 1466776535000L));

        System.out.println(IOUtil.resourceToString(IOUtil.class, "..", "utf-8"));

        Pattern pattern = Pattern.compile("DAY\\((.*)\\)");
        Matcher matcher = pattern.matcher("DAY(jJ)");
        if (matcher.find()) {
            System.out.println(matcher.group(0));
            System.out.println(matcher.group(1));
        }

        System.out.println("D1".matches("\\D[1-7](?:,\\D[1-7])?"));
        System.out.println("D1,D4,D5,D6,D5,D7,D3".matches("^\\D[1-7](?:(?:,\\D[1-7]){0,5},\\D[1-7])?$"));
        ;


        System.out.println(String.format("%02d - %02d", 1, 1542));
        System.out.println("a".matches("^[\\x00-\\xff]+$"));
        System.out.println(0x8f);

        SimpleInfoHandler handler = new SimpleInfoHandler();
        handler.debug("SimpleInfoHandler debug");
        ConsoleUtil.debug(0, "ConsoleUtil debug");

//        ConsoleUtil.execCommand(Runtime.getRuntime().exec("cmd /c start cmd.bat", null, new File("d:/")), System.out::println);

        logTest();
    }

    private static void logTest() {
        new Runnable() {
            @Override
            public void run() {
                LogFormatter formatter = new LogFormatter()
                        .level(level -> level.getAcronym())
                        .with(" ")
                        .timestamp(t -> String.format("%tT", t))
                        .with(" [")
                        .thread(thread -> thread.getName())
                        .with(":")
                        .stackTrace(e -> String.format("%s.%s]", LogFormatter.getSimpleClassName(e), e.getLineNumber()))
                        .with(" ")
                        .message(msg -> msg)
                        .newLine()
                        .stackTrace(e -> String.format("* %s.%s", e.getClassName(), e.getLineNumber()))
                        .newLine()
                        .stackTrace(e -> String.format("** %s.%s", e.getFileName(), e.getLineNumber()))
                        .newLine()
                        .stackTrace(e -> String.format("*** %s.%s]", LogFormatter.getSimpleClassName(e), e.getLineNumber()))
                        .newLine()
                        .stackTrace(e -> String.format("**** %s.%s]", LogFormatter.getSimpleClassName(e), e.getLineNumber()))
                        .newLine()
                        .stackTrace(e -> String.format("***** %s.%s]", LogFormatter.getSimpleClassName(e), e.getLineNumber()))
                        .newLine()
                        .stackTrace(e -> String.format("****** %s.%s]", LogFormatter.getSimpleClassName(e), e.getLineNumber()))
                        .newLine()
                        .stackTrace(e -> String.format("******* %s.%s]", LogFormatter.getSimpleClassName(e), e.getLineNumber()))
                        .newLine()
                        .stackTrace(e -> String.format("******** %s.%s]", LogFormatter.getSimpleClassName(e), e.getLineNumber()))
                        .newLine()
                        .stackTrace(e -> String.format("********* %s.%s]", LogFormatter.getSimpleClassName(e), e.getLineNumber()));

                ConsoleHandler handler = new ConsoleHandler(formatter, null);
                Logger.getDefault().addHandler(handler);
                Logger.getDefault().log(LogLevel.Info, "current time is: %s", System.currentTimeMillis());
                Logger.getDefault().d("dddddd");
            }
        }.run();
    }
}