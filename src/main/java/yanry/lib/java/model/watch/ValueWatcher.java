package yanry.lib.java.model.watch;

public interface ValueWatcher<V> {
    void onValueChange(V to, V from);
}
