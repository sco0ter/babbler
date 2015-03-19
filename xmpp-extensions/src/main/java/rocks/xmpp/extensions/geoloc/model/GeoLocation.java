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

import rocks.xmpp.extensions.time.model.TimeZoneAdapter;

import javax.xml.XMLConstants;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.net.URI;
import java.util.Date;
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
    private String language;

    @XmlElement(name = "accuracy")
    private Double accuracy;

    @XmlElement(name = "altitude")
    private Double altitude;

    @XmlElement(name = "area")
    private String area;

    @XmlElement(name = "bearing")
    private Double bearing;

    @XmlElement(name = "building")
    private String building;

    @XmlElement(name = "country")
    private String country;

    @XmlElement(name = "countrycode")
    private String countryCode;

    @XmlElement(name = "datum")
    private String datum;

    @XmlElement(name = "description")
    private String description;

    @XmlElement(name = "floor")
    private String floor;

    @XmlElement(name = "lat")
    private Double latitude;

    @XmlElement(name = "locality")
    private String locality;

    @XmlElement(name = "lon")
    private Double longitude;

    @XmlElement(name = "postalcode")
    private String postalCode;

    @XmlElement(name = "region")
    private String region;

    @XmlElement(name = "room")
    private String room;

    @XmlElement(name = "speed")
    private Double speed;

    @XmlElement(name = "street")
    private String street;

    @XmlElement(name = "text")
    private String text;

    @XmlElement(name = "timestamp")
    private Date timestamp;

    @XmlJavaTypeAdapter(TimeZoneAdapter.class)
    @XmlElement(name = "tzo")
    private TimeZone timeZone;

    @XmlElement(name = "uri")
    private URI uri;

    /**
     * Creates an empty geolocation element.
     */
    public GeoLocation() {
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
        this.timeZone = builder.timeZone;
        this.uri = builder.uri;
    }

    /**
     * @param latitude  The latitude.
     * @param longitude The longitude.
     * @deprecated Use the builder to create an instance of this class.
     */
    @Deprecated
    public GeoLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
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
     * @see #setAccuracy(Double)
     */
    public final Double getAccuracy() {
        return accuracy;
    }

    /**
     * Sets the horizontal GPS error in meters.
     *
     * @param accuracy The accuracy.
     * @see #getAccuracy()
     * @deprecated Use {@link rocks.xmpp.extensions.geoloc.model.GeoLocation.Builder}.
     */
    @Deprecated
    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    /**
     * Gets the altitude in meters above or below sea level.
     *
     * @return The altitude.
     * @see #setAltitude(Double)
     */
    public final Double getAltitude() {
        return altitude;
    }

    /**
     * Sets the altitude in meters above or below sea level.
     *
     * @param altitude The altitude.
     * @see #getAltitude()
     * @deprecated Use {@link rocks.xmpp.extensions.geoloc.model.GeoLocation.Builder}.
     */
    @Deprecated
    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    /**
     * Gets a named area such as a campus or neighborhood.
     *
     * @return The area.
     * @see #setArea(String)
     */
    public final String getArea() {
        return area;
    }

    /**
     * Sets a named area such as a campus or neighborhood.
     *
     * @param area The area.
     * @see #getArea()
     * @deprecated Use {@link rocks.xmpp.extensions.geoloc.model.GeoLocation.Builder}.
     */
    @Deprecated
    public void setArea(String area) {
        this.area = area;
    }

    /**
     * Gets the GPS bearing (direction in which the entity is heading to reach its next waypoint), measured in decimal degrees relative to true north.
     *
     * @return The bearing.
     * @see #setBearing(Double)
     */
    public final Double getBearing() {
        return bearing;
    }

    /**
     * Sets the GPS bearing (direction in which the entity is heading to reach its next waypoint), measured in decimal degrees relative to true north.
     *
     * @param bearing The bearing.
     * @see #getBearing()
     * @deprecated Use {@link rocks.xmpp.extensions.geoloc.model.GeoLocation.Builder}.
     */
    @Deprecated
    public void setBearing(Double bearing) {
        this.bearing = bearing;
    }

    /**
     * Gets a specific building on a street or in an area.
     *
     * @return The building.
     * @see #setBuilding(String)
     */
    public final String getBuilding() {
        return building;
    }

    /**
     * Sets a specific building on a street or in an area.
     *
     * @param building The building.
     * @see #getBuilding()
     * @deprecated Use {@link rocks.xmpp.extensions.geoloc.model.GeoLocation.Builder}.
     */
    @Deprecated
    public void setBuilding(String building) {
        this.building = building;
    }

    /**
     * Gets the nation where the user is located.
     *
     * @return The country.
     * @see #setCountry(String)
     */
    public final String getCountry() {
        return country;
    }

    /**
     * Sets the nation where the user is located.
     *
     * @param country The country.
     * @see #getCountry()
     * @deprecated Use {@link rocks.xmpp.extensions.geoloc.model.GeoLocation.Builder}.
     */
    @Deprecated
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * Gets the ISO 3166 two-letter country code.
     *
     * @return The country code.
     * @see #setCountryCode(String)
     */
    public final String getCountryCode() {
        return countryCode;
    }

    /**
     * Sets the ISO 3166 two-letter country code.
     *
     * @param countryCode The country code.
     * @see #getCountryCode()
     * @deprecated Use {@link rocks.xmpp.extensions.geoloc.model.GeoLocation.Builder}.
     */
    @Deprecated
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    /**
     * Gets the GPS datum.
     *
     * @return The GPS datum.
     * @see #setDatum(String)
     */
    public final String getDatum() {
        return datum;
    }

    /**
     * Sets the GPS datum.
     *
     * @param datum The GPS datum.
     * @see #getDatum()
     * @deprecated Use {@link rocks.xmpp.extensions.geoloc.model.GeoLocation.Builder}.
     */
    @Deprecated
    public void setDatum(String datum) {
        this.datum = datum;
    }

    /**
     * Gets a natural-language name for or description of the location.
     *
     * @return The description.
     * @see #setDescription(String)
     */
    public final String getDescription() {
        return description;
    }

    /**
     * Sets a natural-language name for or description of the location.
     *
     * @param description The description.
     * @see #getDescription()
     * @deprecated Use {@link rocks.xmpp.extensions.geoloc.model.GeoLocation.Builder}.
     */
    @Deprecated
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets a particular floor in a building.
     *
     * @return The floor.
     * @see #setFloor(String)
     */
    public final String getFloor() {
        return floor;
    }

    /**
     * Sets a particular floor in a building.
     *
     * @param floor The floor.
     * @see #getFloor()
     * @deprecated Use {@link rocks.xmpp.extensions.geoloc.model.GeoLocation.Builder}.
     */
    @Deprecated
    public void setFloor(String floor) {
        this.floor = floor;
    }

    /**
     * Gets the latitude in decimal degrees North.
     *
     * @return The latitude.
     * @see #setLatitude(Double)
     */
    public final Double getLatitude() {
        return latitude;
    }

    /**
     * Sets the latitude in decimal degrees North.
     *
     * @param latitude The latitude.
     * @see #getLatitude()
     * @deprecated Use {@link rocks.xmpp.extensions.geoloc.model.GeoLocation.Builder}.
     */
    @Deprecated
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    /**
     * Gets a locality within the administrative region, such as a town or city.
     *
     * @return The locality.
     * @see #setLocality(String)
     */
    public final String getLocality() {
        return locality;
    }

    /**
     * Sets a locality within the administrative region, such as a town or city.
     *
     * @param locality The locality.
     * @see #getLocality()
     * @deprecated Use {@link rocks.xmpp.extensions.geoloc.model.GeoLocation.Builder}.
     */
    @Deprecated
    public void setLocality(String locality) {
        this.locality = locality;
    }

    /**
     * Gets the longitude in decimal degrees East.
     *
     * @return The longitude.
     * @see #setLongitude(Double)
     */
    public final Double getLongitude() {
        return longitude;
    }

    /**
     * Sets the longitude in decimal degrees East.
     *
     * @param longitude The longitude.
     * @see #getLongitude()
     * @deprecated Use {@link rocks.xmpp.extensions.geoloc.model.GeoLocation.Builder}.
     */
    @Deprecated
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    /**
     * Gets a code used for postal delivery.
     *
     * @return The postal code.
     * @see #setPostalCode(String)
     */
    public final String getPostalCode() {
        return postalCode;
    }

    /**
     * Sets a code used for postal delivery.
     *
     * @param postalCode The postal code.
     * @see #getPostalCode()
     * @deprecated Use {@link rocks.xmpp.extensions.geoloc.model.GeoLocation.Builder}.
     */
    @Deprecated
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    /**
     * Gets an administrative region of the nation, such as a state or province.
     *
     * @return The region.
     * @see #setRegion(String)
     */
    public final String getRegion() {
        return region;
    }

    /**
     * Sets an administrative region of the nation, such as a state or province.
     *
     * @param region The region.
     * @see #getRegion()
     * @deprecated Use {@link rocks.xmpp.extensions.geoloc.model.GeoLocation.Builder}.
     */
    @Deprecated
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * Gets a particular room in a building.
     *
     * @return The room.
     * @see #setRoom(String)
     */
    public final String getRoom() {
        return room;
    }

    /**
     * Sets a particular room in a building.
     *
     * @param room The room.
     * @see #getRoom()
     * @deprecated Use {@link rocks.xmpp.extensions.geoloc.model.GeoLocation.Builder}.
     */
    @Deprecated
    public void setRoom(String room) {
        this.room = room;
    }

    /**
     * Gets the speed at which the entity is moving, in meters per second.
     *
     * @return The speed.
     * @see #setSpeed(Double)
     */
    public final Double getSpeed() {
        return speed;
    }

    /**
     * Sets the speed at which the entity is moving, in meters per second.
     *
     * @param speed The speed.
     * @see #getSpeed()
     * @deprecated Use {@link rocks.xmpp.extensions.geoloc.model.GeoLocation.Builder}.
     */
    @Deprecated
    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    /**
     * Gets a thoroughfare within the locality, or a crossing of two thoroughfares.
     *
     * @return The street.
     * @see #setStreet(String)
     */
    public final String getStreet() {
        return street;
    }

    /**
     * Sets a thoroughfare within the locality, or a crossing of two thoroughfares.
     *
     * @param street The street.
     * @see #setStreet(String)
     * @deprecated Use {@link rocks.xmpp.extensions.geoloc.model.GeoLocation.Builder}.
     */
    @Deprecated
    public void setStreet(String street) {
        this.street = street;
    }

    /**
     * Gets a catch-all element that captures any other information about the location.
     *
     * @return The text.
     * @see #setText(String)
     */
    public final String getText() {
        return text;
    }

    /**
     * Sets a catch-all element that captures any other information about the location.
     *
     * @param text The text.
     * @see #getText()
     * @deprecated Use {@link rocks.xmpp.extensions.geoloc.model.GeoLocation.Builder}.
     */
    @Deprecated
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Gets the UTC timestamp specifying the moment when the reading was taken.
     *
     * @return The timestamp.
     * @see #setTimestamp(java.util.Date)
     */
    public final Date getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the UTC timestamp specifying the moment when the reading was taken.
     *
     * @param timestamp The timestamp.
     * @see #getTimestamp()
     * @deprecated Use {@link rocks.xmpp.extensions.geoloc.model.GeoLocation.Builder}.
     */
    @Deprecated
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Gets a URI or URL pointing to information about the location.
     *
     * @return The URI.
     * @see #setUri(java.net.URI)
     */
    public final URI getUri() {
        return uri;
    }

    /**
     * Sets a URI or URL pointing to information about the location.
     *
     * @param uri The URI.
     * @see #getUri()
     * @deprecated Use {@link rocks.xmpp.extensions.geoloc.model.GeoLocation.Builder}.
     */
    @Deprecated
    public void setUri(URI uri) {
        this.uri = uri;
    }

    /**
     * Gets the the natural language of location data.
     *
     * @return The language.
     * @see #setLanguage(String)
     */
    public final String getLanguage() {
        return language;
    }

    /**
     * Sets the natural language of location data.
     *
     * @param language The language.
     * @see #getLanguage()
     * @deprecated Use {@link rocks.xmpp.extensions.geoloc.model.GeoLocation.Builder}.
     */
    @Deprecated
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Gets the time zone offset from UTC for the current location.
     *
     * @return The time zone.
     */
    public final TimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * Sets the time zone offset from UTC for the current location.
     *
     * @param timeZone The time zone.
     * @deprecated Use {@link rocks.xmpp.extensions.geoloc.model.GeoLocation.Builder}.
     */
    @Deprecated
    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
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
        if (timeZone != null) {
            sb.append("Time Zone: ");
            sb.append(timeZone.getDisplayName());
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

        private Date timestamp;

        private TimeZone timeZone;

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
        public Builder timestamp(Date timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        /**
         * Sets the time zone offset from UTC for the current location.
         *
         * @param timeZone The time zone.
         * @return The builder.
         */
        public Builder timeZone(TimeZone timeZone) {
            this.timeZone = timeZone;
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
