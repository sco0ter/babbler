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

package rocks.xmpp.sample.filetransfer;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.concurrent.Executors;

import rocks.xmpp.core.net.ChannelEncryption;
import rocks.xmpp.core.net.client.TcpConnectionConfiguration;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.core.session.XmppSessionConfiguration;
import rocks.xmpp.debug.gui.VisualDebugger;
import rocks.xmpp.extensions.filetransfer.FileTransfer;
import rocks.xmpp.extensions.filetransfer.FileTransferManager;

/**
 * @author Christian Schudt
 */
public final class FileTransferSender {

    private FileTransferSender() {
    }

    public static void main(String[] args) {

        Executors.newFixedThreadPool(1).execute(() -> {
            try {

                TcpConnectionConfiguration tcpConfiguration = TcpConnectionConfiguration.builder()
                        .port(5222)
                        .channelEncryption(ChannelEncryption.DISABLED)
                        .build();

                XmppSessionConfiguration configuration = XmppSessionConfiguration.builder()
                        .debugger(VisualDebugger.class)
                        .build();

                XmppClient xmppSession = XmppClient.create("localhost", configuration, tcpConfiguration);

                // Connect
                xmppSession.connect();
                // Login
                xmppSession.login("111", "111", "filetransfer");

                FileTransferManager fileTransferManager = xmppSession.getManager(FileTransferManager.class);
                FileTransfer fileTransfer = fileTransferManager.offerFile(Paths.get("info.png"), "Description",
                        xmppSession.getDomain().withLocal("222").withResource("filetransfer"), Duration.ofSeconds(5))
                        .getResult();
                fileTransfer.transfer();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
