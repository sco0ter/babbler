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

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.Executors;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.netty.channel.nio.NioEventLoopGroup;
import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.net.ChannelEncryption;
import rocks.xmpp.core.net.client.SocketConnectionConfiguration;
import rocks.xmpp.core.net.client.TcpConnectionConfiguration;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.core.session.XmppSessionConfiguration;
import rocks.xmpp.core.session.debug.ConsoleDebugger;
import rocks.xmpp.core.stanza.model.Message;
import rocks.xmpp.extensions.compress.CompressionMethod;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.httpbind.BoshConnectionConfiguration;
import rocks.xmpp.extensions.httpbind.HttpClientConnector;
import rocks.xmpp.extensions.last.LastActivityManager;
import rocks.xmpp.extensions.ping.PingManager;
import rocks.xmpp.extensions.sm.model.StreamManagement;
import rocks.xmpp.extensions.time.EntityTimeManager;
import rocks.xmpp.im.roster.RosterManager;
import rocks.xmpp.im.roster.model.Contact;
import rocks.xmpp.nio.netty.client.NettyChannelConnector;
import rocks.xmpp.nio.netty.client.NettyTcpConnectionConfiguration;
import rocks.xmpp.websocket.net.client.JakartaWebSocketConnector;
import rocks.xmpp.websocket.net.client.WebSocketConnectionConfiguration;

/**
 * A simple example for connecting and sending a message.
 */
public final class SampleApplication {

    private static final System.Logger logger = System.getLogger(SampleApplication.class.getName());

    private SampleApplication() {
    }

    public static void main(String[] args) {

        configureLogging();

        // Create a "main application" thread, which keeps the JVM running.
        Executors.newFixedThreadPool(1).execute(() -> {
            try {
                NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(1);

                XmppSessionConfiguration configuration = XmppSessionConfiguration.builder()
                        .debugger(ConsoleDebugger.class)
                        .authenticationMechanisms("PLAIN")
                        .defaultResponseTimeout(Duration.ofSeconds(20))
                        .build();
                long now = System.currentTimeMillis();
                for (int i = 0; i < 1; i++) {

                    SocketConnectionConfiguration socketConnectionConfiguration =
                            SocketConnectionConfiguration.builder()
                                    // The hostname.
                                    .hostname("localhost")
                                    // The XMPP default port.
                                    .port(5222)
                                    // Use an SSL context, which trusts every server. Only use it for testing!
                                    .sslContext(getTrustAllSslContext())
                                    // We want to negotiate a TLS connection.
                                    .channelEncryption(ChannelEncryption.OPTIONAL)
                                    .hostnameVerifier((s, sslSession) -> true)
                                    .build();

                    BoshConnectionConfiguration boshConfiguration = BoshConnectionConfiguration.builder()
                            .hostname("localhost")
                            .port(443)
                            .path("/http-bind")
                            .compressionMethods(CompressionMethod.GZIP)
                            .sslContext(getTrustAllSslContext())
                            .channelEncryption(ChannelEncryption.DIRECT)
                            .hostnameVerifier((s, sslSession) -> true)
                            .connector(new HttpClientConnector())
                            .build();

                    System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true");
                    WebSocketConnectionConfiguration webSocketConfiguration = WebSocketConnectionConfiguration.builder()
                            .hostname("localhost")
                            .port(8443)
                            .path("/xmpp/ws")
                            .sslContext(getTrustAllSslContext())
                            .channelEncryption(ChannelEncryption.DIRECT)
                            .connector(new JakartaWebSocketConnector())
                            .hostnameVerifier((s, sslSession) -> true)
                            .build();

                    NettyTcpConnectionConfiguration nettyTcpConnectionConfiguration =
                            NettyTcpConnectionConfiguration.builder()
                                    //.hostname("localhost")
                                    .port(5222)
                                    .sslContext(getTrustAllSslContext())
                                    //.channelEncryption(ChannelEncryption.DIRECT)
                                    .hostnameVerifier((s, sslSession) -> true)
                                    .eventLoopGroup(eventLoopGroup)
                                    .build();

                    TcpConnectionConfiguration tcpConnectionConfiguration = TcpConnectionConfiguration.builder()
                            .connector(new NettyChannelConnector())
                            .port(5222)
                            .sslContext(getTrustAllSslContext())
                            .channelEncryption(ChannelEncryption.DISABLED)
                            .hostnameVerifier((s, sslSession) -> true)
                            .build();

                    XmppClient xmppClient =
                            XmppClient.create("localhost", configuration, boshConfiguration);

                    // Listen for inbound messages.
                    xmppClient.addInboundMessageListener(
                            e -> logger.log(System.Logger.Level.INFO, "Received: " + e.getMessage()));

                    // Listen for inbound presence.
                    xmppClient.addInboundPresenceListener(
                            e -> logger.log(System.Logger.Level.INFO, "Received: " + e.getPresence()));
                    xmppClient.enableFeature(StreamManagement.NAMESPACE);
                    // Connect
                    xmppClient.connect();
                    // Login
                    xmppClient.login("admin", "admin");

                    // Send a message to myself, which is caught by the listener above.
                    xmppClient.send(new Message(xmppClient.getConnectedResource(), Message.Type.CHAT,
                            "Hello World! öäü!"));
                    xmppClient.send(new Message(xmppClient.getConnectedResource(), Message.Type.CHAT,
                            "Hello World! Echo!"));
                    xmppClient.send(new Message(xmppClient.getConnectedResource(), Message.Type.CHAT,
                            "Hello World! Echo!"));
                    xmppClient.getManager(PingManager.class).pingServer().getResult();

                    OffsetDateTime entityTime =
                            xmppClient.getManager(EntityTimeManager.class).getEntityTime(null).getResult();
                    System.out.println(entityTime);

                    xmppClient.getManager(RosterManager.class)
                            .addContact(new Contact(Jid.of("admin@test"), "test", "group"), false, "").getResult();
                    xmppClient.getManager(LastActivityManager.class)
                            .getLastActivity(Jid.ofDomain(xmppClient.getConnectedResource().getDomain())).getResult();
                    xmppClient.getManager(ServiceDiscoveryManager.class).discoverInformation(xmppClient.getDomain());
                    now = System.currentTimeMillis();
                    // xmppClient.close();
                    System.out.println(System.currentTimeMillis() - now);
                    //logger.info(xmppClient.getActiveConnection().toString());
                }

            } catch (XmppException | GeneralSecurityException e) {
                throw new RuntimeException(e);
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
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(null, new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
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
