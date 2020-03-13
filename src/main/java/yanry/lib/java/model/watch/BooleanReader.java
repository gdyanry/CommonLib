package yanry.lib.java.model.watch;

/**
 * Created by yanry on 2020/3/12.
 */
public interface BooleanReader {
    boolean addWatcher(BooleanWatcher watcher);

    boolean removeWatcher(BooleanWatcher watcher);

    boolean getValue();
}
