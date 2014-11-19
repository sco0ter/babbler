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

package rocks.xmpp.extensions.register.model;

import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.oob.model.x.OobX;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URL;
import java.util.Date;

/**
 * The implementation of the {@code <query/>} element in the {@code jabber:iq:register} namespace.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0077.html">XEP-0077: In-Band Registration</a>
 * @see <a href="http://xmpp.org/extensions/xep-0077.html#schemas-register">XML Schema</a>
 */
@XmlRootElement(name = "query")
public final class Registration {

    @XmlElement(name = "registered")
    private String registered;

    @XmlElement(name = "instructions")
    private String instructions;

    @XmlElement(name = "username")
    private String username;

    @XmlElement(name = "nick")
    private String nickname;

    @XmlElement(name = "password")
    private String password;

    @XmlElement(name = "name")
    private String name;

    @XmlElement(name = "first")
    private String givenName;

    @XmlElement(name = "last")
    private String familyName;

    @XmlElement(name = "email")
    private String email;

    @XmlElement(name = "address")
    private String street;

    @XmlElement(name = "city")
    private String city;

    @XmlElement(name = "region")
    private String region;

    @XmlElement(name = "zip")
    private String postalCode;

    @XmlElement(name = "phone")
    private String telephone;

    @XmlElement(name = "url")
    private URL url;

    @XmlElement(name = "date")
    private Date date;

    @XmlElement(name = "remove")
    private String remove;

    @XmlElementRef
    private DataForm dataForm;

    @XmlElementRef
    private OobX oobX;

    private Registration(Builder builder) {
        this.username = builder.username;
        this.nickname = builder.nickname;
        this.password = builder.password;
        this.name = builder.name;
        this.givenName = builder.givenName;
        this.familyName = builder.familyName;
        this.email = builder.email;
        this.street = builder.street;
        this.city = builder.city;
        this.region = builder.region;
        this.postalCode = builder.postalCode;
        this.telephone = builder.phone;
        this.url = builder.url;
        this.date = builder.date;
        this.dataForm = builder.dataForm;
        this.oobX = builder.oobX;
    }

    /**
     * Creates an empty registration element to request the registration.
     */
    public Registration() {
    }

    /**
     * @param dataForm The data form.
     * @deprecated Use {@link #builder()}}
     */
    @Deprecated
    public Registration(DataForm dataForm) {
        this.dataForm = dataForm;
    }

    /**
     * @param username The username.
     * @param password The password.
     * @param email    The email address.
     * @deprecated Use {@link #builder()}}
     */
    @Deprecated
    public Registration(String username, String password, String email) {
        if (username == null) {
            throw new IllegalArgumentException("username must not be null.");
        }
        if (password == null) {
            throw new IllegalArgumentException("password must not be null.");
        }
        this.username = username;
        this.password = password;
        this.email = email;
    }

    /**
     * @param username The username.
     * @param password The password.
     * @deprecated Use {@link #builder()}}
     */
    @Deprecated
    public Registration(String username, String password) {
        this(username, password, null);
    }

    /**
     * @param remove True, if remove.
     * @deprecated Use {@link #remove()}
     */
    @Deprecated
    public Registration(boolean remove) {
        this.remove = remove ? "" : null;
    }

    /**
     * Creates a registration element with an {@code <remove/>} element used to indicate account removal.
     *
     * @return The registration.
     */
    public static Registration remove() {
        Registration registration = new Registration();
        registration.remove = "";
        return registration;
    }

    /**
     * Creates the builder for an registration.
     *
     * @return The registration.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Indicates whether the account is already registered.
     *
     * @return True, if the account is already registered.
     */
    public boolean isRegistered() {
        return registered != null;
    }

    /**
     * Gets the registration instructions.
     *
     * @return The instructions.
     */
    public String getInstructions() {
        return instructions;
    }

    /**
     * Gets the username.
     *
     * @return The username.
     * @see #setUsername(String)
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     *
     * @param username The username.
     * @see #getUsername()
     * @deprecated Use {@link rocks.xmpp.extensions.register.model.Registration.Builder}.
     */
    @Deprecated
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the nickname.
     *
     * @return The nickname.
     * @see #setNickname(String)
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Sets the nickname.
     *
     * @param nickname The nickname.
     * @see #getNickname()
     * @deprecated Use {@link rocks.xmpp.extensions.register.model.Registration.Builder}.
     */
    @Deprecated
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * Gets the password.
     *
     * @return The password.
     * @see #setPassword(String)
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password.
     *
     * @param password The password.
     * @see #getPassword()
     * @deprecated Use {@link rocks.xmpp.extensions.register.model.Registration.Builder}.
     */
    @Deprecated
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the full name.
     *
     * @return The full name.
     * @see #setFullName(String)
     */
    public String getFullName() {
        return name;
    }

    /**
     * Sets the full name.
     *
     * @param name The full name.
     * @see #getFullName()
     * @deprecated Use {@link rocks.xmpp.extensions.register.model.Registration.Builder}.
     */
    @Deprecated
    public void setFullName(String name) {
        this.name = name;
    }

    /**
     * Gets the given name.
     *
     * @return The given name.
     * @see #setGivenName(String)
     */
    public String getGivenName() {
        return givenName;
    }

    /**
     * Sets the first name.
     *
     * @param givenName The given name.
     * @see #getGivenName()
     * @deprecated Use {@link rocks.xmpp.extensions.register.model.Registration.Builder}.
     */
    @Deprecated
    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    /**
     * Gets the familyName name.
     *
     * @return The familyName name.
     * @see #setLastName(String)
     */
    public String getLastName() {
        return familyName;
    }

    /**
     * Sets the familyName name.
     *
     * @param last The familyName name.
     * @see #getLastName()
     * @deprecated Use {@link rocks.xmpp.extensions.register.model.Registration.Builder}.
     */
    @Deprecated
    public void setLastName(String last) {
        this.familyName = last;
    }

    /**
     * Gets the email street.
     *
     * @return The email street.
     * @see #setEmail(String)
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email street.
     *
     * @param email The email street.
     * @deprecated Use {@link rocks.xmpp.extensions.register.model.Registration.Builder}.
     */
    @Deprecated
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the street.
     *
     * @return The street.
     * @see #setStreet(String)
     */
    public String getStreet() {
        return street;
    }

    /**
     * Sets the street.
     *
     * @param street The street.
     * @see #getStreet()
     * @deprecated Use {@link rocks.xmpp.extensions.register.model.Registration.Builder}.
     */
    @Deprecated
    public void setStreet(String street) {
        this.street = street;
    }

    /**
     * Gets the city.
     *
     * @return The city.
     * @see #setCity(String)
     */
    public String getCity() {
        return city;
    }

    /**
     * Sets the city.
     *
     * @param city The city.
     * @see #getCity()
     * @deprecated Use {@link rocks.xmpp.extensions.register.model.Registration.Builder}.
     */
    @Deprecated
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Gets the region.
     *
     * @return The region.
     * @see #setRegion(String)
     */
    public String getRegion() {
        return region;
    }

    /**
     * Sets the region.
     *
     * @param region The region.
     * @see #getRegion()
     * @deprecated Use {@link rocks.xmpp.extensions.register.model.Registration.Builder}.
     */
    @Deprecated
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * Gets the postal code.
     *
     * @return The postal code.
     * @see #setPostalCode(String)
     */
    public String getPostalCode() {
        return postalCode;
    }

    /**
     * Sets the postal code.
     *
     * @param postalCode The postal code.
     * @see #getPostalCode()
     * @deprecated Use {@link rocks.xmpp.extensions.register.model.Registration.Builder}.
     */
    @Deprecated
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    /**
     * Gets the telephone number.
     *
     * @return The telephone number.
     * @see #setTelephone(String)
     */
    public String getTelephone() {
        return telephone;
    }

    /**
     * Sets the telephone number.
     *
     * @param telephone The telephone number.
     * @see #getTelephone()
     * @deprecated Use {@link rocks.xmpp.extensions.register.model.Registration.Builder}.
     */
    @Deprecated
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    /**
     * Gets the URL to a web page.
     *
     * @return The URL.
     * @see #setUrl(java.net.URL)
     */
    public URL getUrl() {
        return url;
    }

    /**
     * Sets the URL to a web page.
     *
     * @param url The URL.
     * @see #getUrl()
     * @deprecated Use {@link rocks.xmpp.extensions.register.model.Registration.Builder}.
     */
    @Deprecated
    public void setUrl(URL url) {
        this.url = url;
    }

    /**
     * Gets some date (e.g. birth date, hire date, sign-up date).
     *
     * @return The date.
     * @see #setDate(java.util.Date)
     */
    public Date getDate() {
        return date;
    }

    /**
     * Sets some date (e.g. birth date, hire date, sign-up date).
     *
     * @param date The date.
     * @see #getDate()
     * @deprecated Use {@link rocks.xmpp.extensions.register.model.Registration.Builder}.
     */
    @Deprecated
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Gets the extended registration form.
     *
     * @return The registration form.
     * @see #setRegistrationForm(rocks.xmpp.extensions.data.model.DataForm)
     */
    public DataForm getRegistrationForm() {
        return dataForm;
    }

    /**
     * Sets the registration form.
     *
     * @param dataForm The registration form.
     * @see #getRegistrationForm()
     * @deprecated Use {@link rocks.xmpp.extensions.register.model.Registration.Builder}.
     */
    @Deprecated
    public void setRegistrationForm(DataForm dataForm) {
        this.dataForm = dataForm;
    }

    /**
     * Gets the web registration URL, if any.
     *
     * @return The web registration URL or null.
     */
    public URL getWebRegistrationUrl() {
        return oobX != null ? oobX.getUrl() : null;
    }

    /**
     * A builder to build a registration.
     */
    public static final class Builder {

        private String instructions;

        private String username;

        private String password;

        private String nickname;

        private String name;

        private String givenName;

        private String familyName;

        private String email;

        private String street;

        private String city;

        private String region;

        private String postalCode;

        private String phone;

        private URL url;

        private Date date;

        private DataForm dataForm;

        private OobX oobX;

        private Builder() {
        }

        /**
         * Sets the instructions.
         *
         * @param instructions The instructions.
         * @return The builder.
         */
        public Builder instructions(String instructions) {
            this.instructions = instructions;
            return this;
        }

        /**
         * Sets the username.
         *
         * @param username The username.
         * @return The builder.
         */
        public Builder username(String username) {
            this.username = username;
            return this;
        }

        /**
         * Sets the password.
         *
         * @param password The password.
         * @return The builder.
         */
        public Builder password(String password) {
            this.password = password;
            return this;
        }

        /**
         * Sets the nickname.
         *
         * @param nickname The nickname.
         * @return The builder.
         */
        public Builder nickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        /**
         * Sets the name.
         *
         * @param name The name.
         * @return The builder.
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the given name.
         *
         * @param givenName The given name.
         * @return The builder.
         */
        public Builder givenName(String givenName) {
            this.givenName = givenName;
            return this;
        }

        /**
         * Sets the family name.
         *
         * @param familyName The family name.
         * @return The builder.
         */
        public Builder familyName(String familyName) {
            this.familyName = familyName;
            return this;
        }

        /**
         * Sets the email.
         *
         * @param email The email address.
         * @return The builder.
         */
        public Builder email(String email) {
            this.email = email;
            return this;
        }

        /**
         * The street.
         *
         * @param street The street.
         * @return The builder.
         */
        public Builder street(String street) {
            this.street = street;
            return this;
        }

        /**
         * Sets the city.
         *
         * @param city The city.
         * @return The builder.
         */
        public Builder city(String city) {
            this.city = city;
            return this;
        }

        /**
         * Sets the region, e.g. state.
         *
         * @param region The region.
         * @return The builder.
         */
        public Builder region(String region) {
            this.region = region;
            return this;
        }

        /**
         * Sets the postal code.
         *
         * @param postalCode The postal code.
         * @return The builder.
         */
        public Builder postalCode(String postalCode) {
            this.postalCode = postalCode;
            return this;
        }

        /**
         * Sets the phone.
         *
         * @param phone The phone.
         * @return The builder.
         */
        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        /**
         * Sets the URL.
         *
         * @param url The URL.
         * @return The builder.
         */
        public Builder url(URL url) {
            this.url = url;
            return this;
        }

        /**
         * Sets the date, e.g. birth date.
         *
         * @param date The date.
         * @return The builder.
         */
        public Builder date(Date date) {
            this.date = date;
            return this;
        }

        /**
         * Sets the extended registration form.
         *
         * @param dataForm The data form.
         * @return The builder.
         */
        public Builder registrationForm(DataForm dataForm) {
            this.dataForm = dataForm;
            return this;
        }

        /**
         * Sets the web registration URL.
         *
         * @param url The url.
         * @return The builder.
         */
        public Builder webRegistration(URL url) {
            this.oobX = new OobX(url);
            return this;
        }

        /**
         * Builds the registration.
         *
         * @return The builder.
         */
        public Registration build() {
            return new Registration(this);
        }
    }
}
