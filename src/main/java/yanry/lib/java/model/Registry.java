package yanry.lib.java.model;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by yanry on 2020/5/8.
 */
public class Registry<T> extends LinkedList<T> {
    public boolean register(T registrant) {
        if (!contains(registrant)) {
            return add(registrant);
        }
        return false;
    }

    public boolean unregister(T registrant) {
        return remove(registrant);
    }

    public ArrayList<T> getCopy() {
        return new ArrayList<>(this);
    }
}
