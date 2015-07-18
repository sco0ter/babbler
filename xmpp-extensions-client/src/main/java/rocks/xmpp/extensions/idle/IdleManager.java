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

package rocks.xmpp.extensions.idle;

import rocks.xmpp.core.session.Manager;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.PresenceEvent;
import rocks.xmpp.core.stanza.model.Presence;
import rocks.xmpp.core.stanza.model.Presence.Show;
import rocks.xmpp.extensions.idle.model.Idle;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * This manager automatically adds an {@code <idle/>} extension to outbound presences, if the presence is of type {@linkplain Show#AWAY away} or {@linkplain Show#XA xa}.
 * However, sending such presences is still the responsibility of the application developer, i.e. no presences are sent automatically.
 * <p>
 * By default, idle time is determined by outbound messages and non-away, non-xa presences. E.g. whenever a message is sent, the idle time is reset to the current time.
 * Then, when a 'away' or 'xa' presence is sent, the {@code <idle/>} extension is added with the date of the last sent message.
 * <p>
 * The strategy for determining last user interaction can be changed by {@linkplain #setIdleStrategy(Supplier) setting a supplier} which returns the timestamp of last user interaction.
 * Possible alternative strategies is to track mouse movement or keyboard interaction for which cases you would set a supplier which gets the date of the last mouse movement.
 *
 * @author Christian Schudt
 */
public final class IdleManager extends Manager {

    private Supplier<Instant> idleStrategy;

    private Instant lastSentStanza = Instant.now();

    private final Consumer<PresenceEvent> presenceListener;

    private IdleManager(XmppSession xmppSession) {
        super(xmppSession);
        this.idleStrategy = this::getLastSentStanzaTime;
        this.presenceListener = e -> {
            Presence presence = e.getPresence();
            synchronized (this) {
                if (presence.isAvailable() && EnumSet.of(Presence.Show.AWAY, Presence.Show.XA).contains(presence.getShow())) {
                    if (idleStrategy != null && !presence.hasExtension(Idle.class)) {
                        Instant idleSince = idleStrategy.get();
                        if (idleSince != null) {
                            presence.addExtension(Idle.since(OffsetDateTime.ofInstant(idleSince, ZoneOffset.UTC)));
                        }
                    }
                } else {
                    lastSentStanza = Instant.now();
                }
            }
        };
    }

    @Override
    protected void initialize() {
        xmppSession.addOutboundMessageListener(e -> {
            synchronized (this) {
                lastSentStanza = Instant.now();
            }
        });
    }

    @Override
    protected void onEnable() {
        super.onEnable();
        xmppSession.addOutboundPresenceListener(presenceListener);
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        xmppSession.removeOutboundPresenceListener(presenceListener);
    }

    /**
     * Gets the time of the last sent message or non-away, non-xa presence.
     * <p>
     * This is the default strategy for determining last user interaction.
     *
     * @return The time of the last sent stanza.
     */
    public synchronized Instant getLastSentStanzaTime() {
        return lastSentStanza;
    }

    /**
     * Sets an idle strategy, i.e. a supplier for last user interaction.
     *
     * @param idleStrategy The strategy.
     * @see #getLastSentStanzaTime()
     */
    public synchronized void setIdleStrategy(Supplier<Instant> idleStrategy) {
        this.idleStrategy = idleStrategy;
    }

    /**
     * Gets the current idle strategy, i.e. a supplier for last user interaction.
     *
     * @return The strategy or null if no strategy is set.
     */
    public synchronized Supplier<Instant> getIdleStrategy() {
        return idleStrategy;
    }
}
