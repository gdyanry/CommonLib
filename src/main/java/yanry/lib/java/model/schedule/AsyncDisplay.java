package yanry.lib.java.model.schedule;

/**
 * 对应的View需要在创建时调用{@link #notifyCreate(AsyncBridge)}，并在销毁时调用{@link #notifyDismiss(V)}。
 * rongyu.yan
 * 2018/11/13
 **/
public abstract class AsyncDisplay<D extends ShowData, V> extends Display<D, V> {
    private D data;
    private AsyncBridge<D, V> bridge;

    public void notifyCreate(AsyncBridge<D, V> function) {
        this.bridge = function;
        if (data != null) {
            setView(function.show(data));
            data = null;
        }
    }

    @Override
    protected void show(D data) {
        this.data = data;
        if (getView() == null) {
            showView(data);
        } else {
            bridge.show(data);
        }
    }

    @Override
    public boolean notifyDismiss(V view) {
        if (super.notifyDismiss(view)) {
            bridge = null;
            return true;
        }
        return false;
    }

    @Override
    protected void internalDismiss() {
        super.internalDismiss();
        bridge = null;
    }

    protected abstract void showView(D data);
}
