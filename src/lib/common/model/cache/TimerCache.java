/**
 * 
 */
package lib.common.model.cache;

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
public class TimerCache<V> {
	private HashMap<Object, V> cache;
	private int timeout;
	private HashMap<Object, TimerTask> tts;
	private Timer timer;
	private CacheTimeoutListener<V> listener;

	public TimerCache(int timeoutSecond, Timer timer) {
		this(timeoutSecond, timer, null);
	}

	public TimerCache(int timeoutSecond, Timer timer, TimerCache<V> cache) {
		if (cache == null) {
			this.cache = new HashMap<Object, V>();
		} else {
			this.cache = cache.cache;
		}
		timeout = timeoutSecond * 1000;
		tts = new HashMap<Object, TimerTask>();
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

	public void put(final Object key, final V value) {
		cache.put(key, value);
		cancelTask(key);
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				cache.remove(key);
				tts.remove(key);
				if (listener != null) {
					listener.onTimeout(key, value);
				}
			}
		};
		tts.put(key, task);
		timer.schedule(task, timeout);
	}

	public V get(Object key) {
		return cache.get(key);
	}

	public V remove(Object key) {
		cancelTask(key);
		return cache.remove(key);
	}

	private void cancelTask(final Object key) {
		TimerTask task = tts.remove(key);
		if (task != null) {
			task.cancel();
		}
	}

	public void setOnTimeoutListener(CacheTimeoutListener<V> listener) {
		this.listener = listener;
	}

	public interface CacheTimeoutListener<V> {
		void onTimeout(Object key, V value);
	}
}
