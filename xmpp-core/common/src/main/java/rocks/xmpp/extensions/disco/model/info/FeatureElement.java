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

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAttribute;

import rocks.xmpp.util.Strings;

/**
 * Represents a feature offered or protocol supported by an XMPP entity.
 * <blockquote>
 * <p><cite><a href="https://xmpp.org/extensions/xep-0030.html#info">3. Discovering Information About a Jabber
 * Entity</a></cite></p>
 * <p>This information helps requesting entities determine what actions are possible with regard to this entity
 * (registration, search, join, etc.), what protocols the entity supports, and specific feature types of interest, if
 * any (e.g., for the purpose of feature negotiation).</p>
 * </blockquote>
 * This class is immutable.
 *
 * @author Christian Schudt
 */
final class FeatureElement implements Comparable<FeatureElement>, Feature {

    /**
     * Each {@code <feature/>} element MUST possess a 'var' attribute whose value is a protocol namespace or other
     * feature offered by the entity.
     */
    @XmlAttribute(name = "var")
    private final String name;

    private FeatureElement() {
        this.name = null;
    }

    /**
     * Creates a feature.
     *
     * @param name A protocol namespace or other feature offered by the entity.
     */
    FeatureElement(String name) {
        this.name = Objects.requireNonNull(name);
    }

    /**
     * Gets the 'var' attribute, whose value is either a protocol namespace or other feature offered by the entity.
     *
     * @return A protocol namespace or other feature offered by the entity.
     */
    @Override
    public final String getFeatureName() {
        return name;
    }

    /**
     * A feature is considered equal to another one, if their 'var' attribute are equal.
     *
     * @param o The other entity.
     * @return True, if the both features are equal.
     */
    @Override
    public final boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof FeatureElement)) {
            return false;
        }
        FeatureElement other = (FeatureElement) o;
        return Objects.equals(name, other.name);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(name);
    }

    /**
     * Implements a natural ordering of a feature, as suggested and required by <a href="https://xmpp.org/extensions/xep-0115.html">XEP-0115:
     * Entity Capabilities</a>. (The 'var' attributes are compared to each other).
     *
     * @param o The other feature.
     * @return The result of the comparison.
     */
    @Override
    public final int compareTo(FeatureElement o) {
        if (o == null) {
            return 1;
        } else {
            if (name == null && o.name == null) {
                return 0;
            } else if (name == null) {
                return -1;
            } else if (o.name == null) {
                return 1;
            } else {
                return Strings.compareUnsignedBytes(name, o.name, StandardCharsets.UTF_8);
            }
        }
    }

    @Override
    public final String toString() {
        return name;
    }
}
