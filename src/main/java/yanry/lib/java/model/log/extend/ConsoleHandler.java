package yanry.lib.java.model.log.extend;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import yanry.lib.java.model.log.LogHandler;
import yanry.lib.java.model.log.LogRecord;
import yanry.lib.java.util.console.Color;
import yanry.lib.java.util.console.ConsoleUtil;

public class ConsoleHandler extends LogHandler {
    private static final Pattern stackTracePattern = Pattern.compile("^\\tat \\S+\\(\\S+\\.java:\\d+\\)$", Pattern.MULTILINE);

    @Override
    protected void handleFormattedLog(LogRecord logRecord, String formattedLog) {
        StringBuilder sb = new StringBuilder();
        Color color = null;
        switch (logRecord.getLevel()) {
            case Verbose:
                color = Color.Cyan;
                break;
            case Debug:
                color = Color.Magenta;
                break;
            case Info:
                color = Color.Blue;
                break;
            case Warn:
                color = Color.Yellow;
                break;
            case Error:
                color = Color.Red;
                break;
        }
        int mark = 0;
        Matcher matcher = stackTracePattern.matcher(formattedLog);
        int messageStart = formattedLog.indexOf(logRecord.getMessage());
        int messageEnd = messageStart + logRecord.getMessage().length();
        while (matcher.find()) {
            // 含有调用栈信息
            int start = matcher.start();
            if (mark < start) {
                boolean handleMessageStart = messageStart >= mark && messageStart <= start;
                if (handleMessageStart) {
                    // 添加日志内容前面的附加信息
                    sb.append(formattedLog, mark, messageStart);
                    // 设置日志内容颜色
                    ConsoleUtil.appendColorSequences(sb, color, true);
                    mark = messageStart;
                }
                boolean handleMessageEnd = messageEnd >= mark && messageEnd <= start;
                if (handleMessageEnd) {
                    // 添加日志内容
                    sb.append(formattedLog, mark, messageEnd);
                    // 恢复颜色
                    ConsoleUtil.appendColorSequences(sb, Color.Default, true);
                    mark = messageEnd;
                }
                // 添加附加信息
                sb.append(formattedLog, mark, start);
            }
            mark = matcher.end();
            // 设置副色并添加调用栈信息
            ConsoleUtil.appendColorSequences(sb, Color.White, true).append(formattedLog, start, mark);
        }
        int length = formattedLog.length();
        if (mark < length) {
            // 处理调用栈信息结束后的内容
            boolean handleMessageStart = messageStart >= mark && messageStart <= length;
            if (handleMessageStart) {
                sb.append(formattedLog, mark, messageStart);
                ConsoleUtil.appendColorSequences(sb, color, true);
                mark = messageStart;
            }
            boolean handleMessageEnd = messageEnd >= mark && messageEnd <= length;
            if (handleMessageEnd) {
                sb.append(formattedLog, mark, messageEnd);
                ConsoleUtil.appendColorSequences(sb, Color.Default, true);
                mark = messageEnd;
            }
            sb.append(formattedLog, mark, length);
        }
        System.out.println(ConsoleUtil.appendColorSequences(sb, Color.Default, true));
    }

    @Override
    protected void catches(Object tag, Exception e) {
        e.printStackTrace();
    }
}
