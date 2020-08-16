package yanry.lib.java.model.cache;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A map based cache with timeout mechanism.
 *
 * @author yanry
 *
 *         2014年10月11日 上午11:18:00
 */
public class TimedCache<V> {
	private HashMap<Object, V> cache;
	private int timeout;
    private HashMap<Object, TimerTask> tasks;
    private Timer timer;
	private CacheTimeoutListener<V> listener;

    public TimedCache(int timeoutSecond, Timer timer) {
		this(timeoutSecond, timer, null);
	}

    public TimedCache(int timeoutSecond, Timer timer, TimedCache<V> cache) {
		if (cache == null) {
			this.cache = new HashMap<>();
		} else {
			this.cache = cache.cache;
		}
		timeout = timeoutSecond * 1000;
        tasks = new HashMap<>();
        this.timer = timer;
	}

	public int size() {
		return cache.size();
	}

	/**
     *
	 * @return timeout in millisecond
	 */
	public int getTimeoutMilli() {
		return timeout;
	}

    public void put(final Object key, V value) {
        cache.put(key, value);
        scheduleTimeout(key);
    }

    private void scheduleTimeout(final Object key) {
        cancelTask(key);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                cache.remove(key);
                tasks.remove(key);
                if (listener != null) {
                    listener.onTimeout(key, cache.get(key));
                }
            }
        };
        tasks.put(key, task);
        timer.schedule(task, timeout);
    }

	public V get(Object key) {
        scheduleTimeout(key);
        return cache.get(key);
	}

	public V remove(Object key) {
		cancelTask(key);
		return cache.remove(key);
	}

	private void cancelTask(final Object key) {
        TimerTask task = tasks.remove(key);
        if (task != null) {
			task.cancel();
			timer.purge();
		}
	}

	public void setOnTimeoutListener(CacheTimeoutListener<V> listener) {
		this.listener = listener;
	}

	public interface CacheTimeoutListener<V> {
		void onTimeout(Object key, V value);
	}
}
