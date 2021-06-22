package yanry.lib.java.model.cancelable;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by yanrongyu on 16/9/21.
 */

public abstract class Cancelable {
    private Set<Object> tags;
    private boolean isCancel;

    public Cancelable() {
        tags = new HashSet<>();
    }

    public void addTag(Object tag) {
        if (tag != null) {
            synchronized (getManager()) {
                tags.add(tag);
                getManager().get(tag).add(this);
            }
        }
    }

    public void cancel() {
        if (!isCancel) {
            isCancel = true;
            doCancel();
            release();
        }
    }

    public void release() {
        synchronized (getManager()) {
            for (Object t : tags) {
                getManager().remove(t, this);
            }
            tags.clear();
        }
    }

    public boolean isCancel() {
        return isCancel;
    }

    protected abstract CancelableManager getManager();

    protected abstract void doCancel();
}
