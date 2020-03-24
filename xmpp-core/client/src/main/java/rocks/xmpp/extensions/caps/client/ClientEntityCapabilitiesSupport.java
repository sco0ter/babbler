package rocks.xmpp.extensions.caps.client;

import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.OutboundPresenceHandler;
import rocks.xmpp.core.stanza.PresenceEvent;
import rocks.xmpp.core.stanza.model.Presence;
import rocks.xmpp.core.stream.client.StreamFeaturesManager;
import rocks.xmpp.extensions.caps.AbstractEntityCapabilitiesProtocol;
import rocks.xmpp.extensions.caps.model.EntityCapabilities;
import rocks.xmpp.extensions.caps.model.EntityCapabilities1;
import rocks.xmpp.extensions.disco.client.ClientServiceDiscoveryManager;
import rocks.xmpp.im.subscription.PresenceManager;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * @author Christian Schudt
 */
public final class ClientEntityCapabilitiesSupport implements OutboundPresenceHandler {

    private final AbstractEntityCapabilitiesProtocol<? extends EntityCapabilities> entityCapabilitiesProtocol;

    public ClientEntityCapabilitiesSupport(XmppSession xmppSession, AbstractEntityCapabilitiesProtocol<? extends EntityCapabilities> entityCapabilitiesProtocol) {
        this.entityCapabilitiesProtocol = entityCapabilitiesProtocol;

        xmppSession.getManager(ClientServiceDiscoveryManager.class).addCapabilitiesChangeListener(evt -> {
            // If we haven't established a presence session yet, don't care about changes in service discovery.
            // If we change features during a presence session, update the verification string and resend presence.

            // https://xmpp.org/extensions/xep-0115.html#advertise:
            // "If the supported features change during a generating entity's presence session (e.g., a user installs an updated version of a client plugin), the application MUST recompute the verification string and SHOULD send a new presence broadcast."

            // Resend presence. This manager will add the caps extension later.
            PresenceManager presenceManager = xmppSession.getManager(PresenceManager.class);
            Presence lastPresence = presenceManager.getLastSentPresence();

            if (lastPresence != null) {
                // Whenever the verification string has changed, publish the info node.
                entityCapabilitiesProtocol.publishCapsNode();

                xmppSession.send(new Presence(null, lastPresence.getType(), lastPresence.getShow(), lastPresence.getStatuses(), lastPresence.getPriority(), null, null, lastPresence.getLanguage(), null, null));
            }
        });
        xmppSession.addSessionStatusListener(e -> {
            if (e.getStatus() == XmppSession.Status.AUTHENTICATED) {// As soon as we are authenticated, check if the server has advertised Entity Capabilities in its stream features.
                List<EntityCapabilities1> serverCapabilities1 = xmppSession.getManager(StreamFeaturesManager.class).getFeatures(EntityCapabilities1.class);
                // If yes, treat it as other caps.
                if (!serverCapabilities1.isEmpty()) {
                    entityCapabilitiesProtocol.handleEntityCapabilities(serverCapabilities1.get(0), xmppSession.getDomain());
                }
            }
        });
    }

    @Override
    public final void handleOutboundPresence(PresenceEvent e) {
        final Presence presence = e.getPresence();
        if (presence.isAvailable()) {
            if (entityCapabilitiesProtocol.getPublishedNodes().isEmpty()) {
                entityCapabilitiesProtocol.publishCapsNode();
            }
            // a client SHOULD include entity capabilities with every presence notification it sends.
            // Get the last generated verification string here.
            Deque<EntityCapabilities> publishedEntityCaps = new ArrayDeque<>(entityCapabilitiesProtocol.getPublishedNodes().values());
            EntityCapabilities lastPublishedEntityCaps = publishedEntityCaps.getLast();
            presence.putExtension(lastPublishedEntityCaps);
        }
    }
}
