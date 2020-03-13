package yanry.lib.java.model.watch;

/**
 * Created by yanry on 2020/3/12.
 */
public interface ValueVisitor<V> {
    boolean addWatcher(ValueWatcher<V> watcher);

    boolean removeWatcher(ValueWatcher<V> watcher);

    V getValue();
}
