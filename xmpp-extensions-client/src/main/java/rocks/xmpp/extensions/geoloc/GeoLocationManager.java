/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Christian Schudt
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package rocks.xmpp.extensions.geoloc;

import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.XmppUtils;
import rocks.xmpp.core.session.ExtensionManager;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.model.client.Message;
import rocks.xmpp.extensions.geoloc.model.GeoLocation;
import rocks.xmpp.extensions.pubsub.PubSubManager;
import rocks.xmpp.extensions.pubsub.PubSubService;
import rocks.xmpp.extensions.pubsub.model.Item;
import rocks.xmpp.extensions.pubsub.model.event.Event;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Manages the publishing of user location and the notification of it.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0080.html">XEP-0080: User Location</a>
 */
public final class GeoLocationManager extends ExtensionManager {

    private static final Logger logger = Logger.getLogger(GeoLocationManager.class.getName());

    private final Set<Consumer<GeoLocationEvent>> geoLocationListeners = new CopyOnWriteArraySet<>();

    private GeoLocationManager(XmppSession xmppSession) {
        super(xmppSession, GeoLocation.NAMESPACE, GeoLocation.NAMESPACE + "+notify");
    }

    @Override
    protected void initialize() {
        xmppSession.addSessionStatusListener(e -> {
            if (e.getStatus() == XmppSession.Status.CLOSED) {
                geoLocationListeners.clear();
            }
        });
        xmppSession.addInboundMessageListener(e -> {
            if (isEnabled()) {
                Message message = e.getMessage();
                Event event = message.getExtension(Event.class);
                if (event != null) {
                    for (Item item : event.getItems()) {
                        Object payload = item.getPayload();
                        if (payload instanceof GeoLocation) {
                            // Notify the listeners about the reception.
                            XmppUtils.notifyEventListeners(geoLocationListeners, new GeoLocationEvent(GeoLocationManager.this, (GeoLocation) payload, message.getFrom()));
                        }
                    }
                }
            }
        });
    }

    /**
     * Publishes a geo location to the personal eventing service.
     *
     * @param geoLocation The geo location.
     * @throws rocks.xmpp.core.stanza.StanzaException      If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException If the entity did not respond.
     */
    public void publish(GeoLocation geoLocation) throws XmppException {
        PubSubService pepService = xmppSession.getManager(PubSubManager.class).createPersonalEventingService();
        pepService.node(GeoLocation.NAMESPACE).publish(geoLocation);
    }

    /**
     * Adds a listener, which allows to listen for geo location changes.
     *
     * @param geoLocationListener The listener.
     * @see #removeGeoLocationListener(Consumer)
     */
    public void addGeoLocationListener(Consumer<GeoLocationEvent> geoLocationListener) {
        geoLocationListeners.add(geoLocationListener);
    }

    /**
     * Removes a previously added geo location listener.
     *
     * @param geoLocationListener The listener.
     * @see #addGeoLocationListener(Consumer)
     */
    public void removeGeoLocationListener(Consumer<GeoLocationEvent> geoLocationListener) {
        geoLocationListeners.remove(geoLocationListener);
    }
}
