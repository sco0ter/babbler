package org.xmpp.extension.pubsub;

import java.net.URI;

/**
 * Represents the {@code <redirect/>} element, which is used in both 'pubsub#event' and 'pubsub#owner' namespace.
 *
 * @author Christian Schudt
 */
public interface Redirect {

    /**
     * Gets the URI to redirect.
     *
     * @return The URI.
     */
    URI getUri();
}
