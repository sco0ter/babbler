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

package rocks.xmpp.extensions.muc.model;

import rocks.xmpp.core.Jid;
import rocks.xmpp.extensions.data.model.DataForm;

import java.util.ArrayList;
import java.util.List;

/**
 * A helper class to build a standard {@link rocks.xmpp.extensions.data.model.DataForm}, which can be used to request voice in a MUC room.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0045.html#requestvoice">7.13 Requesting Voice</a>
 * @see <a href="http://xmpp.org/extensions/xep-0045.html#registrar-formtype-request">15.5.2 muc#request FORM_TYPE</a>
 */
public final class RequestVoiceForm {

    private static final String FORM_TYPE = "http://jabber.org/protocol/muc#request";

    /**
     * Requested role
     */
    private static final String ROLE = "muc#role";

    /**
     * User ID
     */
    private static final String JID = "muc#jid";

    /**
     * Room Nickname
     */
    private static final String ROOM_NICK = "muc#roomnick";

    /**
     * Whether to grant voice
     */
    private static final String REQUEST_ALLOW = "muc#request_allow";

    private final DataForm dataForm;

    /**
     * Creates the request voice form.
     *
     * @param dataForm The underlying data form.
     */
    public RequestVoiceForm(DataForm dataForm) {
        this.dataForm = dataForm;
    }

    /**
     * Creates the builder to build a form.
     *
     * @return The builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Gets the underlying data form.
     */
    public DataForm getDataForm() {
        return dataForm;
    }

    /**
     * Gets the JID.
     *
     * @return The JID.
     */
    public Jid getJid() {
        return dataForm.findValueAsJid(JID);
    }

    /**
     * Gets the room nick.
     *
     * @return The room nick.
     */
    public String getRoomNick() {
        return dataForm.findValue(ROOM_NICK);
    }

    /**
     * Gets the requested role.
     *
     * @return The requested role.
     */
    public Role getRole() {
        String value = dataForm.findValue(ROLE);
        if (value != null) {
            return Role.valueOf(value.toUpperCase());
        }
        return null;
    }

    /**
     * Indicates, whether the request is approved.
     *
     * @return True, if the request is approved.
     */
    public boolean isRequestAllowed() {
        return dataForm.findValueAsBoolean(REQUEST_ALLOW);
    }

    /**
     * The builder to build a request voice form.
     */
    public static final class Builder extends DataForm.Builder<Builder> {

        private Role role;

        private Jid jid;

        private String roomNick;

        private Boolean allowRequest;

        private Builder() {
        }

        /**
         * Sets the requested role.
         *
         * @param role The role.
         * @return The builder.
         */
        public Builder role(Role role) {
            this.role = role;
            return this;
        }

        /**
         * Sets the JID.
         *
         * @param jid The JID.
         * @return The builder.
         */
        public Builder jid(Jid jid) {
            this.jid = jid;
            return this;
        }

        /**
         * Sets the room nickname.
         *
         * @param roomNick The room nickname.
         * @return The builder.
         */
        public Builder roomNick(String roomNick) {
            this.roomNick = roomNick;
            return this;
        }

        /**
         * Whether to grant voice.
         *
         * @param allowRequest Whether to grant voice.
         * @return The builder.
         */
        public Builder allowRequest(boolean allowRequest) {
            this.allowRequest = allowRequest;
            return this;
        }

        @Override
        protected Builder self() {
            return this;
        }

        /**
         * Builds the request voice form.
         *
         * @return The request voice form.
         */
        public RequestVoiceForm build() {
            List<DataForm.Field> fields = new ArrayList<>();
            if (role != null) {
                fields.add(DataForm.Field.builder().var(ROLE).value(role.name().toLowerCase()).build());
            }
            if (jid != null) {
                fields.add(DataForm.Field.builder().var(JID).value(jid).build());
            }
            if (roomNick != null) {
                fields.add(DataForm.Field.builder().var(ROOM_NICK).value(roomNick).build());
            }
            if (allowRequest != null) {
                fields.add(DataForm.Field.builder().var(REQUEST_ALLOW).value(allowRequest).build());
            }
            fields(fields).formType(FORM_TYPE).type(DataForm.Type.SUBMIT);
            return new RequestVoiceForm(new DataForm(this));
        }
    }
}
