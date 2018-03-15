/**
 * 
 */
package lib.common.model.cache;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import lib.common.model.Singletons;

/**
 * A linked list based object pool with timeout mechanism.
 * 
 * @param <E>
 *            type of pooled object.
 * @author yanry
 *
 *         2014年7月7日下午3:33:11
 */
public abstract class TimerObjectPool<E> {
	private List<E> container;
	private long timeout;
	private HashMap<E, TimerTask> tts;

	public TimerObjectPool(int timeoutSeconds) {
		container = new LinkedList<E>();
		timeout = timeoutSeconds * 1000;
		tts = new HashMap<E, TimerTask>();
	}

	/**
	 * Return the borrowed object to the pool.
	 * 
	 * @param element
	 */
	public synchronized void restore(final E element) {
		if (!container.contains(element)) {
			container.add(element);
			TimerTask tt = new TimerTask() {
				
				@Override
				public void run() {
					container.remove(element);
					tts.remove(element);
					release(element);
				}
			};
			Singletons.get(Timer.class).schedule(tt, timeout);
			tts.put(element, tt);
		}
	}

	/**
	 * Borrow an object from the pool. This method is thread-safe.
	 * @return
	 */
	public synchronized E take() {
		E e;
		if (container.isEmpty()) {
			e = generate();
		} else {
			e = container.remove(0);
		}
		TimerTask tt = tts.remove(e);
		if (tt != null) {
			tt.cancel();
		}
		return e;
	}

	/**
	 * Construct an object.
	 * @return
	 */
	protected abstract E generate();
	
	protected abstract void release(E obj);
}
