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

package rocks.xmpp.extensions.address;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.XmlTest;
import rocks.xmpp.extensions.address.model.Address;
import rocks.xmpp.extensions.address.model.Addresses;
import rocks.xmpp.im.roster.model.Roster;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * @author Christian Schudt
 */
public class AddressTest extends XmlTest {

    protected AddressTest() throws JAXBException, XMLStreamException {
        super(Addresses.class, Roster.class);
    }

    @Test
    public void marshalAddresses() throws JAXBException, XMLStreamException {
        List<Address> addressList = new ArrayList<>();
        addressList.add(new Address(Address.Type.TO, Jid.of("hildjj@jabber.org/Work"), "description", "node"));
        addressList.add(new Address(Address.Type.CC, Jid.of("jer@jabber.org/Home"), new Roster()));
        Addresses addresses = new Addresses(addressList);

        String xml = marshal(addresses);
        Assert.assertEquals(xml, "<addresses xmlns=\"http://jabber.org/protocol/address\"><address type=\"to\" jid=\"hildjj@jabber.org/Work\" desc=\"description\" node=\"node\"></address><address type=\"cc\" jid=\"jer@jabber.org/Home\"><query xmlns=\"jabber:iq:roster\"></query></address></addresses>");
        Assert.assertNotNull(addressList.get(1).getExtension(Roster.class));
    }
}
