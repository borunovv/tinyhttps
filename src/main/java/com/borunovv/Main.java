package com.borunovv;

import com.borunovv.http.HTTPSession;
import com.borunovv.ssl.SSLServer;

/**
 * Entry point.
 * <p>
 * Start server on port 9096.
 * You can check it via https://localhost:9096
 * To stop the server gracefully visit this URL: https://localhost:9096/stop
 * <p>
 * Note1: ensure the 'keystore.jks' file exists in dir ./keystore/ relative to current dir.
 * To generate 'self-signed' certificate for SSL into keystore.jks file use following command:
 * keytool -genkey -keyalg RSA -alias selfsigned -keystore keystore.jks -storepass password -validity 10000 -keysize 2048
 * <p>
 * (keytool is a standard java key generation tool from JAVA_HOME/bin directory)
 * <p>
 * Note2: browser will prompt you about insecure certificate.
 * It's ok because we use 'self-signed' certificate for development purposes only.
 * For production it is need to obtain real certificate.
 */
public class Main {

    private static final int PORT = 9096;

    public static void main(String[] args) throws Exception {
        System.out.println("Server started on port " + PORT);
        System.out.println("To check it's work visit URL: https://localhost:9096/hello?world=1");
        System.out.println("To stop the server visit URL: https://localhost:9096/stop");

        new SSLServer(PORT,
                "./keystore/keystore.jks",
                "password",
                HTTPSession::new)
                .start();

        System.out.println("Server stopped");
    }
}
