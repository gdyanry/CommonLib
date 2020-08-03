package yanry.lib.java.model.event;

import java.util.List;
import java.util.ListIterator;

import yanry.lib.java.model.Registry;
import yanry.lib.java.model.log.Logger;

/**
 * 事件分发器，同时本身也是事件拦截器
 *
 * @author: rongyu.yan
 * @create: 2020-07-25 15:32
 **/
public class EventDispatcher<E extends Event, I extends EventInterceptor<E>> extends Registry<I> implements EventInterceptor<E> {
    private Logger logger;

    public EventDispatcher(Logger logger) {
        this.logger = logger;
    }

    /**
     * 分发事件
     *
     * @param event
     */
    public void dispatchEvent(E event) {
        List<I> copy = getCopy();
        if (copy.size() > 0) {
            ListIterator<I> listIterator = copy.listIterator();
            while (listIterator.hasNext()) {
                if (listIterator.next().onDispatchEvent(event) > 0) {
                    break;
                }
            }
            while (listIterator.hasPrevious()) {
                I previous = listIterator.previous();
                if (previous.onEvent(event) > 0) {
                    return;
                }
            }
        }
    }

    @Override
    public int onDispatchEvent(E event) {
        List<I> copy = getCopy();
        if (copy.size() > 0) {
            ListIterator<I> listIterator = copy.listIterator();
            event.iteratorCache.put(this, listIterator);
            while (listIterator.hasNext()) {
                I next = listIterator.next();
                int skipLevel = next.onDispatchEvent(event);
                if (skipLevel > 0) {
                    if (logger != null) {
                        logger.vv(next, " intercept event: ", event, ", skipLevel=", skipLevel, ", currentLevel=", event.getCurrentLevel());
                    }
                    return --skipLevel;
                }
            }
        }
        return 0;
    }

    @Override
    public int onEvent(E event) {
        ListIterator<I> listIterator = event.iteratorCache.get(this);
        while (listIterator.hasPrevious()) {
            I previous = listIterator.previous();
            int skipLevel = previous.onEvent(event);
            if (skipLevel > 0) {
                if (logger != null) {
                    logger.vv(previous, " handle event: ", event, ", skipLevel=", skipLevel, ", currentLevel=", event.getCurrentLevel());
                }
                event.iteratorCache.remove(this);
                return --skipLevel;
            }
        }
        event.iteratorCache.remove(this);
        return 0;
    }
}
