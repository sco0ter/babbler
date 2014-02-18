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

package org.xmpp.extension.stream.si;

import org.xmpp.extension.featureneg.FeatureNegotiation;

import javax.xml.bind.annotation.*;

/**
 * The implementation of the {@code <si/>} element.
 *
 * @author Christian Schudt
 */
@XmlRootElement(name = "si")
@XmlSeeAlso({BadProfile.class, NoValidStreams.class})
public final class StreamInitiation {

    @XmlAttribute(name = "id")
    private String id;

    @XmlAttribute(name = "mime-type")
    private String mimeType;

    @XmlAttribute(name = "profile")
    private String profile;

    @XmlAnyElement(lax = true)
    private Object profileElement;

    @XmlElementRef
    private FeatureNegotiation featureNegotiation;

    private StreamInitiation() {
    }

    public StreamInitiation(FeatureNegotiation featureNegotiation) {
        this.featureNegotiation = featureNegotiation;
    }

    public StreamInitiation(String id, String profile, String mimeType, Object profileElement, FeatureNegotiation featureNegotiation) {
        this.id = id;
        this.profile = profile;
        this.mimeType = mimeType;
        this.profileElement = profileElement;
        this.featureNegotiation = featureNegotiation;
    }

    /**
     * The "id" attribute is an opaque identifier. This attribute MUST be present on type='set', and MUST be a valid string.
     *
     * @return The id.
     */
    public String getId() {
        return id;
    }

    /**
     * The "mime-type" attribute identifies the MIME-type for the data across the stream.
     *
     * @return The MIME type.
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * The "profile" attribute defines the SI profile in use. This value MUST be present during negotiation, and is the namespace of the profile to use.
     *
     * @return The profile.
     */
    public String getProfile() {
        return profile;
    }

    /**
     * Gets the profile element, e.g. {@link org.xmpp.extension.stream.si.filetransfer.FileTransfer}.
     *
     * @return The profile element.
     */
    public Object getProfileElement() {
        return profileElement;
    }

    /**
     * Gets the feature negotiation element.
     *
     * @return The feature negotiation.
     */
    public FeatureNegotiation getFeatureNegotiation() {
        return featureNegotiation;
    }
}
