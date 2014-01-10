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

import org.xmpp.Connection;
import org.xmpp.extension.servicediscovery.Feature;
import org.xmpp.extension.servicediscovery.ServiceDiscoveryManager;

/**
 * @author Christian Schudt
 */
public abstract class ExtensionManager {

    protected final Connection connection;

    private final ServiceDiscoveryManager serviceDiscoveryManager;

    private volatile boolean enabled;

    protected ExtensionManager(Connection connection) {
        this.connection = connection;
        if (this instanceof ServiceDiscoveryManager) {
            serviceDiscoveryManager = (ServiceDiscoveryManager) this;
        } else {
            serviceDiscoveryManager = connection.getExtensionManager(ServiceDiscoveryManager.class);
        }
    }

    /**
     * Gets the value, whether this extension is enabled or not.
     *
     * @return True, if the extension is enabled.
     * @see #setEnabled(boolean)
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Enables or disables support for the extension.
     *
     * @see #isEnabled()
     */
    public void setEnabled(boolean enabled) {
        if (serviceDiscoveryManager != null) {
            if (enabled) {
                serviceDiscoveryManager.getFeatures().add(getFeature());
            } else {
                serviceDiscoveryManager.getFeatures().remove(getFeature());
            }
        }
        this.enabled = enabled;
    }

    protected abstract Feature getFeature();
}
