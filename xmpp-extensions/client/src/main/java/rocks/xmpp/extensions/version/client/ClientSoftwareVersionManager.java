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

package rocks.xmpp.extensions.version.client;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.session.Manager;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.extensions.version.AbstractSoftwareVersionManager;
import rocks.xmpp.extensions.version.model.SoftwareVersion;
import rocks.xmpp.util.concurrent.AsyncResult;

import java.io.InputStream;
import java.util.Properties;

/**
 * This manager implements <a href="https://xmpp.org/extensions/xep-0092.html">XEP-0092: Software Version</a>.
 * <p>
 * If enabled and a software version has been set, it automatically responds to inbound queries for the software version.
 * </p>
 * It also allows to query for the software version of another entity.
 *
 * @author Christian Schudt
 */
public final class ClientSoftwareVersionManager extends AbstractSoftwareVersionManager {

    private static final SoftwareVersion DEFAULT_VERSION;

    static {
        Properties properties = new Properties();
        SoftwareVersion version;
        try (InputStream inputStream = ClientSoftwareVersionManager.class.getResourceAsStream("/META-INF/maven/rocks.xmpp/xmpp-extensions-client/pom.properties")) {
            properties.load(inputStream);
            version = new SoftwareVersion("Babbler", properties.getProperty("version"));
        } catch (Exception e) {
            version = null;
        }
        DEFAULT_VERSION = version;
    }

    private final XmppSession xmppSession;

    private final Manager manager;

    private ClientSoftwareVersionManager(final XmppSession xmppSession) {
        manager = new Manager(xmppSession);
        this.xmppSession = xmppSession;
        setEnabled(true);
        setSoftwareVersion(DEFAULT_VERSION);
    }

    /**
     * Gets the software version of another entity.
     *
     * @param jid The JID of the entity you want get the software version from. You can also pass null, if you want to get the server's software version.
     * @return The async result with the software version or null, if this protocol is not supported.
     */
    @Override
    public AsyncResult<SoftwareVersion> getSoftwareVersion(Jid jid) {
        return xmppSession.query(IQ.get(jid, new SoftwareVersion()), SoftwareVersion.class);
    }

    @Override
    public void setEnabled(boolean enabled) {
        manager.setEnabled(enabled);
    }

    @Override
    public boolean isEnabled() {
        return manager.isEnabled();
    }
}