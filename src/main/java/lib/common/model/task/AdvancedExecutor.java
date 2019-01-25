/**
 *
 */
package lib.common.model.task;

import lib.common.model.log.Logger;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A thread pool that uses {@link DiscardOldestPolicy} and supports customizing
 * priority and duplicate policy of tasks. Core thread timeout is enable by
 * default.
 *
 * @author yanry
 * <p>
 * 2014年12月9日 上午9:45:18
 */
public class AdvancedExecutor extends ThreadPoolExecutor {

    public AdvancedExecutor(int maxThreadNumber, int keepAliveSecond, boolean isLIFO, DuplicatePolicy duplicatePolicy) {
        super(maxThreadNumber, maxThreadNumber, keepAliveSecond, TimeUnit.SECONDS,
                new TaskQueue(isLIFO, duplicatePolicy), new DiscardOldestPolicy());
        allowCoreThreadTimeOut(true);
    }

    public AdvancedExecutor(int maxThreadNumber, int keepAliveSecond, boolean isLIFO, DuplicatePolicy duplicatePolicy,
                            int queueCapacity) {
        super(maxThreadNumber, maxThreadNumber, keepAliveSecond, TimeUnit.SECONDS,
                new TaskQueue(isLIFO, duplicatePolicy, queueCapacity), new DiscardOldestPolicy());
        allowCoreThreadTimeOut(true);
    }

    /**
     * Strategy to use when trying to put an element into a container and find
     * it already exists.
     *
     * @author yanry
     * <p>
     * 2015年7月20日 下午3:35:19
     */
    public enum DuplicatePolicy {
        DISCARD_NEW, DISCARD_OLD
    }

    private static class TaskQueue extends LinkedBlockingDeque<Runnable> {
        private static final long serialVersionUID = -8968776527901795499L;
        private boolean LIFO;
        private DuplicatePolicy duplicatePolicy;

        TaskQueue(boolean isLIFO, DuplicatePolicy duplicatePolicy) {
            this.LIFO = isLIFO;
            this.duplicatePolicy = duplicatePolicy;
        }

        TaskQueue(boolean isLIFO, DuplicatePolicy duplicatePolicy, int capacity) {
            super(capacity);
            this.LIFO = isLIFO;
            this.duplicatePolicy = duplicatePolicy;
        }

        @Override
        public boolean offer(Runnable e) {
            // this is called in ThreadPoolExecutor.execute();
            if (duplicatePolicy != null) {
                if (contains(e)) {
                    if (duplicatePolicy == DuplicatePolicy.DISCARD_OLD) {
                        remove(e);
                        Logger.getDefault().vv("drop old task: ", e);
                    } else if (duplicatePolicy == DuplicatePolicy.DISCARD_NEW) {
                        Logger.getDefault().vv("drop new task: ", e);
                        // drop the new task by doing nothing and telling the
                        // caller that everything goes well.
                        return true;
                    }
                }
            }
            // enqueue task
            return LIFO ? super.offerFirst(e) : super.offer(e);
        }

        @Override
        public Runnable poll() {
            // this is called in DiscardOldestPolicy.rejectedExecution();
            return LIFO ? super.pollLast() : super.poll();
        }
    }
}
