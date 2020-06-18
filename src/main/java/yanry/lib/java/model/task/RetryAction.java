package yanry.lib.java.model.task;

import yanry.lib.java.model.runner.Runner;

/**
 * 自动重试任务，直到成功或者重试达到指定次数。
 * <p>
 * Created by yanry on 2020/6/12.
 */
public abstract class RetryAction implements Runnable {
    private Runner scheduleRunner;
    private int remainingTry;
    private Runnable schedule;

    /**
     * @param scheduleRunner 用于调度重试的Runner，注意它不等同于执行任务的线程
     * @param retryCount
     */
    public RetryAction(Runner scheduleRunner, int retryCount) {
        this.scheduleRunner = scheduleRunner;
        this.remainingTry = retryCount;
    }

    public void start() {
        if (schedule == null) {
            schedule = () -> execute(this);
            scheduleRunner.run(schedule);
        } else {
            throw new IllegalStateException("retry action has been started.");
        }
    }

    public void cancel() {
        remainingTry = 0;
        scheduleRunner.cancel(schedule);
    }

    /**
     * 执行任务。
     *
     * @param remainingTry
     * @return 返回负数表示任务执行成功；返回非负数表示任务失败，如果此时remainTry大于0，则在该时间（毫秒）后自动重试。
     */
    protected abstract long tryAction(int remainingTry);

    /**
     * 达到retryCount次数后仍然未成功时执行此回调。
     */
    protected abstract void onFail();

    /**
     * 指定任务的执行线程。
     *
     * @param runnable 待执行任务。
     */
    protected abstract void execute(Runnable runnable);

    @Override
    public final void run() {
        if (remainingTry-- > 0) {
            long retryDelay = tryAction(remainingTry);
            if (retryDelay >= 0 && remainingTry > 0) {
                scheduleRunner.schedule(schedule, retryDelay);
            }
        } else {
            onFail();
        }
    }
}
