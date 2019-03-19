package yanry.lib.java.model;

/**
 * @author yanry
 *
 *         2016年5月23日
 */
public abstract class StateVerifier {
	private int count;
	private boolean verifying;

	public void startVerify() {
		if (!verifying) {
			verifying = true;
			count = 0;
			verify();
			verifying = false;
		}
	}

	private void verify() {
		if (!check()) {
			if (prepare(++count)) {
				if (!isAsync()) {
					verify();
				}
			} else {
				onVerifyFail();
			}
		} else {
			onVerifySuccess();
		}
	}

	/**
	 * Remember to call this when asynchronous preparation ({@link #isAsync()}
	 * returns true) is done.
	 */
	public void finishPrepared() {
		verify();
	}

	protected abstract boolean check();

	/**
	 * 
	 * @param count
	 *            number of times the preparation is being processed.
	 * @return return false to trigger {@link #onVerifyFail()}, other wise
	 *         return true to keep trying verifying.
	 */
	protected abstract boolean prepare(int count);

	protected abstract void onVerifySuccess();

	protected abstract void onVerifyFail();

	/**
	 * if the preparation is asynchronous, {@link #finishPrepared()} should be
	 * call in your callback methods.
	 * 
	 * @return
	 */
	protected abstract boolean isAsync();
}
