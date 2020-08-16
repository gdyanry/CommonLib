package yanry.lib.java.model.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import yanry.lib.java.model.runner.Runner;

/**
 * Created by yanry on 2020/4/27.
 */
public abstract class CacheTimer<T> implements Runnable {
    private Runner runner;
    private ConcurrentHashMap<T, Long> tagTime;
    private long minTimeout;

    public CacheTimer(Runner runner) {
        this.runner = runner;
        tagTime = new ConcurrentHashMap<>();
    }

    public void startTiming(long minTimeout) {
        if (minTimeout > 0) {
            this.minTimeout = minTimeout;
            runner.schedule(this, minTimeout);
        }
    }

    public void stopTiming() {
        runner.cancel(this);
    }

    public void refresh(T tag) {
        tagTime.put(tag, System.currentTimeMillis());
    }

    public void refreshAll(Collection<? extends T> tags) {
        long now = System.currentTimeMillis();
        for (T tag : tags) {
            tagTime.put(tag, now);
        }
    }

    public void invalid(T tag) {
        tagTime.remove(tag);
    }

    public void invalidAll() {
        tagTime.clear();
    }

    protected abstract void onTimeout(T tag);

    @Override
    public final void run() {
        long now = System.currentTimeMillis();
        ArrayList<Map.Entry<T, Long>> entries = new ArrayList<>(tagTime.entrySet());
        for (Map.Entry<T, Long> entry : entries) {
            if (now - entry.getValue() >= minTimeout) {
                T tag = entry.getKey();
                tagTime.remove(tag);
                onTimeout(tag);
            }
        }
        runner.schedule(this, minTimeout);
    }
}
