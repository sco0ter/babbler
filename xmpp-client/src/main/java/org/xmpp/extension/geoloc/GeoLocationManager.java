/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Christian Schudt
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

package org.xmpp.extension.geoloc;

import org.xmpp.*;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.pubsub.Item;
import org.xmpp.extension.pubsub.PubSubManager;
import org.xmpp.extension.pubsub.PubSubService;
import org.xmpp.extension.pubsub.event.Event;
import org.xmpp.stanza.MessageEvent;
import org.xmpp.stanza.MessageListener;
import org.xmpp.stanza.StanzaException;
import org.xmpp.stanza.client.Message;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Christian Schudt
 */
public final class GeoLocationManager extends ExtensionManager {

    private static final Logger logger = Logger.getLogger(GeoLocationManager.class.getName());

    private final Set<GeoLocationListener> geoLocationListeners = new CopyOnWriteArraySet<>();

    private PubSubService pepService;

    private GeoLocationManager(XmppSession xmppSession) {
        super(xmppSession, GeoLocation.NAMESPACE, GeoLocation.NAMESPACE + "+notify");

        xmppSession.addConnectionListener(new ConnectionListener() {
            @Override
            public void statusChanged(ConnectionEvent e) {
                if (e.getStatus() == XmppSession.Status.CLOSED) {
                    geoLocationListeners.clear();
                }
            }
        });

        xmppSession.addMessageListener(new MessageListener() {
            @Override
            public void handle(MessageEvent e) {
                if (e.isIncoming() && isEnabled()) {
                    Message message = e.getMessage();
                    Event event = message.getExtension(Event.class);
                    if (event != null) {
                        for (Item item : event.getItems()) {
                            Object payload = item.getPayload();
                            if (payload instanceof GeoLocation) {
                                // Notify the listeners about the reception.
                                for (GeoLocationListener geoLocationListener : geoLocationListeners) {
                                    try {
                                        geoLocationListener.geoLocationUpdated(new GeoLocationEvent(GeoLocationManager.this, (GeoLocation) payload, message.getFrom()));
                                    } catch (Exception ex) {
                                        logger.log(Level.WARNING, ex.getMessage(), ex);
                                    }
                                }
                            }
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
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     */
    public void publish(GeoLocation geoLocation) throws XmppException {
        pepService = xmppSession.getExtensionManager(PubSubManager.class).createPersonalEventingService();
        //pepService.publish(GeoLocation.NAMESPACE, geoLocation);
    }

    /**
     * Adds a listener, which allows to listen for geo location changes.
     *
     * @param geoLocationListener The listener.
     * @see #removeGeoLocationListener(GeoLocationListener)
     */
    public void addGeoLocationListener(GeoLocationListener geoLocationListener) {
        geoLocationListeners.add(geoLocationListener);
    }

    /**
     * Removes a previously added geo location listener.
     *
     * @param geoLocationListener The listener.
     * @see #addGeoLocationListener(GeoLocationListener)
     */
    public void removeGeoLocationListener(GeoLocationListener geoLocationListener) {
        geoLocationListeners.remove(geoLocationListener);
    }
}
