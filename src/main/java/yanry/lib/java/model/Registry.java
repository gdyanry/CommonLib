package yanry.lib.java.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by yanry on 2020/5/8.
 */
public class Registry<T> {
    private ConcurrentLinkedQueue<T> registrants;

    public boolean register(T registrant) {
        if (registrant == null) {
            return false;
        }
        if (registrants == null) {
            synchronized (this) {
                if (registrants == null) {
                    registrants = new ConcurrentLinkedQueue<>();
                    registrants.add(registrant);
                    return true;
                }
            }
        }
        if (registrants.contains(registrant)) {
            return false;
        }
        return registrants.add(registrant);
    }

    public boolean unregister(T registrant) {
        return registrants != null && registrants.remove(registrant);
    }

    /**
     * 获取内部集合，可能为null
     *
     * @return
     */
    public Collection<T> getRaw() {
        return registrants;
    }

    /**
     * 获取内部集合备份，不为null
     *
     * @return
     */
    public List<T> getCopy() {
        return registrants == null || registrants.size() == 0 ? Collections.EMPTY_LIST : new ArrayList<>(registrants);
    }

    public void clear() {
        if (registrants != null) {
            registrants.clear();
        }
    }
}
