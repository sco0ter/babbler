/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Christian Schudt
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

package rocks.xmpp.sample;

import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.TcpConnectionConfiguration;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.core.session.XmppSessionConfiguration;
import rocks.xmpp.core.session.debug.ConsoleDebugger;
import rocks.xmpp.core.stanza.model.Message;
import rocks.xmpp.extensions.httpbind.BoshConnectionConfiguration;
import rocks.xmpp.websocket.WebSocketConnectionConfiguration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.Executors;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * A simple example for connecting and sending a message.
 */
public class SampleApplication {

    private static Logger logger = Logger.getLogger(SampleApplication.class.getName());

    public static void main(String[] args) throws IOException {

        configureLogging();

        // Create a "main application" thread, which keeps the JVM running.
        Executors.newFixedThreadPool(1).execute(() -> {
            try {

                TcpConnectionConfiguration tcpConfiguration = TcpConnectionConfiguration.builder()
                        .hostname("localhost") // The hostname.
                        .port(5222) // The XMPP default port.
                        .sslContext(getTrustAllSslContext()) // Use an SSL context, which trusts every server. Only use it for testing!
                        .secure(false) // We want to negotiate a TLS connection.
                        .build();

                BoshConnectionConfiguration boshConfiguration = BoshConnectionConfiguration.builder()
                        .hostname("localhost")
                        .port(7070)
                                //.sslContext(getTrustAllSslContext())
                        .secure(false)
                        .build();
                WebSocketConnectionConfiguration webSocketConfiguration = WebSocketConnectionConfiguration.builder()
                        .hostname("localhost")
                        .port(7070)
                                //.sslContext(getTrustAllSslContext())
                        .secure(false)
                        .build();
                XmppSessionConfiguration configuration = XmppSessionConfiguration.builder()
                        .debugger(ConsoleDebugger.class)
                        .build();

                XmppClient xmppClient = XmppClient.create("localhost", configuration, tcpConfiguration);

                // Listen for inbound messages.
                xmppClient.addInboundMessageListener(e -> logger.info("Received: " + e.getMessage()));

                // Listen for inbound presence.
                xmppClient.addInboundPresenceListener(e -> logger.info("Received: " + e.getPresence()));

                // Connect
                xmppClient.connect();
                // Login
                xmppClient.login("admin", "admin", "xmpp");

                // Send a message to myself, which is caught by the listener above.
                xmppClient.send(new Message(xmppClient.getConnectedResource(), Message.Type.CHAT, "Hello World! Echo!"));

                logger.info(xmppClient.getActiveConnection().toString());
            } catch (XmppException | GeneralSecurityException e) {
                e.printStackTrace();
            }
        });
    }

    protected static void configureLogging() {

        LogManager.getLogManager().reset();

        // Log everything from the rocks.xmpp package with level FINE or above to the console.

        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.FINE);
        consoleHandler.setFormatter(new LogFormatter());

        Logger logger = Logger.getLogger("rocks.xmpp");
        logger.setLevel(Level.FINE);
        logger.addHandler(consoleHandler);
    }

    protected static SSLContext getTrustAllSslContext() throws GeneralSecurityException {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
        }, new SecureRandom());
        return sslContext;
    }
}
