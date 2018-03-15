/**
 * 
 */
package lib.common.model.communication.base;

/**
 * @author yanry
 *
 *         2015年9月29日
 */
public interface RequestLiveListener {

	/**
	 * Only triggered on new requests.
	 */
	void onNoConnection();

	/**
	 * Could be triggered multiple times for guaranteed requests.
	 */
	void onStartRequest();

	/**
	 * Could be triggered multiple times for guaranteed requests.
	 * 
	 * @param e
	 *            the error object.
	 */
	void onConnectionError(Object e);

	/**
	 * Triggered only once or never.
	 */
	void onFinish(Object data);
	
	/**
	 * Triggered only once or never.
	 */
	void onCancel();
}
