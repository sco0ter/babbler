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

package rocks.xmpp.extensions.pubsub.model;

import rocks.xmpp.core.Jid;
import rocks.xmpp.extensions.data.model.DataForm;

import java.util.Date;
import java.util.List;

/**
 * @author Christian Schudt
 */
public final class PubSubMetaDataForm {

    public static final String FORM_TYPE = "http://jabber.org/protocol/pubsub#meta-data";

    /**
     * The JIDs of those to contact with questions
     */
    private static final String CONTACT = "pubsub#contact";

    /**
     * The datetime when the node was created
     */
    private static final String CREATION_DATE = "pubsub#creation_date";

    /**
     * The JID of the node creator
     */
    private static final String CREATOR = "pubsub#creator";

    /**
     * A description of the node
     */
    private static final String DESCRIPTION = "pubsub#description";

    /**
     * The default language of the node
     */
    private static final String LANGUAGE = "pubsub#language";

    /**
     * The number of subscribers to the node
     */
    private static final String NUM_SUBSCRIBERS = "pubsub#num_subscribers";

    /**
     * The JIDs of those with an affiliation of owner
     */
    private static final String OWNER = "pubsub#owner";

    /**
     * The JIDs of those with an affiliation of publisher
     */
    private static final String PUBLISHER = "pubsub#publisher";

    /**
     * The name of the node
     */
    private static final String TITLE = "pubsub#title";

    /**
     * Payload type
     */
    private static final String TYPE = "pubsub#type";

    private final DataForm dataForm;

    public PubSubMetaDataForm(DataForm dataForm) {
        this.dataForm = dataForm;
    }

    public List<Jid> getContacts() {
        return null;
    }

    public void setContacts(List<Jid> contacts) {

    }

    public Date getCreationDate() {
        return null;
    }

    public Jid getCreator() {
        return null;
    }

    public String getDescription() {
        return null;
    }

    public String getLanguage() {
        return null;
    }

    public int getNumberOfSubscribers() {
        return 0;
    }

    public List<Jid> getOwners() {
        return null;
    }

    public List<Jid> getPublishers() {
        return null;
    }

    public String getTitle() {
        return null;
    }

    public String getType() {
        return null;
    }
}
