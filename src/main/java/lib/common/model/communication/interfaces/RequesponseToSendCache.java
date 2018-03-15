/**
 * 
 */
package lib.common.model.communication.interfaces;

import lib.common.model.communication.entity.Requesponse;

/**
 * A queue structure. Requests and responses to be send, make sure operations on
 * it is thread-safe.
 * 
 * @author yanry
 *
 *         2015年1月16日 下午3:54:21
 */
public interface RequesponseToSendCache {

	/**
	 * Appends the specified element to the end of this cache.
	 * 
	 * @param r
	 */
	void add(Requesponse r);

	int size();

	/**
	 * Get and remove an element from the head of this cache.
	 * 
	 * @return null if there's no element.
	 */
	Requesponse pop();

	boolean remove(Object requestId);
}
