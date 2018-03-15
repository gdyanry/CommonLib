package lib.common.model.cancelable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by yanrongyu on 16/9/21.
 */

public class CancelableManager {
    private Map<Object, Set<Cancelable>> map;

    public CancelableManager() {
        map = new HashMap<>();
    }

    public synchronized void cancelByTag(Object tag) {
        Set<Cancelable> cancelableSet = map.remove(tag);
        if (cancelableSet != null) {
            for (Cancelable c : cancelableSet) {
                c.cancel();
            }
        }
    }

    public synchronized Set<Cancelable> get(Object tag) {
        Set<Cancelable> cancelableSet = map.get(tag);
        if (cancelableSet == null) {
            cancelableSet = new HashSet<>();
            map.put(tag, cancelableSet);
        }
        return cancelableSet;
    }

    public synchronized void remove(Object tag, Cancelable cancelable) {
        Set<Cancelable> cancelableSet = map.get(tag);
        if (cancelableSet != null) {
            cancelableSet.remove(cancelable);
            if (cancelableSet.isEmpty()) {
                map.remove(tag);
            }
        }
    }
}
