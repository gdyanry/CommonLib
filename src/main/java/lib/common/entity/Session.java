/**
 * 
 */
package lib.common.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author yanry
 *
 *         2015年10月4日
 */
public class Session {
	private static Map<Integer, Session> uidMap = new HashMap<Integer, Session>();
	private static Map<String, Session> sidMap = new HashMap<String, Session>();

	private int userId;
	private String sessionId;

	private Session(int userId) {
		this.userId = userId;
		sessionId = UUID.randomUUID().toString();
		Session oldSession = uidMap.get(userId);
		if (oldSession != null) {
			oldSession.destroy();
		}
		uidMap.put(userId, this);
		sidMap.put(sessionId, this);
	}

	private void destroy() {
		uidMap.remove(userId);
		sidMap.remove(sessionId);
	}

	public int getUserId() {
		return userId;
	}

	public String getSessionId() {
		return sessionId;
	}

	public static Session createNew(int userId) {
		return new Session(userId);
	}

	public static Session getBySessionId(String sessionId) {
		return sidMap.get(sessionId);
	}
}
