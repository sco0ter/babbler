/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2021 Christian Schudt
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

package rocks.xmpp.extensions.httpbind;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.extensions.compress.CompressionMethod;
import rocks.xmpp.extensions.httpbind.model.Body;

/**
 * BOSH connection which uses {@link java.net.http.HttpClient} as transport implementation.
 *
 * @author Christian Schudt
 */
final class HttpClientBoshConnection extends BoshConnection {

    private final HttpClient httpClient;

    HttpClientBoshConnection(URL url, XmppSession xmppSession,
                             BoshConnectionConfiguration configuration) {
        super(url, xmppSession, configuration);
        httpClient = HttpClientConnector.newHttpClientBuilder(boshConnectionConfiguration)
                .executor(inOrderRequestExecutor)
                .build();
    }

    @Override
    public CompletableFuture<Void> sendBody(Body body) {

        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(url.toURI())
                    .header("Content-Type", "text/xml; charset=utf-8");

            // We can decompress server responses, so tell the server about it.
            if (clientAcceptEncoding != null) {
                builder.header("Accept-Encoding", clientAcceptEncoding);
            }

            final CompressionMethod compressionMethod;
            synchronized (this) {
                compressionMethod = requestCompressionMethod;
            }
            // If we can compress, tell the server about it.
            if (compressionMethod != null) {
                builder.header("Content-Encoding", compressionMethod.getName());
            }

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                try (OutputStream requestStream = compressionMethod != null
                        ? compressionMethod.compress(outputStream)
                        : outputStream) {

                    try (Writer writer = new OutputStreamWriter(requestStream, StandardCharsets.UTF_8)) {
                        newWriterChain().proceed(body, writer);
                        return httpClient.sendAsync(
                                builder.POST(HttpRequest.BodyPublishers.ofByteArray(outputStream.toByteArray()))
                                        .build(), HttpResponse.BodyHandlers.ofInputStream())
                                .thenAcceptAsync(httpResponse -> {
                                    try {
                                        if (httpResponse.statusCode() == 200) {

                                            String contentEncoding =
                                                    httpResponse.headers().firstValue("Content-Encoding").orElse(null);
                                            try (InputStream responseStream = contentEncoding != null
                                                    ? compressionMethods.get(contentEncoding)
                                                    .decompress(httpResponse.body())
                                                    : httpResponse.body()) {

                                                try (Reader reader = new InputStreamReader(responseStream, UTF_8)) {
                                                    handleSuccessfulResponse(reader, body);
                                                }
                                            }
                                        } else {
                                            handleErrorHttpResponse(httpResponse.statusCode());
                                        }
                                    } catch (Exception e) {
                                        throw new CompletionException(e);
                                    }
                                }, inOrderResponseExecutor);
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder("BOSH connection to ").append(url);
        String streamId = getStreamId();
        if (streamId != null) {
            sb.append(" (").append(streamId).append(')');
        }
        sb.append(" using java.net.http.HttpClient");
        return sb.toString();
    }
}
