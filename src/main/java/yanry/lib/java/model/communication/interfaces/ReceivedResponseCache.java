package yanry.lib.java.model.communication.interfaces;

/**
 * A set structure. 所有收到的响应的标识的缓存，当多次收到同一响应时，不需要派发该响应。
 * 
 * @author yanry
 * 
 *         2015年1月16日 下午3:59:29
 */
public interface ReceivedResponseCache {

	/**
	 * If this cache already contains the element, leave the cache unchanged and
	 * returns false.
	 * 
	 * @param requestId
	 * @return true if this set did not already contain the specified element
	 */
	boolean add(Object requestId);
}
