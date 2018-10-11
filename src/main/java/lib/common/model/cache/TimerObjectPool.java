/**
 *
 */
package lib.common.model.cache;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A linked list based object pool with timeout mechanism.
 *
 * @param <E> type of pooled object.
 * @author yanry
 * <p>
 * 2014年7月7日下午3:33:11
 */
public abstract class TimerObjectPool<E> extends Timer {
    private LinkedList<E> container;
    private LinkedList<Long> timeRecords;

    public TimerObjectPool(int timeoutSeconds) {
        super(true);
        container = new LinkedList<>();
        timeRecords = new LinkedList<>();
        long timeout = timeoutSeconds * 1000;
        schedule(new TimerTask() {
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
    public synchronized void giveBack(E element) {
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
    public synchronized E borrow() {
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
