package yanry.lib.java.model.watch;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by yanry on 2020/3/3.
 */
public class BooleanHolder extends LinkedList<BooleanWatcher> {
    private boolean value;

    public BooleanHolder() {
    }

    public BooleanHolder(boolean value) {
        this.value = value;
    }

    public boolean addWatcher(BooleanWatcher watcher) {
        if (!contains(watcher)) {
            add(watcher);
            return true;
        }
        return false;
    }

    public boolean setValue(boolean value) {
        if (this.value ^ value) {
            this.value = value;
            if (size() > 0) {
                ArrayList<BooleanWatcher> temp = new ArrayList<>(this);
                for (BooleanWatcher watcher : temp) {
                    watcher.onValueChange(value);
                }
            }
            return true;
        }
        return false;
    }

    public boolean getValue() {
        return value;
    }
}