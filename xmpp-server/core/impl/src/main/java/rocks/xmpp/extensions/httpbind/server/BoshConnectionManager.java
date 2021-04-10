/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2018 Christian Schudt
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

package rocks.xmpp.extensions.httpbind.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Singleton;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import rocks.xmpp.extensions.httpbind.model.Body;
import rocks.xmpp.session.server.InboundClientSession;

/**
 * @author Christian Schudt
 */
@Path("/http-bind")
@Singleton
public final class BoshConnectionManager {

    private final Map<String, BoshConnection> connections = new ConcurrentHashMap<>();

    final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    @POST
    @Produces(MediaType.TEXT_XML)
    public void handleRequest(final Body body, @Suspended final AsyncResponse asyncResponse,
                              @Context final SecurityContext securityContext) {
        try {
            if (body.getRid() == null || body.getRid() < 0) {
                asyncResponse
                        .resume(Body.builder().type(Body.Type.TERMINATE).condition(Body.Condition.BAD_REQUEST).build());
                return;
            }

            // Session Creation Request
            if (body.getSid() == null) {
                // We received an error from our MessageBodyReader, immediately send it to the client.
                if (body.getType() == Body.Type.TERMINATE) {
                    asyncResponse.resume(body);
                    return;
                }
                InboundClientSession session = CDI.current().select(InboundClientSession.class).get();
                BoshConnection connection =
                        new BoshConnection(session::handleElement, body, asyncResponse, securityContext, this);
                session.setConnection(connection);
                connections.put(session.getId(), connection);
                connection.closeFuture().whenComplete((aVoid, throwable) -> connections.remove(session.getId()));

                // Handle session creation request.
                session.handleElement(body);
            } else {
                // Subsequent requests
                BoshConnection boshConnection = connections.get(body.getSid());
                if (boshConnection == null) {
                    asyncResponse
                            .resume(Body.builder().type(Body.Type.TERMINATE).condition(Body.Condition.ITEM_NOT_FOUND)
                                    .build());
                } else {
                    boshConnection.requestReceived(body, asyncResponse, securityContext);
                }
            }
        } catch (Exception e) {
            asyncResponse
                    .resume(Body.builder().type(Body.Type.TERMINATE).condition(Body.Condition.INTERNAL_SERVER_ERROR)
                            .build());
        }
    }

    final Map<String, BoshConnection> getConnections() {
        return connections;
    }
}
