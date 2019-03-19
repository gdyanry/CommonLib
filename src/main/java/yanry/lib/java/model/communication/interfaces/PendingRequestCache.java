package yanry.lib.java.model.communication.interfaces;

import yanry.lib.java.model.communication.entity.Requesponse;

/**
 * An order-map structure. Requests that have not yet received responses or send
 * errors, in which case need to be send again automatically.
 * 
 * 
 * @author yanry
 *
 *         2015年1月16日 下午3:57:33
 */
public interface PendingRequestCache {

	/**
	 * Remove this request id and transmit the older pending requests(if exist)
	 * to {@link RequesponseToSendCache}.
	 * 
	 * @param requestId
	 */
	void receiveResponse(Object requestId);

	/**
	 * 
	 * @param requestId
	 * @return the previous value associated with key, or null if there was no
	 *         mapping for key.
	 */
	Requesponse remove(Object requestId);

	void put(Requesponse r);

	boolean containsValue(Requesponse r);
}
