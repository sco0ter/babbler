package rocks.xmpp.core.server;

import java.util.Locale;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

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
