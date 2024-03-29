package yanry.lib.java.model.runner;

import yanry.lib.java.model.log.Logger;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用一个Runner监控另一个Runner是否阻塞。
 */
public abstract class RunnerBlockMonitor implements Runnable {
    private static final int STATE_MONITOR_CHECK = 0;
    private static final int STATE_MONITOR_REQ = 1;
    private static final int STATE_MONITOR_ACK = 2;

    private Runner monitoringRunner;
    private Runner monitoredRunner;

    private long ackTimeout;
    private long checkPeriod;
    private MonitorState state = new MonitorState();

    /**
     * @param monitoringRunner 执行监控的Runner
     */
    public RunnerBlockMonitor(Runner monitoringRunner) {
        this.monitoringRunner = monitoringRunner;
    }

    public void setMonitoredRunner() {
        if (state.compareAndSet(STATE_MONITOR_REQ, STATE_MONITOR_CHECK)) {
            this.monitoredRunner.cancel(state);
        }
    }

    /**
     * 开始监控。
     *
     * @param monitoredRunner 被监控Runner
     * @param ackTimeout      判断是否阻塞的超时时间
     * @param checkPeriod     检测时间间隔
     * @param delay           开始监控的延迟时间
     */
    public void start(Runner monitoredRunner, long ackTimeout, long checkPeriod, long delay) {
        this.ackTimeout = ackTimeout;
        this.checkPeriod = checkPeriod;
        if (state.compareAndSet(STATE_MONITOR_REQ, STATE_MONITOR_CHECK)) {
            this.monitoredRunner.cancel(state);
        }
        this.monitoredRunner = monitoredRunner;
        monitoringRunner.schedule(this, delay);
    }

    /**
     * 停止监控。
     */
    public void stop() {
        monitoringRunner.cancel(this);
        if (state.compareAndSet(STATE_MONITOR_REQ, STATE_MONITOR_CHECK)) {
            monitoredRunner.cancel(state);
            monitoredRunner = null;
        }
    }

    protected abstract void onAckTimeout(Runner runner);

    @Override
    public void run() {
        if (state.compareAndSet(STATE_MONITOR_CHECK, STATE_MONITOR_REQ)) {
            monitoredRunner.schedule(state, 0);
            monitoringRunner.schedule(this, ackTimeout);
        } else if (state.compareAndSet(STATE_MONITOR_ACK, STATE_MONITOR_CHECK)) {
            monitoringRunner.schedule(this, checkPeriod);
        } else {
            Logger.getDefault().ww(monitoredRunner, " ack timeout.");
            onAckTimeout(monitoredRunner);
        }
    }

    private class MonitorState extends AtomicInteger implements Runnable {
        @Override
        public void run() {
            state.compareAndSet(STATE_MONITOR_REQ, STATE_MONITOR_ACK);
        }
    }
}
