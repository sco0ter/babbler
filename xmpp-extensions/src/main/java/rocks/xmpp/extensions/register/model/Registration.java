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
    private String nick;

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

    // TODO add support for jabber:x:oob

    public Registration() {
    }

    public Registration(DataForm dataForm) {
        this.dataForm = dataForm;
    }

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

    public Registration(String username, String password) {
        this(username, password, null);
    }

    public Registration(boolean remove) {
        this.remove = remove ? "" : null;
    }

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
     */
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
        return nick;
    }

    /**
     * Sets the nickname.
     *
     * @param nickname The nickname.
     * @see #getNickname()
     */
    public void setNickname(String nickname) {
        this.nick = nickname;
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
     */
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
     */
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
     */
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
     */
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
     * @see #getEmail()
     */
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
     */
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
     */
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
     */
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
     */
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
     */
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
     */
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
     */
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
     */
    public void setRegistrationForm(DataForm dataForm) {
        this.dataForm = dataForm;
    }
}
