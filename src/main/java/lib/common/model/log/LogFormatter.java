package lib.common.model.log;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.Supplier;

public class LogFormatter {
    private static final int BASE_STACK_TRACE_OFFSET = 5;
    private List<Consumer<LogRecord>> recordProcessors;
    private int stackTraceCount;
    private int stackTraceOffset;

    public LogFormatter(int stackTraceOffset) {
        this.stackTraceOffset = stackTraceOffset;
        recordProcessors = new LinkedList<>();
    }

    public static String getSimpleClassName(StackTraceElement traceElement) {
        String className = traceElement.getClassName();
        return className.substring(className.lastIndexOf(".") + 1);
    }

    public LogFormatter tag(Function<String, Object> tagFormatter) {
        recordProcessors.add(logRecord -> logRecord.getStringBuilder().append(tagFormatter.apply(logRecord.getTag())));
        return this;
    }

    public LogFormatter level(Function<LogLevel, Object> levelFormatter) {
        recordProcessors.add(logRecord -> logRecord.getStringBuilder().append(levelFormatter.apply(logRecord.getLevel())));
        return this;
    }

    public LogFormatter sequenceNumber(LongFunction<Object> sequenceNumberFormatter) {
        recordProcessors.add(logRecord -> logRecord.getStringBuilder().append(sequenceNumberFormatter.apply(logRecord.getSequenceNumber())));
        return this;
    }

    public LogFormatter timestamp(LongFunction<Object> timeFormatter) {
        recordProcessors.add(logRecord -> logRecord.getStringBuilder().append(timeFormatter.apply(logRecord.getTimeMillis())));
        return this;
    }

    public LogFormatter thread(Function<Thread, Object> threadFormatter) {
        recordProcessors.add(logRecord -> logRecord.getStringBuilder().append(threadFormatter.apply(Thread.currentThread())));
        return this;
    }

    public LogFormatter stackTrace(Function<StackTraceElement, Object> stackTraceFormatter) {
        int count = stackTraceCount++;
        recordProcessors.add(logRecord -> {
            int index = BASE_STACK_TRACE_OFFSET + stackTraceOffset + count;
            if (logRecord.getStackTraceElements().length > index) {
                StackTraceElement f = logRecord.getStackTraceElements()[index];
                logRecord.getStringBuilder().append(stackTraceFormatter.apply(f));
            }
        });
        return this;
    }

    public LogFormatter message(Function<String, String> messageFormatter) {
        recordProcessors.add(logRecord -> logRecord.getStringBuilder().append(messageFormatter.apply(logRecord.getMessage())));
        return this;
    }

    public LogFormatter with(Object something) {
        return append(() -> something);
    }

    public LogFormatter newLine() {
        return append(() -> System.lineSeparator());
    }

    public LogFormatter append(Supplier<Object> infoFormatter) {
        recordProcessors.add(logRecord -> logRecord.getStringBuilder().append(infoFormatter.get()));
        return this;
    }

    String format(LogRecord record) {
        for (Consumer<LogRecord> processor : recordProcessors) {
            processor.accept(record);
        }
        return record.getStringBuilder().toString();
    }
}
