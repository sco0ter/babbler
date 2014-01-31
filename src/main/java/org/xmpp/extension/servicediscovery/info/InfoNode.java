package org.xmpp.extension.servicediscovery.info;

import java.util.Set;

/**
 * @author Christian Schudt
 */
public interface InfoNode {

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
    Set<Feature> getFeatures();

    /**
     * Gets the identities.
     *
     * @return The identities.
     */
    Set<Identity> getIdentities();
}
