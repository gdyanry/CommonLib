package lib.common.model.log;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Logger {
    private static HashMap<String, Logger> instances = new HashMap<>();
    private String tag;
    private LogFormatter defaultFormatter;
    private LogLevel level;
    private List<LogHandler> handlers;

    private Logger(String tag) {
        this.tag = tag;
        handlers = new LinkedList<>();
        instances.put(tag, this);
    }

    public static Logger get(String tag) {
        Logger logger = instances.get(tag);
        if (logger == null) {
            synchronized (Logger.class) {
                if ((logger = instances.get(tag)) == null) {
                    logger = new Logger(tag);
                }
            }
        }
        return logger;
    }

    public static Logger getDefault() {
        return get(null);
    }

    public void setDefaultFormatter(LogFormatter defaultFormatter) {
        this.defaultFormatter = defaultFormatter;
    }

    public void setLevel(LogLevel level) {
        this.level = level;
    }

    public void addHandler(LogHandler handler) {
        if (!handlers.contains(handler)) {
            handlers.add(handler);
        }
    }

    public void log(LogLevel level, String msg, Object... args) {
        if (this.level == null || this.level.test(level)) {
            LogRecord record = null;
            for (LogHandler handler : handlers) {
                if (handler.getLevel() == null || handler.getLevel().test(level)) {
                    if (record == null) {
                        record = new LogRecord(tag, level, String.format(msg, args));
                    }
                    LogFormatter formatter = handler.getFormatter();
                    if (formatter == null) {
                        formatter = defaultFormatter;
                    }
                    if (formatter != null) {
                        handler.handleLog(level, tag, formatter.format(record));
                    }
                }
            }
        }
    }
}
