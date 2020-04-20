package yanry.lib.java.model.watch;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Objects;

/**
 * Created by yanry on 2020/3/2.
 */
public class ValueHolderImpl<V> extends LinkedList<ValueWatcher<V>> implements ValueHolder<V> {
    private V value;

    public ValueHolderImpl() {
    }

    public ValueHolderImpl(V value) {
        this.value = value;
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

    @Override
    public boolean addWatcher(ValueWatcher<V> watcher) {
        if (!contains(watcher)) {
            add(watcher);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeWatcher(ValueWatcher<V> watcher) {
        return remove(watcher);
    }

    @Override
    public V getValue() {
        return value;
    }
}
