/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Christian Schudt
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.xmpp.extension.bytestreams.s5b;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A local SOCKS5 server which runs as a singleton on the local machine on port 1080 (default SOCKS port).
 *
 * @author Christian Schudt
 */
enum LocalSocks5Server {

    INSTANCE;

    private static final int DEFAULT_PORT = 1080;

    List<String> allowedAddresses = new CopyOnWriteArrayList<>();

    private ServerSocket serverSocket;

    private Map<String, Socket> socketMap = new ConcurrentHashMap<>();

    /**
     * Starts the local SOCKS5 server.
     */
    public synchronized void start() {
        if (serverSocket == null) {
            try {
                serverSocket = new ServerSocket(DEFAULT_PORT);
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (serverSocket != null) {
                            Socket socket = null;
                            try {
                                socket = serverSocket.accept();
                                socketMap.put(Socks5Protocol.establishServerConnection(socket, allowedAddresses), socket);
                            } catch (IOException e) {
                                if (socket != null) {
                                    try {
                                        socket.close();
                                    } catch (IOException e1) {
                                        // Ignore
                                    }
                                }
                            }
                        }
                    }
                });
                thread.setDaemon(true);
                thread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Stops the server.
     */
    public synchronized void stop() {
        try {
            // This will close the socket and interrupts the accept() method.
            serverSocket.close();
            serverSocket = null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the port.
     *
     * @return The port.
     */
    public synchronized int getPort() {
        return serverSocket != null ? serverSocket.getLocalPort() : DEFAULT_PORT;
    }

    /**
     * Gets the port.
     *
     * @return The port.
     */
    public String getAddress() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostAddress();
    }

    /**
     * Gets the socket for the destination address.
     *
     * @param hash The destination address.
     * @return The socket.
     */
    public Socket getSocket(String hash) {
        return socketMap.remove(hash);
    }

    void removeConnection(String hash) {
        allowedAddresses.remove(hash);
        socketMap.remove(hash);
    }
}
