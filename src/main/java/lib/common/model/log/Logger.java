package lib.common.model.log;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Logger {
    private static HashMap<Object, Logger> instances = new HashMap<>();
    private final static LogFormatter defaultFormatter = new SimpleFormatter();
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
     * @param format
     * @param args
     */
    public void format(int encapsulationLayerCount, LogLevel level, String format, Object... args) {
        doLog(encapsulationLayerCount + 1, level, args.length == 0 ? format : String.format(format, args));
    }

    /**
     * 对于日志方法先封装再使用的场景需要调用此方法输出日志，否则定位不到日志打点处。
     *
     * @param encapsulationLayerCount 日志打点处距离此方法中间封装的方法层数。
     * @param level
     * @param parts
     */
    public void concat(int encapsulationLayerCount, LogLevel level, Object... parts) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Object part : parts) {
            stringBuilder.append(part);
        }
        doLog(encapsulationLayerCount + 1, level, stringBuilder.toString());
    }

    private void doLog(int encapsulationLayerCount, LogLevel level, String log) {
        if (this.level != null && this.level.test(level)) {
            LogRecord record = null;
            for (LogHandler handler : handlers) {
                if (handler.getLevel() == null || handler.getLevel().test(level)) {
                    if (record == null) {
                        record = LogRecord.get(tag, level, log, encapsulationLayerCount);
                    }
                    LogFormatter formatter = handler.getFormatter();
                    if (formatter == null) {
                        formatter = defaultFormatter;
                    }
                    FormattedLog formattedLog = formatter.format(record);
                    handler.handleLog(level, tag, formattedLog.getLog(), formattedLog.getMessageStart(), formattedLog.getMessageEnd());
                    formattedLog.recycle();
                }
            }
            if (record != null) {
                record.recycle();
            }
        }
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
