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

package rocks.xmpp.sample.customiq;

import rocks.xmpp.core.Jid;
import rocks.xmpp.core.session.TcpConnectionConfiguration;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.session.XmppSessionConfiguration;
import rocks.xmpp.core.session.context.extensions.ExtensionContext;
import rocks.xmpp.core.session.debug.ConsoleDebugger;
import rocks.xmpp.core.stanza.model.AbstractIQ;
import rocks.xmpp.core.stanza.model.client.IQ;

import java.io.IOException;
import java.util.concurrent.Executors;

/**
 * @author Christian Schudt
 */
public class CustomIQHandlerRequester {

    public static void main(String[] args) throws IOException {

        Executors.newFixedThreadPool(1).execute(() -> {
            try {

                TcpConnectionConfiguration tcpConfiguration = TcpConnectionConfiguration.builder()
                        .port(5222)
                        .secure(false)
                        .build();

                XmppSessionConfiguration configuration = XmppSessionConfiguration.builder()
                        .debugger(ConsoleDebugger.class)
                                // This registers the custom IQ payload to the JAXB context.
                        .context(new ExtensionContext(Addition.class))
                        .build();

                XmppSession xmppSession = new XmppSession("localhost", configuration, tcpConfiguration);

                // Connect
                xmppSession.connect();
                // Login
                xmppSession.login("222", "222", "iq");

                Addition addition = new Addition(52, 22);
                System.out.println("Requesting: " + addition);
                // Request the sum of two values (52 + 22). 111 will calculate it for you and return a result.
                IQ resultIQ = xmppSession.query(new IQ(new Jid("111", xmppSession.getDomain(), "iq"), AbstractIQ.Type.GET, addition));

                // Print the result.
                System.out.println(resultIQ.getExtension(Addition.class));

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
