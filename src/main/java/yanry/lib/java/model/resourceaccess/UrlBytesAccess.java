package yanry.lib.java.model.resourceaccess;

import yanry.lib.java.entity.ActionAbortException;
import yanry.lib.java.model.http.HttpGet;
import yanry.lib.java.model.http.HttpRequest;
import yanry.lib.java.util.IOUtil;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * @author yanry
 *
 * 2016年5月8日
 */
public abstract class UrlBytesAccess extends CacheResourceAccess<String, byte[], UrlOption, AccessHook<byte[]>> {

	@Override
	protected byte[] generate(String key, byte[] cached, UrlOption option, AccessHook<byte[]> hook)
			throws Exception {
		if (key.length() > 0) {
			HttpRequest request = new HttpGet(key);
			if (request.isSuccess()) {
				InputStream is = request.getConnection().getInputStream();
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				if (option == null) {
					IOUtil.transferStream(is, bos);
				} else {
					if (option.onReadyToDownload(0L, request.getTotalLength())) {
						IOUtil.transferStream(is, bos, option);
						if (option.isStop()) {
							throw new ActionAbortException("aborted by user on downloading");
						}
					} else {
						throw new ActionAbortException("aborted by user");
					}
				}
				return bos.toByteArray();
			} else {
				throw new ActionAbortException("http error: " + request.getConnection().getResponseCode());
			}
		} else {
			throw new ActionAbortException("url is empty");
		}
	}

}
