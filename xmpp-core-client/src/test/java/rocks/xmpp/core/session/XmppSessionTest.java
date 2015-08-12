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

package rocks.xmpp.core.session;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.stanza.model.Message;
import rocks.xmpp.core.stanza.model.Presence;

/**
 * @author Christian Schudt
 */
public class XmppSessionTest {

    /**
     * The initiating entity MUST NOT attempt to send XML stanzas to entities other than itself
     * (i.e., the client's connected resource or any other authenticated resource of the client's account)
     * or the server to which it is connected until stream negotiation has been completed.
     */
    @Test
    public void testSendingStanzasBeforeStreamNegotationCompleted() {
        Jid domain = Jid.ofDomain("domain");
        Jid connectedResource = Jid.of("user@domain/resource");

        // To itself
        Assert.assertTrue(XmppSession.isSentToUserOrServer(new Message(), domain, null));

        // To others
        Assert.assertFalse(XmppSession.isSentToUserOrServer(new Presence(), domain, null));

        // Other server
        Assert.assertFalse(XmppSession.isSentToUserOrServer(new Message(Jid.of("to@domain123")), domain, null));

        // Same bare JID as connected resource
        Assert.assertTrue(XmppSession.isSentToUserOrServer(new Message(Jid.of("user@domain/res2")), domain, connectedResource));

        // To subdomain, allow that too
        Assert.assertTrue(XmppSession.isSentToUserOrServer(new Message(Jid.ofDomain("sub.domain")), domain, connectedResource));
    }
}
