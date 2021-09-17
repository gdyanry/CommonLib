package yanry.lib.java.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yanry
 * <p>
 * 2016年5月22日
 */
public final class Singletons {

    private static Map<Class<?>, Object> container = new HashMap<>();

    private Singletons() {
    }

    public static <T> T get(Class<T> type) {
        Object instance = container.get(type);
        if (instance == null) {
            synchronized (type) {
                instance = container.get(type);
                if (instance == null) {
                    try {
                        instance = type.getDeclaredConstructor().newInstance();
                        container.put(type, instance);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return (T) instance;
    }

    public static <T> T peek(Class<T> type) {
        return (T) container.get(type);
    }

    public static <T> void set(Class<T> type, T instance) {
        container.put(type, instance);
    }

    /**
     * @param type
     * @return the previous created object of the given type, might be null.
     */
    public static <T> T remove(Class<T> type) {
        return (T) container.remove(type);
    }

    public static void clear() {
        container.clear();
    }
}
