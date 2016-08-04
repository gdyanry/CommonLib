/**
 * 
 */
package lib.common.model.communication;

import java.util.HashSet;
import java.util.Set;

import lib.common.model.communication.entity.Requesponse;
import lib.common.model.communication.interfaces.CommunicationCaches;
import lib.common.model.communication.interfaces.PendingRequestCache;
import lib.common.model.communication.interfaces.ReceivedResponseCache;
import lib.common.model.communication.interfaces.RequesponseToSendCache;
import lib.common.model.communication.interfaces.ResponseCache;
import lib.common.model.json.JSONArray;
import lib.common.model.json.JSONObject;
import lib.common.util.ConsoleUtil;

/**
 * 
 * Data format: [[flag, requestId, json], ...]. This class is focus on
 * manipulating caches, including {@link PendingRequestCache},
 * {@link ReceivedResponseCache}, {@link RequesponseToSendCache} and
 * {@link ResponseCache}. One {@link CommunicationHandler} object is dedicated
 * for a specific communication peer, to which the caches are related.
 * 
 * @author yanry
 *
 *         2015年1月16日 下午1:07:53
 */
public abstract class CommunicationHandler {
	public static final int FLAG_RESPONSE = 0;
	/**
	 * 只发送一次的请求，就算失败，也不需要自动重新发送
	 */
	public static final int FLAG_REQUEST_NORMAL = 1;
	public static final int FLAG_REQUEST_KEEP_TILL_READY = 2;
	public static final int FLAG_REQUEST_GUARANTEED = 4;
	public static final int FLAG_REQUEST_PERSIST = 8;

	private boolean locked;
	private Object extra;
	private CommunicationCaches cache;

	public CommunicationHandler(CommunicationCaches cache) {
		this.cache = cache;
	}

	/**
	 * Manually send the cached requests.
	 * 
	 * @return whether this try makes difference.
	 */
	public boolean flushRequests() {
		if (cache.getRequesponseToSendCache().size() > 0 && isReadyToSend(null)) {
			sendRequest(false);
			return true;
		}
		return false;
	}

	public void sendRequest(Object requestId, JSONObject json, int flag) {
		Requesponse r = new Requesponse(FLAG_REQUEST_NORMAL | flag, requestId, json);
		cache.getRequesponseToSendCache().add(r);
		if (isReadyToSend(requestId)) {
			if (!locked) {
				sendRequest(true);
			}
		} else {
			if (flag == FLAG_REQUEST_NORMAL) {
				cache.getRequesponseToSendCache().remove(requestId);
			}
		}
	}

	private void sendRequest(boolean isNewRequest) {
		Set<Object> requestIds = new HashSet<Object>();
		JSONArray requesponses = getRequesponses(requestIds);
		if (requesponses.length() > 0) {
			send(requesponses, requestIds, isNewRequest);
		}
	}

	/**
	 * 
	 * @param requestId
	 * @param json
	 * @param flag
	 * @return 是否第一次收到该请求或响应
	 */
	public boolean receive(Object requestId, JSONObject json, int flag) {
		locked = true;
		if (flag == 0) {
			// 把该响应对应的需要自动重新发送的请求从缓存中移除
			cache.getPendingRequestCache().receiveResponse(requestId);
			boolean newResp = cache.getReceivedResponseCache().add(requestId);
			// 当多次发送同一请求时有可能多次收到同一个响应，此时不做任何处理
			if (newResp) {
				if (json != null) {
					// 派发该事件
					onReceiveResponse(requestId, json);
				}
				return true;
			}
			return false;

		} else {
			// 收到请求
			boolean noCache = false;
			Requesponse r = cache.getResponseCache().get(requestId);
			if (r == null) {
				JSONObject jo = onReceiveRequest(requestId, json);
				r = new Requesponse(0, requestId, jo);
				// 对于只发一次的请求，不需要缓存该请求的响应
				if (flag > FLAG_REQUEST_GUARANTEED) {
					cache.getResponseCache().put(r);
				}
				noCache = true;
			}
			cache.getRequesponseToSendCache().add(r);
			return noCache;
		}
	}

	/**
	 * 因为每次接收到的数据可能包含多个请求或（和）响应，在调用完receive()之后要调用该方法。
	 * 
	 * @param requestIds
	 *            用来装载之前发送失败而且需要顺带发送的请求id。
	 * 
	 * @return requests or responses to respond.
	 */
	public JSONArray endReceive(Set<Object> requestIds) {
		locked = false;
		return getRequesponses(requestIds);
	}

	/**
	 * 取消尚未发送的请求，对于已发送但未收到响应的请求则不往上层分派该请求的响应（如果将来收到的话）。
	 * 
	 * @param requestId
	 * @return 如果已收到过响应则返回false，否则返回true。
	 */
	public boolean cancelRequest(Object requestId) {
		boolean supress = receive(requestId, null, 0);
		// shut the lock
		locked = false;
		return supress || cache.getRequesponseToSendCache().remove(requestId);
	}

	/**
	 * Transmit the specific pending requests to send queue when they fail.
	 * 
	 * @param requestIds
	 */
	public void reloadPendingRequests(Set<Object> requestIds) {
		for (Object id : requestIds) {
			// this request won't be found in pending requests when it's a one
			// night request.
			Requesponse r = cache.getPendingRequestCache().remove(id);
			if (r != null) {
				cache.getRequesponseToSendCache().add(r);
			}
		}
	}

	private JSONArray getRequesponses(Set<Object> requestIds) {
		JSONArray requesponses = new JSONArray();
		Requesponse re = null;
		while ((re = cache.getRequesponseToSendCache().pop()) != null) {
			// reject requests with different id but same value.
			if (cache.getPendingRequestCache().containsValue(re)) {
				ConsoleUtil.error(getClass(), "reject request: " + re);
				onRejectRequest(re.getRequestId());
			} else {
				requesponses.put(re.getJa());
				if ((re.getBitFlag() & FLAG_REQUEST_NORMAL) == FLAG_REQUEST_NORMAL) {
					// collect request id
					if (requestIds != null) {
						requestIds.add(re.getRequestId());
					}
					// put guaranteed and persist requests to pending request cache, so they can be got back on failure.
					if (re.getBitFlag() > FLAG_REQUEST_GUARANTEED) {
						cache.getPendingRequestCache().put(re);
					}
				}
			}
		}
		return requesponses;
	}
	
	public CommunicationCaches getCache() {
		return cache;
	}

	public Object getExtra() {
		return extra;
	}

	public void setExtra(Object extra) {
		this.extra = extra;
	}

	/**
	 * Any error occurs, remember to invoke {@link #reloadPendingRequests(Set)},
	 * other wise the none-one-night requests won't be send again automatically.
	 * Note that all the non-one-night requests have been settled in the pending
	 * requests queue by now.
	 * 
	 * @param json
	 * @param requestIds
	 * @param isNew
	 */
	protected abstract void send(JSONArray json, Set<Object> requestIds, boolean isNew);

	/**
	 * 
	 * @param requestId
	 * @param json
	 * @return response.
	 */
	protected abstract JSONObject onReceiveRequest(Object requestId, JSONObject json);

	protected abstract void onReceiveResponse(Object requestId, JSONObject json);

	protected abstract boolean isReadyToSend(Object newRequestId);

	protected abstract void onRejectRequest(Object requestId);

}
