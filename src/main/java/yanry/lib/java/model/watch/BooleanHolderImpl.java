package yanry.lib.java.model.watch;

import yanry.lib.java.model.Registry;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by yanry on 2020/3/3.
 */
public class BooleanHolderImpl extends Registry<BooleanWatcher> implements BooleanHolder {
    private AtomicBoolean val;

    public BooleanHolderImpl() {
        val = new AtomicBoolean();
    }

    public BooleanHolderImpl(boolean value) {
        val = new AtomicBoolean(value);
    }

    public boolean setValue(boolean value) {
        if (val.compareAndSet(!value, value)) {
            onValueChange(value);
            for (BooleanWatcher watcher : getList()) {
                watcher.onValueChange(value);
            }
            return true;
        }
        return false;
    }

    protected void onValueChange(boolean to) {
    }

    @Override
    public boolean addWatcher(BooleanWatcher watcher) {
        return register(watcher);
    }

    @Override
    public boolean removeWatcher(BooleanWatcher watcher) {
        return unregister(watcher);
    }

    @Override
    public boolean getValue() {
        return val.get();
    }
}
