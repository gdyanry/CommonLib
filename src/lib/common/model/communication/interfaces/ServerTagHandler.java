/**
 * 
 */
package lib.common.model.communication.interfaces;

import lib.common.model.communication.IntegratedCommunicationServer;
import lib.common.model.communication.entity.RequestId;

/**
 * A server-side business interface handler.
 * 
 * @param <U>
 *            see parameter type of {@link IntegratedCommunicationServer}.
 * 
 * @author yanry
 *
 *         2011年2月14日 上午2:07:43
 */
public interface ServerTagHandler<U> {
	Object onAnonymousRequest(Object timestamp, String tag, Object data, Object extra);

	/**
	 * 
	 * @param tag
	 * @param requestId
	 *            basically used to send forward.
	 * @param uid
	 * @param data
	 * @return
	 */
	Object onUserRequest(String tag, RequestId requestId, U uid, Object data);

	void onUserResponse(String tag, U uid, Object data);
}
