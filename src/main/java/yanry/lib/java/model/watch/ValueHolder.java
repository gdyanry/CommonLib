package yanry.lib.java.model.watch;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Objects;

/**
 * Created by yanry on 2020/3/2.
 */
public class ValueHolder<V> extends LinkedList<ValueWatcher<V>> {
    private V value;

    public ValueHolder() {
    }

    public ValueHolder(V value) {
        this.value = value;
    }

    public boolean addWatcher(ValueWatcher<V> watcher) {
        if (!contains(watcher)) {
            add(watcher);
            return true;
        }
        return false;
    }

    public boolean setValue(V value) {
        if (!Objects.equals(this.value, value)) {
            V oldValue = this.value;
            this.value = value;
            if (size() > 0) {
                ArrayList<ValueWatcher<V>> temp = new ArrayList<>(this);
                for (ValueWatcher<V> watcher : temp) {
                    watcher.onValueChange(value, oldValue);
                }
            }
            return true;
        }
        return false;
    }

    public V getValue() {
        return value;
    }
}
