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

package rocks.xmpp.extensions.disco.model.info;

import rocks.xmpp.extensions.data.model.DataForm;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The implementation of the {@code <query/>} element in the {@code http://jabber.org/protocol/disco#info} namespace.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0030.html">XEP-0030: Service Discovery</a>
 * @see <a href="http://xmpp.org/extensions/xep-0030.html#schemas-info">XML Schema</a>
 * @see <a href="http://xmpp.org/extensions/xep-0128.html">XEP-0128: Service Discovery Extensions</a>
 */
@XmlRootElement(name = "query")
@XmlAccessorType(XmlAccessType.FIELD)
public final class InfoDiscovery implements InfoNode {

    @XmlAttribute
    private String node;

    @XmlElement(name = "identity")
    private Set<Identity> identities = new HashSet<>();

    @XmlElement(name = "feature")
    private Set<Feature> features = new HashSet<>();

    @XmlElementRef
    private List<DataForm> extensions = new ArrayList<>();

    /**
     * Creates an empty element, used for info discovery requests.
     */
    public InfoDiscovery() {
    }

    /**
     * Creates an info discovery element with a node attribute.
     *
     * @param node The node.
     */
    public InfoDiscovery(String node) {
        this.node = node;
    }

    /**
     * Creates an info discovery element, used in discovery info responses.
     *
     * @param identities The identities
     * @param features   The features.
     * @param extensions The extensions.
     */
    public InfoDiscovery(Set<Identity> identities, Set<Feature> features, List<DataForm> extensions) {
        this.identities = identities;
        this.features = features;
        this.extensions = extensions;
    }

    /**
     * Creates an info discovery element, used in discovery info responses.
     *
     * @param node       The node.
     * @param identities The identities
     * @param features   The features.
     * @param extensions The extensions.
     */
    public InfoDiscovery(String node, Set<Identity> identities, Set<Feature> features, List<DataForm> extensions) {
        this.node = node;
        this.identities = identities;
        this.features = features;
        this.extensions = extensions;
    }

    @Override
    public Set<Identity> getIdentities() {
        return identities;
    }

    @Override
    public Set<Feature> getFeatures() {
        return features;
    }

    @Override
    public String getNode() {
        return node;
    }

    @Override
    public List<DataForm> getExtensions() {
        return extensions;
    }

    @Override
    public String toString() {
        return "Identity: " + identities + "; Features: " + features;
    }
}
