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

package rocks.xmpp.sample.filetransfer;

import rocks.xmpp.core.session.TcpConnectionConfiguration;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.session.XmppSessionConfiguration;
import rocks.xmpp.core.stanza.model.client.Presence;
import rocks.xmpp.debug.gui.VisualDebugger;
import rocks.xmpp.extensions.filetransfer.FileTransfer;
import rocks.xmpp.extensions.filetransfer.FileTransferManager;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.Executors;

/**
 * @author Christian Schudt
 */
public class FileTransferReceiver {

    public static void main(String[] args) throws IOException {

        Executors.newFixedThreadPool(1).execute(() -> {
            try {

                TcpConnectionConfiguration tcpConfiguration = TcpConnectionConfiguration.builder()
                        .port(5222)
                        .secure(false)
                        .build();

                XmppSessionConfiguration configuration = XmppSessionConfiguration.builder()
                        .debugger(VisualDebugger.class)
                        .defaultResponseTimeout(5000)
                        .build();

                XmppSession xmppSession = new XmppSession("localhost", configuration, tcpConfiguration);

                // Connect
                xmppSession.connect();
                // Login
                xmppSession.login("222", "222", "filetransfer");
                // Send initial presence
                xmppSession.send(new Presence());

                FileTransferManager fileTransferManager = xmppSession.getManager(FileTransferManager.class);
                fileTransferManager.addFileTransferOfferListener(e -> {
                    try {
                        FileTransfer fileTransfer = e.accept(Paths.get("test.png"));
                        fileTransfer.transfer();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
