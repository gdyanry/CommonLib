package yanry.lib.java.model.event;

import java.util.List;
import java.util.ListIterator;

import yanry.lib.java.model.Registry;

/**
 * 事件分发器，同时本身也是事件拦截器
 *
 * @author: rongyu.yan
 * @create: 2020-07-25 15:32
 **/
public class EventDispatcher<E extends Event, I extends EventInterceptor<E>> extends Registry<I> implements EventInterceptor<E> {
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
                I next = listIterator.next();
                if (next.isEnable() && next.onDispatchEvent(event) > 0) {
                    break;
                }
            }
            while (listIterator.hasPrevious()) {
                I previous = listIterator.previous();
                if (previous.isEnable() && previous.onEvent(event) > 0) {
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
            long now = System.currentTimeMillis();
            while (listIterator.hasNext()) {
                I next = listIterator.next();
                if (next.isEnable()) {
                    int skipLevel = next.onDispatchEvent(event);
                    long tick = System.currentTimeMillis();
                    event.log(skipLevel, next, " intercept event: ", event, ", skipLevel=", skipLevel, ", currentLevel=", event.getCurrentLevel(), ", elapsedTime=", tick - now);
                    now = tick;
                    if (skipLevel > 0) {
                        return --skipLevel;
                    }
                }
            }
        }
        return 0;
    }

    @Override
    public int onEvent(E event) {
        ListIterator<I> listIterator = event.iteratorCache.get(this);
        long now = System.currentTimeMillis();
        while (listIterator.hasPrevious()) {
            I previous = listIterator.previous();
            if (previous.isEnable()) {
                int skipLevel = previous.onEvent(event);
                long tick = System.currentTimeMillis();
                event.log(skipLevel, previous, " handle event: ", event, ", skipLevel=", skipLevel, ", currentLevel=", event.getCurrentLevel(), ", elapsedTime=", tick - now);
                now = tick;
                if (skipLevel > 0) {
                    event.iteratorCache.remove(this);
                    return --skipLevel;
                }
            }
        }
        event.iteratorCache.remove(this);
        return 0;
    }
}
