package lib.common.model.log;

import java.util.LinkedList;
import java.util.List;

public class LogFormatter {
    private static int STACK_START_INDEX;
    private List<LogRecordProcessor> recordProcessors;
    private int stackTraceDepth;

    public LogFormatter() {
        recordProcessors = new LinkedList<>();
    }

    public static String getSimpleClassName(StackTraceElement traceElement) {
        String className = traceElement.getClassName();
        return className.substring(className.lastIndexOf(".") + 1);
    }

    public LogFormatter tag(InfoTransformer<String> tagFormatter) {
        recordProcessors.add(logRecord -> logRecord.getStringBuilder().append(tagFormatter.transform(logRecord.getTag())));
        return this;
    }

    public LogFormatter level(InfoTransformer<LogLevel> levelFormatter) {
        recordProcessors.add(logRecord -> logRecord.getStringBuilder().append(levelFormatter.transform(logRecord.getLevel())));
        return this;
    }

    public LogFormatter sequenceNumber(LongTransformer sequenceNumberFormatter) {
        recordProcessors.add(logRecord -> logRecord.getStringBuilder().append(sequenceNumberFormatter.transform(logRecord.getSequenceNumber())));
        return this;
    }

    public LogFormatter timestamp(LongTransformer timeFormatter) {
        recordProcessors.add(logRecord -> logRecord.getStringBuilder().append(timeFormatter.transform(logRecord.getTimeMillis())));
        return this;
    }

    public LogFormatter thread(InfoTransformer<Thread> threadFormatter) {
        recordProcessors.add(logRecord -> logRecord.getStringBuilder().append(threadFormatter.transform(Thread.currentThread())));
        return this;
    }

    public LogFormatter stackTrace(InfoTransformer<StackTraceElement> stackTraceFormatter) {
        int depth = stackTraceDepth++;
        recordProcessors.add(logRecord -> {
            if (STACK_START_INDEX == 0) {
                // 不同虚拟机栈的层数可能不一样，所以需要计算不能写死
                for (int i = 0; i < logRecord.getStackTraceElements().length; i++) {
                    StackTraceElement element = logRecord.getStackTraceElements()[i];
                    if (Logger.class.getName().equals(element.getClassName())) {
                        STACK_START_INDEX = i + 1;
                        break;
                    }
                }
            }
            int index = STACK_START_INDEX + logRecord.getEncapsulationLayerCount() + depth;
            // stack trace index: 2
            if (logRecord.getStackTraceElements().length > index) {
                StackTraceElement f = logRecord.getStackTraceElements()[index];
                logRecord.getStringBuilder().append(stackTraceFormatter.transform(f));
            }
        });
        return this;
    }

    public LogFormatter message(InfoTransformer<String> messageFormatter) {
        recordProcessors.add(logRecord -> logRecord.getStringBuilder().append(messageFormatter.transform(logRecord.getMessage())));
        return this;
    }

    public LogFormatter with(Object something) {
        return append(() -> something);
    }

    public LogFormatter newLine() {
        return append(() -> System.lineSeparator());
    }

    public LogFormatter append(InfoSupplier infoSupplier) {
        recordProcessors.add(logRecord -> logRecord.getStringBuilder().append(infoSupplier.get()));
        return this;
    }

    String format(LogRecord record) {
        for (LogRecordProcessor processor : recordProcessors) {
            processor.process(record);
        }
        return record.getStringBuilder().toString();
    }
}
