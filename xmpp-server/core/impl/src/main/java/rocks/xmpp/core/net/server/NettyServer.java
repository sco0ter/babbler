/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2018 Christian Schudt
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

package rocks.xmpp.core.net.server;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.util.Collections;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import rocks.xmpp.core.extensions.compress.server.CompressionNegotiator;
import rocks.xmpp.core.net.ChannelEncryption;
import rocks.xmpp.core.net.ConnectionConfiguration;
import rocks.xmpp.core.net.TcpBinding;
import rocks.xmpp.core.server.ServerConfiguration;
import rocks.xmpp.core.tls.server.StartTlsNegotiator;
import rocks.xmpp.nio.netty.net.NettyChannelConnection;
import rocks.xmpp.session.server.InboundClientSession;

/**
 * @author Christian Schudt
 */
@ApplicationScoped
public class NettyServer {

    private static final SSLContext SSL_CONTEXT;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    private Channel channel;

    private Instant startTime;

    @Inject
    private ServerConfiguration serverConfiguration;

    static {
        try {
            SelfSignedCertificate ssc = new SelfSignedCertificate();

            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null);
            ks.setKeyEntry("cert", ssc.key(), new char[0], new Certificate[]{ssc.cert()});

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, new char[0]);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), null, null);
            SSL_CONTEXT = sslContext;

        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException
                | IOException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }


    public void start(@Observes @Initialized(ApplicationScoped.class) Object context) {
        this.startTime = Instant.now();

        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();

        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {

                        final InboundClientSession session = CDI.current().select(InboundClientSession.class).get();

                        // Create a new connection for the client.
                        final TcpBinding connection =
                                new NettyChannelConnection(ch, session, null, serverConfiguration::getUnmarshaller,
                                        Collections.emptyList(), serverConfiguration::getMarshaller, null,
                                        new ConnectionConfiguration() {
                                            @Override
                                            public ChannelEncryption getChannelEncryption() {
                                                return ChannelEncryption.DIRECT;
                                            }

                                            @Override
                                            public SSLContext getSSLContext() {
                                                return SSL_CONTEXT;
                                            }
                                        });
                        // Create a new session for the new client connection.

                        session.setConnection(connection);
                        session.getStreamFeatureManager()
                                .registerStreamFeatureProvider(new StartTlsNegotiator(connection));
                        session.getStreamFeatureManager()
                                .registerStreamFeatureProvider(new CompressionNegotiator(connection));
                        ch.pipeline().addLast(new InboundXmppHandler(session));
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        // Bind and start to accept incoming connections.
        channel = b.bind(serverConfiguration.getPort()).channel();
    }

    public void stop(@Observes @Destroyed(ApplicationScoped.class) Object context) {
        channel.close();
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

    public Instant getStartTime() {
        return startTime;
    }
}