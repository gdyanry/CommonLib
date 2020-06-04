package yanry.lib.java.model.schedule.imple;

import yanry.lib.java.model.schedule.ShowData;
import yanry.lib.java.model.schedule.ViewDisplay;

/**
 * rongyu.yan
 * 2018/11/13
 **/
public abstract class SyncDisplay<D extends ShowData, V> extends ViewDisplay<D, V> {

    @Override
    protected void show(D data) {
        setView(showData(getViewHolder().getValue(), data));
    }

    /**
     * @param currentView may be null.
     * @param data
     * @return
     */
    protected abstract V showData(V currentView, D data);
}
