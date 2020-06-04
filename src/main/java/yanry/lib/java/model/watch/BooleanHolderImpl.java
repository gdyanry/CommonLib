package yanry.lib.java.model.watch;

import yanry.lib.java.model.Registry;

/**
 * Created by yanry on 2020/3/3.
 */
public class BooleanHolderImpl extends Registry<BooleanWatcher> implements BooleanHolder {
    private boolean value;

    public BooleanHolderImpl() {
    }

    public BooleanHolderImpl(boolean value) {
        this.value = value;
    }

    public boolean setValue(boolean value) {
        if (this.value ^ value) {
            this.value = value;
            for (BooleanWatcher watcher : getCopy()) {
                watcher.onValueChange(value);
            }
            return true;
        }
        return false;
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
        return value;
    }
}
