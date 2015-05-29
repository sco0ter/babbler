/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Christian Schudt
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

package rocks.xmpp.extensions.vcard;

import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * A VCard which provides common functionality / fields shared between vCard-temp and vCard 4.0.
 *
 * @author Christian Schudt
 */
public interface VCard {

    String getFormattedName();

    Name getName();

    String getNickname();

    // Photo

    LocalDate getBirthday();

    List<? extends Address> getAddresses();

    List<? extends TelephoneNumber> getTelephoneNumbers();

    List<? extends Email> getEmailAddresses();

    ZoneId getTimeZone();

    URI getGeo();

    String getTitle();

    String getRole();

    // Logo

    Organization getOrganization();

    List<String> getCategories();

    String getNote();

    String getProductId();

    String getRevision();

    // sound

    URL getUrl();

    String getVersion();

    interface Name {
        /**
         * Gets the family name.
         *
         * @return The family name.
         */
        String getFamilyName();

        /**
         * Gets the given name.
         *
         * @return The given name.
         */
        String getGivenName();

        /**
         * Gets the middle name.
         *
         * @return The middle name.
         */
        String getMiddleName();

        /**
         * Gets the prefix.
         *
         * @return The prefix.
         */
        String getPrefix();

        /**
         * Gets the suffix.
         *
         * @return The suffix.
         */
        String getSuffix();
    }

    interface Address {
        String getPostOfficeBox();

        String getExtendedAddress();

        String getStreet();

        String getCity();

        String getRegion();

        String getPostalCode();

        String getCountry();
    }

    interface TelephoneNumber {

        String getNumber();

        enum Type {
            /**
             * Indicates that the telephone number supports text messages (SMS).
             */
            TEXT,
            /**
             * Indicates a voice telephone number.
             */
            VOICE,
            /**
             * Indicates a facsimile telephone number.
             */
            FAX,
            /**
             * Indicates a cellular or mobile telephone number.
             */
            CELL,
            /**
             * Indicates a video conferencing telephone number.
             */
            VIDEO,
            /**
             * Indicates a paging device telephone number.
             */
            PAGER,
            /**
             * Indicates a telecommunication device for people with hearing or speech difficulties.
             */
            TEXTPHONE
        }
    }

    interface Email {
        String getEmail();
    }

    interface Organization {
        String getName();

        List<String> getUnits();
    }
}
