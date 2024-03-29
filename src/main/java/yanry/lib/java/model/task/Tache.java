package yanry.lib.java.model.task;

import yanry.lib.java.model.log.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A light-weight producer-consumer pattern model which is originally designed
 * as a tache in an assembly line. Inputs are processed in FIFO order.
 *
 * @param <I> type of inputs.
 * @author yanry
 * <p>
 * 2015年7月17日 下午4:55:40
 */
public abstract class Tache<I> implements Runnable {

    private BlockingQueue<I> q;
    private boolean exit;
    private I exitSignal;

    /**
     * @param exitSignal an abnormal object that serves as a signal to notify the
     *                   looper to quit.
     * @param capacity   max number of input objects that can be held in this
     *                   container. Threads trying to enter excessive inputs will be
     *                   blocked until there's available space. 0 means no limitation.
     */
    public Tache(I exitSignal, int capacity) {
        this.exitSignal = exitSignal;
        q = capacity > 0 ? new LinkedBlockingQueue<I>(capacity) : new LinkedBlockingQueue<I>();
    }

    @Override
    public final void run() {
        Logger.getDefault().dd("start running.");
        while (!exit) {
            try {
                I in = q.take();
                if (in.equals(exitSignal)) {
                    break;
                }
                process(in);
            } catch (InterruptedException e) {
                Logger.getDefault().catches(e);
            }
        }
        Logger.getDefault().dd("exit running.");
    }

    /**
     * Put an input object to this tache.
     *
     * @param in
     * @return return false if {@link #exit(boolean)}  has been invoked, otherwise
     * return true.
     * @throws InterruptedException
     */
    public boolean enter(I in) throws InterruptedException {
        if (!exit) {
            q.put(in);
            return true;
        }
        return false;
    }

    /**
     * Stop the engine.
     *
     * @param immediately whether stop works at hand immediately or wait for the works
     *                    in queue to finish.
     * @return return false if {@link #exit(boolean)} has been invoked, otherwise
     * return true.
     * @throws InterruptedException
     */
    public boolean exit(boolean immediately) throws InterruptedException {
        if (!exit) {
            exit = true;
            if (immediately) {
                q.clear();
            }
            q.put(exitSignal);
            return true;
        }
        return false;
    }

    protected abstract void process(I in);

}
