package yanry.lib.java.model.http;

import java.io.IOException;
import java.util.Map;

/**
 * Created by yanrongyu on 16/9/18.
 */

public class HttpGet extends HttpRequest {
    public HttpGet(String url) throws IOException {
        this(url, null, 0);
    }

    public HttpGet(String url, Map<String, Object> urlParams, long startPos) throws IOException {
        super(url, urlParams);
        if (startPos > 0) {
            getConnection().setRequestProperty("RANGE", "bytes=" + startPos + "-");
        }
    }

    public void send() throws IOException {
        getConnection().connect();
    }

    @Override
    protected String getRequestMethod() {
        return "GET";
    }
}
