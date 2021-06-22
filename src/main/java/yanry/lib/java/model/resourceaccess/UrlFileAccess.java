package yanry.lib.java.model.resourceaccess;

import yanry.lib.java.entity.ActionAbortException;
import yanry.lib.java.model.http.HttpGet;
import yanry.lib.java.model.http.HttpRequest;
import yanry.lib.java.util.IOUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

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
			HttpRequest request;
			if (supportResume()) {
				request = new HttpGet(key, null, temp.length());
			} else {
				request = new HttpGet(key);
			}
			if (request.isSuccess()) {
				InputStream is = request.getConnection().getInputStream();
				if (option == null) {
					FileOutputStream os = new FileOutputStream(temp, false);
					IOUtil.transferStream(is, os);
					os.close();
				} else {
					if (option.onReadyToDownload(temp.length(), request.getTotalLength())) {
						FileOutputStream os = new FileOutputStream(temp, supportResume());
						IOUtil.transferStream(is, os, option);
						os.close();
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
				throw new ActionAbortException("http error: " + request.getConnection().getResponseCode());
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
