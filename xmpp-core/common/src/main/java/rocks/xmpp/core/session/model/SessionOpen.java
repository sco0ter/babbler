package rocks.xmpp.core.session.model;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.LanguageElement;
import rocks.xmpp.core.stream.model.StreamElement;

/**
 * This interface describes the common attributes for opening a XMPP session.
 *
 * <p>It is shared by different connection methods: TCP socket, BOSH and WebSocket, which all have different ways to
 * open a session (initial stream header for socket connection, session creation request for BOSH, open frame for
 * WebSockets).</p>
 *
 * <p>All session openings have the 'from', 'to', 'xml:lang' and an 'id' attribute in common.</p>
 *
 * @author Christian Schudt
 */
public interface SessionOpen extends LanguageElement, StreamElement {

    /**
     * Gets the 'from' attribute.
     *
     * @return The 'from' attribute.
     */
    Jid getFrom();

    /**
     * Gets the 'to' attribute.
     *
     * @return The 'to' attribute.
     */
    Jid getTo();

    /**
     * Gets the 'id' attribute.
     *
     * @return The 'id' attribute.
     */
    String getId();

    /**
     * Gets the XMPP version.
     *
     * @return The XMPP version.
     */
    String getVersion();
}
