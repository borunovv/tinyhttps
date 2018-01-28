package com.borunovv.ssl;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

class SSLServerSession extends Thread {
    private final Socket sock;
    private final Session handler;

    SSLServerSession(Socket s, Session handler) {
        this.sock = s;
        this.handler = handler;
    }

    @Override
    public void run() {
        InetAddress addr;
        try {
            addr = sock.getInetAddress();
            BufferedInputStream br = new BufferedInputStream(sock.getInputStream());
            PrintWriter pw = new PrintWriter(sock.getOutputStream());
            sock.setSoTimeout(5000);

            ByteArrayOutputStream request = new ByteArrayOutputStream();
            byte[] buff = new byte[4028];
            while (true) {
                int count = br.read(buff);
                if (count == -1) {
                    break;
                } else if (count > 0) {
                    request.write(buff, 0, count);
                    if (handler.isCompleteRequest(request.toByteArray())) {
                        break;
                    }
                }
            }

            if (request.size() > 0) {
                String response = handler.handle(request.toByteArray());
                pw.print(response);
                pw.flush();
            } else {
                // Empty request come (it seems you are using Chrome browser. It is using extra pending connection).
                // We will close it.
            }
            pw.close();
            sock.close();
            System.out.printf("Close client %s\n", addr.toString());
        } catch (SocketTimeoutException e) {
            // Client read timeout. Possibly this was a the Chrome's browser extra pending connection.
        } catch (IOException ioe) {
            // Client disconnected
            handler.onDisconnect();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}