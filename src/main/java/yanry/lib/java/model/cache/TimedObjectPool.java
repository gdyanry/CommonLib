package yanry.lib.java.model.cache;

import yanry.lib.java.entity.DaemonTimer;
import yanry.lib.java.model.Singletons;

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
public abstract class TimedObjectPool<E> {
    private LinkedList<E> container;
    private LinkedList<Long> timeRecords;

    public TimedObjectPool(int minTimeoutSecond) {
        super();
        container = new LinkedList<>();
        timeRecords = new LinkedList<>();
        long timeout = minTimeoutSecond * 1000;
        Singletons.get(DaemonTimer.class).schedule(new TimerTask() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                Long time;
                synchronized (this) {
                    while ((time = timeRecords.peekFirst()) != null && now - time > timeout) {
                        timeRecords.removeFirst();
                        onDiscard(container.pollFirst());
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
            synchronized (this) {
                container.addLast(element);
                timeRecords.addLast(System.currentTimeMillis());
            }
        }
    }

    /**
     * Borrow an object from the pool. This method is thread-safe.
     *
     * @return
     */
    public E borrow() {
        if (container.size() == 0) {
            return createInstance();
        }
        synchronized (this) {
            if (container.size() == 0) {
                return createInstance();
            } else {
                timeRecords.removeFirst();
                return container.pollFirst();
            }
        }
    }

    protected abstract E createInstance();

    protected abstract void onReturn(E obj);

    protected abstract void onDiscard(E obj);

    protected abstract void onCleared(int poolSize);
}
