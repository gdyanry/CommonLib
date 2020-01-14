package yanry.lib.java.model.process;

/**
 * 请求结果回调。
 * <p>
 * Created by yanry on 2020/1/11.
 */
public interface ProcessCallback<R> {
    /**
     * 成功回调。
     *
     * @param result 请求结果。
     */
    void onSuccess(R result);

    /**
     * 失败回调。
     *
     * @param isTimeout 是否超时失败。只有当{@link Processor#getTimeout()}大于0时才有可能为true。
     */
    void onFail(boolean isTimeout);
}
