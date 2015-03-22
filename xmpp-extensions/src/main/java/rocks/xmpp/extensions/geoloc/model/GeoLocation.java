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

package rocks.xmpp.extensions.geoloc.model;

import rocks.xmpp.extensions.time.model.ZoneOffsetAdapter;

import javax.xml.XMLConstants;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.TimeZone;

/**
 * The implementation of the {@code <geoloc/>} element in the {@code http://jabber.org/protocol/geoloc} namespace.
 * <p>
 * This class represents the geological location of a user.
 * </p>
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/extensions/xep-0080.html#transport">4. Recommended Transport</a></cite></p>
 * <p>Location information about human users SHOULD be communicated and transported by means of Publish-Subscribe (XEP-0060) [5] or the subset thereof specified in Personal Eventing Protocol (XEP-0163) [6].</p>
 * <p>Although the XMPP publish-subscribe extension is the preferred means for transporting location information about human users, applications that do not involve human users (e.g., device tracking) MAY use other transport methods; however, because location information is not pure presence information and can change independently of network availability, it SHOULD NOT be provided as an extension to {@code <presence/>}.</p>
 * </blockquote>
 * <h3>Usage</h3>
 * This class is immutable, you have to use a builder to create a geo location instance. Here's an example:
 * <pre>
 * {@code
 * GeoLocation geoLocation = GeoLocation.builder()
 *     .countryCode("de")
 *     .latitude(50.2)
 *     .longitude(7.5)
 *     .timeZone(TimeZone.getTimeZone("GMT+1"))
 *     .build();
 * }
 * </pre>
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0080.html">XEP-0080: User Location</a>
 * @see <a href="http://xmpp.org/extensions/xep-0080.html#schema">XML Schema</a>
 */
@XmlRootElement(name = "geoloc")
public final class GeoLocation {

    /**
     * http://jabber.org/protocol/geoloc
     */
    public static final String NAMESPACE = "http://jabber.org/protocol/geoloc";

    @XmlAttribute(name = "lang", namespace = XMLConstants.XML_NS_URI)
    private final String language;

    @XmlElement(name = "accuracy")
    private final Double accuracy;

    @XmlElement(name = "altitude")
    private final Double altitude;

    @XmlElement(name = "area")
    private final String area;

    @XmlElement(name = "bearing")
    private final Double bearing;

    @XmlElement(name = "building")
    private final String building;

    @XmlElement(name = "country")
    private final String country;

    @XmlElement(name = "countrycode")
    private final String countryCode;

    @XmlElement(name = "datum")
    private final String datum;

    @XmlElement(name = "description")
    private final String description;

    @XmlElement(name = "floor")
    private final String floor;

    @XmlElement(name = "lat")
    private final Double latitude;

    @XmlElement(name = "locality")
    private final String locality;

    @XmlElement(name = "lon")
    private final Double longitude;

    @XmlElement(name = "postalcode")
    private final String postalCode;

    @XmlElement(name = "region")
    private final String region;

    @XmlElement(name = "room")
    private final String room;

    @XmlElement(name = "speed")
    private final Double speed;

    @XmlElement(name = "street")
    private final String street;

    @XmlElement(name = "text")
    private final String text;

    @XmlElement(name = "timestamp")
    private final Instant timestamp;

    @XmlJavaTypeAdapter(ZoneOffsetAdapter.class)
    @XmlElement(name = "tzo")
    private final ZoneOffset zoneOffset;

    @XmlElement(name = "uri")
    private final URI uri;

    /**
     * Creates an empty geolocation element.
     */
    private GeoLocation() {
        this.accuracy = null;
        this.altitude = null;
        this.area = null;
        this.bearing = null;
        this.building = null;
        this.country = null;
        this.countryCode = null;
        this.datum = null;
        this.description = null;
        this.floor = null;
        this.language = null;
        this.latitude = null;
        this.locality = null;
        this.longitude = null;
        this.postalCode = null;
        this.region = null;
        this.room = null;
        this.speed = null;
        this.street = null;
        this.text = null;
        this.timestamp = null;
        this.zoneOffset = null;
        this.uri = null;
    }

    private GeoLocation(Builder builder) {
        this.accuracy = builder.accuracy;
        this.altitude = builder.altitude;
        this.area = builder.area;
        this.bearing = builder.bearing;
        this.building = builder.building;
        this.country = builder.country;
        this.countryCode = builder.countryCode;
        this.datum = builder.datum;
        this.description = builder.description;
        this.floor = builder.floor;
        this.language = builder.language;
        this.latitude = builder.latitude;
        this.locality = builder.locality;
        this.longitude = builder.longitude;
        this.postalCode = builder.postalCode;
        this.region = builder.region;
        this.room = builder.room;
        this.speed = builder.speed;
        this.street = builder.street;
        this.text = builder.text;
        this.timestamp = builder.timestamp;
        this.zoneOffset = builder.zoneOffset;
        this.uri = builder.uri;
    }

    /**
     * Creates the builder to build a geo location.
     *
     * @return The builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Gets the horizontal GPS error in meters.
     *
     * @return The accuracy.
     */
    public final Double getAccuracy() {
        return accuracy;
    }

    /**
     * Gets the altitude in meters above or below sea level.
     *
     * @return The altitude.
     */
    public final Double getAltitude() {
        return altitude;
    }

    /**
     * Gets a named area such as a campus or neighborhood.
     *
     * @return The area.
     */
    public final String getArea() {
        return area;
    }

    /**
     * Gets the GPS bearing (direction in which the entity is heading to reach its next waypoint), measured in decimal degrees relative to true north.
     *
     * @return The bearing.
     */
    public final Double getBearing() {
        return bearing;
    }

    /**
     * Gets a specific building on a street or in an area.
     *
     * @return The building.
     */
    public final String getBuilding() {
        return building;
    }

    /**
     * Gets the nation where the user is located.
     *
     * @return The country.
     */
    public final String getCountry() {
        return country;
    }

    /**
     * Gets the ISO 3166 two-letter country code.
     *
     * @return The country code.
     */
    public final String getCountryCode() {
        return countryCode;
    }

    /**
     * Gets the GPS datum.
     *
     * @return The GPS datum.
     */
    public final String getDatum() {
        return datum;
    }

    /**
     * Gets a natural-language name for or description of the location.
     *
     * @return The description.
     */
    public final String getDescription() {
        return description;
    }

    /**
     * Gets a particular floor in a building.
     *
     * @return The floor.
     */
    public final String getFloor() {
        return floor;
    }

    /**
     * Gets the latitude in decimal degrees North.
     *
     * @return The latitude.
     */
    public final Double getLatitude() {
        return latitude;
    }

    /**
     * Gets a locality within the administrative region, such as a town or city.
     *
     * @return The locality.
     */
    public final String getLocality() {
        return locality;
    }

    /**
     * Gets the longitude in decimal degrees East.
     *
     * @return The longitude.
     */
    public final Double getLongitude() {
        return longitude;
    }

    /**
     * Gets a code used for postal delivery.
     *
     * @return The postal code.
     */
    public final String getPostalCode() {
        return postalCode;
    }

    /**
     * Gets an administrative region of the nation, such as a state or province.
     *
     * @return The region.
     */
    public final String getRegion() {
        return region;
    }

    /**
     * Gets a particular room in a building.
     *
     * @return The room.
     */
    public final String getRoom() {
        return room;
    }

    /**
     * Gets the speed at which the entity is moving, in meters per second.
     *
     * @return The speed.
     */
    public final Double getSpeed() {
        return speed;
    }

    /**
     * Gets a thoroughfare within the locality, or a crossing of two thoroughfares.
     *
     * @return The street.
     */
    public final String getStreet() {
        return street;
    }

    /**
     * Gets a catch-all element that captures any other information about the location.
     *
     * @return The text.
     */
    public final String getText() {
        return text;
    }

    /**
     * Gets the UTC timestamp specifying the moment when the reading was taken.
     *
     * @return The timestamp.
     */
    public final Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Gets a URI or URL pointing to information about the location.
     *
     * @return The URI.
     */
    public final URI getUri() {
        return uri;
    }

    /**
     * Gets the the natural language of location data.
     *
     * @return The language.
     */
    public final String getLanguage() {
        return language;
    }

    /**
     * Gets the time zone offset from UTC for the current location.
     *
     * @return The time zone offset.
     * @deprecated Use {@link #getTimeZoneOffset()}
     */
    @Deprecated
    public final TimeZone getTimeZone() {
        return TimeZone.getTimeZone(zoneOffset);
    }

    /**
     * Gets the time zone offset from UTC for the current location.
     *
     * @return The time zone offset.
     */
    public final ZoneOffset getTimeZoneOffset() {
        return zoneOffset;
    }

    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder("Geolocation: ");
        if (accuracy != null) {
            sb.append("Accuracy: ");
            sb.append(accuracy);
            sb.append("; ");
        }
        if (altitude != null) {
            sb.append("Altitude: ");
            sb.append(altitude);
            sb.append("; ");
        }
        if (area != null) {
            sb.append("Area: ");
            sb.append(area);
            sb.append("; ");
        }
        if (bearing != null) {
            sb.append("Bearing: ");
            sb.append(bearing);
            sb.append("; ");
        }
        if (building != null) {
            sb.append("Building: ");
            sb.append(building);
            sb.append("; ");
        }
        if (country != null) {
            sb.append("Country: ");
            sb.append(country);
            sb.append("; ");
        }
        if (countryCode != null) {
            sb.append("Country Code: ");
            sb.append(countryCode);
            sb.append("; ");
        }
        if (datum != null) {
            sb.append("Datum: ");
            sb.append(datum);
            sb.append("; ");
        }
        if (description != null) {
            sb.append("Description: ");
            sb.append(description);
            sb.append("; ");
        }
        if (floor != null) {
            sb.append("Floor: ");
            sb.append(floor);
            sb.append("; ");
        }
        if (latitude != null) {
            sb.append("Latitude: ");
            sb.append(latitude);
            sb.append("; ");
        }
        if (locality != null) {
            sb.append("Locality: ");
            sb.append(locality);
            sb.append("; ");
        }
        if (longitude != null) {
            sb.append("Longitude: ");
            sb.append(longitude);
            sb.append("; ");
        }
        if (postalCode != null) {
            sb.append("Postal Code: ");
            sb.append(postalCode);
            sb.append("; ");
        }
        if (region != null) {
            sb.append("Region: ");
            sb.append(region);
            sb.append("; ");
        }
        if (room != null) {
            sb.append("Room: ");
            sb.append(room);
            sb.append("; ");
        }
        if (speed != null) {
            sb.append("Speed: ");
            sb.append(speed);
            sb.append("; ");
        }
        if (street != null) {
            sb.append("Street: ");
            sb.append(street);
            sb.append("; ");
        }
        if (text != null) {
            sb.append("Text: ");
            sb.append(text);
            sb.append("; ");
        }
        if (timestamp != null) {
            sb.append("Timestamp: ");
            sb.append(timestamp);
            sb.append("; ");
        }
        if (zoneOffset != null) {
            sb.append("Time Zone: ");
            sb.append(zoneOffset);
            sb.append("; ");
        }
        if (uri != null) {
            sb.append("URI: ");
            sb.append(uri);
            sb.append("; ");
        }
        return sb.substring(0, sb.length() - 2);
    }

    /**
     * A builder class to which builds geo location objects.
     */
    public static final class Builder {

        private String language;

        private Double accuracy;

        private Double altitude;

        private String area;

        private Double bearing;

        private String building;

        private String country;

        private String countryCode;

        private String datum;

        private String description;

        private String floor;

        private Double latitude;

        private String locality;

        private Double longitude;

        private String postalCode;

        private String region;

        private String room;

        private Double speed;

        private String street;

        private String text;

        private Instant timestamp;

        private ZoneOffset zoneOffset;

        private URI uri;

        private Builder() {
        }

        /**
         * Sets the natural language of location data.
         *
         * @param language The language.
         * @return The builder.
         */
        public Builder language(String language) {
            this.language = language;
            return this;
        }

        /**
         * Sets the horizontal GPS error in meters.
         *
         * @param accuracy The accuracy.
         * @return The builder.
         */
        public Builder accuracy(Double accuracy) {
            this.accuracy = accuracy;
            return this;
        }

        /**
         * Sets the altitude in meters above or below sea level.
         *
         * @param altitude The altitude.
         * @return The builder.
         */
        public Builder altitude(Double altitude) {
            this.altitude = altitude;
            return this;
        }

        /**
         * Sets a named area such as a campus or neighborhood.
         *
         * @param area The area.
         * @return The builder.
         */
        public Builder area(String area) {
            this.area = area;
            return this;
        }

        /**
         * Sets the GPS bearing (direction in which the entity is heading to reach its next waypoint), measured in decimal degrees relative to true north.
         *
         * @param bearing The bearing.
         * @return The builder.
         */
        public Builder bearing(Double bearing) {
            this.bearing = bearing;
            return this;
        }

        /**
         * Sets a specific building on a street or in an area.
         *
         * @param building The building.
         * @return The builder.
         */
        public Builder building(String building) {
            this.building = building;
            return this;
        }

        /**
         * Sets the nation where the user is located.
         *
         * @param country The country.
         * @return The builder.
         */
        public Builder country(String country) {
            this.country = country;
            return this;
        }

        /**
         * Sets the ISO 3166 two-letter country code.
         *
         * @param countryCode The country code.
         * @return The builder.
         */
        public Builder countryCode(String countryCode) {
            this.countryCode = countryCode;
            return this;
        }

        /**
         * Sets the GPS datum.
         *
         * @param datum The GPS datum.
         * @return The builder.
         */
        public Builder datum(String datum) {
            this.datum = datum;
            return this;
        }

        /**
         * Sets a natural-language name for or description of the location.
         *
         * @param description The description.
         * @return The builder.
         */
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Sets a particular floor in a building.
         *
         * @param floor The floor.
         * @return The builder.
         */
        public Builder floor(String floor) {
            this.floor = floor;
            return this;
        }

        /**
         * Sets the latitude in decimal degrees North.
         *
         * @param latitude The latitude.
         * @return The builder.
         */
        public Builder latitude(Double latitude) {
            this.latitude = latitude;
            return this;
        }

        /**
         * Sets a locality within the administrative region, such as a town or city.
         *
         * @param locality The locality.
         * @return The builder.
         */
        public Builder locality(String locality) {
            this.locality = locality;
            return this;
        }

        /**
         * Sets the longitude in decimal degrees East.
         *
         * @param longitude The longitude.
         * @return The builder.
         */
        public Builder longitude(Double longitude) {
            this.longitude = longitude;
            return this;
        }

        /**
         * Sets a code used for postal delivery.
         *
         * @param postalCode The postal code.
         * @return The builder.
         */
        public Builder postalCode(String postalCode) {
            this.postalCode = postalCode;
            return this;
        }

        /**
         * Sets an administrative region of the nation, such as a state or province.
         *
         * @param region The region.
         * @return The builder.
         */
        public Builder region(String region) {
            this.region = region;
            return this;
        }

        /**
         * Sets a particular room in a building.
         *
         * @param room The room.
         * @return The builder.
         */
        public Builder room(String room) {
            this.room = room;
            return this;
        }

        /**
         * Sets the speed at which the entity is moving, in meters per second.
         *
         * @param speed The speed.
         * @return The builder.
         */
        public Builder speed(Double speed) {
            this.speed = speed;
            return this;
        }

        /**
         * Sets a thoroughfare within the locality, or a crossing of two thoroughfares.
         *
         * @param street The street.
         * @return The builder.
         */
        public Builder street(String street) {
            this.street = street;
            return this;
        }

        /**
         * Sets a catch-all element that captures any other information about the location.
         *
         * @param text The text.
         * @return The builder.
         */
        public Builder text(String text) {
            this.text = text;
            return this;
        }

        /**
         * Sets the UTC timestamp specifying the moment when the reading was taken.
         *
         * @param timestamp The timestamp.
         * @return The builder.
         */
        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        /**
         * Sets the time zone offset from UTC for the current location.
         *
         * @param timeZone The time zone offset.
         * @return The builder.
         * @deprecated Use {@link #timeZoneOffset(java.time.ZoneOffset)}
         */
        @Deprecated
        public Builder timeZone(TimeZone timeZone) {
            int seconds = Math.abs(timeZone.getRawOffset()) / 1000;
            int hours = seconds / 3600;
            int minutes = (seconds % 3600) / 60;
            this.zoneOffset = ZoneOffset.of((timeZone.getRawOffset() < 0 ? "-" : "+") + String.format("%02d:%02d", hours, minutes));
            return this;
        }

        /**
         * Sets the time zone offset from UTC for the current location.
         *
         * @param zoneOffset The time zone offset.
         * @return The builder.
         */
        public Builder timeZoneOffset(ZoneOffset zoneOffset) {
            this.zoneOffset = zoneOffset;
            return this;
        }

        /**
         * Sets a URI or URL pointing to information about the location.
         *
         * @param uri The URI.
         * @return The builder.
         */
        public Builder uri(URI uri) {
            this.uri = uri;
            return this;
        }

        /**
         * Builds the geo location.
         *
         * @return The geo location.
         */
        public GeoLocation build() {
            return new GeoLocation(this);
        }
    }
}
