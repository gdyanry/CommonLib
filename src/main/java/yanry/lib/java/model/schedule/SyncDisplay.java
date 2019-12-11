package yanry.lib.java.model.schedule;

/**
 * rongyu.yan
 * 2018/11/13
 **/
public abstract class SyncDisplay<D extends ShowData, V> extends Display<D, V> {

    @Override
    protected void show(D data) {
        setView(showData(getView(), data));
    }

    /**
     * @param currentInstance may be null.
     * @param data
     * @return
     */
    protected abstract V showData(V currentInstance, D data);
}
