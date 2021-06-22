package yanry.lib.java.model.watch;

/**
 * Created by yanry on 2020/3/12.
 */
public interface BooleanHolder {
    boolean addWatcher(BooleanWatcher watcher);

    boolean removeWatcher(BooleanWatcher watcher);

    boolean getValue();

    default boolean getAndWatch(BooleanWatcher watcher) {
        if (watcher != null) {
            watcher.onValueChange(getValue());
        }
        return addWatcher(watcher);
    }
}
