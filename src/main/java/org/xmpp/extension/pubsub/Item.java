package org.xmpp.extension.pubsub;

/**
 * @author Christian Schudt
 */
public interface Item {

    Object getPayload();

    String getId();

    String getNode();

    String getPublisher();

}
