package yanry.lib.java.model.cache.http;

import yanry.lib.java.model.cache.CacheDataHook;
import yanry.lib.java.model.cache.CacheDataManager;
import yanry.lib.java.model.cancelable.Cancelable;
import yanry.lib.java.model.json.JSONArray;
import yanry.lib.java.model.log.Logger;

/**
 * Created by yanrongyu on 16/9/19.
 */

public abstract class HttpCacheRequest extends Cancelable implements CacheDataHook {
    private long expire;
    private String eTag;
    private boolean hasCache;

    public void start() {
        Logger.getDefault().dd("start: ", getCacheKey());
        if (getCachePolicy() != CachePolicy.NetworkOnly) {
            // get cache
            getCacheDataManager().getData(getCacheKey(), this);
        }
        if (!(getCachePolicy() == CachePolicy.CacheOnly || expire > System.currentTimeMillis()) && onStartRequest(hasCache)) {
            // network request
            if (eTag != null) {
                setRequestProperty("If-None-Match", eTag);
            }
            sendRequest();
        }
        release();
        Logger.getDefault().dd("finish: ", getCacheKey());
    }

    public void handleResponse(Object dataToCache) {
        onDataSuccess(dataToCache, DataSource.SERVER);
        String cacheControl = getResponseHeader("Cache-Control");
        if (cacheControl != null) {
            if (!"no-store".equals(cacheControl) && getCachePolicy() != CachePolicy.CacheOnly) {
                // save cache
                long expire = 0;
                if (cacheControl.startsWith("max-age=")) {
                    expire = Long.parseLong(cacheControl.substring(8)) * 1000 + System.currentTimeMillis();
                }
                String eTag = getResponseHeader("ETag");
                getCacheDataManager().saveCache(getCacheKey(), new JSONArray().put(dataToCache).put(expire).put(eTag), ifRemoveCacheOnLogout());
            }
        }
    }

    @Override
    public void onDataLoaded(boolean inMemory, Object data) {
        // data format: [data, expire_time, ETag]
        JSONArray ja = (JSONArray) data;
        expire = ja.getLong(1);
        if (!ja.isNull(2)) {
            eTag = ja.getString(2);
        }
        onDataSuccess(ja.get(0), inMemory ? DataSource.MEMORY : DataSource.LOCAL);
        hasCache = true;
    }

    @Override
    public void onNoData() {

    }

    @Override
    public Object deserializeData(String localCachedData) {
        return new JSONArray(localCachedData);
    }

    protected abstract String getCacheKey();

    protected abstract CachePolicy getCachePolicy();

    protected abstract boolean onStartRequest(boolean hasCache);

    protected abstract void onDataSuccess(Object data, DataSource source);

    protected abstract CacheDataManager getCacheDataManager();

    protected abstract void setRequestProperty(String key, String value);

    protected abstract void sendRequest();

    protected abstract String getResponseHeader(String key);

    protected abstract boolean ifRemoveCacheOnLogout();
}
