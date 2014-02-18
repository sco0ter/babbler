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
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.data.DataForm;
import org.xmpp.extension.disco.ServiceDiscoveryManager;
import org.xmpp.extension.disco.info.InfoNode;
import org.xmpp.extension.disco.items.Item;
import org.xmpp.extension.disco.items.ItemNode;
import org.xmpp.stanza.IQ;
import org.xmpp.stanza.StanzaException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * @author Christian Schudt
 */
public final class OfflineMessageManager extends ExtensionManager {
    private static final String NODE = "http://jabber.org/protocol/offline";

    protected OfflineMessageManager(Connection connection) {
        super(connection);
    }

    public void discoverServerSupport() {

    }

    public int requestNumberOfMessages() throws TimeoutException, StanzaException {
        ServiceDiscoveryManager serviceDiscoveryManager = connection.getExtensionManager(ServiceDiscoveryManager.class);
        InfoNode infoDiscovery = serviceDiscoveryManager.discoverInformation(null, NODE);
        DataForm dataForm = infoDiscovery.getExtensions().iterator().next();
        if (dataForm != null) {
            for (DataForm.Field field : dataForm.getFields()) {
                if ("number_of_messages".equals(field.getVar())) {
                    String numberOfMessages = field.getValues().get(0);
                    return Integer.parseInt(numberOfMessages);
                }
            }
        }
        return 0;
    }

    public List<OfflineMessageHeader> requestOfflineMessageHeaders() throws TimeoutException, StanzaException {
        List<OfflineMessageHeader> result = new ArrayList<>();
        ServiceDiscoveryManager serviceDiscoveryManager = connection.getExtensionManager(ServiceDiscoveryManager.class);
        ItemNode itemNode = serviceDiscoveryManager.discoverItems(null, NODE);
        for (Item item : itemNode.getItems()) {
            result.add(new OfflineMessageHeader(Jid.fromEscapedString(item.getName()), item.getName()));
        }
        return result;
    }

    public void requestOfflineMessage(String node) {
        IQ result = new IQ(IQ.Type.GET);
    }

    public void removeOfflineMessages(String... ids) throws TimeoutException, StanzaException {
        OfflineMessage offlineMessage = new OfflineMessage();
        for (String id : ids) {
            offlineMessage.getItems().add(new OfflineMessage.Item(id, OfflineMessage.Item.Action.REMOVE));
        }
        connection.query(new IQ(IQ.Type.SET, offlineMessage));
    }

    public void getAllOfflineMessages() throws TimeoutException, StanzaException {
        connection.query(new IQ(IQ.Type.GET, new OfflineMessage(true, false)));
    }

    public void removeAllOfflineMessages() throws TimeoutException, StanzaException {
        connection.query(new IQ(IQ.Type.GET, new OfflineMessage(false, true)));
    }
}
