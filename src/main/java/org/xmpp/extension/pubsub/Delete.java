package org.xmpp.extension.pubsub;

/**
 * Represents the {@code <redirect/>} element, which is used in both 'pubsub#event' and 'pubsub#owner' namespace.
 *
 * @author Christian Schudt
 */
public interface Delete {

    /**
     * Gets the node to delete (in owner context) or the deleted node (in event context).
     *
     * @return The node.
     */
    String getNode();

    /**
     * The redirect element.
     *
     * @return The redirect element.
     */
    Redirect getRedirect();
}
