/**
 *
 */
package lib.common.model.cache;

import lib.common.entity.DaemonTimer;
import lib.common.model.Singletons;

import java.util.LinkedList;
import java.util.TimerTask;

/**
 * A simple but effective linked-list-based object pool with timeout mechanism.
 *
 * @param <E> type of pooled object.
 * @author yanry
 * <p>
 * 2014年7月7日下午3:33:11
 */
public abstract class TimerObjectPool<E> {
    private LinkedList<E> container;
    private LinkedList<Long> timeRecords;

    public TimerObjectPool(int timeoutSeconds) {
        super();
        container = new LinkedList<>();
        timeRecords = new LinkedList<>();
        long timeout = timeoutSeconds * 1000;
        Singletons.get(DaemonTimer.class).schedule(new TimerTask() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                Long time;
                while ((time = timeRecords.peekFirst()) != null && now - time > timeout) {
                    timeRecords.pollFirst();
                    E obj = container.pollFirst();
                    if (obj != null) {
                        onDiscard(obj);
                    }
                }
                onCleared(container.size());
            }
        }, timeout, timeout);
    }

    /**
     * Return the borrowed object to the pool.
     *
     * @param element
     */
    public void giveBack(E element) {
        if (element != null) {
            onReturn(element);
            container.addLast(element);
            timeRecords.addLast(System.currentTimeMillis());
        }
    }

    /**
     * Borrow an object from the pool. This method is thread-safe.
     *
     * @return
     */
    public E borrow() {
        E e = container.pollFirst();
        if (e == null) {
            e = createInstance();
        } else {
            timeRecords.pollFirst();
        }
        return e;
    }

    protected abstract E createInstance();

    protected abstract void onReturn(E obj);

    protected abstract void onDiscard(E obj);

    protected abstract void onCleared(int poolSize);
}
