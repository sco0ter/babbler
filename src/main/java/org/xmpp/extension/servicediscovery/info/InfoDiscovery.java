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

package org.xmpp.extension.servicediscovery.info;

import javax.xml.bind.annotation.*;
import java.util.Set;

/**
 * The implementation of the {@code <query/>} element.
 *
 * @author Christian Schudt
 */
@XmlRootElement(name = "query")
@XmlAccessorType(XmlAccessType.FIELD)
public final class InfoDiscovery {

    @XmlAttribute(name = "node")
    private String node;

    @XmlElement(name = "identity")
    private Set<Identity> identities;

    @XmlElement(name = "feature")
    private Set<Feature> features;

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
     */
    public InfoDiscovery(Set<Identity> identities, Set<Feature> features) {
        this.identities = identities;
        this.features = features;
    }

    /**
     * Gets the identities.
     *
     * @return The identities.
     */
    public Set<Identity> getIdentities() {
        return identities;
    }

    /**
     * Gets the features.
     *
     * @return The features.
     */
    public Set<Feature> getFeatures() {
        return features;
    }

    /**
     * Gets the node.
     *
     * @return The node.
     */
    public String getNode() {
        return node;
    }
}
