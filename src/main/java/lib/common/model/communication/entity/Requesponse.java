/**
 * 
 */
package lib.common.model.communication.entity;

import lib.common.model.json.JSONArray;
import lib.common.model.json.JSONObject;

/**
 * Entity class Representing a request or a response.
 * 
 * @author yanry
 *
 *         2015年1月16日 下午4:33:35
 */
public class Requesponse {
	private int bitFlag;
	private Object requestId;
	private JSONArray ja;
	private String value;

	public Requesponse(JSONArray ja) {
		this.bitFlag = ja.getInt(0);
		this.requestId = ja.get(1);
		this.ja = ja;
	}

	public Requesponse(String s) {
		this(new JSONArray(s));
	}

	public Requesponse(int bitFlags, Object requestId, JSONObject json) {
		this.bitFlag = bitFlags;
		this.requestId = requestId;
		if (json != null) {
			value = json.toString();
		}
		ja = new JSONArray().put(bitFlags).put(requestId).put(json);
	}

	public int getBitFlag() {
		return bitFlag;
	}

	public Object getRequestId() {
		return requestId;
	}

	public JSONArray getJa() {
		return ja;
	}
	
	public boolean containsFlag(int flag) {
		return (bitFlag & flag) == flag;
	}

	@Override
	public String toString() {
		return ja.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Requesponse other = (Requesponse) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (other.value == null) {
			return false;
		} else if (!value.toString().equals(other.value.toString())) {
			return false;
		}
		return true;
	}
}
