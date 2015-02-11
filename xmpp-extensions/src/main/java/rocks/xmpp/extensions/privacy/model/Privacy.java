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

package rocks.xmpp.extensions.privacy.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The implementation of the the {@code <query/>} element in the {@code jabber:iq:privacy} namespace.
 * <p>
 * This class contains information about the active and default list and holds the privacy lists.
 * </p>
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0016.html">XEP-0016: Privacy Lists</a>
 * @see <a href="http://xmpp.org/extensions/xep-0016.html#schema">XML Schema</a>
 */
@XmlRootElement(name = "query")
public final class Privacy {

    /**
     * jabber:iq:privacy
     */
    public static final String NAMESPACE = "jabber:iq:privacy";

    @XmlElement(name = "list")
    private final List<PrivacyList> privacyLists = new ArrayList<>();

    @XmlJavaTypeAdapter(ActiveNameAdapter.class)
    @XmlElement(name = "active")
    private String activeName;

    @XmlJavaTypeAdapter(DefaultNameAdapter.class)
    @XmlElement(name = "default")
    private String defaultName;

    /**
     * Creates an empty privacy element.
     */
    public Privacy() {
    }

    /**
     * Creates a privacy element with one or more privacy lists.
     *
     * @param privacyLists The privacy list(s).
     */
    public Privacy(PrivacyList... privacyLists) {
        this.privacyLists.addAll(Arrays.asList(privacyLists));
    }

    /**
     * Gets the active list name.
     *
     * @return The active list name.
     * @see #setActiveName(String)
     */
    public String getActiveName() {
        return activeName;
    }

    /**
     * Sets the active list name.
     *
     * @param activeName The active list name.
     * @see #getActiveName()
     */
    public void setActiveName(String activeName) {
        this.activeName = activeName;
    }

    /**
     * Sets the default list name.
     *
     * @return The default list name.
     * @see #setDefaultName(String)
     */
    public String getDefaultName() {
        return defaultName;
    }

    /**
     * Gets the default list name.
     *
     * @param defaultName The default list name.
     * @see #getDefaultName()
     */
    public void setDefaultName(String defaultName) {
        this.defaultName = defaultName;
    }

    /**
     * Gets the privacy lists.
     *
     * @return The privacy lists.
     */
    public List<PrivacyList> getPrivacyLists() {
        return Collections.unmodifiableList(privacyLists);
    }

    @Override
    public String toString() {
        return privacyLists.toString();
    }

    private static final class Active {
        @XmlAttribute(name = "name")
        private String name;
    }

    private static final class Default {
        @XmlAttribute(name = "name")
        private String name;
    }

    private static final class ActiveNameAdapter extends XmlAdapter<Active, String> {

        @Override
        public String unmarshal(Active v) throws Exception {
            if (v != null) {
                return v.name;
            }
            return null;
        }

        @Override
        public Active marshal(String v) throws Exception {
            if (v != null) {
                Active active = new Active();
                active.name = v.isEmpty() ? null : v;
                return active;
            }
            return null;
        }
    }

    private static final class DefaultNameAdapter extends XmlAdapter<Default, String> {

        @Override
        public String unmarshal(Default v) throws Exception {
            if (v != null) {
                return v.name;
            }
            return null;
        }

        @Override
        public Default marshal(String v) throws Exception {
            if (v != null) {
                Default def = new Default();
                def.name = v.isEmpty() ? null : v;
                return def;
            }
            return null;
        }
    }
}
