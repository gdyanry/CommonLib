package yanry.lib.java.model.log;

import yanry.lib.java.model.FlagsHolder;
import yanry.lib.java.util.StringUtil;

import java.lang.management.ManagementFactory;

public class SimpleFormatter extends FlagsHolder implements LogFormatter {
    public static final int SEQUENCE_NUMBER = 0;
    public static final int LEVEL = 1;
    public static final int DATE = 2;
    public static final int THREAD = 3;
    public static final int TAG = 4;
    public static final int METHOD = 5;
    public static final int TIME = 6;
    public static final int PROCESS = 7;

    private Object separator;
    private int stackDepth;

    public SimpleFormatter() {
        this(" ");
    }

    public SimpleFormatter(Object separator) {
        super(true);
        this.separator = separator;
    }

    public void setMethodStack(int stackDepth) {
        this.stackDepth = stackDepth;
    }

    @Override
    public String format(LogRecord logRecord) {
        StringBuilder sb = new StringBuilder();
        if (hasFlag(PROCESS)) {
            sb.append(ManagementFactory.getRuntimeMXBean().getName()).append(separator);
        }
        if (hasFlag(SEQUENCE_NUMBER)) {
            sb.append(logRecord.getSequenceNumber()).append(separator);
        }
        if (hasFlag(LEVEL)) {
            sb.append(logRecord.getLevel().getAcronym()).append(separator);
        }
        if (hasFlag(DATE)) {
            sb.append(String.format("%tF%s", logRecord.getTimeMillis(), separator));
        }
        if (hasFlag(TIME)) {
            sb.append(String.format("%tT.%<tL%s", logRecord.getTimeMillis(), separator));
        }
        if (hasFlag(THREAD)) {
            sb.append(Thread.currentThread().getName()).append(separator);
        }
        if (hasFlag(TAG)) {
            sb.append(logRecord.getTag()).append(separator);
        }
        if (hasFlag(METHOD) && stackDepth == 0) {
            StackTraceElement e = logRecord.nextStackTraceElement();
            if (e != null) {
                sb.append(e.getMethodName()).append('(').append(e.getFileName()).append(':').append(e.getLineNumber()).append(')').append(separator);
            }
        }
        sb.append(logRecord.getMessage());
        if (stackDepth > 0) {
            for (int i = 0; i < stackDepth; i++) {
                StackTraceElement e = logRecord.nextStackTraceElement();
                if (e == null) {
                    break;
                }
                sb.append(System.lineSeparator());
                StringUtil.appendStackTrace(sb, e);
            }
        }
        return sb.toString();
    }
}
