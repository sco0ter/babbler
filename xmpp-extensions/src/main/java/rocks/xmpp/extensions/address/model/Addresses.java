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

package rocks.xmpp.extensions.address.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The implementation of the {@code <addresses/>} element in the {@code http://jabber.org/protocol/address} namespace..
 * <p>
 * Use this class to add extended address information to a stanza.
 * </p>
 * <h2>Sample</h2>
 * <pre><code>
 * Address address = new Address(Address.Type.CC, Jid.valueOf("juliet@example.net"));
 * Addresses addresses = new Addresses(Arrays.asList(address));
 * Message message = new Message(Jid.valueOf("romeo@example.net"));
 * message.getExtensions().add(addresses);
 * </code></pre>
 * <p>
 * This class is immutable.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0033.html">XEP-0033: Extended Stanza Addressing</a>
 * @see <a href="http://xmpp.org/extensions/xep-0033.html#schema">XML Schema</a>
 * @see Address
 */
@XmlRootElement(name = "addresses")
public final class Addresses {

    @XmlElement(name = "address")
    private final List<Address> addresses = new ArrayList<>();

    private Addresses() {
    }

    /**
     * Gets the address headers.
     *
     * @param addresses The address headers.
     */
    public Addresses(List<Address> addresses) {
        this.addresses.addAll(addresses);
    }

    /**
     * Gets the addresses.
     *
     * @return The addresses.
     */
    public final List<Address> getAddresses() {
        return Collections.unmodifiableList(addresses);
    }
}
