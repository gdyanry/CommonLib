package yanry.lib.java.model.task;

import yanry.lib.java.interfaces.CommonCallback;
import yanry.lib.java.model.watch.BooleanHolder;
import yanry.lib.java.model.watch.BooleanHolderImpl;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 可保证请求任务按顺序执行的执行器，适用于对执行线程无要求但对执行顺序有要求的任务。
 */
public class SequenceActionRunner {
    private BooleanHolderImpl runningState = new BooleanHolderImpl();
    private ConcurrentLinkedQueue<CommonCallback> pendingActions = new ConcurrentLinkedQueue<>();

    public BooleanHolder getRunningState() {
        return runningState;
    }

    public void schedule(CommonCallback runnable) {
        if (runningState.setValue(true)) {
            try {
                runnable.callback();
                while ((runnable = pendingActions.poll()) != null) {
                    runnable.callback();
                }
            } finally {
                runningState.setValue(false);
            }
        } else {
            pendingActions.add(runnable);
        }
    }
}
