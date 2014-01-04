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

package org.xmpp.extension.servicediscovery;

import javax.xml.bind.annotation.*;
import java.util.Collections;
import java.util.List;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "query")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({Feature.class, Identity.class})
public final class ServiceDiscovery {

    @XmlAttribute(name = "node")
    private String node;

    @XmlElement(name = "identity")
    private List<Identity> identities;

    @XmlElement(name = "feature")
    private List<Feature> features;

    public ServiceDiscovery() {

    }

    public ServiceDiscovery(String node) {
        this.node = node;
    }

    public List<Identity> getIdentities() {
        return Collections.unmodifiableList(identities);
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public String getNode() {
        return node;
    }
}
