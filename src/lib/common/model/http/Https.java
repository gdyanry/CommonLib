/**
 * 
 */
package lib.common.model.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import lib.common.entity.StreamTransferHook;
import lib.common.util.ConsoleUtil;
import lib.common.util.IOUtil;

/**
 * 
 * @author yanry
 *
 *         2014年12月25日 下午2:04:21
 */
public class Https {

	/**
	 * 
	 * @param verifier
	 * @param certificates
	 *            certificates from server as alias-certificate pairs.
	 * @param clientKey
	 *            client keystore used in bidirectional verification scenario,
	 *            may be null. Note that the key file's default format of java
	 *            platform is jks, but android only recognizes bks.
	 * @param password
	 *            password of client keystore.
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	public static void initSSL(HostnameVerifier verifier, Map<String, InputStream> certificates, InputStream clientKey,
			String password) throws GeneralSecurityException, IOException {
		// server certificate
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		KeyStore serverKs = KeyStore.getInstance(KeyStore.getDefaultType());
		serverKs.load(null);
		if (certificates != null) {
			for (String alias : certificates.keySet()) {
				InputStream cer = certificates.get(alias);
				if (cer != null) {
					serverKs.setCertificateEntry(alias, cf.generateCertificate(cer));
					cer.close();
				}
			}
		}
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(serverKs);
		// client key
		KeyManagerFactory kmf = null;
		if (clientKey != null && password != null) {
			KeyStore clientKs = KeyStore.getInstance(KeyStore.getDefaultType());
			clientKs.load(clientKey, password.toCharArray());
			kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(clientKs, password.toCharArray());
		}

		SSLContext ssl = SSLContext.getInstance("TLS");
		ssl.init(kmf == null ? null : kmf.getKeyManagers(), tmf.getTrustManagers(), null);
		HttpsURLConnection.setDefaultSSLSocketFactory(ssl.getSocketFactory());
		if (verifier != null) {
			HttpsURLConnection.setDefaultHostnameVerifier(verifier);
		}
	}

	public static HttpURLConnection getConnection(String url) throws IOException {
		URL u = URI.create(url).toURL();
		ConsoleUtil.debug(Https.class, "open connection: " + u);
		return (HttpURLConnection) u.openConnection();
	}

	public static String getUrl(String baseUrl, Map<String, Object> params) {
		if (params == null || params.isEmpty()) {
			return baseUrl;
		}
		StringBuilder sb = new StringBuilder(baseUrl).append("?");
		for (Entry<String, Object> entry : params.entrySet()) {
			sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
		}
		return sb.deleteCharAt(sb.length() - 1).toString();
	}

	public static HttpResponse get(String baseUrl, Map<String, Object> params) throws IOException {
		return get(baseUrl, params, 0);
	}
	
	public static HttpResponse get(String baseUrl, Map<String, Object> params, long startPos) throws IOException {
		long time = System.currentTimeMillis();
		HttpURLConnection conn = getConnection(getUrl(baseUrl, params));
		conn.setRequestMethod("GET");
		if (startPos > 0) {
			conn.setRequestProperty("RANGE", "bytes=" + startPos + "-");
		}
		conn.connect();
		return new HttpResponse(conn, time);
	}

	public static HttpResponse post(String baseUrl, Map<String, Object> params, byte[] entity) throws IOException {
		return post(baseUrl, params, entity, null);
	}
	
	public static HttpResponse post(String baseUrl, Map<String, Object> params, byte[] entity, StreamTransferHook uploadHook) throws IOException {
		long time = System.currentTimeMillis();
		HttpURLConnection conn = postPreparation(baseUrl, params);
		OutputStream os = conn.getOutputStream();
		if (uploadHook == null) {
			os.write(entity);
			os.flush();
			ConsoleUtil.debug(Https.class, String.format("post content length: %sbytes", entity.length));
		} else {
			IOUtil.bytesToOutputStream(entity, os, uploadHook);
		}
		os.close();
		return new HttpResponse(conn, time);
	}

	private static HttpURLConnection postPreparation(String baseUrl, Map<String, Object> params) throws IOException {
		HttpURLConnection conn = getConnection(getUrl(baseUrl, params));
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setRequestProperty("Content-type", "application/octet-stream");
		return conn;
	}

	public static HttpResponse post(String baseUrl, Map<String, Object> params, InputStream entity) throws IOException {
		return post(baseUrl, params, entity, null);
	}
	
	public static HttpResponse post(String baseUrl, Map<String, Object> params, InputStream entity, StreamTransferHook uploadHook) throws IOException {
		long time = System.currentTimeMillis();
		HttpURLConnection conn = postPreparation(baseUrl, params);
		OutputStream os = conn.getOutputStream();
		if (uploadHook == null) {
			long length = IOUtil.transferStream(entity, os);
			ConsoleUtil.debug(Https.class, String.format("post content length: %sbytes", length));
		} else {
			IOUtil.transferStream(entity, os, uploadHook);
		}
		os.close();
		return new HttpResponse(conn, time);
	}

	public static HttpResponse customizedRequest(String baseUrl, Map<String, Object> params, CustomRequest request)
			throws IOException {
		long time = System.currentTimeMillis();
		HttpURLConnection conn = getConnection(getUrl(baseUrl, params));
		request.customize(conn);
		conn.connect();
		return new HttpResponse(conn, time);
	}
}
