/**
 * 
 */
package lib.common.model.communication.interfaces;

import lib.common.model.communication.entity.Requesponse;

/**
 * A map structure. 所有发送的响应的缓存，当多次收到同一请求时，直接从该缓存中获取相应的响应并发送。
 *
 * @author yanry
 * 
 *         2015年1月16日 下午3:49:12
 */
public interface ResponseCache {

	Requesponse get(Object requestId);

	void put(Requesponse r);
}
