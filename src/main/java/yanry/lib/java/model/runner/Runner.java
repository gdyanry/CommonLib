package yanry.lib.java.model.runner;

public interface Runner {
    void run(Runnable runnable);

    void scheduleTimeout(Runnable runnable, long delay);

    void cancelPendingTimeout(Runnable runnable);
}
