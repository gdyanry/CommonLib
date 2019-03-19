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
    private static ObjectProvider objectProvider;

    private Singletons() {
    }

    public static void setObjectProvider(ObjectProvider objectProvider) {
        Singletons.objectProvider = objectProvider;
    }

    public static <T> T get(Class<T> type) {
        Object instance = container.get(type);
        if (instance == null) {
            synchronized (type) {
                if (instance == null) {
                    if (objectProvider != null) {
                        instance = objectProvider.getInstance(type);
                    }
                    if (instance == null) {
                        try {
                            instance = type.getDeclaredConstructor().newInstance();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    container.put(type, instance);
                }
            }
        }
        return (T) instance;
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

    public interface ObjectProvider {
        <T> T getInstance(Class<T> type);
    }
}
