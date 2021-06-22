package yanry.lib.java.model.cache;

/**
 * @author yanry
 *
 *         2015年10月30日
 */
public interface CacheDataHook {
	void onDataLoaded(boolean inMemory, Object data);

	void onNoData();

	/**
	 * 
	 * @param localCachedData
	 * @return return null to trigger {@link #onNoData()} if needed.
	 */
	Object deserializeData(String localCachedData);
}
