package org.xmpp.extension.servicediscovery.items;

import org.xmpp.extension.servicediscovery.info.Feature;
import org.xmpp.extension.servicediscovery.info.Identity;

import java.util.List;
import java.util.Set;

/**
 * @author Christian Schudt
 */
public interface ItemNode {
    /**
     * Gets the node name.
     *
     * @return The node.
     */
    String getNode();

    /**
     * Gets the features.
     *
     * @return The features.
     */
    List<Item> getItems();
}
