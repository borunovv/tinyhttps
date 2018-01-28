package com.borunovv.http;

import com.borunovv.ssl.SSLServer;
import com.borunovv.ssl.Session;

import java.net.InetAddress;
import java.net.Socket;

public class HTTPSession implements Session {
    private InetAddress addr;
    private SSLServer server;

    public HTTPSession() {

    }

    @Override
    public void init(Socket socket, SSLServer server) {
        this.addr = socket.getInetAddress();
        this.server = server;
        System.out.printf("Client connected: %s\n", addr.toString());
    }

    @Override
    public String handle(byte[] requestAsBytes) {
        HTTPRequest httpRequest = HTTPRequest.unmarshall(requestAsBytes);
        System.out.println(httpRequest);

        HTTPResponse httpResponse = getResponse(httpRequest);

        System.out.println("Response:" + " '" + httpResponse + "'");
        return httpResponse.marshall();
    }

    @Override
    public void onDisconnect() {
        System.out.printf("Client closed: %s\n", addr.toString());
    }

    @Override
    public boolean isCompleteRequest(byte[] requestData) {
        return HTTPRequest.tryUnmarshall(requestData) != null;
    }

    private HTTPResponse getResponse(HTTPRequest request) {
        if (request.getUrl().equals("/stop")) {
            server.stop();
            return HTTPResponse.plainText("Server stopped");
        }

        return HTTPResponse.plainText("Hello, url: '" + request.getUrl() + "'");
    }
}
