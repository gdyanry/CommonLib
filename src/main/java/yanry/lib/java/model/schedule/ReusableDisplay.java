package yanry.lib.java.model.schedule;

public abstract class ReusableDisplay<D extends ShowData, V> extends SyncDisplay<D, V> {

    @Override
    protected final V showData(V currentInstance, D data) {
        if (currentInstance == null || !enableReuse()) {
            currentInstance = createInstance(data);
        }
        if (currentInstance != null) {
            if (!isShowing(currentInstance)) {
                showView(currentInstance);
            }
            setData(currentInstance, data);
        }
        return currentInstance;
    }

    protected boolean enableReuse() {
        return true;
    }

    protected abstract V createInstance(D data);

    protected abstract void setData(V instance, D data);

    protected abstract void showView(V instance);
}
