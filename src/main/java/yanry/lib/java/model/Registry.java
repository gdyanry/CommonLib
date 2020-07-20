package yanry.lib.java.model;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

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

    @Nullable
    public Collection<T> getRaw() {
        return registrants;
    }

    @NotNull
    public List<T> getCopy() {
        return registrants == null || registrants.size() == 0 ? Collections.EMPTY_LIST : new ArrayList<>(registrants);
    }

    public void clear() {
        if (registrants != null) {
            registrants.clear();
        }
    }
}
