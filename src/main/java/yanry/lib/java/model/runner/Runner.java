package yanry.lib.java.model.runner;

public interface Runner {
    void run(Runnable runnable);

    void schedule(Runnable runnable, long delay);

    void cancel(Runnable runnable);

    void terminate();
}
