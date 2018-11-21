package lib.common.t;

import lib.common.model.json.JSONObject;
import lib.common.model.json.pattern.JsonPattern;
import lib.common.model.log.ConsoleHandler;
import lib.common.model.log.LogLevel;
import lib.common.model.log.Logger;
import lib.common.model.log.SimpleFormatter;
import lib.common.util.IOUtil;
import lib.common.util.StringUtil;

import java.io.IOException;
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
        Logger.getDefault().addHandler(new ConsoleHandler(new SimpleFormatter().sequenceNumber().date().time().method(10), null));

        format(IOUtil.getAppRelativeFile("").getAbsolutePath());

        concat(49 >> 2);
        format(StringUtil.generateFixedLengthNumber(new Random(), 10));

        System.out.println(~1);
        System.out.println(~0);
        format("%tF", 1466776535000L);
        format("%tR", 1466776535000L);
        format("%tT  %1$s", 1466776535000L);

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
        Logger.getDefault().vv("verbose");
        Logger.getDefault().dd("debug");
        Logger.getDefault().ii("info");
        Logger.getDefault().ww("warn");
        Logger.getDefault().ee("error");

        pattern = Pattern.compile("-?\\d+");
        matcher = pattern.matcher("-2℃~-5℃");
        while (matcher.find()) {
            concat(matcher.group());
        }

        System.out.println(Arrays.toString("eess\naaa".split("\r\n|\n")));

        JSONObject jsonObject = new JSONObject("{\"result\":{\"bot_id\":\"aries_general\",\"bot_meta\":{\"version\":\"1.0.0\",\"type\":\"其他\",\"description\":\"desc\"},\"nlu\":{\"domain\":\"universal_search\",\"intent\":\"kg\",\"sub_intent\":\"\",\"slots\":{}},\"speech\":{\"type\":\"Text\",\"content\":\"岳云鹏饰演贝小贝，吴京饰演梅办法，吴秀波饰演梅前途。\"},\"views\":[{\"type\":\"txt\",\"content\":\"岳云鹏饰演贝小贝，吴京饰演梅办法，吴秀波饰演梅前途。\"}],\"hint\":[\"竹篮打水一场空是什么意思\",\"明日限号多少\",\"鳄鱼的资料\",\"茅台股份价格\",\"刘德华的资料\",\"现在什么时间\",\"3d开奖结果\",\"深圳能源股价多少\",\"北京今天什么尾号限行\",\"荷花的图片\"],\"show_hint\":[{\"cue_words\":[\"竹篮打水一场空是什么意思\",\"明日限号多少\",\"鳄鱼的资料\",\"茅台股份价格\",\"刘德华的资料\",\"现在什么时间\",\"3d开奖结果\",\"深圳能源股价多少\",\"北京今天什么尾号限行\",\"荷花的图片\"]}],\"resources\":[]},\"id\":\"1542782419_009fundxs\",\"logid\":\"fc2f2b21a7204bec985378a07a03ef52\",\"user_id\":\"1fbd9f106ff68b1cfc8f2561fd7e7d55\",\"time\":1542782419,\"cuid\":\"1fbd9f106ff68b1cfc8f2561fd7e7d55\",\"se_query\":\"祖宗19的演员表\",\"msg\":\"ok\",\"status\":0,\"client_msg_id\":\"690d4929-c7f3-4736-b3f1-944928dfe31b\"}");
        System.out.println(StringUtil.formatJson(JsonPattern.get(jsonObject).toJSONString()));
    }

    private static void format(String msg, Object... args) {
        Logger.getDefault().format(1, LogLevel.Verbose, msg, args);
    }

    private static void concat(Object... parts) {
        Logger.getDefault().concat(1, LogLevel.Debug, parts);
    }
}