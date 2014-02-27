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

package org.xmpp.extension.activity;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.BaseTest;
import org.xmpp.UnmarshalHelper;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;

/**
 * @author Christian Schudt
 */
public class ActivityTest extends BaseTest {

    @Test
    public void unmarshalActivity() throws XMLStreamException, JAXBException {
        String xml = "<activity xmlns='http://jabber.org/protocol/activity'>\n" +
                "  <relaxing>\n" +
                "    <partying/>\n" +
                "  </relaxing>\n" +
                "  <text xml:lang='en'>My nurse&apos;s birthday!</text>\n" +
                "</activity>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        Activity activity = (Activity) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertNotNull(activity);
        Assert.assertEquals(activity.getText(), "My nurse's birthday!");
        Assert.assertTrue(activity.getCategory() instanceof Category.Relaxing);
    }

    @Test
    public void unmarshalSpecificSpecificActivity() throws XMLStreamException, JAXBException {
        String xml = "<activity xmlns='http://jabber.org/protocol/activity'>\n" +
                "  <inactive>\n" +
                "    <sleeping>\n" +
                "      <hibernating xmlns='http://www.ursus.info/states'/>\n" +
                "    </sleeping>\n" +
                "  </inactive>\n" +
                "</activity>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        Activity activity = (Activity) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertNotNull(activity);
        Assert.assertTrue(activity.getCategory() instanceof Category.Inactive);
        Assert.assertTrue(activity.getCategory().getSpecificActivity() instanceof SpecificActivity.Sleeping);
        Assert.assertNull(activity.getCategory().getSpecificActivity().getSpecificActivity());
    }

    @Test
    public void marshalDoingChores() throws JAXBException, XMLStreamException, IOException {
        String xml = marshall(new Activity(new Category.DoingChores()));
        Assert.assertEquals(xml, "<activity xmlns=\"http://jabber.org/protocol/activity\"><doing_chores></doing_chores></activity>");
    }

    @Test
    public void marshalDoingChoresWithText() throws JAXBException, XMLStreamException, IOException {
        String xml = marshall(new Activity(new Category.DoingChores(), "test"));
        Assert.assertEquals(xml, "<activity xmlns=\"http://jabber.org/protocol/activity\"><doing_chores></doing_chores><text>test</text></activity>");
    }
}
