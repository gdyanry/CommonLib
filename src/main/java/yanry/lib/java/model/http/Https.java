package yanry.lib.java.model.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import yanry.lib.java.model.log.Logger;

/**
 * @author yanry
 * <p>
 * 2014年12月25日 下午2:04:21
 */
public class Https {

    /**
     * @param verifier
     * @param certificates certificates from server as alias-certificate pairs.
     * @param clientKey    client keystore used in bidirectional verification scenario,
     *                     may be null. Note that the key file's default format of java
     *                     platform is jks, but android only recognizes bks.
     * @param password     password of client keystore.
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public static void initSSL(HostnameVerifier verifier, Map<String, InputStream> certificates, InputStream clientKey,
                               String password) throws GeneralSecurityException, IOException {
        // client key
        KeyManagerFactory kmf = null;
        if (clientKey != null && password != null) {
            KeyStore clientKs = KeyStore.getInstance(KeyStore.getDefaultType());
            clientKs.load(clientKey, password.toCharArray());
            kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(clientKs, password.toCharArray());
        }

        SSLContext ssl = SSLContext.getInstance("TLS");
        ssl.init(kmf == null ? null : kmf.getKeyManagers(), new TrustManager[]{new CombinedX509TrustManager(certificates)}, null);
        HttpsURLConnection.setDefaultSSLSocketFactory(ssl.getSocketFactory());
        if (verifier != null) {
            HttpsURLConnection.setDefaultHostnameVerifier(verifier);
        }
    }

    public static HttpURLConnection getConnection(String url) throws IOException {
        URL u = URI.create(url).toURL();
        Logger.getDefault().dd("open connection: ", u);
        return (HttpURLConnection) u.openConnection();
    }

    public static String getUrl(String baseUrl, Map<String, ? extends Object> params) {
        if (params == null || params.isEmpty()) {
            return baseUrl;
        }
        StringBuilder sb = new StringBuilder(baseUrl).append("?");
        for (Entry<String, ? extends Object> entry : params.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }
}
