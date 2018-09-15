package lib.common.model.http;

import lib.common.interfaces.StreamTransferHook;
import lib.common.model.log.Logger;
import lib.common.util.IOUtil;

import java.io.*;
import java.util.Map;

/**
 * Created by yanry on 2015/8/10.
 */
public abstract class HttpMultipart extends HttpRequest {
    private static final String BOUNDARY_STR = "--------52WenJY_hlybal--zxy-----7d4a6d158c9";
    private static final String BOUNDARY = "--" + BOUNDARY_STR + "\r\n";

    private OutputStream os;

    public HttpMultipart(String url, Map<String, Object> urlParams) throws IOException {
        super(url, urlParams);
        getConnection().setDoOutput(true);
        getConnection().setUseCaches(false);
        getConnection().setRequestProperty("Content-type", "multipart/form-data;boundary=" + BOUNDARY_STR);
    }

    private OutputStream getOutputStream() throws IOException {
        if (os == null) {
            os = new BufferedOutputStream(getConnection().getOutputStream());
        }
        return os;
    }

    public HttpMultipart addText(String name, String value) throws IOException {
        byte[] bytes = String.format("%sContent-Disposition:form-data;name=\"%s\"\r\n\r\n%s\r\n", BOUNDARY, name, value).getBytes(getCharset());
        StreamTransferHook uploadHook = getUploadHook();
        if (uploadHook == null) {
            getOutputStream().write(bytes);
        } else {
            IOUtil.bytesToOutputStream(bytes, getOutputStream(), uploadHook);
        }
        return this;
    }

    public HttpMultipart addFile(String name, File file) throws IOException {
        if (file.isFile() && file.length() > 0) {
            addStream(name, new FileInputStream(file), file.getName());
        } else {
            Logger.getDefault().e("invalid file: %s", file.getAbsolutePath());
        }
        return this;
    }

    public HttpMultipart addStream(String fieldName, InputStream in, String fileName) throws IOException {
        byte[] leadBytes = String.format("%sContent-Disposition:form-data;Content-Type:application/octet-stream;name=\"%s\";filename=\"%s\"\r\n\r\n", BOUNDARY, fieldName, fileName).getBytes(getCharset());
        byte[] tailBytes = "\r\n".getBytes(getCharset());
        StreamTransferHook uploadHook = getUploadHook();
        if (uploadHook == null) {
            getOutputStream().write(leadBytes);
            long len = IOUtil.transferStream(in, getOutputStream());
            Logger.getDefault().d("add stream field %s: %s(%sB)", fieldName, fileName, len);
            getOutputStream().write(tailBytes);
        } else {
            IOUtil.bytesToOutputStream(leadBytes, getOutputStream(), uploadHook);
            IOUtil.transferStream(in, getOutputStream(), uploadHook);
            IOUtil.bytesToOutputStream(tailBytes, getOutputStream(), uploadHook);
        }
        return this;
    }

    public HttpMultipart addBytes(String fieldName, byte[] bytes, String fileName) throws IOException {
        return addStream(fieldName, new ByteArrayInputStream(bytes), fileName);
    }

    public void commit() throws IOException {
        byte[] bytes = String.format("--%s--\r\n", BOUNDARY_STR).getBytes(getCharset());
        StreamTransferHook uploadHook = getUploadHook();
        if (uploadHook == null) {
            getOutputStream().write(bytes);
        } else {
            IOUtil.bytesToOutputStream(bytes, getOutputStream(), uploadHook);
        }
        getOutputStream().flush();
        getOutputStream().close();
    }

    @Override
    protected String getRequestMethod() {
        return "POST";
    }

    protected abstract StreamTransferHook getUploadHook();

    protected abstract String getCharset();
}
