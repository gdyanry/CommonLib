package lib.common.t;

import lib.common.entity.SimpleInfoHandler;
import lib.common.model.log.*;
import lib.common.util.*;

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
        log(IOUtil.getAppRelativeFile("").getAbsolutePath());
        System.out.println();
        byte[] arr = {-122, 1, 0, 0};
        System.out.println(ConversionUtil.byteArrayToInt(arr, 0, null));
        log(Arrays.toString(ConversionUtil.intToByteArray(36868, null)));

        System.out.println(49 >> 2);
        System.out.println(Math.pow(2, 6));
        log(StringUtil.generateFixedLengthNumber(new Random(), 10));

        System.out.println(1 << 2);

        try {
            log(HexUtil.charsetHex("中文", "utf-8", 20));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

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

        System.out.println("D1".matches("\\D[1-7](?:,\\D[1-7])?"));
        System.out.println("D1,D4,D5,D6,D5,D7,D3".matches("^\\D[1-7](?:(?:,\\D[1-7]){0,5},\\D[1-7])?$"));
        ;


        log(String.format("%02d - %02d", 1, 1542));
        System.out.println("a".matches("^[\\x00-\\xff]+$"));
        System.out.println(0x8f);

        SimpleInfoHandler handler = new SimpleInfoHandler();
        handler.debug("SimpleInfoHandler debug");
        ConsoleUtil.debug(0, "ConsoleUtil debug");

//        ConsoleUtil.execCommand(Runtime.getRuntime().exec("cmd /c start cmd.bat", null, new File("d:/")), System.out::println);

    }

    private static void log(String msg, Object... args) {
        Logger logger = Logger.get("test");
        if (!logger.isReady()) {
            logger.addHandler(new ConsoleHandler(new SimpleFormatterBuilder().build(), null));
        }
        logger.log(1, LogLevel.Debug, msg, args);
    }
    
}