/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Christian Schudt
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
import rocks.xmpp.core.roster.RosterManager;
import rocks.xmpp.core.session.TcpConnectionConfiguration;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.session.XmppSessionConfiguration;
import rocks.xmpp.debug.gui.VisualDebugger;
import rocks.xmpp.extensions.compress.CompressionManager;
import rocks.xmpp.extensions.disco.model.items.ItemDiscovery;
import rocks.xmpp.extensions.httpbind.BoshConnectionConfiguration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Christian Schudt
 */
public class SampleApplication {

    public static void main(String[] args) throws IOException {

        Executors.newFixedThreadPool(1).execute(() -> {
            try {

                Handler consoleHandler = new ConsoleHandler();
                consoleHandler.setLevel(Level.FINE);
                consoleHandler.setFormatter(new LogFormatter());

                final Logger logger = Logger.getLogger("rocks.xmpp");
                logger.addHandler(consoleHandler);

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

                TcpConnectionConfiguration tcpConfiguration = TcpConnectionConfiguration.builder()
                        .port(5222)
                        .sslContext(sslContext)
                        .compressionMethods(CompressionManager.ZLIB)
                        .secure(true)
                        .build();


                BoshConnectionConfiguration boshConnectionConfiguration = BoshConnectionConfiguration.builder()
                        .hostname("localhost")
                        .port(7070)
                                //.secure(true)
                                //.sslContext(sslContext)
                        .hostnameVerifier((s, sslSession) -> true)
                        .file("/http-bind/")
                        .build();

                Class<?>[] extensions = new Class<?>[0];
                Arrays.asList(extensions, XmppSession.class);
                XmppSessionConfiguration configuration = XmppSessionConfiguration.builder()
                        .debugger(VisualDebugger.class)
                        .defaultResponseTimeout(5000)
                        .build();

                XmppSession xmppSession = new XmppSession("localhost", configuration, tcpConfiguration);

                // Listen for inbound messages.
                xmppSession.addInboundMessageListener(e -> System.out.println(e.getMessage()));
                // Connect
                xmppSession.connect();
                // Login
                xmppSession.login("admin", "admin", "xmpp");

                xmppSession.disableFeature(ItemDiscovery.NAMESPACE);

                System.out.println(xmppSession.getActiveConnection());
            } catch (XmppException | NoSuchAlgorithmException | KeyManagementException e) {
                e.printStackTrace();
            }
        });
    }
}
