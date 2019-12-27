package yanry.lib.java.model.schedule;

import yanry.lib.java.model.log.Logger;

public abstract class ReusableDisplay<D extends ShowData, V> extends SyncDisplay<D, V> {

    @Override
    protected final V showData(V currentView, D data) {
        if (currentView == null || !enableReuse()) {
            currentView = createView(data);
            Logger.getDefault().dd("create view: ", currentView);
        }
        if (currentView != null) {
            if (!isShowing(currentView)) {
                Logger.getDefault().vv("show view: ", currentView);
                showView(currentView);
            }
            setData(currentView, data);
        }
        return currentView;
    }

    protected boolean enableReuse() {
        return true;
    }

    protected abstract V createView(D data);

    protected abstract void setData(V view, D data);

    protected abstract void showView(V view);
}
