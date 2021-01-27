package yanry.lib.java.t;

import yanry.lib.java.model.log.LogLevel;
import yanry.lib.java.model.log.Logger;
import yanry.lib.java.model.log.extend.ConsoleHandler;
import yanry.lib.java.model.log.extend.SimpleFormatter;
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
    public static void setupLogger() {
        SimpleFormatter formatter = new SimpleFormatter();
        formatter.addFlag(SimpleFormatter.SEQUENCE_NUMBER).addFlag(SimpleFormatter.TIME).addFlag(SimpleFormatter.LEVEL).addFlag(SimpleFormatter.METHOD);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(formatter);
        handler.setLevel(LogLevel.Verbose);
        Logger.getDefault().addHandler(handler);
    }

    public static void main(String[] args) throws IOException {
        setupLogger();

        format(IOUtil.getAppRelativeFile("").getAbsolutePath());

        format(StringUtil.generateFixedLengthNumber(new Random(), 10));

        System.out.println(~1);
        System.out.println(~0);
        Calendar calendar = GregorianCalendar.getInstance();
        format("%tF %<tT", calendar.getTimeInMillis());
        calendar.set(Calendar.HOUR_OF_DAY, -2);
        format("%tF %<tT", calendar.getTimeInMillis());
        calendar.add(Calendar.HOUR_OF_DAY, 2);
        format("%tF %<tT", calendar.getTimeInMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 14);
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

        System.out.println(String.format("%02x", (int) (0xff * 0.9)));
        System.out.println(1f * 0xdb / 0xff);
        System.out.println(String.format("%x", -60));
        System.out.println(String.format("%x", (byte) -60));

        pattern = Pattern.compile("^(早上|中午|下午).+点(.+分)?在.+上?提醒我?$");
        System.out.println(pattern.matcher("早上九点在电视提醒他").matches());
    }

    private static void format(String msg, Object... args) {
        Logger.getDefault().format(1, LogLevel.Verbose, msg, args);
    }

    private static void concat(Object... parts) {
        Logger.getDefault().concat(1, LogLevel.Debug, parts);
    }
}