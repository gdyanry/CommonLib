package yanry.lib.java.model.schedule;

/**
 * Created by yanry on 2019/12/15.
 */
public interface OnDataStateChangeListener {
    /**
     * @param toState {@link ShowData#STATE_ENQUEUE} means exactly after enqueue;
     *                {@link ShowData#STATE_DEQUEUE} means exactly before dequeue.
     *                {@link ShowData#STATE_SHOWING} means exactly after shown;
     *                {@link ShowData#STATE_DISMISS} generally means before dismiss, except when dismiss is from {@link Display#notifyDismiss(Object)}.
     */
    void onDataStateChange(int toState);
}
