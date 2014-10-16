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

/**
 * @author Christian Schudt
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

    public RequestVoiceForm(DataForm dataForm) {
        this.dataForm = dataForm;
        this.dataForm.setFormType(FORM_TYPE);
    }

    /**
     * Gets the JID.
     *
     * @return The JID.
     */
    public Jid getJid() {
        DataForm.Field field = dataForm.findField(JID);
        if (field != null && !field.getValues().isEmpty()) {
            return Jid.valueOf(field.getValues().get(0));
        }
        return null;
    }

    /**
     * Sets the JID.
     *
     * @param jid The JID.
     */
    public void setJid(Jid jid) {
        DataForm.Field field = dataForm.findField(JID);
        if (field == null) {
            field = new DataForm.Field(DataForm.Field.Type.JID_SINGLE, JID);
            dataForm.getFields().add(field);
        }
        field.getValues().clear();
        field.getValues().add(jid.toEscapedString());
    }

    /**
     * Gets the room nick.
     *
     * @return The room nick.
     * @see #setRoomNick(String)
     */
    public String getRoomNick() {
        DataForm.Field field = dataForm.findField(ROOM_NICK);
        if (field != null && !field.getValues().isEmpty()) {
            return field.getValues().get(0);
        }
        return null;
    }

    /**
     * Sets the room nick.
     *
     * @param roomNick The room nick.
     * @see #getRoomNick()
     */
    public void setRoomNick(String roomNick) {
        DataForm.Field field = dataForm.findField(ROOM_NICK);
        if (field == null) {
            field = new DataForm.Field(DataForm.Field.Type.TEXT_SINGLE, ROOM_NICK);
            dataForm.getFields().add(field);
        }
        field.getValues().clear();
        field.getValues().add(roomNick);
    }

    /**
     * Gets the requested role.
     *
     * @return The requested role.
     */
    public Role getRole() {
        DataForm.Field field = dataForm.findField(ROLE);
        if (field != null && !field.getValues().isEmpty()) {
            return Role.valueOf(field.getValues().get(0).toUpperCase());
        }
        return null;
    }

    /**
     * Sets the requested role.
     *
     * @param role The requested role.
     */
    public void setRole(Role role) {
        DataForm.Field field = dataForm.findField(ROLE);
        if (field == null) {
            field = new DataForm.Field(DataForm.Field.Type.TEXT_SINGLE, ROLE);
            dataForm.getFields().add(field);
        }
        field.getValues().clear();
        field.getValues().add(role.toString().toLowerCase());
    }

    /**
     * Indicates, whether the request is approved.
     *
     * @return True, if the request is approved.
     */
    public boolean isRequestAllowed() {
        DataForm.Field field = dataForm.findField(REQUEST_ALLOW);
        return field != null && !field.getValues().isEmpty() && DataForm.parseBoolean(field.getValues().get(0));
    }

    /**
     * Indicates, whether the request is approved.
     *
     * @param requestAllowed True, if the request is approved.
     */
    public void setRequestAllowed(boolean requestAllowed) {
        DataForm.Field field = dataForm.findField(REQUEST_ALLOW);
        if (field == null) {
            field = new DataForm.Field(DataForm.Field.Type.BOOLEAN, REQUEST_ALLOW);
            dataForm.getFields().add(field);
        }
        field.getValues().clear();
        field.getValues().add(requestAllowed ? "1" : "0");
    }
}
