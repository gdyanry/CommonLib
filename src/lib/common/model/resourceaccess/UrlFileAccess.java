/**
 * 
 */
package lib.common.model.resourceaccess;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import lib.common.entity.ActionAbortException;
import lib.common.model.http.HttpResponse;
import lib.common.model.http.Https;
import lib.common.util.IOUtil;

/**
 * @author yanry
 *
 * 2015年11月14日
 */
public abstract class UrlFileAccess extends CacheResourceAccess<String, File, UrlOption, AccessHook<File>> {
	private FileHashMapper mapper;

	public UrlFileAccess(FileHashMapper mapper) {
		this.mapper = mapper;
	}
	
	public FileHashMapper getMapper() {
		return mapper;
	}
	
	protected abstract boolean supportResume();
	
	@Override
	protected File generate(String key, File cached, UrlOption option, AccessHook<File> hook)
			throws Exception {
		if (key.length() > 0) {
			// use random number as tempt suffix to avoid concurrent issue
			File temp = new File(cached.getAbsolutePath() + ".tmp");
			HttpResponse httpResponse;
			if (supportResume()) {
				httpResponse = Https.get(key, null, temp.length());
			} else {
				httpResponse = Https.get(key, null);
			}
			if (httpResponse.isSuccess()) {
				InputStream is = httpResponse.getConnection().getInputStream();
				if (option == null) {
					IOUtil.transferStream(is, new FileOutputStream(temp, false));
				} else {
					if (option.onReadyToDownload(temp.length(), httpResponse.getTotalLength())) {
						IOUtil.transferStream(is, new FileOutputStream(temp, supportResume()), option);
						if (option.isStop()) {
							throw new ActionAbortException("aborted by user on downloading");
						}
					} else {
						throw new ActionAbortException("aborted by user on ready to download");
					}
				}
				temp.renameTo(cached);
				return cached;
			} else {
				throw new ActionAbortException("http error: " + httpResponse.getConnection().getResponseCode());
			}
		} else {
			throw new ActionAbortException("url is empty");
		}
	}

	@Override
	protected File getCacheValue(String key, UrlOption option) {
		return mapper.getFile(key);
	}

	@Override
	protected void cache(String key, UrlOption option, File generated) {
	}

}
