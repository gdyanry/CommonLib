package lib.common.model.log;

public class ConsoleHandler extends LogHandler {
    public ConsoleHandler(LogFormatter formatter, LogLevel level) {
        super(formatter, level);
    }

    @Override
    protected void handleLog(LogLevel level, String tag, String log) {
        System.out.println(log);
    }
}
