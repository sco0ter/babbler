package rocks.xmpp.core.net.client;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import javax.net.SocketFactory;

import rocks.xmpp.core.net.Connection;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.session.model.SessionOpen;

/**
 * A TCP transport connector which uses {@link Socket}.
 *
 * <p>This is the default connector for TCP based XMPP connections if none is defined.</p>
 *
 * <p>However, you could explicitly set a custom socket factory in the configuration.</p>
 *
 * <h3>Sample Usage</h3>
 *
 * <pre>{@code
 * TcpConnectionConfiguration tcpConfiguration = TcpConnectionConfiguration.builder()
 *     .hostname("localhost")
 *     .port(5222)
 *     .sslContext(sslContext)
 *     .channelEncryption(ChannelEncryption.REQUIRED)
 *     .connector(new SocketConnector(socketFactory))
 *     .build();
 * }</pre>
 *
 * @see TcpConnectionConfiguration.Builder#connector(TransportConnector)
 */
public final class SocketConnector extends AbstractTcpConnector<Socket> {

    private final SocketFactory socketFactory;

    /**
     * Creates a connector using a plain socket.
     */
    public SocketConnector() {
        this(null);
    }

    /**
     * Creates a connector using a socket factory.
     *
     * @param socketFactory The socket factory.
     */
    public SocketConnector(final SocketFactory socketFactory) {
        this.socketFactory = socketFactory;
    }

    @Override
    protected final CompletableFuture<Socket> connect(final String hostname, final int port,
                                                      final TcpConnectionConfiguration configuration) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final Socket socket;
                if (socketFactory == null) {
                    if (configuration.getProxy() != null) {
                        socket = new Socket(configuration.getProxy());
                    } else {
                        socket = new Socket();
                    }
                } else {
                    socket = socketFactory.createSocket();
                }
                // SocketFactory may return an already connected socket,
                // so check the connected state to prevent SocketException.
                if (!socket.isConnected()) {
                    socket.connect(new InetSocketAddress(hostname, configuration.getPort()),
                            configuration.getConnectTimeout());
                }
                return socket;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }, XmppStreamWriter.EXECUTOR);
    }

    @Override
    public final CompletableFuture<Connection> connect(final XmppSession xmppSession,
                                                       final TcpConnectionConfiguration configuration,
                                                       final SessionOpen sessionOpen) {
        return createConnection(xmppSession, configuration,
                (socket, config) -> new SocketConnection(socket, xmppSession, config), sessionOpen);
    }
}
