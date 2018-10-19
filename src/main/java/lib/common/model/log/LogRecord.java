package lib.common.model.log;

import lib.common.model.cache.TimerObjectPool;

import java.util.concurrent.atomic.AtomicLong;

class LogRecord {
    private static final int TIMEOUT_SECOND = 120;
    private static AtomicLong sequenceNumberCreator = new AtomicLong();
    private static Pool pool = new Pool();
    private StringBuilder stringBuilder;
    private Object tag;
    private LogLevel level;
    private long sequenceNumber;
    private String message;
    private long timeMillis;
    private StackTraceElement[] stackTraceElements;
    private int encapsulationLayerCount;

    static LogRecord get(Object tag, LogLevel level, String message, int encapsulationLayerCount) {
        LogRecord record = pool.borrow();
        record.tag = tag;
        record.level = level;
        record.message = message;
        record.encapsulationLayerCount = encapsulationLayerCount;
        record.sequenceNumber = sequenceNumberCreator.getAndIncrement();
        return record;
    }

    StringBuilder getStringBuilder() {
        if (stringBuilder == null) {
            stringBuilder = new StringBuilder();
        }
        return stringBuilder;
    }

    Object getTag() {
        return tag;
    }

    LogLevel getLevel() {
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

    int getEncapsulationLayerCount() {
        return encapsulationLayerCount;
    }

    void recycle() {
        pool.giveBack(this);
    }

    private static class Pool extends TimerObjectPool<LogRecord> {
        Pool() {
            super(TIMEOUT_SECOND);
        }

        @Override
        protected LogRecord createInstance() {
            return new LogRecord();
        }

        @Override
        protected void onReturn(LogRecord obj) {
            obj.tag = null;
            obj.message = null;
            obj.stringBuilder = null;
            obj.timeMillis = 0;
            obj.stackTraceElements = null;
        }

        @Override
        protected void onDiscard(LogRecord obj) {
        }

        @Override
        protected void onCleared(int poolSize) {
        }
    }
}
