/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2020 Christian Schudt
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

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.ExtensionProtocol;
import rocks.xmpp.core.stanza.IQHandler;
import rocks.xmpp.extensions.version.model.SoftwareVersion;
import rocks.xmpp.util.concurrent.AsyncResult;

/**
 * Represents <a href="https://xmpp.org/extensions/xep-0092.html">XEP-0092: Software Version</a>.
 * <p>
 * It handles IQ queries and provides the required service discovery features.
 * </p>
 *
 * @author Christian Schudt
 */
public interface SoftwareVersionManager extends IQHandler, ExtensionProtocol {

    /**
     * Queries another entity for its software version.
     *
     * @param jid The entity's JID.
     * @return The result containing the software version.
     */
    AsyncResult<SoftwareVersion> getSoftwareVersion(Jid jid);

    /**
     * Gets my own software version.
     *
     * @return My software version.
     * @see #setSoftwareVersion(SoftwareVersion)
     */
    SoftwareVersion getSoftwareVersion();

    /**
     * Sets my own software version. The set version is returned when this handler is queried for the software version.
     *
     * @param softwareVersion My software version.
     * @see #getSoftwareVersion()
     */
    void setSoftwareVersion(SoftwareVersion softwareVersion);
}
