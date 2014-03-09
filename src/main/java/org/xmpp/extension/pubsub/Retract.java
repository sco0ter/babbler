package org.xmpp.extension.pubsub;

/**
 * Represents the {@code <retract/>} element, which is used in both 'pubsub' and 'pubsub#owner' namespace.
 *
 * @author Christian Schudt
 */
public interface Retract {

    /**
     * Gets the id of the item to retract (in owner context) or the retracted item (in event context).
     *
     * @return The node.
     */
    String getId();
}
