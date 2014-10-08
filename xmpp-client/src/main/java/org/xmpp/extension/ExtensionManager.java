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

package org.xmpp.extension;

import org.xmpp.Manager;
import org.xmpp.XmppSession;
import org.xmpp.extension.disco.ServiceDiscoveryManager;
import org.xmpp.extension.disco.info.Feature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Christian Schudt
 */
public abstract class ExtensionManager extends Manager {

    protected final XmppSession xmppSession;

    protected final Collection<String> features;

    private final ServiceDiscoveryManager serviceDiscoveryManager;

    protected ExtensionManager(XmppSession xmppSession, String... features) {
        this.xmppSession = xmppSession;
        this.features = new ArrayList<>(Arrays.asList(features));

        if (this instanceof ServiceDiscoveryManager) {
            serviceDiscoveryManager = (ServiceDiscoveryManager) this;
        } else {
            serviceDiscoveryManager = xmppSession.getExtensionManager(ServiceDiscoveryManager.class);
        }
    }

    /**
     * Enables or disables support for the extension.
     *
     * @param enabled True, if support for the managed extension should be enabled; otherwise false.
     * @see #isEnabled()
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (serviceDiscoveryManager != null) {
            for (String namespace : features) {
                if (enabled) {
                    serviceDiscoveryManager.addFeature(new Feature(namespace));
                } else {
                    serviceDiscoveryManager.removeFeature(new Feature(namespace));
                }
            }
        }
    }
}
