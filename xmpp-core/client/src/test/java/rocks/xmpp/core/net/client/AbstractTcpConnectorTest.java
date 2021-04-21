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

package rocks.xmpp.core.net.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.addr.Jid;
import rocks.xmpp.dns.DnsResolver;
import rocks.xmpp.dns.SrvRecord;

public class AbstractTcpConnectorTest {

    @Test
    @SuppressWarnings("unchecked")
    public void connectWithXmppServiceDomain() throws ExecutionException, InterruptedException, UnknownHostException {
        AbstractTcpConnector<Socket> tcpConnector = Mockito.spy(AbstractTcpConnector.class);

        try (MockedStatic<DnsResolver> mockedStatic = Mockito.mockStatic(DnsResolver.class)) {
            mockedStatic
                    .when(() -> DnsResolver
                            .resolveSRV(Mockito.eq("xmpp-client"), Mockito.any(), Mockito.any(), Mockito.anyLong()))
                    .thenReturn(List.of(
                            new SrvRecord(1, 1, 5222, "host1"),
                            new SrvRecord(2, 2, 5222, "host2"),
                            new SrvRecord(3, 3, 5222, "host3"),
                            new SrvRecord(4, 4, 5222, "host4")));
            mockedStatic
                    .when(() -> DnsResolver
                            .resolveSRV(Mockito.eq("xmpps-client"), Mockito.any(), Mockito.any(), Mockito.anyLong()))
                    .thenReturn(List.of(
                            new SrvRecord(1, 1, 5222, "host5")));

            Mockito.when(tcpConnector.connect(Mockito.eq("host1"), Mockito.anyInt(), Mockito.any()))
                    .thenReturn(CompletableFuture.failedFuture(new IOException()));
            Mockito.when(tcpConnector.connect(Mockito.eq("host2"), Mockito.anyInt(), Mockito.any()))
                    .thenReturn(CompletableFuture.failedFuture(new IOException()));
            Mockito.when(tcpConnector.connect(Mockito.eq("host5"), Mockito.anyInt(), Mockito.any()))
                    .thenReturn(CompletableFuture.failedFuture(new IOException()));

            Mockito.when(tcpConnector.connect(Mockito.eq("host3"), Mockito.anyInt(), Mockito.any()))
                    .thenReturn(CompletableFuture.failedFuture(new IOException()));
            Mockito.when(tcpConnector.connect(Mockito.eq("host4"), Mockito.anyInt(), Mockito.any()))
                    .thenReturn(CompletableFuture.failedFuture(new IOException()));

            Socket socket3 = Mockito.mock(Socket.class);
            Mockito.when(socket3.getInetAddress())
                    .thenReturn(InetAddress.getByAddress("host3", new byte[]{0, 0, 0, 0}));
            Mockito.when(tcpConnector.connect(Mockito.eq("host3"), Mockito.anyInt(), Mockito.any()))
                    .thenReturn(CompletableFuture.completedFuture(socket3));

            Socket socket4 = Mockito.mock(Socket.class);
            Mockito.when(socket4.getInetAddress())
                    .thenReturn(InetAddress.getByAddress("host4", new byte[]{0, 0, 0, 0}));
            Mockito.when(tcpConnector.connect(Mockito.eq("host4"), Mockito.anyInt(), Mockito.any()))
                    .thenReturn(CompletableFuture.completedFuture(socket4));

            CompletableFuture<Socket> future = tcpConnector
                    .connectWithXmppServiceDomain(Jid.of("test"), TcpConnectionConfiguration.builder().build(),
                            "0.0.0.0",
                            isDirectTls -> {
                            });
            Socket usedSocket = future.get();
            Assert.assertNotNull(usedSocket);
            Assert.assertEquals(usedSocket.getInetAddress().getHostName(), "host3");
        }
    }
}
