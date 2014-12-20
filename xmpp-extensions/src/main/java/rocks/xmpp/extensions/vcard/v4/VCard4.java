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

package rocks.xmpp.extensions.vcard.v4;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "vcard")
public final class VCard4 {

    /**
     * urn:ietf:params:xml:ns:vcard-4.0
     */
    public static final String NAMESPACE = "urn:ietf:params:xml:ns:vcard-4.0";

    /**
     * Cardinality:  1*
     */
    @XmlElement(name = "fn")
    @XmlJavaTypeAdapter(TextAdapter.class)
    private List<String> formattedNames;

    /**
     * Cardinality:  *1
     */
    @XmlElement(name = "n")
    private Name name;

    /**
     * Cardinality:  *
     */
    @XmlElement(name = "nickname")
    @XmlJavaTypeAdapter(TextAdapter.class)
    private List<String> nicknames;

    /**
     * Cardinality:  *
     */
    @XmlElement(name = "photo")
    private List<Photo> photos;

    /**
     * Cardinality:  *1
     */
    @XmlElement(name = "bday")
    @XmlJavaTypeAdapter(DateAdapter.class)
    private Date birthday;

    /**
     * Cardinality:  *1
     */
    private Date anniversary;

    /**
     * Cardinality:  *1
     */
    private String gender;

    /**
     * Cardinality:  *
     */
    private List<Address> addresses;

    /**
     * Cardinality:  *
     */
    private List<String> telephone;

    /**
     * Cardinality:  *
     */

    @XmlElement(name = "email")
    private List<EmailAddress> email;

    /**
     * Cardinality:  *
     */
    private List<URI> impp;

    /**
     * Cardinality:  *
     */
    private List<String> language;

    /**
     * Cardinality:  *
     */
    @XmlElement(name = "tz")
    private List<TimeZone> timeZone;

    /**
     * Cardinality:  *
     */
    @XmlElement(name = "geo")
    private List<URI> geo;

    /**
     * Cardinality:  *
     */
    private List<String> title;

    /**
     * Cardinality:  *
     */
    private List<String> role;

    /**
     * Cardinality:  *
     */
    private List<URI> logo;

    /**
     * Cardinality:  *
     */
    private List<String> org;

    /**
     * Cardinality:  *
     */
    private List<String> member;

    /**
     * Cardinality:  *
     */
    private List<URI> related;

    /**
     * Cardinality:  *
     */
    private List<Object> categories;

    /**
     * Cardinality:  *
     */
    @XmlElement(name = "note")
    @XmlJavaTypeAdapter(TextAdapter.class)
    private List<String> notes;

    /**
     * Cardinality:  *1
     */
    private String prodId;

    /**
     * Cardinality:  *1
     */
    private String revision;

    /**
     * Cardinality:  *
     */
    private List<URI> sound;

    /**
     * Cardinality:  *1
     */
    private String uid;

    /**
     * Cardinality:  *
     */
    private List<String> clientidmap;

    /**
     * Cardinality:  *
     */
    private List<URL> url;

    /**
     * Cardinality:  1
     */
    private String version = "4.0";

    /**
     * Cardinality:  *
     */
    private List<URI> key;

    /**
     * Cardinality:  *
     */
    private List<URI> fbUrl;

    /**
     * Cardinality:  *
     */
    private List<URI> caladrUri;

    /**
     * Cardinality:  *
     */
    private List<URI> calUri;

    public List<String> getNotes() {
        return notes;
    }

    public List<EmailAddress> getEmailAddresses() {
        return email;
    }

    public List<String> getFormattedNames() {
        return formattedNames;
    }

    public List<String> getNicknames() {
        return nicknames;
    }

    public Name getName() {
        return name;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public Date getBirthday() {
        return birthday;
    }

    public enum Type {
        @XmlEnumValue("work")
        WORK,
        @XmlEnumValue("home")
        HOME
    }

    public enum Sex {
        @XmlEnumValue("M")
        MALE,
        @XmlEnumValue("F")
        FEMALE,
        @XmlEnumValue("O")
        OTHER,
        @XmlEnumValue("N")
        NONE,
        @XmlEnumValue("U")
        UNKNOWN
    }

    public enum CommunicationType {
        @XmlEnumValue("work")
        WORK,
        @XmlEnumValue("home")
        HOME,
        @XmlEnumValue("text")
        TEXT,
        @XmlEnumValue("voice")
        VOICE,
        @XmlEnumValue("fax")
        FAX,
        @XmlEnumValue("cell")
        CELL,
        @XmlEnumValue("video")
        VIDEO,
        @XmlEnumValue("pager")
        PAGER,
        @XmlEnumValue("textphone")
        TEXTPHONE
    }

    public static final class Name {
        @XmlElement(name = "surname")
        private String surname;

        @XmlElement(name = "given")
        private String givenName;

        @XmlElement(name = "additional")
        private String additionalName;

        @XmlElement(name = "prefix")
        private String prefix;

        @XmlElement(name = "suffix")
        private String suffix;

        public String getSurname() {
            return surname;
        }

        public String getGivenName() {
            return givenName;
        }

        public String getAdditionalName() {
            return additionalName;
        }

        public String getPrefix() {
            return prefix;
        }

        public String getSuffix() {
            return suffix;
        }
    }

    public static final class Photo extends Parameterizable {

        private URI uri;

        public URI getUri() {
            return uri;
        }
    }

    private static final class Address {

        @XmlElement(name = "pobox")
        private String postBox;

        @XmlElement(name = "ext")
        private String ext;

        @XmlElement(name = "street")
        private String street;

        @XmlElement(name = "locality")
        private String locality;

        @XmlElement(name = "region")
        private String region;

        @XmlElement(name = "code")
        private String postalCode;

        @XmlElement(name = "country")
        private String country;
    }

    @XmlTransient
    private static abstract class Parameterizable {
        @XmlElementWrapper(name = "parameters")
        @XmlElements({
                @XmlElement(name = "language", type = Parameter.LanguageParameter.class),
                @XmlElement(name = "type", type = Parameter.TypeParameter.class),
                @XmlElement(name = "label", type = Parameter.LabelParameter.class)})
        private List<Parameter> parameters;

        public List<Parameter> getParameters() {
            return parameters;
        }
    }

    public static final class EmailAddress extends Parameterizable {
        @XmlElement(name = "text")
        private String text;

        public String getEmailAddress() {
            return text;
        }

    }

    @XmlTransient
    public static abstract class Parameter {

        public static final class LanguageParameter extends Parameter {

        }

        public static final class PreferenceParameter extends Parameter {

        }

        public static final class AlternativeId extends Parameter {
            private String text;
        }

        public static final class PidParameter extends Parameter {

        }

        public static final class TypeParameter extends Parameter {

            @XmlElement(name = "text")
            private List<Type> types;

            public List<Type> getTypes() {
                return types;
            }
        }

        public static final class MediaTypeParameter extends Parameter {
            private String text;
        }

        public static final class CalendarScaleParameter extends Parameter {
            private String text;
        }

        public static final class SortAsParameter extends Parameter {
            private String text;
        }

        public static final class GeoParameter extends Parameter {
            private URI uri;
        }

        public static final class TimeZoneParameter extends Parameter {
            private String text;
        }

        public static final class LabelParameter extends Parameter {
            @XmlElement(name = "text")
            private String text;
        }
    }

    private static class TextValue {
        @XmlElement(name = "text")
        private String text;

        private TextValue() {
        }

        private TextValue(String text) {
            this.text = text;
        }
    }

    private static class DateValue {
        @XmlElement(name = "date")
        private Date date;

        private DateValue() {
        }

        private DateValue(Date date) {
            this.date = date;
        }
    }

    private static final class TextAdapter extends XmlAdapter<TextValue, String> {

        @Override
        public String unmarshal(TextValue v) throws Exception {
            return v != null ? v.text : null;
        }

        @Override
        public TextValue marshal(String v) throws Exception {
            return new TextValue(v);
        }
    }

    private static final class DateAdapter extends XmlAdapter<DateValue, Date> {

        @Override
        public Date unmarshal(DateValue v) throws Exception {
            return v != null ? v.date : null;
        }

        @Override
        public DateValue marshal(Date v) throws Exception {
            return new DateValue(v);
        }
    }
}
