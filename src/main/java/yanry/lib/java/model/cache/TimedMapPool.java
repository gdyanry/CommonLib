package yanry.lib.java.model.cache;

import yanry.lib.java.entity.DaemonTimer;
import yanry.lib.java.model.Singletons;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimerTask;

public abstract class TimedMapPool<K, V> {
    private HashMap<K, V> map;
    private HashMap<K, Long> accessTime;

    public TimedMapPool(int minTimeoutSecond) {
        long timeout = minTimeoutSecond * 1000;
        map = new HashMap<>();
        accessTime = new HashMap<>();
        Singletons.get(DaemonTimer.class).schedule(new TimerTask() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                Iterator<Map.Entry<K, Long>> iterator = accessTime.entrySet().iterator();
                synchronized (accessTime) {
                    while (iterator.hasNext()) {
                        Map.Entry<K, Long> next = iterator.next();
                        if (now - next.getValue() >= timeout) {
                            iterator.remove();
                            map.remove(next.getKey());
                        }
                    }
                }
            }
        }, timeout, timeout);
    }

    public V get(K key) {
        synchronized (accessTime) {
            accessTime.put(key, System.currentTimeMillis());
        }
        V value = map.get(key);
        if (value == null) {
            value = offerByKey(key);
            map.put(key, value);
        }
        return value;
    }

    protected abstract V offerByKey(K key);
}
