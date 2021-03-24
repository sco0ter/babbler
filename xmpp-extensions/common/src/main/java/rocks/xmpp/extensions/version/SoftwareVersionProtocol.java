/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2021 Christian Schudt
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

import rocks.xmpp.core.stanza.AbstractIQHandler;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.extensions.softwareinfo.SoftwareInfoProvider;
import rocks.xmpp.extensions.version.model.SoftwareVersion;

import java.util.Collections;
import java.util.Set;

/**
 * An abstract implementation of <a href="https://xmpp.org/extensions/xep-0092.html">XEP-0092: Software Version</a>.
 * <p>
 * It handles IQ queries and provides the required service discovery features.
 * </p>
 * This class is thread-safe.
 *
 * @author Christian Schudt
 */
public class SoftwareVersionProtocol extends AbstractIQHandler implements SoftwareInfoProvider<SoftwareVersion> {

    private static final Set<String> FEATURES = Collections.singleton(SoftwareVersion.NAMESPACE);

    /**
     * Guarded by "this".
     */
    private SoftwareVersion softwareVersion;

    public SoftwareVersionProtocol() {
        super(SoftwareVersion.class, IQ.Type.GET);
    }

    @Override
    protected final IQ processRequest(IQ iq) {
        synchronized (this) {
            if (softwareVersion != null) {
                return iq.createResult(softwareVersion);
            }
        }
        return iq.createError(Condition.SERVICE_UNAVAILABLE);
    }

    @Override
    public final synchronized SoftwareVersion getSoftwareInfo() {
        return softwareVersion;
    }

    @Override
    public final synchronized void setSoftwareInfo(SoftwareVersion softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    @Override
    public final boolean isEnabled() {
        return getSoftwareInfo() != null;
    }

    @Override
    public final Set<String> getFeatures() {
        return FEATURES;
    }
}
