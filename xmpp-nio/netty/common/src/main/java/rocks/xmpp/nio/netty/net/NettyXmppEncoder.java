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

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import rocks.xmpp.core.net.WriterInterceptor;
import rocks.xmpp.core.net.WriterInterceptorChain;
import rocks.xmpp.core.stream.model.StreamElement;

/**
 * Encodes stream elements to byte buffers.
 * <p>
 * This class should be added to Netty's channel pipeline.
 *
 * @author Christian Schudt
 */
final class NettyXmppEncoder extends MessageToByteEncoder<StreamElement> {

    private final Iterable<WriterInterceptor> writerInterceptors;

    private final Consumer<Throwable> onFailure;

    /**
     * @param writerInterceptors The writer interceptors.
     * @param onFailure          Called when an exception in the pipeline has occurred. If null, the exception is propagated to next handler. If non-null this callback is called instead.
     */
    NettyXmppEncoder(final Iterable<WriterInterceptor> writerInterceptors, final Consumer<Throwable> onFailure) {
        this.writerInterceptors = writerInterceptors;
        this.onFailure = onFailure;
    }

    @Override
    protected final void encode(final ChannelHandlerContext ctx, final StreamElement streamElement, final ByteBuf byteBuf) throws Exception {
        try (Writer writer = new OutputStreamWriter(new ByteBufOutputStream(byteBuf), StandardCharsets.UTF_8)) {
            WriterInterceptorChain chain = new WriterInterceptorChain(writerInterceptors);
            chain.proceed(streamElement, writer);
        }
    }

    @Override
    public final void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        if (onFailure != null) {
            onFailure.accept(cause);
        } else {
            super.exceptionCaught(ctx, cause);
        }
    }
}
