package yanry.lib.java.model.watch;

import java.util.Objects;

import yanry.lib.java.model.Registry;

/**
 * Created by yanry on 2020/3/2.
 */
public class ValueHolderImpl<V> extends Registry<ValueWatcher<V>> implements ValueHolder<V> {
    private V value;

    public ValueHolderImpl() {
    }

    public ValueHolderImpl(V value) {
        this.value = value;
    }

    /**
     * @param value the new value to set.
     * @return previous value.
     */
    public V setValue(V value) {
        V oldValue = this.value;
        if (!Objects.equals(this.value, value)) {
            this.value = value;
            onValueChange(value, oldValue);
            for (ValueWatcher<V> watcher : getList()) {
                watcher.onValueChange(value, oldValue);
            }
        }
        return oldValue;
    }

    protected void onValueChange(V to, V from) {
    }

    @Override
    public boolean addWatcher(ValueWatcher<V> watcher) {
        return register(watcher);
    }

    @Override
    public boolean removeWatcher(ValueWatcher<V> watcher) {
        return unregister(watcher);
    }

    @Override
    public V getValue() {
        return value;
    }
}
