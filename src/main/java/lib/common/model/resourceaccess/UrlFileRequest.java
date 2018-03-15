/**
 * 
 */
package lib.common.model.resourceaccess;

import java.io.File;

/**
 * @author yanry
 *
 * 2016年7月20日
 */
public abstract class UrlFileRequest implements UrlOption, AccessHook<File> {
	private long startPos;
	private long totalLen;
	
	public void start(UrlFileAccess access, String key) {
		access.get(key, this, this);
	}

	protected abstract void onUpdate(long currentPos, long totalLen);
	
	protected abstract void onFileAvailable(File file);
	
	@Override
	public boolean isStop() {
		return false;
	}

	@Override
	public void onUpdate(long transferedBytes) {
		onUpdate(transferedBytes + startPos, totalLen);
	}

	@Override
	public void onFinish(boolean isStopped) {
	}

	@Override
	public int getBufferSize() {
		return 0;
	}

	@Override
	public boolean onStartGenerate(File cached) {
		if (cached.isFile()) {
			onFileAvailable(cached);
			return false;
		} else {
			return true;
		}
	}

	@Override
	public boolean onStartCache(File generated) {
		if (generated.isFile()) {
			onFileAvailable(generated);
		}
		return false;
	}

	@Override
	public boolean onReadyToDownload(long startPos, long totalLen) {
		this.startPos = startPos;
		this.totalLen = totalLen;
		onUpdate(startPos, totalLen);
		return true;
	}

}
