package yanry.lib.java.model.log;

import yanry.lib.java.util.console.Color;
import yanry.lib.java.util.console.ConsoleUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConsoleHandler extends LogHandler {
    private static final Pattern stackTracePattern = Pattern.compile("^ {4}at \\S+\\(\\S+\\.java:\\d+\\)$", Pattern.MULTILINE);

    public ConsoleHandler(LogFormatter formatter, LogLevel level) {
        super(formatter, level);
    }

    @Override
    protected void handleLog(LogLevel level, Object tag, String log, int messageStart, int messageEnd) {
        StringBuilder sb = new StringBuilder();
        Color color = null;
        switch (level) {
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
        Matcher matcher = stackTracePattern.matcher(log);
        while (matcher.find()) {
            int start = matcher.start();
            if (mark < start) {
                boolean handleMessageStart = messageStart >= mark && messageStart <= start;
                if (handleMessageStart) {
                    sb.append(log, mark, messageStart);
                    ConsoleUtil.appendColorSequences(sb, color, true);
                    mark = messageStart;
                }
                boolean handleMessageEnd = messageEnd >= mark && messageEnd <= start;
                if (handleMessageEnd) {
                    sb.append(log, mark, messageEnd);
                    ConsoleUtil.appendColorSequences(sb, Color.Default, true);
                    mark = messageEnd;
                }
                sb.append(log, mark, start);
            }
            mark = matcher.end();
            ConsoleUtil.appendColorSequences(sb, Color.White, true).append(log, start, mark);
        }
        int length = log.length();
        if (mark < length) {
            boolean handleMessageStart = messageStart >= mark && messageStart <= length;
            if (handleMessageStart) {
                sb.append(log, mark, messageStart);
                ConsoleUtil.appendColorSequences(sb, color, true);
                mark = messageStart;
            }
            boolean handleMessageEnd = messageEnd >= mark && messageEnd <= length;
            if (handleMessageEnd) {
                sb.append(log, mark, messageEnd);
                ConsoleUtil.appendColorSequences(sb, Color.Default, true);
                mark = messageEnd;
            }
            sb.append(log, mark, length);
        }
        System.out.println(ConsoleUtil.appendColorSequences(sb, Color.Default, true));
    }

    @Override
    protected void catches(Object tag, Exception e) {
        e.printStackTrace();
    }
}
