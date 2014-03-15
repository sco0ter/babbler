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

package org.xmpp.extension.offline;

import org.xmpp.Connection;
import org.xmpp.Jid;
import org.xmpp.NoResponseException;
import org.xmpp.XmppException;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.data.DataForm;
import org.xmpp.extension.disco.ServiceDiscoveryManager;
import org.xmpp.extension.disco.info.Feature;
import org.xmpp.extension.disco.info.InfoNode;
import org.xmpp.extension.disco.items.Item;
import org.xmpp.extension.disco.items.ItemNode;
import org.xmpp.stanza.IQ;
import org.xmpp.stanza.StanzaException;

import java.util.ArrayList;
import java.util.List;

/**
 * This manager covers the use cases of <a href="http://xmpp.org/extensions/xep-0013.html">XEP-0013: Flexible Offline Message Retrieval</a>.
 * <p>
 * Offline Message Retrieval must be used before sending initial presence, in order to tell the server, that it must not flood the client with offline messages later.
 * </p>
 * Enabling or disabling this manager has no effect.
 *
 * @author Christian Schudt
 */
public final class OfflineMessageManager extends ExtensionManager {


    private OfflineMessageManager(Connection connection) {
        super(connection);
    }

    /**
     * Discovers support for flexible offline message retrieval.
     *
     * @return True, if the server supports flexible offline message retrieval; otherwise false.
     * @throws StanzaException     If the server returned a stanza error.
     * @throws NoResponseException If the server did not respond.
     */
    public boolean isSupported() throws XmppException {
        ServiceDiscoveryManager serviceDiscoveryManager = connection.getExtensionManager(ServiceDiscoveryManager.class);
        InfoNode infoNode = serviceDiscoveryManager.discoverInformation(null);
        return infoNode.getFeatures().contains(new Feature(OfflineMessage.NAMESPACE));
    }

    /**
     * Gets the number of offline messages.
     *
     * @return The number of offline messages.
     * @throws StanzaException     If the server returned a stanza error.
     * @throws NoResponseException If the server did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0013.html#request-number">2.2 Requesting Number of Messages</a>
     */
    public int requestNumberOfMessages() throws XmppException {
        ServiceDiscoveryManager serviceDiscoveryManager = connection.getExtensionManager(ServiceDiscoveryManager.class);
        InfoNode infoDiscovery = serviceDiscoveryManager.discoverInformation(null, OfflineMessage.NAMESPACE);
        if (!infoDiscovery.getExtensions().isEmpty()) {
            DataForm dataForm = infoDiscovery.getExtensions().get(0);
            if (dataForm != null) {
                for (DataForm.Field field : dataForm.getFields()) {
                    if ("number_of_messages".equals(field.getVar())) {
                        String numberOfMessages = field.getValues().get(0);
                        return Integer.parseInt(numberOfMessages);
                    }
                }
            }
        }
        return 0;
    }

    /**
     * Gets the offline message headers.
     *
     * @return The list of message headers.
     * @throws StanzaException     If the server returned a stanza error.
     * @throws NoResponseException If the server did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0013.html#request-headers">2.3 Requesting Message Headers</a>
     */
    public List<OfflineMessageHeader> requestMessageHeaders() throws XmppException {
        List<OfflineMessageHeader> result = new ArrayList<>();
        ServiceDiscoveryManager serviceDiscoveryManager = connection.getExtensionManager(ServiceDiscoveryManager.class);
        ItemNode itemNode = serviceDiscoveryManager.discoverItems(null, OfflineMessage.NAMESPACE);
        for (Item item : itemNode.getItems()) {
            result.add(new OfflineMessageHeader(Jid.valueOf(item.getName()), item.getNode()));
        }
        return result;
    }

    /**
     * Requests a specific offline message. The message will be sent in a normal way and contains the {@link OfflineMessage} extension, which can be used to match the id {@link org.xmpp.extension.offline.OfflineMessage#getId()}.
     *
     * @param id The offline message id, which corresponds to {@link org.xmpp.extension.offline.OfflineMessageHeader#getId()}
     * @throws StanzaException     If the server returned a stanza error.
     * @throws NoResponseException If the server did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0013.html#retrieve-specific">2.4 Retrieving Specific Messages</a>
     */
    public void requestMessage(String id) throws XmppException {
        connection.query(new IQ(IQ.Type.GET, new OfflineMessage(new OfflineMessage.Item(id, OfflineMessage.Item.Action.VIEW))));
    }

    /**
     * Removes specific offline messages.
     *
     * @param ids The offline message ids, which correspond to {@link org.xmpp.extension.offline.OfflineMessageHeader#getId()}
     * @throws StanzaException     If the server returned a stanza error.
     * @throws NoResponseException If the server did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0013.html#remove-specific">2.5 Removing Specific Messages</a>
     */
    public void removeMessages(String... ids) throws XmppException {
        OfflineMessage offlineMessage = new OfflineMessage();
        for (String id : ids) {
            offlineMessage.getItems().add(new OfflineMessage.Item(id, OfflineMessage.Item.Action.REMOVE));
        }
        connection.query(new IQ(IQ.Type.SET, offlineMessage));
    }

    /**
     * Requests all offline messages.
     *
     * @throws StanzaException     If the server returned a stanza error.
     * @throws NoResponseException If the server did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0013.html#retrieve-all">2.6 Retrieving All Messages</a>
     */
    public void requestAllMessages() throws XmppException {
        connection.query(new IQ(IQ.Type.GET, new OfflineMessage(true, false)));
    }

    /**
     * Removes all offline messages.
     *
     * @throws StanzaException     If the server returned a stanza error.
     * @throws NoResponseException If the server did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0013.html#remove-all">2.7 Removing All Messages</a>
     */
    public void removeAllMessages() throws XmppException {
        connection.query(new IQ(IQ.Type.SET, new OfflineMessage(false, true)));
    }
}
