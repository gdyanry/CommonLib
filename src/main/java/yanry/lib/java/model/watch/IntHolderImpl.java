package yanry.lib.java.model.watch;

import yanry.lib.java.model.Registry;

import java.util.Objects;

public class IntHolderImpl extends Registry<IntWatcher> implements IntHolder {
    private int value;

    public IntHolderImpl() {
    }

    public IntHolderImpl(int value) {
        this.value = value;
    }

    public int setValue(int value) {
        int oldValue = this.value;
        if (!Objects.equals(this.value, value)) {
            this.value = value;
            onDispatchValueChange(value, oldValue);
            for (IntWatcher watcher : getList()) {
                watcher.onValueChange(value, oldValue);
            }
        }
        return oldValue;
    }

    protected void onDispatchValueChange(int to, int from) {
    }

    @Override
    public boolean addWatcher(IntWatcher watcher) {
        return register(watcher);
    }

    @Override
    public boolean removeWatcher(IntWatcher watcher) {
        return unregister(watcher);
    }

    @Override
    public int getValue() {
        return value;
    }
}
