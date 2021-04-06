/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2019 Christian Schudt
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

package rocks.xmpp.extensions.csi.model;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import rocks.xmpp.core.stream.model.StreamElement;
import rocks.xmpp.core.stream.model.StreamFeature;

/**
 * Represents a client state, i.e. active or inactive.
 *
 * <p>Because elements of Client State Indication are stateless, instances of this class are static singletons.</p>
 * 
 * <pre>{@code
 * ClientState.ACTIVE;
 * ClientState.INACTIVE;
 * ClientState.FEATURE;
 * }</pre>
 *
 * @author Christian Schudt
 * @see <a href="https://xmpp.org/extensions/xep-0352.html">XEP-0352: Client State Indication</a>
 * @see #ACTIVE
 * @see #INACTIVE
 * @see #FEATURE
 */
@XmlTransient
@XmlSeeAlso({ClientState.Active.class, ClientState.Inactive.class, ClientState.Csi.class})
public abstract class ClientState implements StreamElement {

    /**
     * The stream feature for servers to advertise support for client state indication.
     */
    public static final StreamFeature FEATURE = new Csi();

    /**
     * The active state.
     */
    public static final ClientState ACTIVE = new Active();

    /**
     * The inactive state.
     */
    public static final ClientState INACTIVE = new Inactive();

    private ClientState() {
    }

    /**
     * urn:xmpp:csi:0
     */
    public static final String NAMESPACE = "urn:xmpp:csi:0";

    @XmlRootElement
    @XmlType(factoryMethod = "create")
    static final class Csi extends StreamFeature {

        private Csi() {
        }

        @SuppressWarnings("unused")
        private static Csi create() {
            return (Csi) ClientState.FEATURE;
        }
    }

    @XmlRootElement
    @XmlType(factoryMethod = "create")
    static final class Active extends ClientState {

        private Active() {
        }

        @SuppressWarnings("unused")
        private static Active create() {
            return (Active) ClientState.ACTIVE;
        }
    }

    @XmlRootElement
    @XmlType(factoryMethod = "create")
    static final class Inactive extends ClientState {

        private Inactive() {
        }

        @SuppressWarnings("unused")
        private static Inactive create() {
            return (Inactive) ClientState.INACTIVE;
        }
    }
}
