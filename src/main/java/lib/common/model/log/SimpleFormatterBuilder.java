package lib.common.model.log;

public class SimpleFormatterBuilder {
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

    public SimpleFormatterBuilder() {
        this(" ");
    }

    public SimpleFormatterBuilder(Object separator) {
        this.separator = separator;
        flags = new boolean[7];
    }

    private SimpleFormatterBuilder setFlag(int index) {
        flags[index] = true;
        return this;
    }

    public SimpleFormatterBuilder sequenceNumber() {
        return setFlag(SEQUENCE_NUMBER);
    }

    public SimpleFormatterBuilder level() {
        return setFlag(LEVEL);
    }

    public SimpleFormatterBuilder date() {
        return setFlag(DATE);
    }

    public SimpleFormatterBuilder time() {
        return setFlag(TIME);
    }

    public SimpleFormatterBuilder thread() {
        return setFlag(THREAD);
    }

    public SimpleFormatterBuilder tag() {
        return setFlag(TAG);
    }

    public SimpleFormatterBuilder method() {
        return setFlag(METHOD);
    }

    public SimpleFormatterBuilder method(int stackDepth) {
        this.stackDepth = stackDepth;
        return this;
    }

    public LogFormatter build() {
        LogFormatter formatter = new LogFormatter();
        if (flags[SEQUENCE_NUMBER]) {
            formatter.sequenceNumber(n -> n).with(separator);
        }
        if (flags[LEVEL]) {
            formatter.level(level -> level.getAcronym()).with(separator);
        }
        if (flags[DATE]) {
            formatter.timestamp(t -> String.format("%tF%s", t, separator));
        }
        if (flags[TIME]) {
            formatter.timestamp(t -> String.format("%tT.%<tL%s", t, separator));
        }
        if (flags[THREAD]) {
            formatter.thread(thread -> thread.getName()).with(separator);
        }
        if (flags[TAG]) {
            formatter.tag(t -> t).with(separator);
        }
        if (flags[METHOD] && stackDepth == 0) {
            formatter.stackTrace(e -> String.format("%s.%s()", LogFormatter.getSimpleClassName(e), e.getMethodName())).with(separator);
        }
        formatter.message(msg -> msg);
        if (stackDepth > 0) {
            for (int i = 0; i < stackDepth; i++) {
                formatter.stackTrace(e -> LogFormatter.print(e));
            }
        }
        return formatter;
    }
}
