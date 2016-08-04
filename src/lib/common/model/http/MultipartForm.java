package lib.common.model.http;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import lib.common.entity.StreamTransferHook;
import lib.common.util.ConsoleUtil;
import lib.common.util.IOUtil;

/**
 * Created by yanry on 2015/8/10.
 */
public class MultipartForm implements CustomRequest {
    private static final String BOUNDARY_STR = "--------52WenJY_hlybal--zxy-----7d4a6d158c9";
    private static final String BOUNDARY = "--" + BOUNDARY_STR + "\r\n";

    private HttpURLConnection conn;
    private OutputStream os;
    private String charset;
    private long startTime;
    private StreamTransferHook uploadHook;

    public MultipartForm(String url, String charset, StreamTransferHook uploadHook) throws IOException {
    	startTime = System.currentTimeMillis();
        conn = Https.getConnection(url);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setRequestProperty("Content-type", "multipart/form-data;boundary=" + BOUNDARY_STR);
        customize(conn);
        os = new BufferedOutputStream(conn.getOutputStream());
        this.charset = charset;
        this.uploadHook = uploadHook;
    }

    public MultipartForm addText(String name, String value) throws IOException {
    	byte[] bytes = String.format("%sContent-Disposition:form-data;name=\"%s\"\r\n\r\n%s\r\n", BOUNDARY, name, value).getBytes(charset);
    	if (uploadHook == null) {
			os.write(bytes);
    	} else {
    		IOUtil.bytesToOutputStream(bytes, os, uploadHook);
    	}
        return this;
    }

    public MultipartForm addFile(String name, File file) throws IOException {
        if (file.isFile()) {
            if (file.length() > 0) {
                addStream(name, new FileInputStream(file), file.getName());
            } else {
                ConsoleUtil.error(getClass(), "invalid file: " + file.getAbsolutePath());
            }
            return this;
        } else {
            throw new IllegalArgumentException(String.format("invalid file: " + file.getAbsolutePath()));
        }
    }

    public MultipartForm addStream(String fieldName, InputStream in, String fileName) throws IOException{
        byte[] leadBytes = String.format("%sContent-Disposition:form-data;Content-Type:application/octet-stream;name=\"%s\";filename=\"%s\"\r\n\r\n", BOUNDARY, fieldName, fileName).getBytes(charset);
        byte[] tailBytes = "\r\n".getBytes(charset);
        if (uploadHook == null) {
        	os.write(leadBytes);
        	long len = IOUtil.transferStream(in, os);
        	ConsoleUtil.debug(getClass(), String.format("add stream field %s: %s(%sbytes)", fieldName, fileName, len));
        	os.write(tailBytes);
        } else {
        	IOUtil.bytesToOutputStream(leadBytes, os, uploadHook);
        	IOUtil.transferStream(in, os, uploadHook);
        	IOUtil.bytesToOutputStream(tailBytes, os, uploadHook);
        }
        return this;
    }

    public MultipartForm addBytes(String fieldName, byte[] bytes, String fileName) throws IOException {
        return addStream(fieldName, new ByteArrayInputStream(bytes), fileName);
    }

    public HttpResponse getResponse() throws IOException {
        byte[] bytes = String.format("--%s--\r\n", BOUNDARY_STR).getBytes(charset);
        if (uploadHook == null) {
        	os.write(bytes);
        } else {
        	IOUtil.bytesToOutputStream(bytes, os, uploadHook);
        }
        os.flush();
        os.close();
        return new HttpResponse(conn, startTime);
    }

	@Override
	public void customize(HttpURLConnection conn) {
		// TODO Auto-generated method stub
		
	}
}
