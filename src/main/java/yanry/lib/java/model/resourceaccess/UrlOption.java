package yanry.lib.java.model.resourceaccess;

import yanry.lib.java.interfaces.StreamTransferHook;

/**
 * @author yanry
 * <p>
 * 2016年7月15日
 */
public interface UrlOption extends StreamTransferHook {

    boolean onReadyToDownload(long startPos, long totalLen);
}
