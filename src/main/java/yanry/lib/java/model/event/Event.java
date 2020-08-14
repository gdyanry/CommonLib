package yanry.lib.java.model.event;

import java.util.HashMap;
import java.util.ListIterator;

import yanry.lib.java.model.log.LogLevel;
import yanry.lib.java.model.log.Logger;

/**
 * @author: rongyu.yan
 * @create: 2020-07-25 16:01
 **/
public class Event {
    private Logger logger;
    private LogLevel logLevel;

    public void configLogger(Logger logger, LogLevel logLevel) {
        this.logger = logger;
        this.logLevel = logLevel;
    }

    void log(Object... logParts) {
        if (logger != null) {
            logger.concat(logLevel, logParts);
        }
    }

    HashMap<EventInterceptor, ListIterator> iteratorCache;

    public Event() {
        iteratorCache = new HashMap<>();
    }

    public int getCurrentLevel() {
        return iteratorCache.size() + 1;
    }
}
