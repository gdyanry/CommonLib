package yanry.lib.java.model.schedule;

/**
 * Created by yanry on 2019/12/27.
 */
public interface SchedulerWatcher {
    void onSchedulerStateChange(Scheduler scheduler, boolean visible);
}
