package yanry.lib.java.model;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by yanry on 2020/5/8.
 */
public class Registry<T> {
    private CopyOnWriteArrayList<T> registrants;

    public boolean register(T registrant) {
        if (registrant == null) {
            return false;
        }
        if (registrants == null) {
            synchronized (this) {
                if (registrants == null) {
                    registrants = new CopyOnWriteArrayList<>();
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
     * 获取内部List，不为null
     *
     * @return
     */
    public List<T> getList() {
        return registrants == null ? Collections.EMPTY_LIST : registrants;
    }
}
