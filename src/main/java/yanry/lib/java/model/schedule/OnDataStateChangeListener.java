package yanry.lib.java.model.schedule;

/**
 * Created by yanry on 2019/12/15.
 */
public interface OnDataStateChangeListener {
    /**
     * ShowData状态变化的回调，此时调用{@link ShowData#getState()}拿到的是旧的状态。
     * 正常情况下状态变化的回调在状态变化之前触发，这样是为了保证状态变化的时序符合预期，考虑数据在show的时候dismiss自身的情况。
     *
     * @param toState {@link ShowData#STATE_ENQUEUE} means exactly before enqueue;
     *                {@link ShowData#STATE_DEQUEUE} means exactly before dequeue;
     *                {@link ShowData#STATE_SHOWING} means exactly before show;
     *                {@link ShowData#STATE_DISMISS} generally means before dismiss, except when dismiss is from {@link Display#notifyDismiss(Object)}.
     */
    void onDataStateChange(int toState);
}
