package yanry.lib.java.model.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

import yanry.lib.java.entity.DaemonTimer;
import yanry.lib.java.model.Singletons;

/**
 * Created by yanry on 2020/4/27.
 */
public abstract class CacheTimer<T> {
    private HashMap<T, Long> tagTime;
    private TimerTask timerTask;

    public CacheTimer() {
        tagTime = new HashMap<>();
    }

    public boolean startTiming(long minTimeout) {
        if (timerTask == null) {
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    long now = System.currentTimeMillis();
                    ArrayList<Map.Entry<T, Long>> entries = new ArrayList<>(tagTime.entrySet());
                    for (Map.Entry<T, Long> entry : entries) {
                        if (now - entry.getValue() >= minTimeout) {
                            T tag = entry.getKey();
                            tagTime.remove(tag);
                            onTimeout(tag);
                        }
                    }
                }
            };
            Singletons.get(DaemonTimer.class).schedule(timerTask, minTimeout, minTimeout);
            return true;
        }
        return false;
    }

    public void stopTiming() {
        if (timerTask != null) {
            timerTask.cancel();
        }
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
}
