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

import rocks.xmpp.core.stanza.model.AbstractPresence;
import rocks.xmpp.core.stanza.model.client.Presence;
import rocks.xmpp.extensions.data.model.DataForm;

import javax.xml.bind.DatatypeConverter;
import java.util.*;

/**
 * @author Christian Schudt
 */
public class SubscribeOptions {

    private static final String FORM_TYPE = "http://jabber.org/protocol/pubsub#subscribe_options";

    /**
     * Whether an entity wants to receive
     * or disable notifications
     */
    private static final String DELIVER = "pubsub#deliver";

    /**
     * Whether an entity wants to receive digests
     * (aggregations) of notifications or all
     * notifications individually
     */
    private static final String DIGEST = "pubsub#digest";

    /**
     * The minimum number of milliseconds between
     * sending any two notification digests
     */
    private static final String DIGEST_FREQUENCY = "pubsub#digest_frequency";

    /**
     * The DateTime at which a leased subscription
     * will end or has ended
     */
    private static final String EXPIRE = "pubsub#expire";

    /**
     * Whether an entity wants to receive an XMPP
     * message body in addition to the payload
     * format
     */
    private static final String INCLUDE_BODY = "pubsub#include_body";

    /**
     * The presence states for which an entity
     * wants to receive notifications
     */
    private static final String SHOW_VALUES = "pubsub#show-values";

    private static final String SUBSCRIPTION_TYPE = "pubsub#subscription_type";

    private static final String SUBSCRIPTION_DEPTH = "pubsub#subscription_depth";

    private final Boolean deliver;

    private final Boolean digest;

    private final Integer digestFrequency;

    private final Date expire;

    private final Boolean includeBody;

    private final Set<AbstractPresence.Show> showValues;

    private final Boolean temporary;

    private final SubscriptionType subscriptionType;

    private final Integer subscriptionDepth;

    private SubscribeOptions(Builder builder) {
        this.deliver = builder.deliver;
        this.digest = builder.digest;
        this.digestFrequency = builder.digestFrequency;
        this.expire = builder.expireAt;
        this.includeBody = builder.includeBody;
        this.showValues = builder.showValues;
        this.temporary = builder.temporary;
        this.subscriptionType = builder.subscriptionType;
        this.subscriptionDepth = builder.subscriptionDepth;
    }

    /**
     * Creates a builder to build subscribe options.
     *
     * @return The builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    public DataForm toDataForm() {
        DataForm dataForm = new DataForm(DataForm.Type.SUBMIT);
        dataForm.setFormType(FORM_TYPE);

        List<DataForm.Field> fields = new ArrayList<>();

        if (deliver != null) {
            fields.add(new DataForm.Field(DataForm.Field.Type.BOOLEAN, DELIVER, deliver ? "1" : "0"));
        }
        if (digest != null) {
            fields.add(new DataForm.Field(DataForm.Field.Type.BOOLEAN, DIGEST, digest ? "1" : "0"));
        }
        if (digestFrequency != null) {
            fields.add(new DataForm.Field(DataForm.Field.Type.TEXT_SINGLE, DIGEST_FREQUENCY, String.valueOf(digestFrequency)));
        }
        if (temporary != null && temporary) {
            // To subscribe temporarily, the subscriber MUST set the "pubsub#expire" subscription configuration option to a literal value of "presence".
            fields.add(new DataForm.Field(DataForm.Field.Type.TEXT_SINGLE, EXPIRE, "presence"));
        } else if (expire != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(expire);
            fields.add(new DataForm.Field(DataForm.Field.Type.TEXT_SINGLE, EXPIRE, DatatypeConverter.printDateTime(calendar)));
        }
        if (includeBody != null) {
            fields.add(new DataForm.Field(DataForm.Field.Type.BOOLEAN, INCLUDE_BODY, includeBody ? "1" : "0"));
        }
        if (!showValues.isEmpty()) {
            DataForm.Field field = new DataForm.Field(DataForm.Field.Type.LIST_MULTI, SHOW_VALUES);
            for (AbstractPresence.Show show : showValues) {
                if (show != null) {
                    field.getValues().add(show.name().toLowerCase());
                } else {
                    field.getValues().add("online");
                }
            }
            fields.add(field);
        }
        if (subscriptionType != null) {
            fields.add(new DataForm.Field(DataForm.Field.Type.LIST_SINGLE, SUBSCRIPTION_TYPE, subscriptionType.name().toLowerCase()));
        }
        if (subscriptionDepth != null) {
            DataForm.Field field = new DataForm.Field(DataForm.Field.Type.LIST_SINGLE, SUBSCRIPTION_DEPTH);
            if (subscriptionDepth < 1) {
                field.getValues().add("all");
            } else {
                field.getValues().add(subscriptionDepth.toString());
            }
            fields.add(field);
        }
        dataForm.getFields().addAll(fields);

        return dataForm;
    }

    /**
     * The subscription type.
     *
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#auto-subscribe">9.1 Auto-Subscribe</a>
     */
    public enum SubscriptionType {
        /**
         * Receive notification of new items only
         */
        ITEMS,
        /**
         * Receive notification of new nodes only
         */
        NODES
    }

    /**
     * A builder for the subscribe options.
     *
     * @see rocks.xmpp.extensions.pubsub.model.SubscribeOptions
     */
    public static final class Builder {

        private final Set<Presence.Show> showValues = new HashSet<>();

        private Boolean deliver;

        private Boolean digest;

        private Integer digestFrequency;

        private Boolean includeBody;

        private Date expireAt;

        private Boolean temporary;

        private SubscriptionType subscriptionType;

        private Integer subscriptionDepth;

        private Builder() {
        }

        /**
         * Sets whether an entity wants to receive or disable notifications.
         *
         * @param deliver Whether an entity wants to receive or disable notifications.
         * @return The builder.
         */
        public Builder deliver(boolean deliver) {
            this.deliver = deliver;
            return this;
        }

        /**
         * Sets whether you want to receive digests (aggregations) of notifications or all notifications individually.
         *
         * @param digest Whether you want to receive digests (aggregations) of notifications or all notifications individually.
         * @return The builder.
         */
        public Builder digest(boolean digest) {
            this.digest = digest;
            return this;
        }

        /**
         * Sets the minimum number of milliseconds between sending any two notification digests
         *
         * @param digestFrequency The minimum number of milliseconds between sending any two notification digests.
         * @return The builder.
         */
        public Builder digestFrequency(int digestFrequency) {
            this.digestFrequency = digestFrequency;
            return this;
        }

        /**
         * Sets whether you want to receive an XMPP message body in addition to the payload.
         *
         * @param includeBody Whether you want to receive an XMPP message body in addition to the payload.
         * @return The builder.
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#impl-body">12.7 Including a Message Body</a>
         */
        public Builder includeBody(boolean includeBody) {
            this.includeBody = includeBody;
            return this;
        }

        /**
         * Sets the presence states for which an entity wants to receive notifications. A null value corresponds to "available" presence.
         *
         * @param showValues The presence states for which an entity wants to receive notifications.
         * @return The builder.
         */
        public Builder showValues(AbstractPresence.Show... showValues) {
            this.showValues.clear();
            this.showValues.addAll(Arrays.asList(showValues));
            return this;
        }

        /**
         * Sets the expiration date.
         *
         * @param expireAt The expiration date.
         * @return The builder.
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#impl-leases">12.18 Time-Based Subscriptions (Leases)</a>
         */
        public Builder expireAt(Date expireAt) {
            this.expireAt = expireAt;
            return this;
        }

        /**
         * If the subscription is temporary, i.e. only as long as you are online.
         *
         * @param temporary If the subscription is temporary, i.e. only as long as you are online.
         * @return The builder.
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#impl-tempsub">12.4 Temporary Subscriptions</a>
         */
        public Builder temporary(boolean temporary) {
            this.temporary = temporary;
            return this;
        }

        /**
         * Sets the subscription type.
         *
         * @param subscriptionType The subscription type.
         * @return The builder.
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#auto-subscribe">9.1 Auto-Subscribe</a>
         */
        public Builder subscriptionType(SubscriptionType subscriptionType) {
            this.subscriptionType = subscriptionType;
            return this;
        }

        /**
         * Sets the subscription depth. If the depth is negative, the depth is interpreted as "all".
         *
         * @param subscriptionDepth The subscription depth.
         * @return The builder.
         * @see <a href="http://xmpp.org/extensions/xep-0060.html#auto-subscribe">9.1 Auto-Subscribe</a>
         */
        public Builder subscriptionDepth(int subscriptionDepth) {
            this.subscriptionDepth = subscriptionDepth;
            return this;
        }

        /**
         * Builds the subscribe options.
         *
         * @return The subscribe options.
         */
        public SubscribeOptions build() {
            return new SubscribeOptions(this);
        }
    }
}
