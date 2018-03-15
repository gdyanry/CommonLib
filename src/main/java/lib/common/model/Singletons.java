/**
 * 
 */
package lib.common.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yanry
 *
 *         2016年5月22日
 */
@SuppressWarnings("unchecked")
public final class Singletons {

	private static Map<Class<?>, Object> container = new HashMap<Class<?>, Object>();

	private Singletons() {
	}

	/**
	 * 
	 * @param type
	 *            this class must have an accessible parameterless constructor.
	 * @return
	 */
	public static <T> T get(Class<T> type) {
		T obj = (T) container.get(type);
		if (obj == null) {
			synchronized (type) {
				obj = (T) container.get(type);
				if (obj == null) {
					try {
						obj = type.newInstance();
						container.put(type, obj);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		return obj;
	}

	/**
	 * 
	 * @param type
	 * @return the previous created object of the given type, might be null.
	 */
	public static <T> T remove(Class<T> type) {
		return (T) container.remove(type);
	}
	
	public static void release() {
		container.clear();
	}
}
