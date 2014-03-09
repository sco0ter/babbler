package org.xmpp.extension.pubsub;

import org.xmpp.extension.data.DataForm;

/**
 * Represents the {@code <configuration/>} element, which is used in both 'pubsub' and 'pubsub#event' namespace.
 *
 * @author Christian Schudt
 */
public interface Configuration {

    DataForm getConfigurationForm();

    String getNode();
}
