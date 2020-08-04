package yanry.lib.java.model.watch;

/**
 * Created by yanry on 2020/3/12.
 */
public interface ValueHolder<V> {
    boolean addWatcher(ValueWatcher<V> watcher);

    boolean removeWatcher(ValueWatcher<V> watcher);

    V getValue();

    default boolean getAndWatch(ValueWatcher<V> watcher) {
        if (watcher != null) {
            watcher.onValueChange(getValue(), null);
        }
        return addWatcher(watcher);
    }
}
