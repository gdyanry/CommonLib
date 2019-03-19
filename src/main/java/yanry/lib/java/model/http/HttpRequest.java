package yanry.lib.java.model.http;

import yanry.lib.java.model.log.Logger;
import yanry.lib.java.util.IOUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;

/**
 * @author yanry
 * <p>
 * 2014年10月10日 下午5:02:56
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
                Logger.getDefault().e("content contentLength is unreachable for %s", conn.getURL());
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
        return IOUtil.streamToString(conn.getInputStream(), charset);
    }

    public boolean isSuccess() throws IOException {
        int responseCode = conn.getResponseCode();
        return responseCode >= 200 && responseCode < 300;
    }

    protected abstract String getRequestMethod();
}
