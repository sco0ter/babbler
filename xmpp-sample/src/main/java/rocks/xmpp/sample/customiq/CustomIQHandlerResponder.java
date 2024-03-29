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

package rocks.xmpp.sample.customiq;

import java.util.concurrent.Executors;

import rocks.xmpp.core.net.ChannelEncryption;
import rocks.xmpp.core.net.client.TcpConnectionConfiguration;
import rocks.xmpp.core.session.Extension;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.core.session.XmppSessionConfiguration;
import rocks.xmpp.core.session.debug.ConsoleDebugger;
import rocks.xmpp.core.stanza.IQHandler;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.StanzaError;
import rocks.xmpp.core.stanza.model.errors.Condition;

/**
 * @author Christian Schudt
 */
public final class CustomIQHandlerResponder {

    private CustomIQHandlerResponder() {
    }

    public static void main(String[] args) {

        Executors.newFixedThreadPool(1).execute(() -> {
            try {

                TcpConnectionConfiguration tcpConfiguration = TcpConnectionConfiguration.builder()
                        .hostname("localhost")
                        .port(5222)
                        .channelEncryption(ChannelEncryption.DISABLED)
                        .build();

                XmppSessionConfiguration configuration = XmppSessionConfiguration.builder()
                        .debugger(ConsoleDebugger.class)
                        // This registers the custom IQ payload to the JAXB context.
                        .extensions(Extension.of(Addition.class))
                        .build();

                XmppClient xmppSession = XmppClient.create("localhost", configuration, tcpConfiguration);

                // Reqister an IQ Handler, which will return the sum of two values.
                xmppSession.addIQHandler(new IQHandler() {
                    @Override
                    public Class<?> getPayloadClass() {
                        return Addition.class;
                    }

                    @Override
                    public IQ handleRequest(IQ iq) {
                        Addition addition = iq.getExtension(Addition.class);
                        if (addition.getSummand1() == null) {
                            return iq.createError(new StanzaError(Condition.BAD_REQUEST, "No summand provided."));
                        }
                        return iq.createResult(new Addition(addition.getSummand1() + addition.getSummand2()));
                    }
                });

                // Connect
                xmppSession.connect();
                // Login
                xmppSession.login("111", "111", "iq");

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
