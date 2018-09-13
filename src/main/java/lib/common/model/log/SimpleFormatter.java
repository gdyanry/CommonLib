package lib.common.model.log;

public class SimpleFormatter {
    private static final int SEQUENCE_NUMBER = 0;
    private static final int LEVEL = 1;
    private static final int DATE = 2;
    private static final int THREAD = 3;
    private static final int TAG = 4;
    private static final int METHOD = 5;

    private int stackTraceDepth;
    private boolean[] flags;

    public SimpleFormatter() {
        stackTraceDepth = 1;
        flags = new boolean[6];
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

    public SimpleFormatter thread() {
        return setFlag(THREAD);
    }

    public SimpleFormatter tag() {
        return setFlag(TAG);
    }

    public SimpleFormatter method() {
        return setFlag(METHOD);
    }

    public LogFormatter create(int stackTraceOffset) {
        LogFormatter formatter = new LogFormatter();
        return formatter;
    }
}
