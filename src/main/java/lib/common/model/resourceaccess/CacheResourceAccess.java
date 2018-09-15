/**
 *
 */
package lib.common.model.resourceaccess;

import lib.common.model.log.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * This class defines a way of resource access: generate the target resource
 * (which is strictly related to a specify key) and save it to cache if it
 * doesn't exist in cache at first, other wise access that resource from cache
 * directly.
 *
 * @param <K> type of key used to access resource.
 * @param <R> type of target resource.
 * @param <O> type of resource access option.
 * @param <H> type of resource access hook.
 * @author yanry
 * <p>
 * 2015年11月14日
 */
public abstract class CacheResourceAccess<K, R, O, H extends AccessHook<R>> {
    private List<TaskEntity> taskCache;
    private int cacheLimit;

    /**
     * This method is thread-safe, accessing resource with the same key will be
     * processed in FIFO order.
     *
     * @param key
     * @param option
     * @param hook
     */
    public void get(final K key, final O option, final H hook) {
        final R cached = getCacheValue(key, option);
        if (hook.onStartGenerate(cached)) {
            Executor exe = getGenerationExecutor();
            if (exe == null) {
                generate(key, option, hook, cached);
            } else {
                exe.execute(new Runnable() {
                    @Override
                    public void run() {
                        generate(key, option, hook, cached);
                    }
                });
            }
        }
    }

    private void generate(K key, O option, H hook, R cached) {
        try {
            R generated = generate(key, cached, option, hook);
            if (hook.onStartCache(generated)) {
                cache(key, option, generated);
            }
        } catch (Exception e) {
            hook.onGenerateException(e);
        }
    }

    /**
     * @param cacheCapacity max number of task can be held in cache, excessive tasks will
     *                      cause the oldest tasks to be dropped.
     */
    public void enableCacheTask(int cacheCapacity) {
        if (cacheCapacity > 0) {
            taskCache = new ArrayList<TaskEntity>(cacheCapacity);
            cacheLimit = cacheCapacity;
        }
    }

    /**
     * Put an access task into cache instead of execute it right away. This
     * function is disable by default.
     *
     * @param key
     * @param option
     * @param hook
     */
    public void cacheTask(K key, O option, H hook) {
        if (cacheLimit > 0) {
            while (cacheLimit > 0 && taskCache.size() >= cacheLimit) {
                taskCache.remove(0);
            }
            taskCache.add(new TaskEntity(key, option, hook));
        } else {
            Logger.getDefault().e("cache task is disable.");
        }
    }

    /**
     * Flush and execute the cached access tasks.
     */
    public void flushTaskCache() {
        if (taskCache != null) {
            for (TaskEntity task : taskCache) {
                get(task.key, task.option, task.hook);
            }
        }
    }

    protected abstract R getCacheValue(K key, O option);

    /**
     * @return the executor to run generation task. Return null to use
     * single-thread mode.
     */
    protected abstract Executor getGenerationExecutor();

    /**
     * @param key
     * @param cached cache value returned by {@link #getCacheValue(Object, Object)}
     * @param option
     * @param hook
     * @return the generated resource.
     * @throws Exception
     */
    protected abstract R generate(K key, R cached, O option, H hook) throws Exception;

    /**
     * @param key
     * @param generated generated value returned by
     *                  {@link #generate(Object, Object, Object, AccessHook)}.
     */
    protected abstract void cache(K key, O option, R generated);

    private class TaskEntity {
        private K key;
        private O option;
        private H hook;

        TaskEntity(K key, O option, H hook) {
            this.key = key;
            this.option = option;
            this.hook = hook;
        }
    }
}
