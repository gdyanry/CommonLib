package yanry.lib.java.model.schedule;

public interface ScheduleRunner {
    void run(Runnable runnable);

    void scheduleTimeout(Runnable runnable, long delay);

    void cancelPendingTimeout(Runnable runnable);
}
