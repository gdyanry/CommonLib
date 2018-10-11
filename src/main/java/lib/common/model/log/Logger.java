package lib.common.model.log;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Logger {
    private static HashMap<Object, Logger> instances = new HashMap<>();
    private final static LogFormatter defaultFormatter = new SimpleFormatterBuilder().build();
    private static Object defaultTag;
    private Object tag;
    private LogLevel level;
    private List<LogHandler> handlers;

    private Logger(Object tag) {
        this.tag = tag;
        handlers = new LinkedList<>();
        level = LogLevel.Verbose;
        instances.put(tag, this);
    }

    public static Logger get(Object tag) {
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

    public static void setDefaultTag(Object defaultTag) {
        Logger.defaultTag = defaultTag;
    }

    public static Logger getDefault() {
        return get(defaultTag);
    }

    /**
     * @param level 当level为null时不输出任何日志。
     */
    public void setLevel(LogLevel level) {
        this.level = level;
    }

    public void addHandler(LogHandler handler) {
        if (!handlers.contains(handler)) {
            handlers.add(handler);
        }
    }

    public boolean isReady() {
        return !handlers.isEmpty();
    }

    /**
     * 对于日志方法先封装再使用的场景需要调用此方法输出日志，否则定位不到日志打点处。
     *
     * @param encapsulationLayerCount 日志打点处距离此方法中间封装的方法层数。
     * @param level
     * @param msg
     * @param args
     */
    public void log(int encapsulationLayerCount, LogLevel level, String msg, Object... args) {
        if (this.level != null && this.level.test(level)) {
            LogRecord record = null;
            for (LogHandler handler : handlers) {
                if (handler.getLevel() == null || handler.getLevel().test(level)) {
                    if (record == null) {
                        record = new LogRecord(tag, level, args.length == 0 ? msg : String.format(msg, args), encapsulationLayerCount);
                    }
                    LogFormatter formatter = handler.getFormatter();
                    if (formatter == null) {
                        formatter = defaultFormatter;
                    }
                    handler.handleLog(level, tag, formatter.format(record));
                }
            }
        }
    }

    public void log(LogLevel level, String msg, Object... args) {
        log(1, level, msg, args);
    }

    public void v(String msg, Object... args) {
        log(1, LogLevel.Verbose, msg, args);
    }

    public void v(Object info) {
        log(1, LogLevel.Verbose, info == null ? null : info.toString());
    }

    public void d(String msg, Object... args) {
        log(1, LogLevel.Debug, msg, args);
    }

    public void d(Object info) {
        log(1, LogLevel.Debug, info == null ? null : info.toString());
    }

    public void i(String msg, Object... args) {
        log(1, LogLevel.Info, msg, args);
    }

    public void i(Object info) {
        log(1, LogLevel.Info, info == null ? null : info.toString());
    }

    public void w(String msg, Object... args) {
        log(1, LogLevel.Warn, msg, args);
    }

    public void w(Object info) {
        log(1, LogLevel.Warn, info == null ? null : info.toString());
    }

    public void e(String msg, Object... args) {
        log(1, LogLevel.Error, msg, args);
    }

    public void e(Object info) {
        log(1, LogLevel.Error, info == null ? null : info.toString());
    }

    public void catches(Exception e) {
        for (LogHandler handler : handlers) {
            handler.catches(tag, e);
        }
    }
}
