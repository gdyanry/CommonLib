package yanry.lib.java.model.event;

import yanry.lib.java.model.Registry;
import yanry.lib.java.model.task.SequenceActionRunner;

import java.util.List;
import java.util.ListIterator;

/**
 * 事件分发器，同时本身也是事件拦截器
 *
 * @author: rongyu.yan
 * @create: 2020-07-25 15:32
 **/
public class EventDispatcher<E extends Event, I extends EventInterceptor<? super E>> extends Registry<I> implements EventInterceptor<E> {
    private SequenceActionRunner sequenceActionRunner = new SequenceActionRunner();

    /**
     * 分发事件
     *
     * @param event
     */
    public final void dispatchEvent(E event) {
        sequenceActionRunner.schedule(() -> {
            List<I> copy = getList();
            if (copy.size() > 0) {
                ListIterator<I> listIterator = copy.listIterator();
                dispatchEvent(event, listIterator);
                handleEvent(event, listIterator);
            }
            onEventDispatched(event);
        });
    }

    private int dispatchEvent(E event, ListIterator<I> listIterator) {
        event.iteratorCache.put(this, listIterator);
        int currentLevel = event.getCurrentLevel();
        long now = System.currentTimeMillis();
        while (listIterator.hasNext()) {
            I next = listIterator.next();
            if (next.isEnable()) {
                int skipLevel = next.onDispatchEvent(event);
                long tick = System.currentTimeMillis();
                event.log(skipLevel, next, " intercept event: ", event, ", skipLevel=", skipLevel, ", currentLevel=", currentLevel, ", elapsedTime=", tick - now);
                now = tick;
                if (skipLevel > 0) {
                    return --skipLevel;
                }
            }
        }
        return 0;
    }

    private int handleEvent(E event, ListIterator<I> listIterator) {
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

    protected void onEventDispatched(E event) {
    }

    @Override
    public final int onDispatchEvent(E event) {
        List<I> copy = getList();
        if (copy.size() > 0) {
            return dispatchEvent(event, copy.listIterator());
        }
        return 0;
    }

    @Override
    public final int onEvent(E event) {
        ListIterator<I> listIterator = event.iteratorCache.get(this);
        if (listIterator != null) {
            return handleEvent(event, listIterator);
        }
        return 0;
    }
}
