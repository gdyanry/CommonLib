package lib.common.model.log;

import lib.common.util.console.ConsoleUtil;

public class SimpleFormatter implements LogFormatter {
    private static final int SEQUENCE_NUMBER = 0;
    private static final int LEVEL = 1;
    private static final int DATE = 2;
    private static final int THREAD = 3;
    private static final int TAG = 4;
    private static final int METHOD = 5;
    private static final int TIME = 6;

    private boolean[] flags;
    private Object separator;
    private int stackDepth;

    public SimpleFormatter() {
        this(" ");
    }

    public SimpleFormatter(Object separator) {
        this.separator = separator;
        flags = new boolean[7];
    }

    private SimpleFormatter setFlag(int index) {
        flags[index] = true;
        return this;
    }

    public SimpleFormatter sequenceNumber() {
        return setFlag(SEQUENCE_NUMBER);
    }

    public SimpleFormatter level() {
        return setFlag(LEVEL);
    }

    public SimpleFormatter date() {
        return setFlag(DATE);
    }

    public SimpleFormatter time() {
        return setFlag(TIME);
    }

    public SimpleFormatter thread() {
        return setFlag(THREAD);
    }

    public SimpleFormatter tag() {
        return setFlag(TAG);
    }

    public SimpleFormatter method() {
        return setFlag(METHOD);
    }

    public SimpleFormatter method(int stackDepth) {
        this.stackDepth = stackDepth;
        return this;
    }

    @Override
    public FormattedLog format(LogRecord logRecord) {
        StringBuilder sb = new StringBuilder();
        if (flags[SEQUENCE_NUMBER]) {
            sb.append(logRecord.getSequenceNumber()).append(separator);
        }
        if (flags[LEVEL]) {
            sb.append(logRecord.getLevel().getAcronym()).append(separator);
        }
        if (flags[DATE]) {
            sb.append(String.format("%tF%s", logRecord.getTimeMillis(), separator));
        }
        if (flags[TIME]) {
            sb.append(String.format("%tT.%<tL%s", logRecord.getTimeMillis(), separator));
        }
        if (flags[THREAD]) {
            sb.append(Thread.currentThread().getName()).append(separator);
        }
        if (flags[TAG]) {
            sb.append(logRecord.getTag()).append(separator);
        }
        if (flags[METHOD] && stackDepth == 0) {
            StackTraceElement e = logRecord.nextStackTraceElement();
            if (e != null) {
                String name = e.getClassName();
                sb.append(name.substring(name.lastIndexOf(".") + 1)).append('.').append(e.getMethodName()).append('(').append(')').append(separator);
            }
        }
        int start = sb.length();
        sb.append(logRecord.getMessage());
        int end = sb.length();
        if (stackDepth > 0) {
            for (int i = 0; i < stackDepth; i++) {
                StackTraceElement e = logRecord.nextStackTraceElement();
                if (e == null) {
                    break;
                }
                sb.append(System.lineSeparator());
                ConsoleUtil.appendStackTrace(sb, e);
            }
        }
        return FormattedLog.get(sb.toString(), start, end);
    }
}
