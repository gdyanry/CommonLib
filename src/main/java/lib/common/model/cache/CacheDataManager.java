/**
 *
 */
package lib.common.model.cache;

import java.lang.ref.SoftReference;

/**
 * Two level data cache.
 *
 * @author yanry
 * <p>
 * 2015年10月28日
 */
public abstract class CacheDataManager {

    protected ResizableLruCache<String, SoftReference<Object>> memCache;

    /**
     * @param memCacheSize max number of data in memory LRU cache.
     */
    public CacheDataManager(int memCacheSize) {
        memCache = new ResizableLruCache<>(memCacheSize);
    }

    public void getData(String key, CacheDataHook hook) {
        if (ifBypassCache()) {
            hook.onNoData();
            return;
        }
        Object cachedData = null;
        SoftReference<Object> reference = memCache.get(key);
        if (reference == null || (cachedData = reference.get()) == null) {
            // get from local.
            String localCache = getLocalCache(key);
            if (localCache != null && localCache.length() > 0) {
                cachedData = hook.deserializeData(localCache);
                if (cachedData != null) {
                    memCache.put(key, new SoftReference<>(cachedData));
                    hook.onDataLoaded(false, cachedData);
                    return;
                }
            }
        }
        if (cachedData == null) {
            hook.onNoData();
        } else {
            hook.onDataLoaded(true, cachedData);
        }
    }

    /**
     * 网络请求得到数据时调用此方法。
     */
    public void saveCache(String key, Object data, boolean removeOnLogout) {
        if (!ifBypassCache()) {
            memCache.put(key, new SoftReference<>(data));
            saveToLocalCache(key, data, removeOnLogout);
        }
    }

    public void removeCache(String key) {
        SoftReference<Object> ref = memCache.remove(key);
        if (ref != null) {
            ref.clear();
        }
        removeLocalCache(key);
    }

    protected abstract String getLocalCache(String key);

    protected abstract void removeLocalCache(String key);

    protected abstract void saveToLocalCache(String key, Object data, boolean removeOnLogout);

    protected abstract boolean ifBypassCache();

}
