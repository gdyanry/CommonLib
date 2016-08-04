/**
 * 
 */
package lib.common.model.cache;

/**
 * Two level data cache.
 * 
 * @author yanry
 *
 *         2015年10月28日
 */
public abstract class CacheDataManager {

	protected ResizableLruCache<String, Object> memCache;

	/**
	 * 
	 * @param memCacheSize max number of data in memory LRU cache.
	 */
	public CacheDataManager(int memCacheSize) {
		memCache = new ResizableLruCache<String, Object>(memCacheSize);
	}

	public void getData(String key, CacheDataHook hook) {
		if (ifBypassCache()) {
			hook.onNoData();
			return;
		}
		Object cachedData = memCache.get(key);
		if (cachedData != null) {
			hook.onDataLoaded(true, cachedData);
		} else {
			// get from local.
			String localCache = getLocalCache(key);
			if (localCache != null && localCache.length() > 0) {
				cachedData = hook.deserializeData(localCache);
				if (cachedData != null) {
					memCache.put(key, cachedData);
					hook.onDataLoaded(false, cachedData);
					return;
				}
			}
			hook.onNoData();
		}
	}

	/**
	 * 网络请求得到数据时调用此方法。
	 * 
	 */
	public void saveCache(String key, Object data, boolean removeOnLogout) {
		if (!ifBypassCache()) {
			memCache.put(key, data);
			saveToLocalCache(key, data, removeOnLogout);
		}
	}
	
	public void removeCache(String key) {
		memCache.remove(key);
		removeLocalCache(key);
	}

	protected abstract String getLocalCache(String key);
	
	protected abstract void removeLocalCache(String key);

	protected abstract void saveToLocalCache(String key, Object data, boolean removeOnLogout);

	protected abstract boolean ifBypassCache();

}
