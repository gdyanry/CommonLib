package lib.common.model.log;

public class ConsoleHandler extends LogHandler {
    public ConsoleHandler(LogFormatter formatter, LogLevel level) {
        super(formatter, level);
    }

    @Override
    protected void handleLog(LogLevel level, Object tag, String log) {
        if (level == LogLevel.Error) {
            System.err.println(log);
        } else {
            System.out.println(log);
        }
    }

    @Override
    protected void catches(Object tag, Exception e) {
        e.printStackTrace();
    }
}
