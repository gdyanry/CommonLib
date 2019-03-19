package yanry.lib.java.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A class that manages {@link PendingOperation} objects.
 * 
 * @param <D>
 *            type of data that is supplied when triggering pending operations.
 * @date 2014-3-21
 * 
 */
public class PendingOperationManager<D> {
	private Map<Object, List<PendingOperation>> cache;

	public PendingOperationManager() {
		cache = new HashMap<Object, List<PendingOperation>>();
	}

	/**
	 * {@link PendingOperation} objects constructed with the given trigger will
	 * be executed.
	 * 
	 * @param trigger
	 * @param data
	 */
	public void notify(Object trigger, D data) {
		List<PendingOperation> list = cache.remove(trigger);
		if (list != null) {
			for (PendingOperation po : list) {
				po.exec(data);
			}
		}
	}

	/**
	 * {@link PendingOperation} objects constructed with the given trigger will
	 * be removed.
	 * 
	 * @param trigger
	 */
	public void cancel(Object trigger) {
		cache.remove(trigger);
	}

	private List<PendingOperation> getOpList(Object trigger) {
		if (cache.get(trigger) == null) {
			synchronized (trigger) {
				if (cache.get(trigger) == null) {
					cache.put(trigger, new LinkedList<PendingOperation>());
				}
			}
		}
		return cache.get(trigger);
	}

	/**
	 * This class represents an operation that can be executed with some data in
	 * the future.
	 * 
	 * @author yanry
	 *
	 *         2015年7月14日 上午10:05:05
	 */
	public abstract class PendingOperation {

		/**
		 * 
		 * @param trigger
		 *            served as identity or category of this object. One trigger
		 *            can be binded to multiple {@link PendingOperation}
		 *            objects.
		 */
		public PendingOperation(Object trigger) {
			getOpList(trigger).add(this);
		}

		/**
		 * Code that will be executed in the future.
		 * 
		 * @param data
		 */
		protected abstract void exec(D data);
	}
}
