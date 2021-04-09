/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2020 Christian Schudt
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

package rocks.xmpp.extensions.rpc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.ExtensionProtocol;
import rocks.xmpp.core.stanza.AbstractIQHandler;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.extensions.disco.model.info.DiscoverableInfo;
import rocks.xmpp.extensions.disco.model.info.Identity;
import rocks.xmpp.extensions.disco.model.info.InfoDiscovery;
import rocks.xmpp.extensions.disco.model.info.InfoProvider;
import rocks.xmpp.extensions.rpc.model.Rpc;
import rocks.xmpp.extensions.rpc.model.Value;

/**
 * This manager allows you to call remote procedures and handle inbound calls, if enabled.
 *
 * <p>By default this manager is disabled. If you want to support RPC (i.e. handle inbound calls) {@linkplain #setRpcHandler(RpcHandler) set a RPC handler}, which allows you to handle inbound calls.</p>
 *
 * @author Christian Schudt
 * @see <a href="https://xmpp.org/extensions/xep-0009.html">XEP-0009: Jabber-RPC</a>
 */
public abstract class AbstractRpcManager extends AbstractIQHandler implements InfoProvider, RpcManager, ExtensionProtocol {

    private static final Set<String> FEATURES = Collections.singleton(Rpc.NAMESPACE);

    private static final System.Logger logger = System.getLogger(AbstractRpcManager.class.getName());

    private RpcHandler rpcHandler;

    protected AbstractRpcManager() {
        super(Rpc.class, IQ.Type.SET);
    }

    @Override
    public final synchronized void setRpcHandler(RpcHandler rpcHandler) {
        this.rpcHandler = rpcHandler;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@value Rpc#NAMESPACE}
     */
    @Override
    public final String getNamespace() {
        return Rpc.NAMESPACE;
    }

    @Override
    public final synchronized boolean isEnabled() {
        return rpcHandler != null;
    }

    @Override
    public final Set<String> getFeatures() {
        return FEATURES;
    }

    @Override
    protected final IQ processRequest(IQ iq) {
        Rpc rpc = iq.getExtension(Rpc.class);
        // If there's an inbound RPC
        RpcHandler rpcHandler1;
        synchronized (this) {
            rpcHandler1 = rpcHandler;
        }
        if (rpcHandler1 != null) {
            final Rpc.MethodCall methodCall = rpc.getMethodCall();
            final List<Value> parameters = new ArrayList<>(methodCall.getParameters());

            try {
                Value value = rpcHandler1.process(iq.getFrom(), methodCall.getMethodName(), parameters);
                return iq.createResult(Rpc.ofMethodResponse(value));
            } catch (RpcException e1) {
                return iq.createResult(Rpc.ofFaultResponse(e1.getFaultCode(), e1.getFaultString()));
            } catch (Throwable e1) {
                logger.log(System.Logger.Level.WARNING, e1.getMessage(), e1);
                return iq.createError(Condition.INTERNAL_SERVER_ERROR);
            }
        }
        return iq.createError(Condition.SERVICE_UNAVAILABLE);
    }

    @Override
    public final DiscoverableInfo getInfo(Jid to, Jid from, String node, Locale locale) {
        if (isEnabled()) {
            return new InfoDiscovery(Collections.singleton(Identity.automationRpc()), FEATURES, Collections.emptyList());
        }
        return null;
    }
}
