package yanry.lib.java.model.log;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Logger {
    private final static HashMap<Object, Logger> instances = new HashMap<>();
    private final static LogFormatter defaultFormatter = new SimpleFormatter();
    private final static ConsoleHandler defaultHandler = new ConsoleHandler(null, null);
    private static Object defaultTag;

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

    private Object tag;
    private LogLevel level;
    private List<LogHandler> handlers;

    private Logger(Object tag) {
        this.tag = tag;
        handlers = new LinkedList<>();
        level = LogLevel.Verbose;
        instances.put(tag, this);
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
     * @param format
     * @param args
     */
    public void format(int encapsulationLayerCount, LogLevel level, String format, Object... args) {
        if (this.level != null && this.level.test(level)) {
            LogRecord record = null;
            if (handlers.isEmpty()) {
                handleFormatLog(encapsulationLayerCount + 1, level, null, defaultHandler, format, args);
            } else {
                for (LogHandler handler : handlers) {
                    record = handleFormatLog(encapsulationLayerCount + 1, level, record, handler, format, args);
                }
            }
        }
    }

    private LogRecord handleFormatLog(int encapsulationLayerCount, LogLevel level, LogRecord record, LogHandler handler, String format, Object... args) {
        if (handler.getLevel() == null || handler.getLevel().test(level)) {
            if (record == null) {
                // 此处距离formatter.format()还隔着一层调用，所以encapsulationLayerCount需要再加1
                record = new FormatLogRecord(tag, level, encapsulationLayerCount + 1, format, args);
            }
            handleLogRecord(level, record, handler);
        }
        return record;
    }

    private void handleLogRecord(LogLevel level, LogRecord record, LogHandler handler) {
        LogFormatter formatter = handler.getFormatter();
        if (formatter == null) {
            formatter = defaultFormatter;
        }
        FormattedLog formattedLog = formatter.format(record);
        handler.handleLog(level, tag, formattedLog.getLog(), formattedLog.getMessageStart(), formattedLog.getMessageEnd());
        formattedLog.recycle();
    }

    /**
     * 对于日志方法先封装再使用的场景需要调用此方法输出日志，否则定位不到日志打点处。
     *
     * @param encapsulationLayerCount 日志打点处距离此方法中间封装的方法层数。
     * @param level
     * @param parts
     */
    public void concat(int encapsulationLayerCount, LogLevel level, Object... parts) {
        if (this.level != null && this.level.test(level)) {
            LogRecord record = null;
            if (handlers.isEmpty()) {
                handleConcatLog(encapsulationLayerCount + 1, level, null, defaultHandler, parts);
            } else {
                for (LogHandler handler : handlers) {
                    record = handleConcatLog(encapsulationLayerCount + 1, level, record, handler, parts);
                }
            }
        }
    }

    private LogRecord handleConcatLog(int encapsulationLayerCount, LogLevel level, LogRecord record, LogHandler handler, Object... parts) {
        if (handler.getLevel() == null || handler.getLevel().test(level)) {
            if (record == null) {
                // 此处距离formatter.format()还隔着一层调用，所以encapsulationLayerCount需要再加1
                record = new ConcatLogRecord(tag, level, encapsulationLayerCount + 1, parts);
            }
            handleLogRecord(level, record, handler);
        }
        return record;
    }

    public void format(LogLevel level, String msg, Object... args) {
        format(1, level, msg, args);
    }

    public void concat(LogLevel level, Object... parts) {
        concat(1, level, parts);
    }

    public void v(String msg, Object... args) {
        format(1, LogLevel.Verbose, msg, args);
    }

    public void vv(Object... parts) {
        concat(1, LogLevel.Verbose, parts);
    }

    public void d(String msg, Object... args) {
        format(1, LogLevel.Debug, msg, args);
    }

    public void dd(Object... parts) {
        concat(1, LogLevel.Debug, parts);
    }

    public void i(String msg, Object... args) {
        format(1, LogLevel.Info, msg, args);
    }

    public void ii(Object... parts) {
        concat(1, LogLevel.Info, parts);
    }

    public void w(String msg, Object... args) {
        format(1, LogLevel.Warn, msg, args);
    }

    public void ww(Object... parts) {
        concat(1, LogLevel.Warn, parts);
    }

    public void e(String msg, Object... args) {
        format(1, LogLevel.Error, msg, args);
    }

    public void ee(Object... parts) {
        concat(1, LogLevel.Error, parts);
    }

    public void catches(Exception e) {
        for (LogHandler handler : handlers) {
            handler.catches(tag, e);
        }
    }
}
