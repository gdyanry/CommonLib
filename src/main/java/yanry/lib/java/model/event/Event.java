package yanry.lib.java.model.event;

import yanry.lib.java.model.log.LogLevel;
import yanry.lib.java.model.log.Logger;

import java.util.HashMap;
import java.util.ListIterator;

/**
 * @author: rongyu.yan
 * @create: 2020-07-25 16:01
 **/
public class Event {
    HashMap<EventInterceptor, ListIterator> iteratorCache = new HashMap<>();
    private Logger logger;
    private LogLevel logLevel;

    public void configLogger(Logger logger, LogLevel logLevel) {
        this.logger = logger;
        this.logLevel = logLevel;
    }

    void log(int skipLevel, Object... logParts) {
        if (logger != null) {
            logger.concat(1, skipLevel > 0 ? logLevel : LogLevel.Verbose, logParts);
        }
    }

    /**
     * 获取当前事件的最大分发深度。
     *
     * @return
     */
    public int getCurrentLevel() {
        return iteratorCache.size();
    }
}
