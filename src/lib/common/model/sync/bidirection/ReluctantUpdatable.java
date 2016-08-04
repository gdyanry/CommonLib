/**
 * 
 */
package lib.common.model.sync.bidirection;

/**
 * @author yanry
 *
 * 2015年7月7日 上午2:47:11
 */
public interface ReluctantUpdatable {

	long getGlobalLoginTime();
	
	long getLocalLoginTime(Object localId);
	
	void updateLoginTime(Object serverId, long loginTime);
}
