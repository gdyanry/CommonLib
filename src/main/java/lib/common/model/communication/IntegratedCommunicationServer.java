/**
 *
 */
package lib.common.model.communication;

import lib.common.model.PendingOperationManager;
import lib.common.model.cache.TimerCache;
import lib.common.model.cache.TimerObjectPool;
import lib.common.model.communication.entity.MemoryCommunicationCaches;
import lib.common.model.communication.entity.RequestId;
import lib.common.model.communication.interfaces.CommunicationCaches;
import lib.common.model.communication.interfaces.ServerTagHandler;
import lib.common.model.json.JSONArray;
import lib.common.model.json.JSONObject;
import lib.common.model.log.Logger;

import java.util.*;

/**
 * 因为获取二进制对象时如果缓存里没有该对象，线程会进入等待，直到往缓存放对象的线程将其唤醒，
 * 所以如果获取二进制对象的文本线程与接收二进制对象的线程为同一线程时 ，必须先接收二进制对象（
 * {@link #onReceiveBinary(Object, byte[])}），再处理文本（
 * {@link #onReceiveText(String, Object)}）！
 *
 * @param <U> type of user id.
 * @author yanry
 * <p>
 * 2014年7月7日下午2:46:52
 */
public abstract class IntegratedCommunicationServer<U> {
    private PendingOperationManager<JSONObject> pom;
    private Map<String, CommunicationHandler> chCache;
    private TimerObjectPool<CommunicationHandler> anonymousCHs;
    private Map<String, JSONObject> requestCache;
    private TimerCache<byte[]> binaryCache;
    private Map<String, ServerTagHandler<U>> handlers;

    /**
     * @param binaryTimeoutMinute                            timeout minute of binary gotten by
     *                                                       {@link #onReceiveBinary(Object, byte[])}.
     * @param idleAnonymousCommunicationHandlerKeepAliveSeconds keep alive time of idle {@link CommunicationHandler} objects
     *                                                       for handling anonymous requests.
     * @param timer
     */
    public IntegratedCommunicationServer(int binaryTimeoutMinute, int idleAnonymousCommunicationHandlerKeepAliveSeconds,
                                         Timer timer) {
        pom = new PendingOperationManager<>();
        chCache = new HashMap<>();
        requestCache = new HashMap<>();
        if (binaryTimeoutMinute > 0) {
            binaryCache = new TimerCache<>(binaryTimeoutMinute * 60, timer);
        }
        anonymousCHs = new TimerObjectPool<>(idleAnonymousCommunicationHandlerKeepAliveSeconds) {
            @Override
            protected CommunicationHandler createInstance() {
                return new AnonymousCommunicationHandler();
            }

            @Override
            protected void onReturn(CommunicationHandler obj) {
                obj.getCache().clear();
                obj.setExtra(null);
            }

            @Override
            protected void onDiscard(CommunicationHandler obj) {
            }

            @Override
            protected void onCleared(int poolSize) {
            }
        };
        handlers = new HashMap<>();
    }

    /**
     * Receive binary uploaded from client.
     *
     * @param key    key for the binary.
     * @param binary binary data.
     */
    public void onReceiveBinary(final Object key, byte[] binary) {
        binaryCache.put(key, binary);
        // 通知已收到文件
        pom.notify(key, null);
    }

    /**
     * Get binary data gotten by {@link #onReceiveBinary(Object, byte[])}.
     *
     * @param key
     * @return
     * @throws InterruptedException
     */
    public byte[] getBinary(final Object key) throws InterruptedException {
        byte[] binary = binaryCache.remove(key);
        if (binary != null) {
            return binary;
        } else {
            // 在延时操作中唤醒线程
            pom.new PendingOperation(key) {
                @Override
                protected void exec(JSONObject result) {
                    synchronized (key) {
                        key.notifyAll();
                    }
                }
            };
            // 线程进入等待
            synchronized (key) {
                key.wait(binaryCache.getTimeoutMilli());
            }
            // 线程被唤醒或等待超时，从缓存中取出对象
            return binaryCache.remove(key);
        }
    }

    /**
     * Receive text from client.
     *
     * @param text  text format: [session_id, [[flag, timestamp, {tag: [data,
     *              ...], ...}], ...]]
     * @param extra extra data, which is passed in from upstream section, for
     *              example, the ip address of client.
     * @return response, could be null when there's no request in the received
     * text.
     */
    public String onReceiveText(String text, Object extra) {
        JSONArray ja = new JSONArray(text);
        // ja: [session_id, [...]]
        CommunicationHandler ch;
        boolean isAnonym = false;
        // 若是匿名请求，则session_id为null
        U uid = null;
        String sessionId = null;
        if (ja.isNull(0)) {
            isAnonym = true;
            // anonymous requests are supposed to be one-night requests, because
            // there's no strict mapping between sessions and communication
            // handlers.
            ch = anonymousCHs.borrow();
            ch.setExtra(extra);
        } else {
            sessionId = ja.getString(0);
            uid = getUid(sessionId);
            if (uid == null) {
                return getInvalidSessionResponse();
            } else {
                updateExtra(sessionId, extra);
                ch = getCommunicationHandler(sessionId, uid);
            }
        }
        JSONArray ja1 = ja.getJSONArray(1);
        Logger.getDefault().d("%s%n%n  %s >> %s%n", sessionId, uid, ja1);
        // ja1: [[非0/0, timestamp, {tag: [data, ...], ...}], ...]
        for (int i = 0; i < ja1.length(); i++) {
            JSONArray ja2 = ja1.getJSONArray(i);
            ch.receive(ja2.getLong(1), ja2.getJSONObject(2), ja2.getInt(0));
        }
        JSONArray jaResponse = ch.endReceive(null);
        Logger.getDefault().d("%s%n%n  %s << %s%n", sessionId, uid, jaResponse);
        if (isAnonym) {
            // 放回容器重用
            anonymousCHs.giveBack(ch);
        }
        return jaResponse == null ? null : jaResponse.toString();
    }

    private CommunicationHandler getCommunicationHandler(final String sessionId, final U uid) {
        if (chCache.get(sessionId) == null) {
            synchronized (sessionId) {
                if (chCache.get(sessionId) == null) {
                    chCache.put(sessionId, new CommunicationHandler(createCache(sessionId)) {

                        @Override
                        protected void send(JSONArray json, Set<Object> requestIds, boolean isNew) {
                            sendText(sessionId, json.toString(), requestIds, isNew);
                        }

                        @Override
                        protected JSONObject onReceiveRequest(Object requestTag, JSONObject json) {
                            return onReceiveUserRequest(sessionId, uid, requestTag, json);
                        }

                        @Override
                        protected void onReceiveResponse(Object requestTag, JSONObject json) {
                            onReceiveUserResponse(sessionId, uid, requestTag, json);
                        }

                        @Override
                        protected boolean isReadyToSend(Object newRequestId) {
                            return isPushable(sessionId);
                        }

                        @Override
                        protected void onRejectRequest(Object requestId) {
                            // TODO Auto-generated method stub

                        }
                    });
                }
            }
        }
        return chCache.get(sessionId);
    }

    private JSONObject onReceiveUserRequest(String sessionId, U uid, Object requestTimestamp, JSONObject json) {
        // json: {tag: [data, ...], ...}
        RequestId requestId = new RequestId(sessionId, (Long) requestTimestamp);
        JSONObject responseJo = new JSONObject();
        Iterator<?> it = json.keys();
        while (it.hasNext()) {
            JSONArray responseJa = new JSONArray();
            String tag = it.next().toString();
            ServerTagHandler<U> h = getTagHandler(tag);
            if (h != null) {
                JSONArray jaData = json.getJSONArray(tag);
                for (int i = 0; i < jaData.length(); i++) {
                    Object responseData = h.onUserRequest(tag, requestId, uid, jaData.get(i));
                    // if null, try to treat it as anonymous request.
                    if (responseData == null) {
                        responseData = h.onAnonymousRequest(requestTimestamp, tag, jaData.get(i), uid);
                    }
                    responseJa.put(responseData);
                }
            }
            responseJo.put(tag, responseJa);
        }
        return responseJo;
    }

    private void onReceiveUserResponse(String sessionId, U uid, Object requestTimestamp, JSONObject json) {
        RequestId requestId = new RequestId(sessionId, (Long) requestTimestamp);
        pom.notify(requestId, json);
        Iterator<?> it = json.keys();
        while (it.hasNext()) {
            String tag = it.next().toString();
            JSONArray ja = json.getJSONArray(tag);
            ServerTagHandler<U> h = getTagHandler(tag);
            if (h != null) {
                for (int i = 0; i < ja.length(); i++) {
                    h.onUserResponse(tag, uid, ja.get(i));
                }
            }
        }
    }

    /**
     * Get a {@link ServerTagHandler} object associated with the given tag.
     *
     * @param tag
     * @return return null if no handler is found for the tag.
     */
    public ServerTagHandler<U> getTagHandler(String tag) {
        ServerTagHandler<U> th = handlers.get(tag);
        if (th == null) {
            th = newTagHandler(tag);
            if (th != null) {
                handlers.put(tag, th);
            } else {
                Logger.getDefault().e("tag handler for %s is missing!", tag);
            }
        }
        return th;
    }

    /**
     * Get the internal {@link PendingOperationManager} object using the request
     * timestamp as trigger.
     *
     * @return
     */
    public PendingOperationManager<JSONObject> getPom() {
        return pom;
    }

    /**
     * Do this when user login or logout (destroy session).
     *
     * @param sessionId
     */
    public void deleteCommunicationHandler(String sessionId) {
        CommunicationHandler ch = chCache.remove(sessionId);
        if (ch != null) {
            ch.getCache().clear();
        }
    }

    /**
     * Do this when you want to put the specific pending requests(if any) to
     * send list (encounter error when sending the requests in
     * {@link #sendText(String, String, Set, boolean)}, etc.).
     *
     * @param sessionId
     * @param requestTimestamps
     */
    protected void reloadPendingRequests(String sessionId, Set<Object> requestTimestamps) {
        U uid = getUid(sessionId);
        if (uid == null) {
            deleteCommunicationHandler(sessionId);
        } else {
            getCommunicationHandler(sessionId, uid).reloadPendingRequests(requestTimestamps);
        }
    }

    /**
     * Append an request to the specific client having the given session id.
     * Remember to call {@link #commitRequest(String, int)} at the end.
     *
     * @param sessionId
     * @param tag
     * @param data
     * @return return self to support call chain.
     */
    public IntegratedCommunicationServer<U> appendRequest(String sessionId, String tag, Object data) {
        JSONObject json = requestCache.get(sessionId);
        if (json == null) {
            json = new JSONObject();
            requestCache.put(sessionId, json);
        }
        synchronized (json) {
            // json格式：{tag: [data, ...], ...}
            if (json.has(tag)) {
                json.getJSONArray(tag).put(data);
            } else {
                json.put(tag, new JSONArray().put(data));
            }
        }
        return this;
    }

    /**
     * Send the appended requests to client with the given session id.
     *
     * @param sessionId
     * @param flag
     * @return the timestamp of this request, or 0 if there's nothing in the
     * request cache or if the session is invalid.
     */
    public long commitRequest(String sessionId, int flag) {
        long timestamp = 0;
        JSONObject jo = requestCache.get(sessionId);
        if (jo != null) {
            synchronized (jo) {
                if (jo.length() > 0) {
                    U uid = getUid(sessionId);
                    if (uid == null) {
                        deleteCommunicationHandler(sessionId);
                    } else {
                        timestamp = System.currentTimeMillis();
                        getCommunicationHandler(sessionId, uid).sendRequest(timestamp, jo, flag);
                    }
                    requestCache.remove(sessionId);
                }
            }
        }
        return timestamp;
    }

    /**
     * Instantiate a handler of the given tag.
     *
     * @param tag
     * @return
     */
    protected abstract ServerTagHandler<U> newTagHandler(String tag);

    /**
     * Update extra information associated with the given session id.
     *
     * @param sessionId
     * @param extra
     */
    protected abstract void updateExtra(String sessionId, Object extra);

    /**
     * 推送消息。Any error occurs, remember to invoke
     * {@link #reloadPendingRequests(String, Set)}, other wise the
     * none-one-night requests won't be send again automatically. Note that all
     * the non-one-night requests have been settled in the pending requests
     * queue by now.
     *
     * @param sessionId
     * @param text
     * @param requestTimestamps
     * @param isNewRequest      whether this send-text behavior is triggered by a new request.
     */
    protected abstract void sendText(String sessionId, String text, Set<Object> requestTimestamps,
                                     boolean isNewRequest);

    /**
     * Get user id by session id.
     *
     * @param sessionId
     * @return 若无userId与该sessionId对应，则返回null。
     */
    protected abstract U getUid(String sessionId);

    /**
     * Instantiate a new {@link CommunicationCaches} for the given session id.
     *
     * @param sessionId
     * @return
     */
    protected abstract CommunicationCaches createCache(String sessionId);

    /**
     * A check on whether message can be pushed to the client.
     *
     * @param sessionId
     * @return
     */
    protected abstract boolean isPushable(String sessionId);

    protected abstract String getInvalidSessionResponse();

    private class AnonymousCommunicationHandler extends CommunicationHandler {

        AnonymousCommunicationHandler() {
            super(new MemoryCommunicationCaches());
        }

        @Override
        protected void send(JSONArray json, Set<Object> requestTimstamps, boolean isNew) {
        }

        @Override
        protected JSONObject onReceiveRequest(Object requestTag, JSONObject json) {
            // json: {tag: [data, ...], ...}
            JSONObject responseJo = new JSONObject();
            Iterator<?> it = json.keys();
            while (it.hasNext()) {
                JSONArray responseJa = new JSONArray();
                String tag = it.next().toString();
                ServerTagHandler<U> h = getTagHandler(tag);
                if (h != null) {
                    JSONArray jaData = json.getJSONArray(tag);
                    for (int i = 0; i < jaData.length(); i++) {
                        Object responseData = h.onAnonymousRequest(requestTag, tag, jaData.get(i), getExtra());
                        responseJa.put(responseData);
                    }
                }
                responseJo.put(tag, responseJa);
            }
            return responseJo;
        }

        @Override
        protected void onReceiveResponse(Object requestTag, JSONObject json) {
        }

        @Override
        protected boolean isReadyToSend(Object newRequestId) {
            return false;
        }

        @Override
        protected void onRejectRequest(Object requestId) {
        }
    }

}
