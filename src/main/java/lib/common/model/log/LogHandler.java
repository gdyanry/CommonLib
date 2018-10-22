package lib.common.model.log;

public abstract class LogHandler {
    private LogFormatter formatter;
    private LogLevel level;

    public LogHandler(LogFormatter formatter, LogLevel level) {
        this.formatter = formatter;
        this.level = level;
    }

    LogFormatter getFormatter() {
        return formatter;
    }

    LogLevel getLevel() {
        return level;
    }

    protected abstract void handleLog(LogLevel level, Object tag, String log, int messageStart, int messageEnd);

    protected abstract void catches(Object tag, Exception e);
}
