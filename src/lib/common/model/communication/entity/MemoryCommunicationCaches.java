/**
 * 
 */
package lib.common.model.communication.entity;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lib.common.model.communication.CommunicationHandler;
import lib.common.model.communication.interfaces.CommunicationCaches;
import lib.common.model.communication.interfaces.PendingRequestCache;
import lib.common.model.communication.interfaces.ReceivedResponseCache;
import lib.common.model.communication.interfaces.RequesponseToSendCache;
import lib.common.model.communication.interfaces.ResponseCache;
import lib.common.model.json.JSONArray;
import lib.common.model.json.JSONObject;

/**
 * In-memory implementation of {@link CommunicationCaches}.
 * 
 * @author yanry
 *
 *         2015年1月19日 上午9:58:07
 */
public class MemoryCommunicationCaches implements CommunicationCaches {
	private Map<Object, Requesponse> response;
	private List<Requesponse> requesponseToSend;
	private PendingRequestMap pendingRequest;
	private Set<Object> receivedResponse;
	private ResponseCache responseCache;
	private RequesponseToSendCache requestponseToSendCache;
	private PendingRequestCache pendingRequestCache;
	private ReceivedResponseCache receivedResponseCache;

	public MemoryCommunicationCaches() {
		response = new HashMap<Object, Requesponse>();
		requesponseToSend = Collections.synchronizedList(new LinkedList<Requesponse>());
		pendingRequest = new PendingRequestMap();
		receivedResponse = new HashSet<Object>();
		responseCache = new ResponseCache() {

			@Override
			public Requesponse get(Object requestId) {
				return response.get(requestId);
			}

			@Override
			public void put(Requesponse r) {
				response.put(r.getRequestId(), r);
			}
		};
		requestponseToSendCache = new RequesponseToSendCache() {

			@Override
			public void add(Requesponse r) {
				synchronized (requesponseToSend) {
					requesponseToSend.add(r);
				}
			}

			@Override
			public int size() {
				return requesponseToSend.size();
			}

			@Override
			public Requesponse pop() {
				synchronized (requesponseToSend) {
					if (requesponseToSend.size() > 0) {
						return requesponseToSend.remove(0);
					}
				}
				return null;
			}

			@Override
			public boolean remove(Object requestId) {
				synchronized (requesponseToSend) {
					for (int i = 0; i < requesponseToSend.size(); i++) {
						Requesponse r = requesponseToSend.get(i);
						if (r.getRequestId().equals(requestId)) {
							requesponseToSend.remove(i);
							return true;
						}
					}
				}
				return false;
			}
		};
		pendingRequestCache = new PendingRequestCache() {

			@Override
			public void receiveResponse(Object requestId) {
				pendingRequest.receiveResponse(requestId);
			}

			@Override
			public Requesponse remove(Object requestId) {
				return pendingRequest.remove(requestId);
			}

			@Override
			public void put(Requesponse r) {
				pendingRequest.put(r.getRequestId(), r);
			}

			@Override
			public boolean containsValue(Requesponse r) {
				return pendingRequest.containsValue(r);
			}
		};
		receivedResponseCache = new ReceivedResponseCache() {

			@Override
			public boolean add(Object requestId) {
				return receivedResponse.add(requestId);
			}
		};
	}
	
	/**
	 * 
	 * @param requests [[bitFlag, requestId, JSON], ...]
	 */
	public void addRequestsToSend(JSONArray requests) {
		if (requests.length() > 0) {
			synchronized (requesponseToSend) {
				for (int i = 0; i < requests.length(); i++) {
					requesponseToSend.add(new Requesponse(requests.getJSONArray(i)));
				}
			}
		}
	}

	/**
	 * Serialize cache in {@link RequesponseToSendCache} and
	 * {@link PendingRequestCache}.
	 * 
	 * @param flag
	 *            constants defined in {@link CommunicationHandler}
	 * @return
	 */
	public JSONArray serialize(int flag) {
		JSONArray ja = new JSONArray();
		synchronized (requesponseToSend) {
			for (Requesponse r : requesponseToSend) {
				if (r.containsFlag(flag)) {
					ja.put(r.getJa());
				}
			}
		}
		for (Requesponse r : pendingRequest.values()) {
			if (r.containsFlag(flag)) {
				ja.put(r.getJa());
			}
		}
		return ja;
	}

	public JSONArray serializeAll() {
		JSONArray ja = new JSONArray();
		synchronized (requesponseToSend) {
			for (Requesponse r : requesponseToSend) {
				ja.put(r.getJa());
			}
		}
		for (Requesponse r : pendingRequest.values()) {
			ja.put(r.getJa());
		}
		return ja;
	}

	@Override
	public String toString() {
		return String.format(
				"requests & responses to send: %s%npending requests: %s%nresponses: %s%nreceived responses: %s",
				Arrays.toString(requesponseToSend.toArray()), pendingRequest, response,
				Arrays.toString(receivedResponse.toArray()));
	}

	@Override
	public ResponseCache getResponseCache() {
		return responseCache;
	}

	@Override
	public RequesponseToSendCache getRequesponseToSendCache() {
		return requestponseToSendCache;
	}

	@Override
	public PendingRequestCache getPendingRequestCache() {
		return pendingRequestCache;
	}

	@Override
	public ReceivedResponseCache getReceivedResponseCache() {
		return receivedResponseCache;
	}
	
	@Override
	public void clear() {
		response.clear();
		requesponseToSend.clear();
		pendingRequest.clear();
		receivedResponse.clear();
	}

	private class PendingRequestMap extends LinkedHashMap<Object, Requesponse> {
		private static final long serialVersionUID = -6204913891310797069L;
		private Object id;
		private Requesponse emptyRequesponse = new Requesponse(-1, JSONObject.NULL, null);

		private void receiveResponse(Object id) {
			// when the map is empty, put() will not trigger
			// removeEldestEntry()!
			if (!isEmpty()) {
				this.id = id;
				// removeEldestEntry() comes before put action!
				put(emptyRequesponse.getRequestId(), emptyRequesponse);
				remove(emptyRequesponse.getRequestId());
			}
		}

		@Override
		protected boolean removeEldestEntry(Entry<Object, Requesponse> eldest) {
			if (id != null) {
				// the request correspond to the received response is not in
				// pending requests, when it's a one-night request, so here we
				// need to check.
				if (containsKey(id)) {
					remove(eldest.getKey());
					if (!id.equals(eldest.getKey()) && !id.equals(emptyRequesponse.getRequestId())) {
						// not equal means this eldest request fail to receive
						// response for some reason, so we transmit it to send
						// queue.
						getRequesponseToSendCache().add(eldest.getValue());
						// and recursively push.
						// must remove here, a little weird, given that
						// "removeEldestEntry() comes before put action".
						remove(emptyRequesponse.getRequestId());
						put(emptyRequesponse.getRequestId(), emptyRequesponse);
					}
				}
				// recover.
				id = null;
			}
			return false;
		}
	}

}
