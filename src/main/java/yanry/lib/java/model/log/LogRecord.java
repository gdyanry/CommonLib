package yanry.lib.java.model.log;

import java.util.concurrent.atomic.AtomicLong;

public abstract class LogRecord {
    private static final String LOGGER_CLASS_NAME = Logger.class.getName();
    private static AtomicLong sequenceNumberCreator = new AtomicLong();
    private int initStartTrace;
    private Object tag;
    private LogLevel level;
    private long sequenceNumber;
    private String message;
    private long timeMillis;
    private StackTraceElement[] stackTrace;
    private int encapsulationLayerCount;
    private int currentDepth;
    private boolean anonymous;

    LogRecord(Object tag, LogLevel level, int encapsulationLayerCount, boolean anonymous) {
        this.anonymous = anonymous;
        sequenceNumber = sequenceNumberCreator.getAndIncrement();
        timeMillis = System.currentTimeMillis();
        this.tag = tag;
        this.level = level;
        this.encapsulationLayerCount = encapsulationLayerCount;
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
        if (message == null) {
            message = buildMessage();
        }
        return message;
    }

    public long getTimeMillis() {
        return timeMillis;
    }

    public StackTraceElement nextStackTraceElement() {
        if (anonymous) {
            return null;
        }
        if (stackTrace == null) {
            // stack trace index: 1
            stackTrace = Thread.currentThread().getStackTrace();
        }
        // 不同虚拟机栈的层数可能不一样，相同虚拟机不同的函数调用也可能不一样，所以需要计算不能写死
        int depth = stackTrace.length;
        if (initStartTrace == 0) {
            for (int i = 0; i < depth; i++) {
                if (LOGGER_CLASS_NAME.equals(stackTrace[i].getClassName())) {
                    initStartTrace = i + 1;
                    break;
                }
            }
        }
        int index = initStartTrace + encapsulationLayerCount + currentDepth++;
        if (depth > index) {
            return stackTrace[index];
        }
        return null;
    }

    protected abstract String buildMessage();
}
