package yanry.lib.java.model.log;

public abstract class LogHandler {
    private LogFormatter formatter;
    private LogLevel level;

    public LogLevel getLevel() {
        return level;
    }

    public void setLevel(LogLevel level) {
        this.level = level;
    }

    public LogHandler setFormatter(LogFormatter formatter) {
        this.formatter = formatter;
        return this;
    }

    final void handleLog(LogRecord logRecord) {
        handleFormattedLog(logRecord, formatter == null ? logRecord.getMessage() : formatter.format(logRecord));
    }

    protected abstract void handleFormattedLog(LogRecord logRecord, String formattedLog);

    protected abstract void catches(Object tag, Exception e);
}
