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

package org.xmpp.extension.register;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.BaseTest;
import org.xmpp.UnmarshalHelper;
import org.xmpp.extension.register.feature.RegisterFeature;
import org.xmpp.stanza.IQ;
import org.xmpp.stream.Feature;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;

/**
 * @author Christian Schudt
 */
public class RegistrationTest extends BaseTest {

    @Test
    public void unmarshalRegistration() throws XMLStreamException, JAXBException {
        String xml = "<iq type='result' id='reg1'>\n" +
                "  <query xmlns='jabber:iq:register'>\n" +
                "    <instructions>\n" +
                "      Choose a username and password for use with this service.\n" +
                "      Please also provide your email address.\n" +
                "    </instructions>\n" +
                "    <username/>\n" +
                "    <password/>\n" +
                "    <email/>\n" +
                "  </query>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        Registration registration = iq.getExtension(Registration.class);
        Assert.assertNotNull(registration);
        Assert.assertNotNull(registration.getUsername());
        Assert.assertNotNull(registration.getPassword());
        Assert.assertNotNull(registration.getEmail());
    }

    @Test
    public void unmarshalAlreadyRegistered() throws XMLStreamException, JAXBException {
        String xml = "<iq type='result' id='reg1'>\n" +
                "  <query xmlns='jabber:iq:register'>\n" +
                "    <registered/>\n" +
                "    <username>juliet</username>\n" +
                "    <password>R0m30</password>\n" +
                "    <email>juliet@capulet.com</email>\n" +
                "  </query>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        Registration registration = iq.getExtension(Registration.class);
        Assert.assertNotNull(registration);
        Assert.assertTrue(registration.isRegistered());
        Assert.assertEquals(registration.getUsername(), "juliet");
        Assert.assertEquals(registration.getPassword(), "R0m30");
        Assert.assertEquals(registration.getEmail(), "juliet@capulet.com");
    }

    @Test
    public void unmarshalForm() throws XMLStreamException, JAXBException {
        String xml = "<iq type='result'\n" +
                "    from='contests.shakespeare.lit'\n" +
                "    to='juliet@capulet.com/balcony'\n" +
                "    id='reg3'>\n" +
                "  <query xmlns='jabber:iq:register'>\n" +
                "    <instructions>Use the enclosed form to register. If your Jabber client does not</instructions>\n" +
                "    <x xmlns='jabber:x:data' type='form'>\n" +
                "      <title>Contest Registration</title>\n" +
                "      <instructions>\n" +
                "        Please provide the following information\n" +
                "        to sign up for our special contests!\n" +
                "      </instructions>\n" +
                "      <field type='hidden' var='FORM_TYPE'>\n" +
                "        <value>jabber:iq:register</value>\n" +
                "      </field>\n" +
                "      <field type='text-single' label='Given Name' var='first'>\n" +
                "        <required/>\n" +
                "      </field>\n" +
                "      <field type='text-single' label='Family Name' var='last'>\n" +
                "        <required/>\n" +
                "      </field>\n" +
                "      <field type='text-single' label='Email Address' var='email'>\n" +
                "        <required/>\n" +
                "      </field>\n" +
                "      <field type='list-single' label='Gender' var='x-gender'>\n" +
                "        <option label='Male'><value>M</value></option>\n" +
                "        <option label='Female'><value>F</value></option>\n" +
                "      </field>\n" +
                "    </x>\n" +
                "  </query>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        Registration registration = iq.getExtension(Registration.class);
        Assert.assertNotNull(registration);
        Assert.assertEquals(registration.getInstructions(), "Use the enclosed form to register. If your Jabber client does not");
        Assert.assertNotNull(registration.getRegistrationForm());
    }

    @Test
    public void marshalRemove() throws JAXBException, XMLStreamException, IOException {
        Registration registration = new Registration(true);
        String xml = marshall(registration);
        Assert.assertEquals(xml, "<query xmlns=\"jabber:iq:register\"><remove></remove></query>");
    }

    @Test
    public void testFeature() throws JAXBException, XMLStreamException, IOException {
        String feature = "<register xmlns='http://jabber.org/features/iq-register'/>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(feature);
        Feature registerFeature = (Feature) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertTrue(registerFeature instanceof RegisterFeature);
    }
}
