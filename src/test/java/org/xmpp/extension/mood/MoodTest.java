package org.xmpp.extension.mood;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.BaseTest;
import org.xmpp.UnmarshalHelper;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;

/**
 * @author Christian Schudt
 */
public class MoodTest extends BaseTest {

    @Test
    public void unmarshalMood() throws XMLStreamException, JAXBException {
        String xml = "<mood xmlns='http://jabber.org/protocol/mood'>\n" +
                "  <happy/>\n" +
                "  <text>Yay, the mood spec has been approved!</text>\n" +
                "</mood>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        Mood mood = (Mood) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertNotNull(mood);
        Assert.assertEquals(mood.getText(), "Yay, the mood spec has been approved!");
    }
}
