package yanry.lib.java.model.communication.interfaces;

import yanry.lib.java.model.communication.CommunicationHandler;

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
