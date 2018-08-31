/**
 *
 */
package lib.common.model;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yanry
 * <p>
 * 2016年5月22日
 */
public final class Singletons {

    private static Map<Class<?>, SoftReference<Object>> container = new HashMap<>();
    private static ObjectProvider objectProvider;

    private Singletons() {
    }

    public static void setObjectProvider(ObjectProvider objectProvider) {
        Singletons.objectProvider = objectProvider;
    }

    public static <T> T get(Class<T> type) {
        SoftReference<Object> reference = container.get(type);
        if (reference == null || reference.get() == null) {
            synchronized (type) {
                if (reference == null || reference.get() == null) {
                    Object obj = null;
                    if (objectProvider != null) {
                        obj = objectProvider.getInstance(type);
                    }
                    if (obj == null) {
                        try {
                            obj = type.getDeclaredConstructor().newInstance();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    reference = new SoftReference<>(obj);
                    container.put(type, reference);
                }
            }
        }
        return (T) reference.get();
    }

    /**
     * @param type
     * @return the previous created object of the given type, might be null.
     */
    public static <T> T remove(Class<T> type) {
        SoftReference<Object> reference = container.remove(type);
        return reference == null ? null : (T) reference.get();
    }

    public static void release() {
        container.clear();
    }

    public interface ObjectProvider {
        <T> T getInstance(Class<T> type);
    }
}
