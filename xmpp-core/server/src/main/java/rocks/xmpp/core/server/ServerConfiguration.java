package rocks.xmpp.core.server;

import rocks.xmpp.addr.Jid;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * @author Christian Schudt
 */
public interface ServerConfiguration {

    JAXBContext getJAXBContext();

    Marshaller getMarshaller();

    Unmarshaller getUnmarshaller();

    int getPort();

    Jid getDomain();
}
