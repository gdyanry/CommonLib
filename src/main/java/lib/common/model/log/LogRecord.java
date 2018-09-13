package lib.common.model.log;

import java.util.concurrent.atomic.AtomicLong;

class LogRecord {
    private static AtomicLong sequenceNumberCreator = new AtomicLong();
    private StringBuilder stringBuilder;
    private String tag;
    private LogLevel level;
    private long sequenceNumber;
    private String message;
    private long timeMillis;
    private StackTraceElement[] stackTraceElements;
    private int stackTraceOffset;

    LogRecord(String tag, LogLevel level, String message, int stackTraceOffset) {
        this.tag = tag;
        this.level = level;
        this.message = message;
        this.stackTraceOffset = stackTraceOffset;
        sequenceNumber = sequenceNumberCreator.getAndIncrement();
    }

    StringBuilder getStringBuilder() {
        if (stringBuilder == null) {
            stringBuilder = new StringBuilder();
        }
        return stringBuilder;
    }

    String getTag() {
        return tag;
    }

    public LogLevel getLevel() {
        return level;
    }

    long getSequenceNumber() {
        return sequenceNumber;
    }

    String getMessage() {
        return message;
    }

    long getTimeMillis() {
        if (timeMillis == 0) {
            timeMillis = System.currentTimeMillis();
        }
        return timeMillis;
    }

    StackTraceElement[] getStackTraceElements() {
        if (stackTraceElements == null) {
            // stack trace index: 1
            stackTraceElements = Thread.currentThread().getStackTrace();
        }
        return stackTraceElements;
    }

    public int getStackTraceOffset() {
        return stackTraceOffset;
    }
}
