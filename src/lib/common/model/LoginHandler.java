/**
 * 
 */
package lib.common.model;

import lib.common.util.ConsoleUtil;

/**
 * A model of login procedure in client application.
 * 
 * @author yanry
 * 
 */
public abstract class LoginHandler {
	private static final String ACCOUNT = "login.component.account";
	private static final String SESSION_ID = "login.component.session.id";
	private static final String USER_ID = "login.component.user.id";

	private boolean initialized;

	/**
	 * Invoke this method when application starts up. This will do
	 * initialization if client is login.
	 */
	public void startUp() {
		if (containsKey(SESSION_ID)) {
			if (!initialized) {
				String uid = getValue(USER_ID);
				if (uid != null) {
					init(uid);
					initialized = true;
				}
			}
		}
	}

	/**
	 * Invoke this method when login button is clicked.
	 * 
	 * @param account
	 *            the account to save which can be retrieved from
	 *            {@link #getAccount()}
	 */
	public void saveAccount(String account) {
		save(ACCOUNT, account);
	}

	/**
	 * Invoke this method when client receives success login response.
	 * 
	 * @param sessionId
	 *            session id from server response.
	 * @param uid
	 *            user id.
	 */
	public void updateSession(String sessionId, Object uid) {
		save(SESSION_ID, sessionId);
		save(USER_ID, uid.toString());
		init(uid.toString());
		initialized = true;
	}

	public void logout() {
		ConsoleUtil.debug("logout: " + getValue(SESSION_ID));
		removeEntry(SESSION_ID);
		removeEntry(USER_ID);
		initialized = false;
	}

	public boolean isLogined() {
		return containsKey(SESSION_ID);
	}

	public String getSessionId() {
		return getValue(SESSION_ID);
	}

	public String getAccount() {
		return getValue(ACCOUNT);
	}

	public String getUid() {
		return getValue(USER_ID);
	}

	/**
	 * Now that the client has been login, so do some initialization work here,
	 * such as initialize database, start background service, etc.
	 * 
	 * @param uid
	 *            unique identifier of current user.
	 */
	protected abstract void init(String uid);

	protected abstract boolean containsKey(String key);

	protected abstract String getValue(String key);

	protected abstract void save(String key, String value);

	protected abstract void removeEntry(String key);
}
