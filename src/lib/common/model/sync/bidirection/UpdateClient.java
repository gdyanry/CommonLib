/**
 * 
 */
package lib.common.model.sync.bidirection;

import lib.common.model.communication.IntegratedCommunicationClient;
import lib.common.model.communication.IntegratedCommunicationClient.TextRequest;
import lib.common.model.communication.base.RequestDataHook;
import lib.common.model.json.JSONArray;
import lib.common.model.json.JSONObject;

/**
 * Synchronize client that supports update besides add and delete.
 * 
 * @author yanry
 *
 *         2015年7月10日 上午2:17:28
 */
public abstract class UpdateClient<F> extends BaseClient<F> {
	private ReluctantUpdatable ru;
	private RequestDataHook updatePushHook;
	private RequestDataHook updateCheckHook;
	private RequestDataHook updatePullHook;

	public UpdateClient(ReluctantUpdatable ru) {
		this.ru = ru;
		updatePushHook = new RequestDataHook() {
			
			@Override
			public void onResponse(Object responseData) {
				// UPDATE_PUSH: [server_id, if_success]
				JSONArray result = (JSONArray) responseData;
				if (result.getBoolean(1)) {
					removeUpdateMark(result.get(0));
					updateLoginTime(result.get(0));
					onServerResult(tagUpdatePush(), true);
				} else {
					onServerResult(tagUpdatePush(), false);
				}
			}
		};
		updateCheckHook = new RequestDataHook() {
			
			@Override
			public void onResponse(Object responseData) {
				// UPDATE_CHECK: [server_id, PUSH/PULL/CHECKED]
				JSONArray data = (JSONArray) responseData;
				int result = data.getInt(1);
				Object serverId = data.get(0);
				if (isPush(result)) {
					// UPDATE_PUSH: [server_id, update_time,
					// {attribute_name: attribute_value, ...}]
					getIcc().new TextRequest().guaranteed().append(tagUpdatePush(),
							getPushUpdate(serverId), updatePushHook).send(null);
				} else if (isPull(result)) {
					getIcc().new TextRequest().guaranteed().append(tagUpdatePull(), serverId, updatePullHook).send(null);
				} else if (isChecked(result)) {
					removeUpdateMark(serverId);
					updateLoginTime(serverId);
				}
			}
		};
		updatePullHook = new RequestDataHook() {
			
			@Override
			public void onResponse(Object responseData) {
				// UPDATE_PULL: [server_id, update_time,
				// other_attributes...]
				JSONArray data = (JSONArray) responseData;
				pullUpdate(data);
				Object serverId = data.get(0);
				removeUpdateMark(serverId);
				updateLoginTime(serverId);
			}
		};
	}

	@Override
	public void onReady(TextRequest builder) {
		super.onReady(builder);
		for (JSONArray record : getPushUpdate()) {
			builder.append(tagUpdatePush(), record, updatePushHook);
		}
	}
	
	public void onUpdateCheckRequest(Object serverId, TextRequest builder) {
		// UPDATE_CHECK: server_id
		builder.append(tagUpdateCheck(),
				new JSONArray().put(serverId).put(getUpdateTime(serverId)), updateCheckHook);
	}

	/**
	 * 
	 * @param localId
	 * @param updateInfo in json format: {attribute_name: value, ...}
	 */
	public void pushUpdate(Object localId, JSONObject updateInfo, TextRequest builder) {
		// UPDATE_PUSH: [server_id, update_time, {attribute_name:
		// attribute_value, ...}]
		long time = System.currentTimeMillis();
		Object serverId = localUpdate(localId, time, updateInfo);
		builder.append(tagUpdatePush(),
				new JSONArray().put(serverId).put(time).put(updateInfo), updatePushHook);
	}

	public boolean pullUpdate(Object localId, TextRequest builder) {
		Object serverId = getServerId(localId);
		if (serverId != null) {
			if (ru == null
					|| ru.getLocalLoginTime(localId) < ru.getGlobalLoginTime()) {
				// UPDATE_CHECK: [server_id, update_time]
				builder.append(tagUpdateCheck(),
						new JSONArray().put(serverId).put(
								getUpdateTime(serverId)), updateCheckHook);
				return true;
			}
		}
		return false;
	}

	private void updateLoginTime(Object serverId) {
		if (ru != null) {
			ru.updateLoginTime(serverId, ru.getGlobalLoginTime());
		}
	}

	protected abstract boolean isPush(int result);

	protected abstract boolean isPull(int result);

	protected abstract boolean isChecked(int result);

	// ////////////////////////tags///////////////////////////////

	protected String tagUpdateCheck() {
		return getTagDefaultName("UPDATE_CHECK");
	}

	protected String tagUpdatePush() {
		return getTagDefaultName("UPDATE_PUSH");
	}

	protected String tagUpdatePull() {
		return getTagDefaultName("UPDATE_PULL");
	}

	// ////////////////////database///////////////////////////////

	/**
	 * Get records that marked as update.
	 * 
	 * @return array element format: [server_id, update_time, {attribute_name:
	 *         attribute_value, ...}]
	 */
	protected abstract JSONArray[] getPushUpdate();

	/**
	 * 
	 * @param serverId
	 * @return format: [server_id, update_time, {attribute_name:
	 *         attribute_value, ...}]
	 */
	protected abstract JSONArray getPushUpdate(Object serverId);

	protected abstract void removeUpdateMark(Object serverId);

	/**
	 * 
	 * @param record
	 *            format: [server_id, update_time, other_attributes...]
	 */
	protected abstract void pullUpdate(JSONArray record);

	/**
	 * Update info and mark the record as update.
	 * 
	 * @param localId
	 * @param time
	 * @param updateInfo
	 * @return server id.
	 */
	protected abstract Object localUpdate(Object localId, long time,
			JSONObject updateInfo);

	protected abstract long getUpdateTime(Object serverId);
	
	protected abstract IntegratedCommunicationClient getIcc();
}
