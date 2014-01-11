package org.xmpp.extension.stanzaforwarding;

import org.xmpp.extension.delayeddelivery.DelayedDelivery;
import org.xmpp.stanza.IQ;
import org.xmpp.stanza.Message;
import org.xmpp.stanza.Presence;
import org.xmpp.stanza.Stanza;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The implementation of the {@code <forwarded/>} element.
 *
 * @author Christian Schudt
 */
@XmlRootElement(name = "forwarded")
@XmlAccessorType(XmlAccessType.FIELD)
public final class Forwarded {

    @XmlElementRef
    private DelayedDelivery delayedDelivery;

    @XmlElementRef
    private Message message;

    @XmlElementRef
    private Presence presence;

    @XmlElementRef
    private IQ iq;

    private Forwarded() {

    }

    /**
     * Creates a forwarded element.
     *
     * @param delayedDelivery The delayed delivery, which indicates, when the forwarded stanza was originally received by the forwarder.
     * @param stanza          The stanza to forward.
     */
    public Forwarded(DelayedDelivery delayedDelivery, Stanza stanza) {
        this.delayedDelivery = delayedDelivery;
        if (stanza instanceof Message) {
            this.message = (Message) stanza;
        } else if (stanza instanceof Presence) {
            this.presence = (Presence) stanza;
        } else if (stanza instanceof IQ) {
            this.iq = (IQ) stanza;
        }
    }

    /**
     * Gets the date, when the forwarding entity received the forwarded stanza.
     *
     * @return The delay.
     */
    public DelayedDelivery getDelayedDelivery() {
        return delayedDelivery;
    }

    /**
     * Gets the forwarded message.
     *
     * @return The forwarded message.
     */
    public Message getMessage() {
        return message;
    }

    /**
     * Gets the forwarded presence.
     *
     * @return The forwarded presence.
     */
    public Presence getPresence() {
        return presence;
    }

    /**
     * Gets the forwarded iq.
     *
     * @return The forwarded iq.
     */
    public IQ getIq() {
        return iq;
    }
}
