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

package rocks.xmpp.extensions.ping.handler;

import java.util.Collections;
import java.util.Set;

import rocks.xmpp.core.ExtensionProtocol;
import rocks.xmpp.core.stanza.AbstractIQHandler;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.extensions.disco.model.info.DiscoverableInfo;
import rocks.xmpp.extensions.ping.model.Ping;

/**
 * Handles an XMPP-level ping request, by returning a pong.
 *
 * @author Christian Schudt
 */
public final class PingHandler extends AbstractIQHandler implements ExtensionProtocol, DiscoverableInfo {

    private static final Set<String> FEATURES = Collections.singleton(Ping.NAMESPACE);

    public PingHandler() {
        super(Ping.class, IQ.Type.GET);
    }

    @Override
    protected final IQ processRequest(IQ iq) {
        return iq.createResult();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@value Ping#NAMESPACE}
     */
    @Override
    public final String getNamespace() {
        return Ping.NAMESPACE;
    }

    @Override
    public final boolean isEnabled() {
        return true;
    }

    @Override
    public final Set<String> getFeatures() {
        return FEATURES;
    }
}
