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

package rocks.xmpp.extensions.pubsub;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.Jid;
import rocks.xmpp.core.XmlTest;
import rocks.xmpp.core.stanza.model.AbstractMessage;
import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.pubsub.model.*;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Christian Schudt
 */
public class PubSubConfigurationFormsTest extends XmlTest {

    protected PubSubConfigurationFormsTest() throws JAXBException, XMLStreamException {
        super(DataForm.class);
    }

    @Test
    public void testMetaData() throws JAXBException, XMLStreamException {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        PubSubMetaDataForm pubSubMetaDataForm = PubSubMetaDataForm.builder()
                .contacts(Arrays.asList(Jid.valueOf("contact")))
                .creationDate(date)
                .creator(Jid.valueOf("creator"))
                .description("desc")
                .language("de")
                .numberOfSubscribers(2)
                .owners(Arrays.asList(Jid.valueOf("owner")))
                .publishers(Arrays.asList(Jid.valueOf("publisher")))
                .title("title")
                .payloadType("namespace")
                .build();

        String xml = marshal(pubSubMetaDataForm.getDataForm());
        Assert.assertEquals(xml, "<x xmlns=\"jabber:x:data\" type=\"result\">" +
                "<field type=\"hidden\" var=\"FORM_TYPE\"><value>http://jabber.org/protocol/pubsub#meta-data</value></field>" +
                "<field type=\"jid-multi\" var=\"pubsub#contact\"><value>contact</value></field>" +
                "<field type=\"text-single\" var=\"pubsub#creation_date\"><value>" + DatatypeConverter.printDateTime(calendar) + "</value></field>" +
                "<field type=\"jid-single\" var=\"pubsub#creator\"><value>creator</value></field>" +
                "<field type=\"text-single\" var=\"pubsub#description\"><value>desc</value></field>" +
                "<field type=\"list-single\" var=\"pubsub#language\"><value>de</value></field>" +
                "<field type=\"text-single\" var=\"pubsub#num_subscribers\"><value>2</value></field>" +
                "<field type=\"jid-multi\" var=\"pubsub#owner\"><value>owner</value></field>" +
                "<field type=\"jid-multi\" var=\"pubsub#publisher\"><value>publisher</value></field>" +
                "<field type=\"text-single\" var=\"pubsub#title\"><value>title</value></field>" +
                "<field type=\"text-single\" var=\"pubsub#type\"><value>namespace</value></field>" +
                "</x>");
        DataForm dataForm = unmarshal(xml, DataForm.class);
        PubSubMetaDataForm pubSubMetaDataForm1 = new PubSubMetaDataForm(dataForm);
        Assert.assertEquals(pubSubMetaDataForm1.getCreationDate(), date);
        Assert.assertEquals(pubSubMetaDataForm1.getCreator(), Jid.valueOf("creator"));
        Assert.assertEquals(pubSubMetaDataForm1.getDescription(), "desc");
        Assert.assertEquals(pubSubMetaDataForm1.getLanguage(), "de");
        Assert.assertEquals(pubSubMetaDataForm1.getNumberOfSubscribers(), new Integer(2));
        Assert.assertEquals(pubSubMetaDataForm1.getOwners(), Arrays.asList(Jid.valueOf("owner")));
        Assert.assertEquals(pubSubMetaDataForm1.getPublishers(), Arrays.asList(Jid.valueOf("publisher")));
        Assert.assertEquals(pubSubMetaDataForm1.getTitle(), "title");
        Assert.assertEquals(pubSubMetaDataForm1.getPayloadType(), "namespace");
    }

    @Test
    public void testPublishOptions() throws JAXBException, XMLStreamException {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        PublishOptions publishOptions = PublishOptions.builder()
                .accessModel(AccessModel.AUTHORIZE)
                .persistItems(true)
                .rosterGroupsAllowed(Arrays.asList("Friends"))
                .sendLastPublishedItem(SendLastPublishedItem.ON_SUB)
                .build();

        String xml = marshal(publishOptions.getDataForm());
        Assert.assertEquals(xml, "<x xmlns=\"jabber:x:data\" type=\"submit\">" +
                "<field type=\"hidden\" var=\"FORM_TYPE\"><value>http://jabber.org/protocol/pubsub#publish-options</value></field>" +
                "<field type=\"list-single\" var=\"pubsub#access_model\"><value>authorize</value></field>" +
                "<field type=\"boolean\" var=\"pubsub#persist_items\"><value>1</value></field>" +
                "<field type=\"list-single\" var=\"pubsub#send_last_published_item\"><value>on_sub</value></field>" +
                "<field type=\"list-multi\" var=\"pubsub#roster_groups_allowed\"><value>Friends</value></field>" +
                "</x>");
        DataForm dataForm = unmarshal(xml, DataForm.class);
        PublishOptions publishOptionsForm = new PublishOptions(dataForm);
        Assert.assertEquals(publishOptionsForm.getAccessModel(), AccessModel.AUTHORIZE);
        Assert.assertTrue(publishOptionsForm.isPersistItems());
        Assert.assertEquals(publishOptionsForm.getSendLastPublishedItem(), SendLastPublishedItem.ON_SUB);
        Assert.assertEquals(publishOptionsForm.getRosterGroupsAllowed(), Arrays.asList("Friends"));
    }

    @Test
    public void testNodeConfiguration() throws JAXBException, XMLStreamException, MalformedURLException {
        NodeConfiguration nodeConfiguration = NodeConfiguration.builder()
                .accessModel(AccessModel.AUTHORIZE)
                .bodyXslt(new URL("http://xmpp.org"))
                .childrenAssociationPolicy(ChildrenAssociationPolicy.OWNERS)
                .childrenAssociationWhitelist(Arrays.asList(Jid.valueOf("domain")))
                .children(Arrays.asList("collection1"))
                .childrenMax(23)
                .collection(Arrays.asList("collections"))
                .contact(Arrays.asList(Jid.valueOf("contact")))
                .dataformXslt(new URL("http://www.xmpp.org"))
                .deliverNotifications(true)
                .deliverPayloads(false)
                .description("description")
                .itemExpire(2)
                .itemReply(ItemReply.OWNER)
                .language("de")
                .maxItems(4)
                .maxPayloadSize(54)
                .nodeType(NodeType.LEAF)
                .notificationType(AbstractMessage.Type.NORMAL)
                .notifyConfig(true)
                .notifyDelete(true)
                .notifyRetract(true)
                .notifySub(true)
                .persistItems(true)
                .presenceBasedDelivery(true)
                .publisherModel(PublisherModel.OPEN)
                .purgeOffline(false)
                .rosterGroupsAllowed(Arrays.asList("group1", "group2"))
                .sendLastPublishedItem(SendLastPublishedItem.ON_SUB_AND_PRESENCE)
                .temporarySubscriptions(true)
                .allowSubscriptions(true)
                .title("Title")
                .type("Type")
                .build();

        String xml = marshal(nodeConfiguration.getDataForm());
        Assert.assertEquals(xml, "<x xmlns=\"jabber:x:data\" type=\"submit\">" +
                "<field type=\"hidden\" var=\"FORM_TYPE\"><value>http://jabber.org/protocol/pubsub#node_config</value></field>" +
                "<field type=\"list-single\" var=\"pubsub#access_model\"><value>authorize</value></field>" +
                "<field type=\"text-single\" var=\"pubsub#body_xslt\"><value>http://xmpp.org</value></field>" +
                "<field type=\"list-single\" var=\"pubsub#children_association_policy\"><value>owners</value></field>" +
                "<field type=\"jid-multi\" var=\"pubsub#children_association_whitelist\"><value>domain</value></field>" +
                "<field type=\"text-multi\" var=\"pubsub#children\"><value>collection1</value></field>" +
                "<field type=\"text-single\" var=\"pubsub#children_max\"><value>23</value></field>" +
                "<field type=\"text-multi\" var=\"pubsub#collection\"><value>collections</value></field>" +
                "<field type=\"jid-multi\" var=\"pubsub#contact\"><value>contact</value></field>" +
                "<field type=\"text-single\" var=\"pubsub#dataform_xslt\"><value>http://www.xmpp.org</value></field>" +
                "<field type=\"boolean\" var=\"pubsub#deliver_notifications\"><value>1</value></field>" +
                "<field type=\"boolean\" var=\"pubsub#deliver_payloads\"><value>0</value></field>" +
                "<field type=\"text-single\" var=\"pubsub#description\"><value>description</value></field>" +
                "<field type=\"text-single\" var=\"pubsub#item_expire\"><value>2</value></field>" +
                "<field type=\"list-single\" var=\"pubsub#itemreply\"><value>owner</value></field>" +
                "<field type=\"list-single\" var=\"pubsub#language\"><value>de</value></field>" +
                "<field type=\"text-single\" var=\"pubsub#max_items\"><value>4</value></field>" +
                "<field type=\"text-single\" var=\"pubsub#max_payload_size\"><value>54</value></field>" +
                "<field type=\"list-single\" var=\"pubsub#node_type\"><value>leaf</value></field>" +
                "<field type=\"list-single\" var=\"pubsub#notification_type\"><value>normal</value></field>" +
                "<field type=\"boolean\" var=\"pubsub#notify_config\"><value>1</value></field>" +
                "<field type=\"boolean\" var=\"pubsub#notify_delete\"><value>1</value></field>" +
                "<field type=\"boolean\" var=\"pubsub#notify_retract\"><value>1</value></field>" +
                "<field type=\"boolean\" var=\"pubsub#notify_sub\"><value>1</value></field>" +
                "<field type=\"boolean\" var=\"pubsub#persist_items\"><value>1</value></field>" +
                "<field type=\"boolean\" var=\"pubsub#presence_based_delivery\"><value>1</value></field>" +
                "<field type=\"list-single\" var=\"pubsub#publish_model\"><value>open</value></field>" +
                "<field type=\"boolean\" var=\"pubsub#purge_offline\"><value>0</value></field>" +
                "<field type=\"list-multi\" var=\"pubsub#roster_groups_allowed\"><value>group1</value><value>group2</value></field>" +
                "<field type=\"list-single\" var=\"pubsub#send_last_published_item\"><value>on_sub_and_presence</value></field>" +
                "<field type=\"boolean\" var=\"pubsub#tempsub\"><value>1</value></field>" +
                "<field type=\"boolean\" var=\"pubsub#subscribe\"><value>1</value></field>" +
                "<field type=\"text-single\" var=\"pubsub#title\"><value>Title</value></field>" +
                "<field type=\"text-single\" var=\"pubsub#type\"><value>Type</value></field>" +
                "</x>");

        DataForm dataForm = unmarshal(xml, DataForm.class);
        NodeConfiguration nodeConfiguration1 = new NodeConfiguration(dataForm);
        Assert.assertEquals(nodeConfiguration1.getAccessModel(), AccessModel.AUTHORIZE);
    }
}
