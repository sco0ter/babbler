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

package rocks.xmpp.extensions.reach.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The implementation of the {@code <reach/>} element in the {@code urn:xmpp:reach:0} namespace.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0152.html">XEP-0152: Reachability Addresses</a>
 * @see <a href="http://xmpp.org/extensions/xep-0152.html#schema">XML Schema</a>
 */
@XmlRootElement(name = "reach")
public final class Reachability {

    public static final String NAMESPACE = "urn:xmpp:reach:0";

    @XmlElement(name = "addr")
    private final List<Address> addresses = new ArrayList<>();

    public Reachability(List<Address> addresses) {
        this.addresses.addAll(addresses);
    }

    public Reachability() {
    }

    /**
     * Gets the addresses.
     *
     * @return The addresses.
     */
    public List<Address> getAddresses() {
        return Collections.unmodifiableList(addresses);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Reachability)) {
            return false;
        }
        Reachability other = (Reachability) o;

        return Objects.equals(addresses, other.addresses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(addresses);
    }

    @Override
    public String toString() {
        return addresses.toString();
    }
}
