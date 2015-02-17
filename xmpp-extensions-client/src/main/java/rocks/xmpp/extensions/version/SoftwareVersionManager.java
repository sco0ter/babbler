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

package rocks.xmpp.extensions.version;

import rocks.xmpp.core.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.ExtensionManager;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.AbstractIQHandler;
import rocks.xmpp.core.stanza.model.AbstractIQ;
import rocks.xmpp.core.stanza.model.client.IQ;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.extensions.version.model.SoftwareVersion;

/**
 * This manager implements <a href="http://xmpp.org/extensions/xep-0092.html">XEP-0092: Software Version</a>.
 * <p>
 * If enabled and a software version has been set, it automatically responds to incoming queries for the software version.
 * </p>
 * It also allows to query for the software version of another entity.
 *
 * @author Christian Schudt
 */
public final class SoftwareVersionManager extends ExtensionManager {

    private SoftwareVersion softwareVersion;

    private SoftwareVersionManager(final XmppSession xmppSession) {
        super(xmppSession, SoftwareVersion.NAMESPACE);
        setEnabled(true);
    }

    @Override
    protected void initialize() {
        xmppSession.addIQHandler(SoftwareVersion.class, new AbstractIQHandler(this, AbstractIQ.Type.GET) {
            @Override
            protected IQ processRequest(IQ iq) {
                synchronized (SoftwareVersionManager.this) {
                    if (softwareVersion != null) {
                        return iq.createResult(softwareVersion);
                    }
                }
                return iq.createError(Condition.SERVICE_UNAVAILABLE);
            }
        });
    }

    /**
     * Gets the software version of another entity.
     *
     * @param jid The JID of the entity you want get the software version from. You can also pass null, if you want to get the server's software version.
     * @return The software version or null, if this protocol is not supported.
     * @throws rocks.xmpp.core.stanza.StanzaException      If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException If the entity did not respond.
     */
    public SoftwareVersion getSoftwareVersion(Jid jid) throws XmppException {
        IQ result = xmppSession.query(new IQ(jid, IQ.Type.GET, new SoftwareVersion()));
        return result.getExtension(SoftwareVersion.class);
    }

    /**
     * Gets my own software version, which should be set first.
     *
     * @return My software version.
     * @see #setSoftwareVersion(SoftwareVersion)
     */
    public synchronized SoftwareVersion getSoftwareVersion() {
        return softwareVersion;
    }

    /**
     * Sets my own software version.
     *
     * @param softwareVersion My software version.
     * @see #getSoftwareVersion()
     */
    public synchronized void setSoftwareVersion(SoftwareVersion softwareVersion) {
        this.softwareVersion = softwareVersion;
    }
}
