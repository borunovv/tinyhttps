package com.borunovv.ssl;

import javax.net.ssl.*;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Simple SSL Server
 * <p>
 * Note: for development reasons to generate selfsigned certificate
 * for SSL into keystore.jks file use following command:
 * <p>
 * keytool -genkey -keyalg RSA -alias selfsigned -keystore keystore.jks -storepass password -validity 10000 -keysize 2048
 * <p>
 * (keytool is a standard java key generation tool in JAVA_HOME/bin directory)
 */
public class SSLServer {

    private final int port;
    private final String keyStoreFilePath;
    private final String keyStorePassword;
    private final SessionFactory sessionFactory;

    private volatile boolean stopRequested = false;

    /**
     * To generate selfsigned certificate for SSL into keystore.jks file use following command:
     * keytool -genkey -keyalg RSA -alias selfsigned -keystore keystore.jks -storepass password -validity 10000 -keysize 2048
     * <p>
     * (keytool is a standard java key generation tool in JAVA_HOME/bin directory)
     *
     * @param port             server listening port
     * @param keyStoreFilePath path to keystore.jks
     * @param keyStorePassword password for keystore.jks
     */
    public SSLServer(int port, String keyStoreFilePath, String keyStorePassword, SessionFactory sessionFactory) {
        this.port = port;
        this.keyStoreFilePath = keyStoreFilePath;
        this.keyStorePassword = keyStorePassword;
        this.sessionFactory = sessionFactory;
    }

    public void start() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        SSLServerSocket serverSocket = createSSLServerSocket();
        serverSocket.setSoTimeout(1000); // Need for periodically check stopRequested flag.

        while (!stopRequested) {
            try {
                Socket client = serverSocket.accept();
                startNewSession(client);
            } catch (SocketTimeoutException ignore) {
            }
        }
    }

    private SSLServerSocket createSSLServerSocket() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, UnrecoverableKeyException, KeyManagementException {
        KeyManagerFactory kmf = getKeyManagerFactory();
        SSLContext sc = getSSLContext(kmf);
        SSLServerSocket ssl = (SSLServerSocket) sc.getServerSocketFactory().createServerSocket(this.port);
        ssl.setEnabledProtocols(new String[]{"TLSv1", "TLSv1.1", "TLSv1.2", "SSLv3"});
        return ssl;
    }

    public void stop() {
        stopRequested = true;
    }

    private void startNewSession(Socket sock) {
        Session session = sessionFactory.getSession();
        session.init(sock, this);
        new SSLServerSession(sock, session).start();
    }

    private KeyManagerFactory getKeyManagerFactory() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, UnrecoverableKeyException {
        return getKeyManagerFactory(loadKeyStoreFromFile(this.keyStoreFilePath, this.keyStorePassword), this.keyStorePassword);
    }

    private KeyManagerFactory getKeyManagerFactory(KeyStore ks, String keyPassword) throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException {
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, keyPassword.toCharArray());
        return kmf;
    }

    private KeyStore loadKeyStoreFromFile(String path, String password) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException {
        return loadKeyStore(new BufferedInputStream(new FileInputStream(path)), password);
    }

    private KeyStore loadKeyStore(InputStream ksIs, String password) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        KeyStore ks = KeyStore.getInstance("JKS");
        try {
            ks.load(ksIs, password.toCharArray());
        } finally {
            if (ksIs != null) {
                ksIs.close();
            }
        }
        return ks;
    }

    private SSLContext getSSLContext(KeyManagerFactory kmf) throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sc = SSLContext.getInstance("SSL");
        TrustManager[] trustAllCerts = {getSimpleTrustManager()};
        sc.init(kmf.getKeyManagers(), trustAllCerts, new SecureRandom());
        return sc;
    }

    private TrustManager getSimpleTrustManager() {
        return new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                return;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                return;
            }
        };
    }
}
