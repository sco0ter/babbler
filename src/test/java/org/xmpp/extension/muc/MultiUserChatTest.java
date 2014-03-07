package org.xmpp.extension.muc;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.BaseTest;
import org.xmpp.stanza.Presence;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author Christian Schudt
 */
public class MultiUserChatTest extends BaseTest {

    @Test
    public void testEnterRoom() throws JAXBException, XMLStreamException, IOException {
        Presence presence = new Presence();
        presence.getExtensions().add(new Muc());
        String xml = marshall(presence);
        Assert.assertEquals(xml, "<presence><x xmlns=\"http://jabber.org/protocol/muc\"></x></presence>");
    }

    @Test
    public void testEnterRoomWithPassword() throws JAXBException, XMLStreamException, IOException {
        Presence presence = new Presence();
        presence.getExtensions().add(new Muc("cauldronburn"));
        String xml = marshall(presence);
        Assert.assertEquals(xml, "<presence><x xmlns=\"http://jabber.org/protocol/muc\"><password>cauldronburn</password></x></presence>");
    }

    @Test
    public void testEnterRoomWithHistoryMaxChars() throws JAXBException, XMLStreamException, IOException {
        Presence presence = new Presence();
        presence.getExtensions().add(new Muc(History.forMaxChars(65000)));
        String xml = marshall(presence);
        Assert.assertEquals(xml, "<presence><x xmlns=\"http://jabber.org/protocol/muc\"><history maxchars=\"65000\"></history></x></presence>");
    }

    @Test
    public void testEnterRoomWithHistoryMaxStanzas() throws JAXBException, XMLStreamException, IOException {
        Presence presence = new Presence();
        presence.getExtensions().add(new Muc(History.forMaxStanzas(20)));
        String xml = marshall(presence);
        Assert.assertEquals(xml, "<presence><x xmlns=\"http://jabber.org/protocol/muc\"><history maxstanzas=\"20\"></history></x></presence>");
    }

    @Test
    public void testEnterRoomWithHistorySeconds() throws JAXBException, XMLStreamException, IOException {
        Presence presence = new Presence();
        presence.getExtensions().add(new Muc(History.forSeconds(180)));
        String xml = marshall(presence);
        Assert.assertEquals(xml, "<presence><x xmlns=\"http://jabber.org/protocol/muc\"><history seconds=\"180\"></history></x></presence>");
    }

    @Test
    public void testEnterRoomWithHistorySince() throws JAXBException, XMLStreamException, IOException {
        Date date = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        Presence presence = new Presence();
        presence.getExtensions().add(new Muc(History.since(date)));
        String xml = marshall(presence);
        Assert.assertEquals(xml, "<presence><x xmlns=\"http://jabber.org/protocol/muc\"><history since=\"" + DatatypeConverter.printDateTime(calendar) + "\"></history></x></presence>");
    }
}
