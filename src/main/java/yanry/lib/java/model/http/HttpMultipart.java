package yanry.lib.java.model.http;

import yanry.lib.java.interfaces.StreamTransferHook;
import yanry.lib.java.model.log.Logger;
import yanry.lib.java.util.IOUtil;

import java.io.*;
import java.util.Map;

/**
 * Created by yanry on 2015/8/10.
 */
public abstract class HttpMultipart extends HttpRequest {
    private static final String BOUNDARY_STR = "--------52WenJY_hlybal--zxy-----7d4a6d158c9";
    private static final String BOUNDARY = "--" + BOUNDARY_STR + "\r\n";

    private OutputStream os;

    public HttpMultipart(String url, Map<String, ?> urlParams) throws IOException {
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

    public static void main(String[] args) throws IOException {
        HttpMultipart httpMultipart = new HttpMultipart("https://dev-aicloud.tclai.top/filecenter/internal/uploadwakeupfilemul", null) {
            @Override
            protected StreamTransferHook getUploadHook() {
                return null;
            }

            @Override
            protected String getCharset() {
                return "utf-8";
            }
        };
        httpMultipart.getConnection().setConnectTimeout(30000);
        httpMultipart.getConnection().setRequestMethod("POST");
        httpMultipart
                .addFile("uploadFile", new File("f:/yanry.kdbx"))
                .addText("dnum", "540539126")
                .addText("clientType", "TCL-CN-MS848C-C6")
                .addText("date", "1640314108775")
                .addText("queryId", "4f67c244-138f-48d2-b025-0bdafb60e92d")
                .addText("maker", "ifly-jni_so_1.3.1_sdk127837")
                .addText("confidence", "800")
                .addText("wakeupWord", "xiao4txiao4t");
        httpMultipart.commit();
        if (httpMultipart.isSuccess()) {
            System.out.println("upload wakeup voice resp: " + httpMultipart.getString("utf-8"));
        } else {
            System.out.println("upload wakeup voice error: " + httpMultipart.getConnection().getResponseCode() +
                    ", " +
                    IOUtil.streamToString(httpMultipart.getConnection().getErrorStream(), "utf-8"));
        }
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

    public HttpMultipart addStream(String fieldName, InputStream in, String fileName) throws IOException {
        byte[] leadBytes = String.format("%sContent-Disposition: form-data; name=\"%s\"; filename=\"%s\"\r\nContent-Type: application/octet-stream\r\n\r\n", BOUNDARY, fieldName, fileName).getBytes(getCharset());
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

    @Override
    protected String getRequestMethod() {
        return "POST";
    }

    protected abstract StreamTransferHook getUploadHook();

    protected abstract String getCharset();
}
