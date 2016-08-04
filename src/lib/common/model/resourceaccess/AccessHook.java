/**
 * 
 */
package lib.common.model.resourceaccess;

/**
 * @author yanry
 *
 *         2015年11月14日
 */
public interface AccessHook<R> {

	/**
	 * 
	 * @param cached
	 *            the cached resource value, may be null.
	 * @return whether do generation work.
	 */
	boolean onStartGenerate(R cached);

	/**
	 * 
	 * @param e
	 *            exception captured during resource generation period.
	 */
	void onGenerateException(Exception e);

	/**
	 * 
	 * @param generated
	 *            the generated resource returned by
	 *            {@link CacheResourceAccess#generate(Object, Object, Object, AccessHook)}
	 *            , may be null.
	 * @return whether do caching.
	 */
	boolean onStartCache(R generated);
}
