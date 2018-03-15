/**
 * 
 */
package lib.common.model.communication.base;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author yanry
 *
 * 2016年6月30日
 */
public abstract class RequestPack {
	private static final AtomicLong atom = new AtomicLong(System.currentTimeMillis());
	
	private RequestLiveListener l;
	private int appendCount;
	private long identifier;
	
	public RequestPack() {
		identifier = atom.incrementAndGet();
	}

	/**
	 * 
	 * @param tag
	 * @param param The value should be a Boolean, Double, Integer, JSONArray, JSONObject, Long, or String, or the JSONObject.NULL object.
	 * @param hook
	 * @return
	 */
	public RequestPack append(String tag, Object param, RequestDataHook hook) {
		if (appendCount < 0) {
			throw new IllegalStateException("can not append after a request has been sent.");
		}
		appendCount++;
		doAppend(tag, param, hook);
		return this;
	}

	public void send(RequestLiveListener visualizer) {
		if (appendCount > 0) {
			appendCount = -1;
			l = visualizer;
			doSend();
		} else if (visualizer != null) {
			visualizer.onCancel();
		}
	}

	public void cancel() {
		if (l != null) {
			l.onCancel();
		}
		if (appendCount == -1) {
			doCancel();
		}
	}
	
	public long getIdentifier() {
		return identifier;
	}
	
	public RequestLiveListener getRequestLiveListener() {
		return l;
	}
	
	protected abstract void doAppend(String tag, Object param, RequestDataHook hook);
	
	protected abstract void doSend();
	
	protected abstract void doCancel();

}
