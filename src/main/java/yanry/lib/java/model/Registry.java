package yanry.lib.java.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by yanry on 2020/5/8.
 */
public class Registry<T> {
    private LinkedList<T> registrants;

    public Registry() {
        registrants = new LinkedList<>();
    }

    public boolean register(T registrant) {
        if (registrants == null) {
            registrants = new LinkedList<>();
            registrants.add(registrant);
            return true;
        } else if (registrants.contains(registrant)) {
            return false;
        }
        return registrants.add(registrant);
    }

    public boolean unregister(T registrant) {
        return registrants != null && registrants.remove(registrant);
    }

    public List<T> getCopy() {
        return registrants == null ? Collections.EMPTY_LIST : new ArrayList<>(registrants);
    }
}
