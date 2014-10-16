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

import rocks.xmpp.extensions.data.model.DataForm;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Christian Schudt
 */
public final class RoomRegistrationForm {

    /**
     * Allow this person to register with the room?
     */
    private static final String REGISTER_ALLOW = "muc#register_allow";

    /**
     * Email Address
     */
    private static final String EMAIL = "muc#register_email";

    /**
     * FAQ Entry
     */
    private static final String FAQ_ENTRY = "muc#register_faqentry";

    /**
     * Given Name
     */
    private static final String GIVEN_NAME = "muc#register_first";

    /**
     * Family Name
     */
    private static final String FAMILY_NAME = "muc#register_last";

    /**
     * Desired Nickname
     */
    private static final String ROOM_NICK = "muc#register_roomnick";

    /**
     * A Web Page
     */
    private static final String URL = "muc#register_url";

    private final DataForm dataForm;

    public RoomRegistrationForm(DataForm dataForm) {
        this.dataForm = dataForm;
    }

    /**
     * Gets the e-mail address.
     *
     * @return The e-mail address.
     */
    public String getEmail() {
        DataForm.Field field = dataForm.findField(EMAIL);
        if (field != null && !field.getValues().isEmpty()) {
            return field.getValues().get(0);
        }
        return null;
    }

    /**
     * Sets the e-mail address.
     *
     * @param email The e-mail address.
     */
    public void setEmail(String email) {
        DataForm.Field field = dataForm.findField(EMAIL);
        if (field == null) {
            field = new DataForm.Field(DataForm.Field.Type.TEXT_SINGLE, EMAIL);
            dataForm.getFields().add(field);
        }
        field.getValues().clear();
        field.getValues().add(email);
    }

    /**
     * Gets the family name.
     *
     * @return The family name.
     * @see #setFamilyName(String)
     */
    public String getFamilyName() {
        DataForm.Field field = dataForm.findField(FAMILY_NAME);
        if (field != null && !field.getValues().isEmpty()) {
            return field.getValues().get(0);
        }
        return null;
    }

    /**
     * Sets the family name.
     *
     * @param familyName The family name.
     * @see #getFamilyName()
     */
    public void setFamilyName(String familyName) {
        DataForm.Field field = dataForm.findField(FAMILY_NAME);
        if (field == null) {
            field = new DataForm.Field(DataForm.Field.Type.TEXT_SINGLE, FAMILY_NAME);
            dataForm.getFields().add(field);
        }
        field.getValues().clear();
        field.getValues().add(familyName);
    }

    /**
     * Gets the given name.
     *
     * @return The given name.
     * @see #setGivenName(String)
     */
    public String getGivenName() {
        DataForm.Field field = dataForm.findField(GIVEN_NAME);
        if (field != null && !field.getValues().isEmpty()) {
            return field.getValues().get(0);
        }
        return null;
    }

    /**
     * Sets the given name.
     *
     * @param givenName The given name.
     * @see #setGivenName(String)
     */
    public void setGivenName(String givenName) {
        DataForm.Field field = dataForm.findField(GIVEN_NAME);
        if (field == null) {
            field = new DataForm.Field(DataForm.Field.Type.TEXT_SINGLE, GIVEN_NAME);
            dataForm.getFields().add(field);
        }
        field.getValues().clear();
        field.getValues().add(givenName);
    }

    /**
     * Gets the desired room nick.
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
     * Sets the desired room nick.
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
     * Gets an URL to a web page.
     *
     * @return The URL.
     */
    public URL getWebPage() {
        DataForm.Field field = dataForm.findField(URL);
        if (field != null && !field.getValues().isEmpty()) {
            try {
                return new URL(field.getValues().get(0));
            } catch (MalformedURLException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Sets an URL to a web page.
     *
     * @param webPage The URL.
     */
    public void setWebPage(URL webPage) {
        DataForm.Field field = dataForm.findField(URL);
        if (field == null) {
            field = new DataForm.Field(DataForm.Field.Type.TEXT_SINGLE, URL);
            dataForm.getFields().add(field);
        }
        field.getValues().clear();
        field.getValues().add(webPage.toString());
    }

    /**
     * Gets the FAQ entry.
     *
     * @return The FAQ entry.
     */
    public String getFaqEntry() {
        DataForm.Field field = dataForm.findField(FAQ_ENTRY);
        if (field != null && !field.getValues().isEmpty()) {
            return field.getValues().get(0);
        }
        return null;
    }

    /**
     * Sets the FAQ entry.
     *
     * @param faqEntry The FAQ entry.
     */
    public void setFaqEntry(String faqEntry) {
        DataForm.Field field = dataForm.findField(FAQ_ENTRY);
        if (field == null) {
            field = new DataForm.Field(DataForm.Field.Type.TEXT_SINGLE, FAQ_ENTRY);
            dataForm.getFields().add(field);
        }
        field.getValues().clear();
        field.getValues().add(faqEntry);
    }

    /**
     * Indicates, whether the registration request is approved.
     *
     * @return True, if the registration request is approved.
     */
    public boolean isRegisterAllowed() {
        DataForm.Field field = dataForm.findField(REGISTER_ALLOW);
        return field != null && !field.getValues().isEmpty() && DataForm.parseBoolean(field.getValues().get(0));
    }

    /**
     * Indicates, whether the registration request is approved.
     *
     * @param registerAllowed True, if the registration request is approved.
     */
    public void setRegisterAllowed(boolean registerAllowed) {
        DataForm.Field field = dataForm.findField(REGISTER_ALLOW);
        if (field == null) {
            field = new DataForm.Field(DataForm.Field.Type.BOOLEAN, REGISTER_ALLOW);
            dataForm.getFields().add(field);
        }
        field.getValues().clear();
        field.getValues().add(registerAllowed ? "1" : "0");
    }
}
