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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import javax.net.ssl.HttpsURLConnection;

import rocks.xmpp.core.net.WriterInterceptorChain;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stream.model.StreamErrorException;
import rocks.xmpp.extensions.compress.CompressionMethod;
import rocks.xmpp.extensions.httpbind.model.Body;

/**
 * BOSH connection which uses {@link java.net.HttpURLConnection} as transport implementation.
 *
 * @author Christian Schudt
 */
final class HttpUrlBoshConnection extends BoshConnection {

    HttpUrlBoshConnection(URL url, XmppSession xmppSession,
                          BoshConnectionConfiguration configuration) {
        super(url, xmppSession, configuration);
    }

    @Override
    public CompletableFuture<Void> sendBody(Body body) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpURLConnection httpConnection;
                httpConnection = getConnection();
                httpConnection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");

                // We can decompress server responses, so tell the server about it.
                if (clientAcceptEncoding != null) {
                    httpConnection.setRequestProperty("Accept-Encoding", clientAcceptEncoding);
                }

                final CompressionMethod compressionMethod;

                compressionMethod = requestCompressionMethod;

                // If we can compress, tell the server about it.
                if (compressionMethod != null) {
                    httpConnection.setRequestProperty("Content-Encoding", compressionMethod.getName());
                }

                httpConnection.setDoOutput(true);
                httpConnection.setRequestMethod("POST");
                // If the connection manager does not respond in time, throw a SocketTimeoutException, which
                // terminates the connection.
                httpConnection
                        .setReadTimeout(((int) boshConnectionConfiguration.getWait().getSeconds() + 5) * 1000);

                try (OutputStream requestStream = compressionMethod != null
                        ? compressionMethod.compress(httpConnection.getOutputStream())
                        : httpConnection.getOutputStream()) {
                    // Create the writer for this connection.
                    // Then write the XML to the output stream by marshalling the object to the writer.
                    // Marshaller needs to be recreated here, because it's not thread-safe.
                    try (Writer writer = new OutputStreamWriter(requestStream, StandardCharsets.UTF_8)) {
                        WriterInterceptorChain writerInterceptorChain = newWriterChain();
                        writerInterceptorChain.proceed(body, writer);
                    }
                }
                return httpConnection;
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, inOrderRequestExecutor).thenApplyAsync(httpConnection -> {
            try {
                // Wait for the response
                if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                    String contentEncoding = httpConnection.getHeaderField("Content-Encoding");
                    try (InputStream responseStream = contentEncoding != null
                            ? compressionMethods.get(contentEncoding)
                            .decompress(httpConnection.getInputStream())
                            : httpConnection.getInputStream()) {

                        try (Reader reader = new InputStreamReader(responseStream, UTF_8)) {
                            handleSuccessfulResponse(reader, body);
                        } catch (StreamErrorException e) {
                            logger.log(System.Logger.Level.WARNING, "Server responded with malformed XML.", e);
                        }
                    }
                    // Wait shortly before sending the long polling request.
                    // This allows the send method to chime in and send a <body/> with actual payload
                    // instead of an empty body just to "hold the line".
                    Thread.sleep(50);
                } else {
                    // Shutdown the connection, we don't want to send further requests from now on.
                    handleErrorHttpResponse(httpConnection.getResponseCode());
                    try (InputStream errorStream = httpConnection.getErrorStream()) {
                        while (errorStream.read() > -1) {
                            // Just read the error stream, so that the connection can be reused.
                            // http://docs.oracle.com/javase/8/docs/technotes/guides/net/http-keepalive.html
                        }
                    }
                }
            } catch (Exception e) {
                throw new CompletionException(e);
            }
            return httpConnection;
        }, HTTP_BIND_EXECUTOR).handle((httpConnection, exc) -> {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
            if (exc != null) {
                throw exc instanceof CompletionException ? (CompletionException) exc : new CompletionException(exc);
            }
            return null;
        });
    }

    private HttpURLConnection getConnection() throws IOException {
        Proxy proxy = boshConnectionConfiguration.getProxy();
        HttpURLConnection httpURLConnection;
        if (proxy != null) {
            httpURLConnection = (HttpURLConnection) url.openConnection(proxy);
        } else {
            httpURLConnection = (HttpURLConnection) url.openConnection();
        }
        if (httpURLConnection instanceof HttpsURLConnection) {
            if (boshConnectionConfiguration.getSSLContext() != null) {
                ((HttpsURLConnection) httpURLConnection)
                        .setSSLSocketFactory(boshConnectionConfiguration.getSSLContext().getSocketFactory());
            }
            if (boshConnectionConfiguration.getHostnameVerifier() != null) {
                ((HttpsURLConnection) httpURLConnection)
                        .setHostnameVerifier(boshConnectionConfiguration.getHostnameVerifier());
            }
        }
        return httpURLConnection;
    }

    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder("BOSH connection to ").append(url);
        String streamId = getStreamId();
        if (streamId != null) {
            sb.append(" (").append(streamId).append(')');
        }
        sb.append(" using java.net.HttpURLConnection");
        return sb.toString();
    }
}
