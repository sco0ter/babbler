package org.xmpp.extension.pubsub;

import org.xmpp.Jid;

import java.util.Date;

/**
 * Represents the {@code <subscription/>} element, which is used in both 'pubsub' and 'pubsub#owner' namespace.
 *
 * @author Christian Schudt
 */
public interface Subscription {

    Jid getJid();

    String getNode();

    String getSubId();

    SubscriptionStatus getSubscriptionStatus();

    Date getExpiry();

    boolean isConfigurationRequired();

    boolean isConfigurationSupported();
}
