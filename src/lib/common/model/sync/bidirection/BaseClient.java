/**
 * 
 */
package lib.common.model.sync.bidirection;

import lib.common.model.communication.IntegratedCommunicationClient;
import lib.common.model.communication.IntegratedCommunicationClient.TextRequest;
import lib.common.model.communication.base.RequestDataHook;
import lib.common.model.json.JSONArray;
import lib.common.util.StringUtil;

/**
 * Synchronize client that supports add and delete.
 * @param <F> same with {@link IntegratedCommunicationClient}.
 * 
 * @author yanry
 *
 *         2015年7月4日 下午1:23:16
 */
public abstract class BaseClient<F> {
	private RequestDataHook addCheckHook;
	private RequestDataHook deleteHook;
	private RequestDataHook addHook;
	private RequestDataHook deleteCheckHook;
	
	public BaseClient() {
		addHook = new RequestDataHook() {
			
			@Override
			public void onResponse(Object responseData) {
				// ADD: [local_id, server_id] #[]
				JSONArray ja = (JSONArray) responseData;
				if (ja.length() > 0) {
					updateServerId(ja);
					onServerResult(tagAdd(), true);
				} else {
					onServerResult(tagAdd(), false);
				}
			}
		};
		deleteHook = new RequestDataHook() {
			
			@Override
			public void onResponse(Object responseData) {
				// DELETE: if_success
				onServerResult(tagDelete(), (Boolean) responseData);
			}
		};
		deleteCheckHook = new RequestDataHook() {
			
			@Override
			public void onResponse(Object responseData) {
				// DELETE_CHECK: [delete_time, [server_id, ...]]
				JSONArray ja = (JSONArray) responseData;
				pullDelete(ja.getJSONArray(1));
				saveLatestDeleteTime(ja.getLong(0));
			}
		};
		addCheckHook = new RequestDataHook() {
			
			@Override
			public void onResponse(Object responseData) {
				// ADD_CHECK: [server_id, ...]
				pullAdd((JSONArray) responseData);
			}
		};
	}

	/**
	 * Ready means entering main user interface for the first time, typically
	 * from login procedure or application startup. C-S add and delete will
	 * proceed on ready.
	 */
	public void onReady(TextRequest builder) {
		for (JSONArray record : getPushAdd()) {
			builder.append(tagAdd(), record, addHook);
		}
		for (Object serverId : getPushDelete()) {
			builder.append(tagDelete(), serverId, deleteHook);
		}
		builder.append(tagDeleteCheck(), getLatestDeleteTime(), deleteCheckHook);
	}
	
	/**
	 * S-C add and delete will proceed on login.
	 */
	public void onLogin(TextRequest builder) {
		builder.append(tagAddCheck(), getMaxIncreasingField(), addCheckHook).append(tagDeleteCheck(), getLatestDeleteTime(), deleteCheckHook);
	}
	
	public void onAddCheckRequest(TextRequest builder) {
		builder.append(tagAddCheck(), getMaxIncreasingField(), addCheckHook);
	}
	
	public void onDeleteCheckRequest(TextRequest builder) {
		builder.append(tagDeleteCheck(), getLatestDeleteTime(), deleteCheckHook);
	}

	/**
	 * 
	 * @param attributes
	 *            the elements must be aligned in some specific order.
	 */
	public void createNew(Object[] attributes, TextRequest builder) {
		// ADD: [local_id, update_time, other_attributes...]
		long time = System.currentTimeMillis();
		Object localId = localAdd(time, attributes);
		JSONArray data = new JSONArray().put(localId).put(time);
		for (Object attr : attributes) {
			data.put(attr);
		}
		builder.append(tagAdd(), data, addHook);
	}

	public void delete(Object localId, TextRequest builder) {
		Object serverId = getServerId(localId);
		if (serverId == null) {
			localDelete(localId);
		} else {
			markAsDelete(localId);
			builder.append(tagDelete(), serverId, deleteHook).append(tagDeleteCheck(), getLatestDeleteTime(), deleteCheckHook);
		}
	}

	/**
	 * Need not to deal with data, just for example, inform user on server result if needed.
	 * @param tag one of {@link #tagAdd()} or {@link #tagDelete()}.
	 * @param fSuccess
	 */
	protected abstract void onServerResult(String tag, boolean fSuccess);

	/**
	 * 
	 * @param localId
	 * @return return null if there's no valid server id.
	 */
	protected abstract Object getServerId(Object localId);

	// ////////////////////////tags//////////////////////////

	protected String tagAddCheck() {
		return getTagDefaultName("ADD_CHECK");
	}

	protected String tagDeleteCheck() {
		return getTagDefaultName("DELETE_CHECK");
	}

	protected String tagAdd() {
		return getTagDefaultName("ADD");
	}

	protected String tagDelete() {
		return getTagDefaultName("DELETE");
	}

	protected String getTagDefaultName(String subfix) {
		return StringUtil.getLogTag(getClass()) + "_" + subfix;
	}

	// /////////////////////////S-C add///////////////////////////

	/**
	 * 
	 * @return the max value of increasing field of records with valid server
	 *         id.
	 */
	protected abstract Object getMaxIncreasingField();

	protected abstract void pullAdd(JSONArray serverIds);

	// ////////////////////////S-C delete/////////////////////////

	protected abstract long getLatestDeleteTime();

	protected abstract void pullDelete(JSONArray serverIds);

	protected abstract void saveLatestDeleteTime(long deleteTime);

	// /////////////////////////C-S add/////////////////////////

	/**
	 * Get records that without valid server_id.
	 * 
	 * @return array element format: [local_id, update_time,
	 *         other_attributes...]
	 */
	protected abstract JSONArray[] getPushAdd();

	/**
	 * 
	 * @param idPair
	 *            format: [local_id, server_id] #[]
	 */
	protected abstract void updateServerId(JSONArray idPair);

	/**
	 * 
	 * @param time
	 * @param attributes
	 *            the elements must be aligned in some specific order.
	 * @return local id
	 */
	protected abstract Object localAdd(long time, Object[] attributes);

	// //////////////////////C-S delete///////////////////////////

	/**
	 * 
	 * @return server_id of records that marked as delete.
	 */
	protected abstract Object[] getPushDelete();

	protected abstract void localDelete(Object localId);

	protected abstract void markAsDelete(Object localId);
}
