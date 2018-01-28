package com.borunovv.ssl;

import java.net.Socket;

public interface Session {
    void init(Socket socket, SSLServer server);
    String handle(byte[] requestData);
    void onDisconnect();
    boolean isCompleteRequest(byte[] requestData);
}
