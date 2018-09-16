package lib.common.model.log;

public class ConsoleHandler extends LogHandler {
    public ConsoleHandler(LogFormatter formatter, LogLevel level) {
        super(formatter, level);
    }

    @Override
    protected void handleLog(LogLevel level, Object tag, String log) {
        System.out.println(log);
    }

    @Override
    protected void catches(Exception e) {
        e.printStackTrace();
    }
}
