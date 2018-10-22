package lib.common.t;

import lib.common.model.log.ConsoleHandler;
import lib.common.model.log.LogLevel;
import lib.common.model.log.Logger;
import lib.common.model.log.SimpleFormatter;
import lib.common.util.IOUtil;
import lib.common.util.StringUtil;

import java.io.IOException;
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
        Logger.getDefault().addHandler(new ConsoleHandler(new SimpleFormatter().sequenceNumber().date().time().method(10), null));

        log(IOUtil.getAppRelativeFile("").getAbsolutePath());

        System.out.println(49 >> 2);
        log(StringUtil.generateFixedLengthNumber(new Random(), 10));

        System.out.println(~1);
        System.out.println(~0);
        log(String.format("%tF", 1466776535000L));
        log(String.format("%tR", 1466776535000L));
        log(String.format("%tT  %1$s", 1466776535000L));

        log(IOUtil.resourceToString(IOUtil.class, "..", "utf-8"));

        Pattern pattern = Pattern.compile("DAY\\((.*)\\)");
        Matcher matcher = pattern.matcher("DAY(jJ)");
        if (matcher.find()) {
            log(matcher.group(0));
            log(matcher.group(1));
        }
        pattern = Pattern.compile("\\d+-\\d+");
        matcher = pattern.matcher("紫外线辐射弱，无需特别防护。如果长期出门，建议你涂抹防晒指数在8-12之间的防晒霜");
        if (matcher.find()) {
            System.out.println(matcher.group());
        }

        System.out.println("D1".matches("\\D[1-7](?:,\\D[1-7])?"));
        System.out.println("D1,D4,D5,D6,D5,D7,D3".matches("^\\D[1-7](?:(?:,\\D[1-7]){0,5},\\D[1-7])?$"));

        log(String.format("%02d - %02d", 1, 1542));
        System.out.println("a".matches("^[\\x00-\\xff]+$"));
        System.out.println(0x8f);

//        ConsoleUtil.execCommand(Runtime.getRuntime().exec("cmd /c start cmd.bat", null, new File("d:/")), System.out::println);
        Logger.getDefault().v("verbose");
        Logger.getDefault().d("debug");
        Logger.getDefault().i("info");
        Logger.getDefault().w("warn");
        Logger.getDefault().e("error");

        String temp = "20℃~26℃";
        System.out.println(new StringBuilder().append(temp, 0, temp.length()));
    }

    private static void log(String msg, Object... args) {
        Logger.getDefault().log(1, LogLevel.Verbose, msg, args);
    }
}