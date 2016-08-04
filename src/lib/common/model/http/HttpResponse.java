/**
 * 
 */
package lib.common.model.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import lib.common.util.ConsoleUtil;

/**
 * @author yanry
 *
 *         2014年10月10日 下午5:02:56
 */
public class HttpResponse {
	private HttpURLConnection conn;
	private long startTime;
	private long length;

	HttpResponse(HttpURLConnection conn, long startTime)
			throws IOException {
		this.conn = conn;
		this.startTime = startTime;
		if (!isSuccess()) {
			ConsoleUtil.error(getClass(), "response code: " + conn.getResponseCode());
		}
	}
	
	public long getTotalLength() {
		if (length == 0) {
			String contentRange = conn.getHeaderField("Content-Range");
			if (contentRange != null) {
				int i = contentRange.indexOf("/");
				if (i != -1) {
					String len = contentRange.substring(i + 1);
					if (len.length() > 0) {
						length = Long.parseLong(len);
					}
				}
			}
			String contentLength = conn.getHeaderField("Content-Length");
			if (contentLength != null) {
				length = Long.parseLong(contentLength);
			} else {
				ConsoleUtil.error(getClass(), "content length is unreachable for " + conn.getURL());
				length = -1;
			}
		}
		return length;
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
		return conn.getResponseCode() == 200;
	}
}
