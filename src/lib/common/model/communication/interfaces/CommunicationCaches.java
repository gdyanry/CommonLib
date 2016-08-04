/**
 * 
 */
package lib.common.model.communication.interfaces;

import lib.common.model.communication.CommunicationHandler;

/**
 * Caches used in {@link CommunicationHandler}.
 * 
 * @author yanry
 *
 *         2015年1月28日 下午4:43:43
 */
public interface CommunicationCaches {
	ResponseCache getResponseCache();

	RequesponseToSendCache getRequesponseToSendCache();

	PendingRequestCache getPendingRequestCache();

	ReceivedResponseCache getReceivedResponseCache();
	
	void clear();
}
