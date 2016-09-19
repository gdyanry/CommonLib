/**
 * 
 */
package lib.common.model.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.Map;

import lib.common.util.ConsoleUtil;

/**
 * @author yanry
 *
 *         2014年10月10日 下午5:02:56
 */
public abstract class HttpRequest {
	private HttpURLConnection conn;
	private long startTime;
	private long contentLength;
	private boolean cancel;

	public HttpRequest(String url, Map<String, Object> urlParams) throws IOException {
		startTime = System.currentTimeMillis();
		conn = Https.getConnection(Https.getUrl(url, urlParams));
		conn.setRequestMethod(getRequestMethod());
	}

	public void cancel() {
		cancel = true;
	}

	public boolean isCancel() {
		return cancel;
	}

	public long getTotalLength() {
		if (contentLength == 0) {
			String contentRange = conn.getHeaderField("Content-Range");
			if (contentRange != null) {
				int i = contentRange.indexOf("/");
				if (i != -1) {
					String len = contentRange.substring(i + 1);
					if (len.length() > 0) {
						contentLength = Long.parseLong(len);
					}
				}
			}
			String contentLength = conn.getHeaderField("Content-Length");
			if (contentLength != null) {
				this.contentLength = Long.parseLong(contentLength);
			} else {
				ConsoleUtil.error(getClass(), "content contentLength is unreachable for " + conn.getURL());
				this.contentLength = -1;
			}
		}
		return contentLength;
	}

	public HttpURLConnection getConnection() {
		return conn;
	}

	public long getElapsedTimeMillis() {
		return System.currentTimeMillis() - startTime;
	}

	public String getString(String charset) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(),
				charset));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line).append(System.getProperty("line.separator"));
		}
		int i = sb.lastIndexOf(System.getProperty("line.separator"));
		if (i != -1) {
			sb.delete(i, sb.length());
		}
		return sb.toString();
	}

	public boolean isSuccess() throws IOException {
		int responseCode = conn.getResponseCode();
		return responseCode >= 200 && responseCode < 300;
	}

	protected abstract String getRequestMethod();
}
