/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 Christian Schudt
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

package rocks.xmpp.session.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import javax.enterprise.context.ApplicationScoped;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.Session;

/**
 * @author Christian Schudt
 */
@ApplicationScoped
public class SessionManager {

    private Map<Jid, Session> sessionMap = new ConcurrentHashMap<>();

    public void addSession(Jid address, InboundClientSession session) {
        session.getConnection().closeFuture().whenComplete((result, exc) -> sessionMap.remove(address));
        sessionMap.put(address, session);
    }

    public Stream<Session> getUserSessions(Jid bareJid) {
        return sessionMap.values().stream().filter(session -> {
            Jid remoteAddress = session.getRemoteXmppAddress();
            return remoteAddress != null && remoteAddress.asBareJid().equals(bareJid);
        });
    }

    public Session getSession(Jid fullJid) {
        return sessionMap.get(fullJid);
    }
}
