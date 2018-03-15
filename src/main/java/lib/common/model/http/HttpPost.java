package lib.common.model.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import lib.common.entity.StreamTransferHook;
import lib.common.util.ConsoleUtil;
import lib.common.util.IOUtil;

/**
 * Created by yanrongyu on 16/9/18.
 */

public abstract class HttpPost extends HttpRequest {

    public HttpPost(String url, Map<String, Object> urlParams) throws IOException {
        this(url, urlParams, "application/octet-stream");
    }

    public HttpPost(String url, Map<String, Object> urlParams, String contentType) throws IOException {
        super(url, urlParams);
        getConnection().setDoOutput(true);
        getConnection().setUseCaches(false);
        getConnection().setRequestProperty("Content-type", contentType);
    }

    public void send(Map<String, Object> textFields, String charset) throws IOException {
        if (textFields != null && !textFields.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Object> entry : textFields.entrySet()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            send(sb.deleteCharAt(sb.length() - 1).toString().getBytes(charset));
        }
    }

    public void send(byte[] entity) throws IOException {
        OutputStream os = getConnection().getOutputStream();
        StreamTransferHook uploadHook = getUploadHook();
        if (uploadHook == null) {
            os.write(entity);
            os.flush();
            ConsoleUtil.debug(String.format("post bytes length: %sb", entity.length));
        } else {
            IOUtil.bytesToOutputStream(entity, os, uploadHook);
        }
        os.close();
    }

    public void send(InputStream entity) throws IOException {
        OutputStream os = getConnection().getOutputStream();
        StreamTransferHook uploadHook = getUploadHook();
        if (uploadHook == null) {
            long length = IOUtil.transferStream(entity, os);
            ConsoleUtil.debug(String.format("post input stream length: %sb", length));
        } else {
            IOUtil.transferStream(entity, os, uploadHook);
        }
        os.close();
    }

    @Override
    protected String getRequestMethod() {
        return "POST";
    }

    protected abstract StreamTransferHook getUploadHook();
}
