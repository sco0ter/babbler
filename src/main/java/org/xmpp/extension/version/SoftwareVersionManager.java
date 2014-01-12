/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Christian Schudt
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

package org.xmpp.extension.version;

import org.xmpp.Connection;
import org.xmpp.Jid;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.stanza.IQ;
import org.xmpp.stanza.IQEvent;
import org.xmpp.stanza.IQListener;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeoutException;

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

    private static final String FEATURE = "jabber:iq:version";

    private SoftwareVersion softwareVersion;

    public SoftwareVersionManager(final Connection connection) {
        super(connection);
        connection.addIQListener(new IQListener() {
            @Override
            public void handle(IQEvent e) {
                IQ iq = e.getIQ();
                // If an entity asks us for our software version, reply.
                if (e.isIncoming() && iq.getType() == IQ.Type.GET && iq.getExtension(SoftwareVersion.class) != null) {
                    synchronized (SoftwareVersionManager.this) {
                        if (isEnabled() && softwareVersion != null) {
                            IQ result = iq.createResult();
                            result.setExtension(softwareVersion);
                            connection.send(result);
                        } else {
                            sendServiceUnavailable(iq);
                        }
                    }
                }
            }
        });
        setEnabled(true);
    }

    /**
     * Gets the software version of another entity.
     *
     * @param jid The JID of the entity you want get the software version from. You can also pass null, if you want to get the server's software version.
     * @return The software version or null, if this protocol is not supported.
     * @throws TimeoutException If the request timed out.
     */
    public SoftwareVersion getSoftwareVersion(Jid jid) throws TimeoutException {
        IQ iq = new IQ(IQ.Type.GET, new SoftwareVersion());
        iq.setTo(jid);
        IQ result = connection.query(iq);

        if (result.getError() == null) {
            return result.getExtension(SoftwareVersion.class);
        }
        return null;
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

    @Override
    protected Collection<String> getFeatureNamespaces() {
        return Arrays.asList(FEATURE);
    }
}
