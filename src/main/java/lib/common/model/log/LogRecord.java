package lib.common.model.log;

import java.util.concurrent.atomic.AtomicLong;

public class LogRecord {
    private static AtomicLong sequenceNumberCreator = new AtomicLong();
    private StringBuilder stringBuilder;
    private String tag;
    private LogLevel level;
    private long sequenceNumber;
    private String message;
    private long timeMillis;
    private StackTraceElement[] stackTraceElements;

    LogRecord(String tag, LogLevel level, String message) {
        this.tag = tag;
        this.level = level;
        this.message = message;
        sequenceNumber = sequenceNumberCreator.getAndIncrement();
    }

    StringBuilder getStringBuilder() {
        if (stringBuilder == null) {
            stringBuilder = new StringBuilder();
        }
        return stringBuilder;
    }

    public String getTag() {
        return tag;
    }

    public LogLevel getLevel() {
        return level;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public String getMessage() {
        return message;
    }

    public long getTimeMillis() {
        if (timeMillis == 0) {
            timeMillis = System.currentTimeMillis();
        }
        return timeMillis;
    }

    StackTraceElement[] getStackTraceElements() {
        if (stackTraceElements == null) {
            stackTraceElements = Thread.currentThread().getStackTrace();
        }
        return stackTraceElements;
    }
}
