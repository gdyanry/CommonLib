/**
 * 
 */
package lib.common.entity;

/**
 * @author yanry
 *
 *         2016年7月12日
 */
public interface StreamTransferHook {

	/**
	 * 
	 * @return return true to stop transferring stream.
	 */
	boolean isStop();

	/**
	 * 
	 * @return the minimum interval {@link #onUpdate(long)} is triggered in millisecond.
	 */
	int getUpdateInterval();

	/**
	 * 
	 * @param transferedBytes
	 *            the transferred bytes, not necessarily equals to current
	 *            position if start position is not 0.
	 */
	void onUpdate(long transferedBytes);

	/**
	 * 
	 * @param isStopped
	 *            whether this transfer procedure is stopped by returning true
	 *            in {@link #isStop()}.
	 */
	void onFinish(boolean isStopped);

	/**
	 * 
	 * @return you can return 0 to use default size
	 */
	int getBufferSize();
}
