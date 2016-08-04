/**
 * 
 */
package lib.common.model.communication;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import lib.common.model.communication.base.RequestDataHook;
import lib.common.model.communication.base.RequestLiveListener;
import lib.common.model.communication.base.RequestPack;
import lib.common.model.communication.entity.MemoryCommunicationCaches;
import lib.common.model.json.JSONArray;
import lib.common.model.json.JSONObject;

/**
 * Data format form server: [[flag, timestamp, {tag: [[random, data], ...],
 * ...}], ...]. The intermediate part "[[flag, timestamp, json], ...]" is
 * processed by {@link CommunicationHandler}.
 * 
 * @author yanry
 * 
 *         2014-6-25 上午8:42:43
 */
public abstract class IntegratedCommunicationClient extends CommunicationHandler {
	private List<FileRequestListener> fileCache;
	private AtomicInteger atom;
	private JSONObject receivedReq;
	private JSONObject receivedRes;
	private Map<Long, TextRequest> textCallbacks;
	
	public IntegratedCommunicationClient() {
		super(new MemoryCommunicationCaches());
		atom = new AtomicInteger();
		textCallbacks = new HashMap<Long, TextRequest>();
		fileCache = new LinkedList<FileRequestListener>();
	}

	private void prepareAndSend(JSONArray json, Set<Object> requestTimestamps) {
		if (isConnectionAvailable()) {
			for (Object timestamp : requestTimestamps) {
				TextRequest tr = textCallbacks.get(timestamp);
				if (tr != null) {
					RequestLiveListener visualizer = tr.getRequestLiveListener();
					if (visualizer != null) {
						visualizer.onStartRequest();
					}
				}
			}
			sendText(new JSONArray().put(getSessionId()).put(json).toString(), requestTimestamps);
		}
	}

	/**
	 * Receive text from server.
	 * 
	 * @param jsonArray
	 *            text format: [[flag, timestamp, {tag, [data, ...], ...}],
	 *            ...].
	 */
	public void processReceivedJson(JSONArray jsonArray) {
		if (jsonArray.length() > 0) {
			synchronized (atom) {
				if (atom.getAndIncrement() == 0) {
					receivedReq = new JSONObject();
					receivedRes = new JSONObject();
				}
			}
			boolean persist = false;
			// use try-finally block to ensure endReceive() is called to release
			// lock, other wise the subsequent requests can't be sent instantly,
			// @see CommunicationHandler.sendRequest().
			try {
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONArray requesponse = jsonArray.getJSONArray(i);
					// process it
					int flag = requesponse.getInt(0);
					receive(requesponse.get(1), requesponse.getJSONObject(2), flag);
					if ((flag
							& CommunicationHandler.FLAG_REQUEST_PERSIST) == CommunicationHandler.FLAG_REQUEST_PERSIST) {
						persist = true;
					}
				}
			} finally {
				Set<Object> requestTimestamps = new HashSet<Object>();
				JSONArray response = endReceive(requestTimestamps);
				if (persist) {
					persistCommunicationCache(((MemoryCommunicationCaches)getCache()).serialize(CommunicationHandler.FLAG_REQUEST_PERSIST));
				}

				synchronized (atom) {
					if (atom.decrementAndGet() == 0) {
						onReceiveBatch(receivedReq, receivedRes);
					}
				}

				if (response != null && response.length() > 0) {
					prepareAndSend(response, requestTimestamps);
				}
			}
		}
	}

	public boolean flushFiles() {
		boolean b = false;
		Iterator<FileRequestListener> it = fileCache.iterator();
		while (it.hasNext() && isConnectionAvailable()) {
			FileRequestListener f = it.next();
			if (f.state == FileRequestListener.STATE_INIT) {
				f.state = FileRequestListener.STATE_UPLOADING;
				if (!f.guaranteed) {
					fileCache.remove(f);
				}
				f.onStartRequest();
				sendFile(f);
				b = true;
			}
		}
		return b;
	}

	public void onConnectionError(Set<Object> requestTimestamps, Object err) {
		for (Object t : requestTimestamps) {
			TextRequest l = textCallbacks.get(t);
			if (l != null) {
				RequestLiveListener visualizer = l.getRequestLiveListener();
				if (visualizer != null) {
					visualizer.onConnectionError(err);
				}
				if (!l.guaranteed) {
					textCallbacks.remove(t);
				}
			}
		}
		reloadPendingRequests(requestTimestamps);
	}

	/**
	 * Get the session id stored somewhere and used as server authentication.
	 * 
	 * @return
	 */
	public abstract String getSessionId();

	/**
	 * A callback on all received requests and responses have been processed.
	 * 
	 * @param requestData
	 * @param responseData
	 */
	protected abstract void onReceiveBatch(JSONObject requestData, JSONObject responseData);

	/**
	 * Actually send text and files.
	 * 
	 * @param text
	 * @param requestTimestamps
	 *            used to invoke {@link #onConnectionError(Set, Object)}.
	 */
	protected abstract void sendText(String text, Set<Object> requestTimestamps);

	/**
	 * Remember to invoke {@link FileRequestListener#onFinish(Object)} or
	 * {@link FileRequestListener#onConnectionError(Object)}!
	 * 
	 * @param file
	 */
	protected abstract void sendFile(FileRequestListener file);

	protected abstract boolean isConnectionAvailable();

	protected abstract Object onRequest(String tag, Object data);

	protected abstract void handleResponseInBackground(String tag, Object resp, long timestamp);

	protected abstract void persistCommunicationCache(JSONArray cacheJa);

	/**
	 * This request is abandoned on no connection or connection error by
	 * default, so {@link TextRequest#retryOnConnected()} or
	 * {@link TextRequest#guaranteed()} needs to be called before
	 * {@link TextRequest#send(RequestLiveListener)}, if necessary.
	 * 
	 * @author yanry
	 *
	 *         2016年1月28日
	 */
	public class TextRequest extends RequestPack {
		private boolean guaranteed;
		private Map<String, Map<Object, RequestDataHook>> listeners;
		private JSONObject jsonData;
		private boolean retryOnConnected;
		private boolean persist;
		private int count;

		public TextRequest() {
			listeners = new HashMap<String, Map<Object, RequestDataHook>>();
			jsonData = new JSONObject();
		}

		/**
		 * If there's no network connection when this request is send, it will
		 * be cached until network is available. This attribute is appended to
		 * guaranteed requests by default.
		 * 
		 * @return
		 */
		public TextRequest retryOnConnected() {
			retryOnConnected = true;
			return this;
		}

		/**
		 * This request will be sent under any circumstance until response is
		 * received. Usually applicable for background requests.
		 * 
		 * @return
		 */
		public TextRequest guaranteed() {
			this.guaranteed = true;
			return this;
		}

		public TextRequest persist() {
			persist = true;
			return this;
		}
		
		@Override
		protected void doCancel() {
			cancelRequest(getIdentifier());
		}

		@Override
		protected void doAppend(String tag, Object data, RequestDataHook hook) {
			int random = count++;
			if (hook != null) {
				Map<Object, RequestDataHook> innerMap = listeners.get(tag);
				if (innerMap == null) {
					innerMap = new HashMap<Object, RequestDataHook>();
					listeners.put(tag, innerMap);
				}
				innerMap.put(random, hook);
			}
			jsonData.append(tag, new JSONArray().put(random).put(data));
		}

		@Override
		protected void doSend() {
			if (jsonData.length() > 0) {
				textCallbacks.put(getIdentifier(), this);
				int flag = CommunicationHandler.FLAG_REQUEST_NORMAL;
				if (retryOnConnected) {
					flag |= CommunicationHandler.FLAG_REQUEST_KEEP_TILL_READY;
				}
				if (guaranteed) {
					flag |= CommunicationHandler.FLAG_REQUEST_GUARANTEED;
				}
				sendRequest(getIdentifier(), jsonData, flag);
				if (persist) {
					persistCommunicationCache(
							((MemoryCommunicationCaches)getCache()).serialize(CommunicationHandler.FLAG_REQUEST_PERSIST));
				}
			}
		}
	}
	
	public abstract class FileRequestListener implements RequestLiveListener {
		private static final int STATE_INIT = 0;
		private static final int STATE_UPLOADING = 1;
		private static final int STATE_FINISH = 2;
		private static final int STATE_CANCEL = 3;
		private int state;
		private boolean guaranteed;

		@Override
		public void onConnectionError(Object e) {
			state = STATE_INIT;
		}

		@Override
		public void onFinish(Object data) {
			state = STATE_FINISH;
			fileCache.remove(this);
		}

		public void cancel() {
			state = STATE_CANCEL;
			fileCache.remove(this);
		}

		/**
		 * 
		 * @param guaranteed
		 *            when true, this request will be kept in memory and be sent
		 *            (on connection error) along with newly created file
		 *            requests until succeed.
		 */
		public void upload(boolean guaranteed) {
			this.guaranteed = guaranteed;
			fileCache.add(0, this);
			if (!isConnectionAvailable()) {
				onNoConnection();
			} else {
				flushFiles();
			}
		}

		public boolean isCanceled() {
			return state == STATE_CANCEL;
		}
	}
	
	@Override
	protected void send(JSONArray json, Set<Object> requestTimestamps, boolean isNew) {
		prepareAndSend(json, requestTimestamps);
	}

	@Override
	protected JSONObject onReceiveRequest(Object requestTag, JSONObject json) {
		// json: {tag: [data, ...], ...}
		JSONObject responseJo = new JSONObject();
		Iterator<?> it = json.keys();
		while (it.hasNext()) {
			JSONArray responseJa = new JSONArray();
			String tag = it.next().toString();
			JSONArray requestJa = json.getJSONArray(tag);
			for (int i = 0; i < requestJa.length(); i++) {
				Object requestData = requestJa.get(i);
				receivedReq.append(tag, requestData);
				Object responseData = onRequest(tag, requestData);
				responseJa.put(responseData);
			}
			responseJo.put(tag, responseJa);
		}
		return responseJo;
	}

	@Override
	protected void onReceiveResponse(Object requestId, JSONObject json) {
		TextRequest tr = textCallbacks.get(requestId);
		Iterator<?> it = json.keys();
		while (it.hasNext()) {
			String tag = it.next().toString();
			JSONArray ja = json.getJSONArray(tag);
			for (int i = 0; i < ja.length(); i++) {
				JSONArray jaData = ja.getJSONArray(i);
				Object data = jaData.get(1);
				receivedRes.append(tag, data);
				handleResponseInBackground(tag, data, (Long) requestId);
				if (tr != null) {
					Map<Object, RequestDataHook> map = tr.listeners.get(tag);
					if (map != null) {
						RequestDataHook listener = map.get(jaData.get(0));
						if (listener != null) {
							listener.onResponse(data);
						}
					}
				}
			}
		}
		if (tr != null) {
			RequestLiveListener visualizer = tr.getRequestLiveListener();
			if (visualizer != null) {
				visualizer.onFinish(json);
			}
			textCallbacks.remove(requestId);
		}
	}

	@Override
	protected boolean isReadyToSend(Object newRequestTimestamp) {
		if (isConnectionAvailable()) {
			return true;
		}
		if (newRequestTimestamp != null) {
			TextRequest tr = textCallbacks.get(newRequestTimestamp);
			if (tr != null) {
				RequestLiveListener visualizer = tr.getRequestLiveListener();
				if (visualizer != null) {
					visualizer.onNoConnection();
				}
				if (!tr.retryOnConnected) {
					// remove from cache
					textCallbacks.remove(newRequestTimestamp);
				}
			}
		}
		return false;
	}

	@Override
	protected void onRejectRequest(Object requestId) {
		textCallbacks.remove(requestId);
	}

}
