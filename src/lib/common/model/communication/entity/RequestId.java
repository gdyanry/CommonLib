/**
 * 
 */
package lib.common.model.communication.entity;

/**
 * Server request identifier that contains session id and request timestamp.
 * 
 * @author yanry
 *
 *         2015年1月20日 上午10:20:52
 */
public class RequestId {
	private String sessionId;
	private long timestamp;

	public RequestId(String sessionId) {
		this.sessionId = sessionId;
	}

	public RequestId(String sessionId, long timestamp) {
		this.sessionId = sessionId;
		this.timestamp = timestamp;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sessionId == null) ? 0 : sessionId.hashCode());
		result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof RequestId))
			return false;
		RequestId other = (RequestId) obj;
		if (sessionId == null) {
			if (other.sessionId != null)
				return false;
		} else if (!sessionId.equals(other.sessionId))
			return false;
		if (timestamp != other.timestamp)
			return false;
		return true;
	}

}
