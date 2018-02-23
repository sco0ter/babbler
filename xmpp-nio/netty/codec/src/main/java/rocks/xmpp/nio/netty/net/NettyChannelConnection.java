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

package rocks.xmpp.nio.netty.net;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import rocks.xmpp.core.net.AbstractConnection;
import rocks.xmpp.core.net.ConnectionConfiguration;
import rocks.xmpp.core.session.model.SessionOpen;
import rocks.xmpp.core.stream.model.StreamElement;
import rocks.xmpp.core.stream.model.StreamHeader;
import rocks.xmpp.nio.netty.codec.NettyXmppDecoder;
import rocks.xmpp.nio.netty.codec.NettyXmppEncoder;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A NIO connection based on Netty.
 *
 * @author Christian Schudt
 */
public class NettyChannelConnection extends AbstractConnection {

    protected final Channel channel;

    private final NettyXmppDecoder decoder;

    private final BiConsumer<String, StreamElement> onRead;

    protected SessionOpen sessionOpen;

    public NettyChannelConnection(final Channel channel,
                                  final BiConsumer<String, StreamElement> onRead,
                                  final Supplier<Unmarshaller> unmarshallerSupplier,
                                  final BiConsumer<String, StreamElement> onWrite,
                                  final Supplier<Marshaller> marshallerSupplier,
                                  final Consumer<Throwable> onException,
                                  final ConnectionConfiguration connectionConfiguration) {
        super(connectionConfiguration);
        this.channel = channel;
        this.onRead = onRead;
        this.decoder = new NettyXmppDecoder(this::onRead, unmarshallerSupplier, onException);
        channel.pipeline().addLast(decoder, new NettyXmppEncoder(onWrite, marshallerSupplier, onException));
    }

    private CompletionStage<Void> write(final StreamElement streamElement, final Function<StreamElement, ChannelFuture> writeFunction) {
        final CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        if (!isClosed() || streamElement == StreamHeader.CLOSING_STREAM_TAG) {
            writeFunction.apply(streamElement).addListener(future -> {
                if (future.isSuccess()) {
                    completableFuture.complete(null);
                } else {
                    completableFuture.completeExceptionally(future.cause());
                }
            });
        } else {
            completableFuture.completeExceptionally(new IllegalStateException("Connection closed"));
        }
        return completableFuture;
    }

    protected void onRead(final String xml, final StreamElement streamElement) {
        if (onRead != null) {
            onRead.accept(xml, streamElement);
        }
        if (streamElement instanceof SessionOpen) {
            openedByPeer((SessionOpen) streamElement);
        } else if (streamElement == StreamHeader.CLOSING_STREAM_TAG) {
            closedByPeer();
        }
    }

    @Override
    public final CompletionStage<Void> open(final SessionOpen sessionOpen) {
        this.sessionOpen = sessionOpen;
        return send(sessionOpen);
    }

    @Override
    public final CompletionStage<Void> send(final StreamElement streamElement) {
        return write(streamElement, channel::writeAndFlush);
    }

    @Override
    public final CompletionStage<Void> write(final StreamElement streamElement) {
        return write(streamElement, channel::write);
    }

    @Override
    public final void flush() {
        channel.flush();
    }

    public void secure() {
    }

    public boolean compress(String method, Runnable onSuccess, Runnable onUnsupported) {
        return false;
    }

    @Override
    public final boolean isSecure() {
        return channel.pipeline().toMap().containsKey("SSL");
    }

    @Override
    public void restartStream() {
        decoder.restart();
    }

    @Override
    protected final CompletionStage<Void> closeStream() {
        return send(StreamHeader.CLOSING_STREAM_TAG);
    }

    @Override
    protected final CompletionStage<Void> closeConnection() {
        final CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        channel.close().addListener(future -> {
            if (future.isSuccess()) {
                completableFuture.complete(null);
            } else {
                completableFuture.completeExceptionally(future.cause());
            }
        });
        return completableFuture;
    }
}
