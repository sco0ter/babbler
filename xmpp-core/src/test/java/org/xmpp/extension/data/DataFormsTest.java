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

package org.xmpp.extension.data;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.UnmarshalTest;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

/**
 * @author Christian Schudt
 */
public class DataFormsTest extends UnmarshalTest {

    protected DataFormsTest() throws JAXBException, XMLStreamException {
        super(DataForm.class);
    }

    @Test
    public void unmarshalDataForm() throws XMLStreamException, JAXBException {
        String xml = "<x xmlns='jabber:x:data' type='form'>\n" +
                "      <title>Bot Configuration</title>\n" +
                "      <instructions>Fill out this form to configure your new bot!</instructions>\n" +
                "      <field type='hidden'\n" +
                "             var='FORM_TYPE'>\n" +
                "        <value>jabber:bot</value>\n" +
                "      </field>\n" +
                "      <field type='fixed'><value>Section 1: Bot Info</value></field>\n" +
                "      <field type='text-single'\n" +
                "             label='The name of your bot'\n" +
                "             var='botname'/>\n" +
                "      <field type='text-multi'\n" +
                "             label='Helpful description of your bot'\n" +
                "             var='description'/>\n" +
                "      <field type='boolean'\n" +
                "             label='Public bot?'\n" +
                "             var='public'>\n" +
                "        <required/>\n" +
                "      </field>\n" +
                "      <field type='text-private'\n" +
                "             label='Password for special access'\n" +
                "             var='password'/>\n" +
                "      <field type='fixed'><value>Section 2: Features</value></field>\n" +
                "      <field type='list-multi'\n" +
                "             label='What features will the bot support?'\n" +
                "             var='features'>\n" +
                "        <option label='Contests'><value>contests</value></option>\n" +
                "        <option label='News'><value>news</value></option>\n" +
                "        <option label='Polls'><value>polls</value></option>\n" +
                "        <option label='Reminders'><value>reminders</value></option>\n" +
                "        <option label='Search'><value>search</value></option>\n" +
                "        <value>news</value>\n" +
                "        <value>search</value>\n" +
                "      </field>\n" +
                "      <field type='fixed'><value>Section 3: Subscriber List</value></field>\n" +
                "      <field type='list-single'\n" +
                "             label='Maximum number of subscribers'\n" +
                "             var='maxsubs'>\n" +
                "        <value>20</value>\n" +
                "        <option label='10'><value>10</value></option>\n" +
                "        <option label='20'><value>20</value></option>\n" +
                "        <option label='30'><value>30</value></option>\n" +
                "        <option label='50'><value>50</value></option>\n" +
                "        <option label='100'><value>100</value></option>\n" +
                "        <option label='None'><value>none</value></option>\n" +
                "      </field>\n" +
                "      <field type='fixed'><value>Section 4: Invitations</value></field>\n" +
                "      <field type='jid-multi'\n" +
                "             label='People to invite'\n" +
                "             var='invitelist'>\n" +
                "        <desc>Tell all your friends about your new bot!</desc>\n" +
                "      </field>\n" +
                "    </x>\n";

        DataForm dataForm = unmarshal(xml, DataForm.class);

        Assert.assertNotNull(dataForm);
        Assert.assertEquals(dataForm.getType(), DataForm.Type.FORM);
        Assert.assertEquals(dataForm.getTitle(), "Bot Configuration");
        Assert.assertEquals(dataForm.getInstructions().get(0), "Fill out this form to configure your new bot!");

        Assert.assertEquals(dataForm.getFields().size(), 12);
        Assert.assertEquals(dataForm.getFields().get(0).getValues().get(0), "jabber:bot");
        Assert.assertEquals(dataForm.getFields().get(0).getType(), DataForm.Field.Type.HIDDEN);
        Assert.assertEquals(dataForm.getFields().get(0).getVar(), "FORM_TYPE");

        Assert.assertEquals(dataForm.getFields().get(1).getType(), DataForm.Field.Type.FIXED);
        Assert.assertEquals(dataForm.getFields().get(1).getValues().get(0), "Section 1: Bot Info");

        Assert.assertEquals(dataForm.getFields().get(2).getType(), DataForm.Field.Type.TEXT_SINGLE);
        Assert.assertEquals(dataForm.getFields().get(2).getVar(), "botname");
        Assert.assertEquals(dataForm.getFields().get(2).getLabel(), "The name of your bot");

        Assert.assertEquals(dataForm.getFields().get(3).getType(), DataForm.Field.Type.TEXT_MULTI);
        Assert.assertEquals(dataForm.getFields().get(3).getVar(), "description");
        Assert.assertEquals(dataForm.getFields().get(3).getLabel(), "Helpful description of your bot");

        Assert.assertEquals(dataForm.getFields().get(4).getType(), DataForm.Field.Type.BOOLEAN);
        Assert.assertEquals(dataForm.getFields().get(4).getVar(), "public");
        Assert.assertEquals(dataForm.getFields().get(4).getLabel(), "Public bot?");

        Assert.assertEquals(dataForm.getFields().get(5).getType(), DataForm.Field.Type.TEXT_PRIVATE);
        Assert.assertEquals(dataForm.getFields().get(5).getVar(), "password");
        Assert.assertEquals(dataForm.getFields().get(5).getLabel(), "Password for special access");

        Assert.assertEquals(dataForm.getFields().get(6).getType(), DataForm.Field.Type.FIXED);
        Assert.assertEquals(dataForm.getFields().get(6).getValues().get(0), "Section 2: Features");

        Assert.assertEquals(dataForm.getFields().get(7).getType(), DataForm.Field.Type.LIST_MULTI);
        Assert.assertEquals(dataForm.getFields().get(7).getVar(), "features");
        Assert.assertEquals(dataForm.getFields().get(7).getLabel(), "What features will the bot support?");
        Assert.assertEquals(dataForm.getFields().get(7).getOptions().size(), 5);
        Assert.assertEquals(dataForm.getFields().get(7).getOptions().get(0).getLabel(), "Contests");
        Assert.assertEquals(dataForm.getFields().get(7).getOptions().get(0).getValue(), "contests");
        Assert.assertEquals(dataForm.getFields().get(7).getOptions().get(1).getLabel(), "News");
        Assert.assertEquals(dataForm.getFields().get(7).getOptions().get(1).getValue(), "news");
        Assert.assertEquals(dataForm.getFields().get(7).getOptions().get(2).getLabel(), "Polls");
        Assert.assertEquals(dataForm.getFields().get(7).getOptions().get(2).getValue(), "polls");
        Assert.assertEquals(dataForm.getFields().get(7).getOptions().get(3).getLabel(), "Reminders");
        Assert.assertEquals(dataForm.getFields().get(7).getOptions().get(3).getValue(), "reminders");
        Assert.assertEquals(dataForm.getFields().get(7).getOptions().get(4).getLabel(), "Search");
        Assert.assertEquals(dataForm.getFields().get(7).getOptions().get(4).getValue(), "search");
        Assert.assertEquals(dataForm.getFields().get(7).getValues().get(0), "news");
        Assert.assertEquals(dataForm.getFields().get(7).getValues().get(1), "search");

        Assert.assertEquals(dataForm.getFields().get(8).getType(), DataForm.Field.Type.FIXED);
        Assert.assertEquals(dataForm.getFields().get(8).getValues().get(0), "Section 3: Subscriber List");

        Assert.assertEquals(dataForm.getFields().get(9).getType(), DataForm.Field.Type.LIST_SINGLE);
        Assert.assertEquals(dataForm.getFields().get(9).getVar(), "maxsubs");
        Assert.assertEquals(dataForm.getFields().get(9).getLabel(), "Maximum number of subscribers");
        Assert.assertEquals(dataForm.getFields().get(9).getOptions().size(), 6);
        Assert.assertEquals(dataForm.getFields().get(9).getOptions().get(0).getLabel(), "10");
        Assert.assertEquals(dataForm.getFields().get(9).getOptions().get(0).getValue(), "10");
        Assert.assertEquals(dataForm.getFields().get(9).getOptions().get(1).getLabel(), "20");
        Assert.assertEquals(dataForm.getFields().get(9).getOptions().get(1).getValue(), "20");
        Assert.assertEquals(dataForm.getFields().get(9).getOptions().get(2).getLabel(), "30");
        Assert.assertEquals(dataForm.getFields().get(9).getOptions().get(2).getValue(), "30");
        Assert.assertEquals(dataForm.getFields().get(9).getOptions().get(3).getLabel(), "50");
        Assert.assertEquals(dataForm.getFields().get(9).getOptions().get(3).getValue(), "50");
        Assert.assertEquals(dataForm.getFields().get(9).getOptions().get(4).getLabel(), "100");
        Assert.assertEquals(dataForm.getFields().get(9).getOptions().get(4).getValue(), "100");
        Assert.assertEquals(dataForm.getFields().get(9).getOptions().get(5).getLabel(), "None");
        Assert.assertEquals(dataForm.getFields().get(9).getOptions().get(5).getValue(), "none");
        Assert.assertEquals(dataForm.getFields().get(9).getValues().get(0), "20");

        Assert.assertEquals(dataForm.getFields().get(10).getType(), DataForm.Field.Type.FIXED);
        Assert.assertEquals(dataForm.getFields().get(10).getValues().get(0), "Section 4: Invitations");

        Assert.assertEquals(dataForm.getFields().get(11).getType(), DataForm.Field.Type.JID_MULTI);
        Assert.assertEquals(dataForm.getFields().get(11).getVar(), "invitelist");
        Assert.assertEquals(dataForm.getFields().get(11).getLabel(), "People to invite");

        Assert.assertNotNull(dataForm.findField("FORM_TYPE"));
    }
}
