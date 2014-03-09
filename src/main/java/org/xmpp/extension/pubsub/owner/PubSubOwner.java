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

package org.xmpp.extension.pubsub.owner;

import org.xmpp.extension.data.DataForm;
import org.xmpp.extension.pubsub.AffiliationNode;
import org.xmpp.extension.pubsub.Subscription;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.List;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "pubsub")
public final class PubSubOwner {

    @XmlElements({
            @XmlElement(name = "affiliations", type = Affiliations.class),
            @XmlElement(name = "configure", type = Configure.class),
            @XmlElement(name = "default", type = Default.class),
            @XmlElement(name = "delete", type = Delete.class),
            @XmlElement(name = "purge", type = Purge.class),
            @XmlElement(name = "subscriptions", type = Subscriptions.class)
    })
    private PubSubOwnerChildElement type;

    private PubSubOwner() {
    }

    private PubSubOwner(PubSubOwnerChildElement type) {
        this.type = type;
    }

    public static PubSubOwner forConfiguration(String node) {
        return new PubSubOwner(new Configure(node));
    }

    public static PubSubOwner forConfiguration(String node, DataForm dataForm) {
        return new PubSubOwner(new Configure(node, dataForm));
    }

    public static PubSubOwner forDefaultConfiguration() {
        return new PubSubOwner(new Default());
    }

    public static PubSubOwner forDelete(String node) {
        return new PubSubOwner(new Delete(node));
    }

    public static PubSubOwner forDelete(String node, URI replacementNode) {
        return new PubSubOwner(new Delete(node, new Delete.Redirect(replacementNode)));
    }

    public static PubSubOwner forPurge(String node) {
        return new PubSubOwner(new Purge(node));
    }

    public static PubSubOwner forSubscriptions(String node) {
        return new PubSubOwner(new Subscriptions(node));
    }

    public static PubSubOwner forSubscriptions(String node, org.xmpp.extension.pubsub.Subscription... subscriptions) {
        return new PubSubOwner((new Subscriptions(node))); // TODO
    }

    public static PubSubOwner forAffiliations(String node) {
        return new PubSubOwner((new Affiliations(node)));
    }

    public static PubSubOwner forAffiliations(String node, AffiliationNode... affiliationNodes) {
        return new PubSubOwner((new Affiliations(node))); // TODO
    }

    public DataForm getConfiguration() {
        return type instanceof Configure ? ((Configure) type).getDataForm() : null;
    }

    public DataForm getDefaultConfiguration() {
        return type instanceof Default ? ((Default) type).getDataForm() : null;
    }

    public String getNode() {
        return type != null ? type.getNode() : null;
    }

    public boolean isConfigure() {
        return type instanceof Configure;
    }

    public boolean isDefault() {
        return type instanceof Default;
    }

    public boolean isDelete() {
        return type instanceof Delete;
    }

    public boolean isPurge() {
        return type instanceof Purge;
    }

    public boolean isSubscriptions() {
        return type instanceof Subscriptions;
    }

    public List<? extends AffiliationNode> getAffiliations() {
        if (type instanceof Affiliations) {
            return ((Affiliations) type).getAffiliations();
        }
        return null;
    }

    public URI getRedirectUri() {
        if (type instanceof Delete && ((Delete) type).getRedirect() != null) {
            return ((Delete) type).getRedirect().getUri();
        }
        return null;
    }

    public List<? extends Subscription> getSubscriptions() {
        if (type instanceof Subscriptions) {
            return ((Subscriptions) type).getSubscriptions();
        }
        return null;
    }
}
