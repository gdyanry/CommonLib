package lib.common.model.http;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;

public class CombinedX509TrustManager implements X509TrustManager {
    private LinkedList<X509TrustManager> managers;

    public CombinedX509TrustManager(Map<String, InputStream> certificates) throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
        managers = new LinkedList<>();
        // custom TrustManager
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null);
        if (certificates != null) {
            for (String alias : certificates.keySet()) {
                InputStream cer = certificates.get(alias);
                if (cer != null) {
                    keyStore.setCertificateEntry(alias, certificateFactory.generateCertificate(cer));
                    cer.close();
                }
            }
        }
        TrustManagerFactory customFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        customFactory.init(keyStore);
        for (TrustManager trustManager : customFactory.getTrustManagers()) {
            if (trustManager instanceof X509TrustManager) {
                managers.add((X509TrustManager) trustManager);
            }
        }
        // default TrustManager
        TrustManagerFactory defaultFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        defaultFactory.init((KeyStore) null);
        for (TrustManager trustManager : defaultFactory.getTrustManagers()) {
            if (trustManager instanceof X509TrustManager) {
                managers.add((X509TrustManager) trustManager);
            }
        }
    }

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        CertificateException exception = null;
        for (X509TrustManager manager : managers) {
            try {
                manager.checkClientTrusted(x509Certificates, s);
                return;
            } catch (CertificateException e) {
                exception = e;
            }
        }
        throw exception == null ? new CertificateException() : exception;
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        CertificateException exception = null;
        for (X509TrustManager manager : managers) {
            try {
                manager.checkServerTrusted(x509Certificates, s);
                return;
            } catch (CertificateException e) {
                exception = e;
            }
        }
        throw exception == null ? new CertificateException() : exception;
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        ArrayList<X509Certificate> certificates = new ArrayList<>();
        for (X509TrustManager manager : managers) {
            X509Certificate[] acceptedIssuers = manager.getAcceptedIssuers();
            if (acceptedIssuers != null) {
                for (X509Certificate issuer : acceptedIssuers) {
                    certificates.add(issuer);
                }
            }
        }
        X509Certificate[] result = new X509Certificate[certificates.size()];
        return certificates.toArray(result);
    }
}
