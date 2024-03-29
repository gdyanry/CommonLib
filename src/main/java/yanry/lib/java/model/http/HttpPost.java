package yanry.lib.java.model.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import yanry.lib.java.interfaces.StreamTransferHook;
import yanry.lib.java.model.log.Logger;
import yanry.lib.java.util.IOUtil;

/**
 * Created by yanrongyu on 16/9/18.
 */

public class HttpPost extends HttpRequest {

    public HttpPost(String url, Map<String, ? extends Object> urlParams) throws IOException {
        this(url, urlParams, "application/octet-stream");
    }

    public HttpPost(String url, Map<String, ? extends Object> urlParams, String contentType) throws IOException {
        super(url, urlParams);
        getConnection().setDoOutput(true);
        getConnection().setUseCaches(false);
        getConnection().setRequestProperty("Content-type", contentType);
    }

    public void send(Map<String, ? extends Object> textFields, String charset) throws IOException {
        if (textFields != null && !textFields.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, ? extends Object> entry : textFields.entrySet()) {
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
            Logger.getDefault().v("post bytes length: %sb", entity.length);
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
            Logger.getDefault().v("post input stream length: %sb", length);
        } else {
            IOUtil.transferStream(entity, os, uploadHook);
        }
        os.close();
    }

    protected StreamTransferHook getUploadHook() {
        return null;
    }

    @Override
    protected String getRequestMethod() {
        return "POST";
    }
}
