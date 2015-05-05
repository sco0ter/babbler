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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Christian Schudt
 */
final class FeatureRegistry {

    private final Set<String> enabledFeatures;

    private final XmppSession xmppSession;

    FeatureRegistry(XmppSession xmppSession) {
        this.xmppSession = xmppSession;
        this.enabledFeatures = new HashSet<>();
    }

    private Map<String, Extension> featureToExtension = new HashMap<>();

    private Map<Class<? extends Manager>, Set<Extension>> managersToExtensions = new HashMap<>();

    void registerFeature(Extension extension) {
        if (extension.getNamespace() != null) {
            featureToExtension.put(extension.getNamespace(), extension);
        }
        if (extension.getManager() != null) {
            Set<Extension> extensions = managersToExtensions.computeIfAbsent(extension.getManager(), key -> new HashSet<>());
            extensions.add(extension);
        }
        if (extension.isEnabled()) {
            setEnabled(Collections.singleton(extension), null, true);
        }
    }

    void enableFeature(String namespace) {
        Extension extension = featureToExtension.get(namespace);
        setEnabled(extension != null ? Collections.singleton(extension) : null, namespace, true);
    }

    void disableFeature(String namespace) {
        Extension extension = featureToExtension.get(namespace);
        setEnabled(extension != null ? Collections.singleton(extension) : null, namespace, false);
    }

    void enableFeature(Class<? extends Manager> managerClass) {
        setEnabled(managersToExtensions.get(managerClass), null, true);
    }

    void disableFeature(Class<? extends Manager> managerClass) {
        setEnabled(managersToExtensions.get(managerClass), null, false);
    }

    private void setEnabled(Iterable<Extension> extensions, String feature, boolean enabled) {
        if (extensions != null) {
            for (Extension extension : extensions) {
                Class<? extends Manager> managerClass = extension.getManager();
                if (managerClass != null) {
                    Manager manager = xmppSession.getManager(managerClass);
                    manager.setEnabled(enabled);
                }
                enableFeature(extension.getNamespace(), enabled);
                for (String subFeature : extension.getFeatures()) {
                    enableFeature(subFeature, enabled);
                }
            }
        } else {
            enableFeature(feature, enabled);
        }
    }

    private void enableFeature(String feature, boolean enabled) {
        if (feature != null) {
            if (enabled) {
                enabledFeatures.add(feature);
            } else {
                enabledFeatures.remove(feature);
            }
        }
    }

    Set<String> getEnabledFeatures() {
        return enabledFeatures;
    }
}
