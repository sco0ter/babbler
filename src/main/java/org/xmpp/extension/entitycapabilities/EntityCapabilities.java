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

package org.xmpp.extension.entitycapabilities;

import org.xmpp.stream.Feature;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "c")
public final class EntityCapabilities extends Feature {

    /**
     * The hashing algorithm used to generate the verification string.
     */
    @XmlAttribute
    private String hash;

    @XmlAttribute
    private String node;

    /**
     * The 'ver' attribute is a specially-constructed string (called a "verification string") that represents the entity's service discovery identity.
     */
    @XmlAttribute
    private String ver;

    public EntityCapabilities(String node, String hash, String ver) {
        this.node = node;
        this.hash = hash;
        this.ver = ver;
    }

    /**
     * Gets the hashing algorithm used to generate the verification string.
     *
     * @return The verification string.
     * @see #setHashingAlgorithm(String)
     */
    public String getHashingAlgorithm() {
        return hash;
    }

    /**
     * Sets the hashing algorithm used to generate the verification string.
     *
     * @param hash The verification string.
     * @see #getHashingAlgorithm()
     */
    public void setHashingAlgorithm(String hash) {
        this.hash = hash;
    }

    /**
     * Gets the node.
     * <blockquote>
     * <p>A URI that uniquely identifies a software application, typically a URL at the website of the project or company that produces the software.</p>
     * <p>It is RECOMMENDED for the value of the 'node' attribute to be an HTTP URL at which a user could find further information about the software product, such as "http://psi-im.org" for the Psi client; this enables a processing application to also determine a unique string for the generating application, which it could maintain in a list of known software implementations (e.g., associating the name received via the disco#info reply with the URL found in the caps data).</p>
     * </blockquote>
     *
     * @return The node.
     * @see #setNode(String)
     */
    public String getNode() {
        return node;
    }

    /**
     * Sets the node.
     *
     * @param node The node.
     * @see #getNode()
     */
    public void setNode(String node) {
        this.node = node;
    }

    /**
     * Gets the verification string that is used to verify the identity and supported features of the entity.
     *
     * @return The verification string.
     * @see #setVerificationString(String)
     */
    public String getVerificationString() {
        return ver;
    }

    /**
     * Sets the verification string.
     *
     * @param verificationString The verification string.
     * @see #getVerificationString()
     */
    public void setVerificationString(String verificationString) {
        this.ver = verificationString;
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
