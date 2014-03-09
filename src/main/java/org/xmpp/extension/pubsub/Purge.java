package org.xmpp.extension.pubsub;

/**
 * Represents the {@code <purge/>} element, which is used in both 'pubsub#event' and 'pubsub#owner' namespace.
 *
 * @author Christian Schudt
 */
public interface Purge {
    /**
     * Gets the node to purge (in owner context) or the purged node (in event context).
     *
     * @return The node.
     */
    String getNode();
}
