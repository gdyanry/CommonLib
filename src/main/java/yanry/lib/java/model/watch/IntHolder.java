package yanry.lib.java.model.watch;

public interface IntHolder {
    boolean addWatcher(IntWatcher watcher);

    boolean removeWatcher(IntWatcher watcher);

    int getValue();

    default boolean getAndWatch(IntWatcher watcher) {
        if (watcher != null) {
            watcher.onValueChange(getValue(), 0);
        }
        return addWatcher(watcher);
    }
}
