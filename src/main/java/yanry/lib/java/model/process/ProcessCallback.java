package yanry.lib.java.model.process;

/**
 * Created by yanry on 2020/1/11.
 */
public interface ProcessCallback<R> {
    void onSuccess(R result);

    void onFail(boolean isTimeout);
}
