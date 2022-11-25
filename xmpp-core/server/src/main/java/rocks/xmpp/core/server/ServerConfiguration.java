package rocks.xmpp.core.server;

import java.util.Locale;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import rocks.xmpp.addr.Jid;

/**
 * @author Christian Schudt
 */
public interface ServerConfiguration {

    JAXBContext getJAXBContext();

    Marshaller getMarshaller();

    Unmarshaller getUnmarshaller(Locale locale);

    int getPort();

    Jid getDomain();
}
