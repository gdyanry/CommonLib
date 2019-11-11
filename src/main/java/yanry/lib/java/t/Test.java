package yanry.lib.java.t;

import yanry.lib.java.model.log.ConsoleHandler;
import yanry.lib.java.model.log.LogLevel;
import yanry.lib.java.model.log.Logger;
import yanry.lib.java.model.log.SimpleFormatter;
import yanry.lib.java.util.HexUtil;
import yanry.lib.java.util.IOUtil;
import yanry.lib.java.util.StringUtil;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author yanry
 * <p>
 * 2015年2月11日 上午9:34:13
 */
public class Test {

    private static final byte[] IV = new byte[]{65, 20, -91, 123, -102, 126, 105, -28, -15, 13, 51, 32, 53, 45, -97, -40};
    private static final byte[] KEY = new byte[]{-49, 59, -97, -82, 5, -125, -92, -15, -7, -4, 95, -87, 85, -47, -34, -10};

    public static void main(String[] args) throws IOException {
        Logger.getDefault().addHandler(new ConsoleHandler(new SimpleFormatter().sequenceNumber().date().time().method(10), null));

        format(IOUtil.getAppRelativeFile("").getAbsolutePath());

        concat(49 >> 2);
        format(StringUtil.generateFixedLengthNumber(new Random(), 10));

        System.out.println(~1);
        System.out.println(~0);
        Calendar calendar = GregorianCalendar.getInstance();
        format("%tF %<tT", calendar.getTimeInMillis());
        calendar.add(Calendar.WEEK_OF_MONTH, -1);
        format("%tF %<tT", calendar.getTimeInMillis());

        format(IOUtil.resourceToString(IOUtil.class, "..", "utf-8"));

        Pattern pattern = Pattern.compile("DAY\\((.*)\\)");
        Matcher matcher = pattern.matcher("DAY(jJ)");
        if (matcher.find()) {
            concat(matcher.group(0));
            concat(matcher.group(1));
        }
        pattern = Pattern.compile("\\d+-\\d+");
        matcher = pattern.matcher("紫外线辐射弱，无需特别防护。如果长期出门，建议你涂抹防晒指数在8-12之间的防晒霜");
        if (matcher.find()) {
            concat(matcher.group());
        }

        System.out.println("D1".matches("\\D[1-7](?:,\\D[1-7])?"));
        System.out.println("D1,D4,D5,D6,D5,D7,D3".matches("^\\D[1-7](?:(?:,\\D[1-7]){0,5},\\D[1-7])?$"));

        format("%02d - %02d", 1, 1542);
        concat("a".matches("^[\\x00-\\xff]+$"));
        concat(0x8f);

//        ConsoleUtil.execCommand(Runtime.getRuntime().exec("cmd /c start cmd.bat", null, new File("d:/")), System.out::println);

        pattern = Pattern.compile("-?\\d+");
        matcher = pattern.matcher("-2℃~-5℃");
        while (matcher.find()) {
            concat(matcher.group());
        }
        System.out.println(HexUtil.bytesToHex(KEY));
        System.out.println(HexUtil.bytesToHex(IV));
        String t = null;
        System.out.println(new StringBuilder("a").append(t).append("b"));
    }

    private static void format(String msg, Object... args) {
        Logger.getDefault().format(1, LogLevel.Verbose, msg, args);
    }

    private static void concat(Object... parts) {
        Logger.getDefault().concat(1, LogLevel.Debug, parts);
    }
}