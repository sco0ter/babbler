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

package rocks.xmpp.extensions.disco.model.info;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.xml.XMLConstants;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import rocks.xmpp.core.LanguageElement;
import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.disco.model.ServiceDiscoveryNode;

/**
 * The implementation of the {@code <query/>} element in the {@code http://jabber.org/protocol/disco#info} namespace.
 *
 * <p>This class is immutable.</p>
 *
 * @author Christian Schudt
 * @see <a href="https://xmpp.org/extensions/xep-0030.html">XEP-0030: Service Discovery</a>
 * @see <a href="https://xmpp.org/extensions/xep-0030.html#schemas-info">XML Schema</a>
 * @see <a href="https://xmpp.org/extensions/xep-0128.html">XEP-0128: Service Discovery Extensions</a>
 */
@XmlRootElement(name = "query")
public final class InfoDiscovery implements DiscoverableInfo, ServiceDiscoveryNode, LanguageElement {

    /**
     * http://jabber.org/protocol/disco#info
     */
    public static final String NAMESPACE = "http://jabber.org/protocol/disco#info";

    private final Set<Identity> identity = new TreeSet<>();

    private final Set<FeatureElement> feature = new TreeSet<>();

    @XmlElementRef
    private final List<DataForm> extensions = new ArrayList<>();

    @XmlAttribute
    private final String node;

    @XmlAttribute(namespace = XMLConstants.XML_NS_URI)
    private final Locale lang;

    /**
     * Creates an empty element, used for info discovery requests.
     */
    public InfoDiscovery() {
        this(null);
    }

    /**
     * Creates an info discovery element with a node attribute.
     *
     * @param node The node.
     */
    public InfoDiscovery(String node) {
        this.node = node;
        this.lang = null;
    }

    /**
     * Creates an info discovery element, used in discovery info responses.
     *
     * @param identities The identities
     * @param features   The features.
     */
    public InfoDiscovery(Collection<Identity> identities, Collection<String> features) {
        this(null, identities, features, null);
    }

    /**
     * Creates an info discovery element, used in discovery info responses.
     *
     * @param identities The identities
     * @param features   The features.
     * @param extensions The extensions.
     */
    public InfoDiscovery(Collection<Identity> identities, Collection<String> features,
                         Collection<DataForm> extensions) {
        this(null, identities, features, extensions);
    }

    public InfoDiscovery(String node, Collection<Identity> identities, Collection<String> features,
                         Collection<DataForm> extensions) {
        this(node, identities, features, extensions, null);
    }

    /**
     * Creates an info discovery element, used in discovery info responses.
     *
     * @param node       The node.
     * @param identities The identities
     * @param features   The features.
     * @param extensions The extensions.
     * @param lang       The language.
     */
    public InfoDiscovery(String node, Collection<Identity> identities, Collection<String> features,
                         Collection<DataForm> extensions, Locale lang) {
        this.node = node;
        if (identities != null) {
            this.identity.addAll(identities);
        }
        if (features != null) {
            this.feature.addAll(features.stream().map(FeatureElement::new).collect(Collectors.toList()));
        }
        if (extensions != null) {
            this.extensions.addAll(extensions);
        }
        this.lang = lang;
    }

    @Override
    public final Set<Identity> getIdentities() {
        return Collections.unmodifiableSet(identity);
    }

    @Override
    public final Set<String> getFeatures() {
        Set<String> set = feature.stream().filter(f -> f.getFeatureName() != null).map(FeatureElement::getFeatureName)
                .collect(Collectors.toCollection(TreeSet::new));
        return Collections.unmodifiableSet(set);
    }

    @Override
    public final String getNode() {
        return node;
    }

    @Override
    public final List<DataForm> getExtensions() {
        return Collections.unmodifiableList(extensions);
    }

    @Override
    public final Locale getLanguage() {
        return lang;
    }

    @Override
    public final boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof InfoDiscovery)) {
            return false;
        }
        InfoDiscovery other = (InfoDiscovery) o;

        return Objects.equals(getNode(), other.getNode())
                && Objects.equals(getIdentities(), other.getIdentities())
                && Objects.equals(getFeatures(), other.getFeatures())
                && Objects.equals(getExtensions(), other.getExtensions());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getNode(), getIdentities(), getFeatures(), getExtensions());
    }

    @Override
    public final String toString() {
        return "Identity: " + identity + "; Features: " + feature;
    }
}
