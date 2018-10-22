package lib.common.model.log;

import lib.common.model.cache.TimedObjectPool;

import java.util.concurrent.atomic.AtomicLong;

public class LogRecord {
    static final int TIMEOUT_SECOND = 120;
    private static AtomicLong sequenceNumberCreator = new AtomicLong();
    private static Pool pool = new Pool();
    private static int INIT_STACK_TRACE;
    private Object tag;
    private LogLevel level;
    private long sequenceNumber;
    private String message;
    private long timeMillis;
    private StackTraceElement[] stackTrace;
    private int encapsulationLayerCount;
    private int currentDepth;

    static LogRecord get(Object tag, LogLevel level, String message, int encapsulationLayerCount) {
        LogRecord record = pool.borrow();
        record.tag = tag;
        record.level = level;
        record.message = message;
        record.encapsulationLayerCount = encapsulationLayerCount;
        record.sequenceNumber = sequenceNumberCreator.getAndIncrement();
        return record;
    }

    public Object getTag() {
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

    public StackTraceElement nextStackTraceElement() {
        if (stackTrace == null) {
            // stack trace index: 1
            stackTrace = Thread.currentThread().getStackTrace();
        }
        if (INIT_STACK_TRACE == 0) {
            // 不同虚拟机栈的层数可能不一样，所以需要计算不能写死
            for (int i = 0; i < stackTrace.length; i++) {
                if (Logger.class.getName().equals(stackTrace[i].getClassName())) {
                    INIT_STACK_TRACE = i + 1;
                    break;
                }
            }
        }
        int index = INIT_STACK_TRACE + encapsulationLayerCount + currentDepth++;
        if (stackTrace.length > index) {
            return stackTrace[index];
        }
        return null;
    }

    void recycle() {
        pool.giveBack(this);
    }

    private static class Pool extends TimedObjectPool<LogRecord> {
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
            obj.level = null;
            obj.message = null;
            obj.timeMillis = 0;
            obj.stackTrace = null;
            obj.sequenceNumber = 0;
            obj.encapsulationLayerCount = 0;
            obj.currentDepth = 0;
        }

        @Override
        protected void onDiscard(LogRecord obj) {
        }

        @Override
        protected void onCleared(int poolSize) {
        }
    }
}
