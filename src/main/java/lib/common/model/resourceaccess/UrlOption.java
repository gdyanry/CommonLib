/**
 * 
 */
package lib.common.model.resourceaccess;

import lib.common.interfaces.StreamTransferHook;

/**
 * @author yanry
 *
 * 2016年7月15日
 */
public interface UrlOption extends StreamTransferHook {

	boolean onReadyToDownload(long startPos, long totalLen);
}
