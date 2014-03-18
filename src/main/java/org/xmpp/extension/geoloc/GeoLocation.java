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

package org.xmpp.extension.geoloc;

import javax.xml.XMLConstants;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.Date;

/**
 * The implementation of <a href="http://xmpp.org/extensions/xep-0080.html">XEP-0080: User Location</a>.
 * <p>
 * This class represents the geological location of a user.
 * </p>
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/extensions/xep-0080.html#transport">4. Recommended Transport</a></cite></p>
 * <p>Location information about human users SHOULD be communicated and transported by means of Publish-Subscribe (XEP-0060) [5] or the subset thereof specified in Personal Eventing Protocol (XEP-0163) [6].</p>
 * <p>Although the XMPP publish-subscribe extension is the preferred means for transporting location information about human users, applications that do not involve human users (e.g., device tracking) MAY use other transport methods; however, because location information is not pure presence information and can change independently of network availability, it SHOULD NOT be provided as an extension to {@code <presence/>}.</p>
 * </blockquote>
 *
 * @author Christian Schudt
 */
@XmlRootElement(name = "geoloc")
public final class GeoLocation {

    static final String NAMESPACE = "http://jabber.org/protocol/geoloc";

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

    @XmlElement(name = "uri")
    private URI uri;

    public GeoLocation() {
    }

    public GeoLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Gets the horizontal GPS error in meters.
     *
     * @return The accuracy.
     * @see #setAccuracy(Double)
     */
    public Double getAccuracy() {
        return accuracy;
    }

    /**
     * Sets the horizontal GPS error in meters.
     *
     * @param accuracy The accuracy.
     * @see #getAccuracy()
     */
    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    /**
     * Gets the altitude in meters above or below sea level.
     *
     * @return The altitude.
     * @see #setAltitude(Double)
     */
    public Double getAltitude() {
        return altitude;
    }

    /**
     * Sets the altitude in meters above or below sea level.
     *
     * @param altitude The altitude.
     * @see #getAltitude()
     */
    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    /**
     * Gets a named area such as a campus or neighborhood.
     *
     * @return The area.
     * @see #setArea(String)
     */
    public String getArea() {
        return area;
    }

    /**
     * Sets a named area such as a campus or neighborhood.
     *
     * @param area The area.
     * @see #getArea()
     */
    public void setArea(String area) {
        this.area = area;
    }

    /**
     * Gets the GPS bearing (direction in which the entity is heading to reach its next waypoint), measured in decimal degrees relative to true north.
     *
     * @return The bearing.
     * @see #setBearing(Double)
     */
    public Double getBearing() {
        return bearing;
    }

    /**
     * Sets the GPS bearing (direction in which the entity is heading to reach its next waypoint), measured in decimal degrees relative to true north.
     *
     * @param bearing The bearing.
     * @see #getBearing()
     */
    public void setBearing(Double bearing) {
        this.bearing = bearing;
    }

    /**
     * Gets a specific building on a street or in an area.
     *
     * @return The building.
     * @see #setBuilding(String)
     */
    public String getBuilding() {
        return building;
    }

    /**
     * Sets a specific building on a street or in an area.
     *
     * @param building The building.
     * @see #getBuilding()
     */
    public void setBuilding(String building) {
        this.building = building;
    }

    /**
     * Gets the nation where the user is located.
     *
     * @return The country.
     * @see #setCountry(String)
     */
    public String getCountry() {
        return country;
    }

    /**
     * Sets the nation where the user is located.
     *
     * @param country The country.
     * @see #getCountry()
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * Gets the ISO 3166 two-letter country code.
     *
     * @return The country code.
     * @see #setCountryCode(String)
     */
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * Sets the ISO 3166 two-letter country code.
     *
     * @param countryCode The country code.
     * @see #getCountryCode()
     */
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    /**
     * Gets the GPS datum.
     *
     * @return The GPS datum.
     * @see #setDatum(String)
     */
    public String getDatum() {
        return datum;
    }

    /**
     * Sets the GPS datum.
     *
     * @param datum The GPS datum.
     * @see #getDatum()
     */
    public void setDatum(String datum) {
        this.datum = datum;
    }

    /**
     * Gets a natural-language name for or description of the location.
     *
     * @return The description.
     * @see #setDescription(String)
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets a natural-language name for or description of the location.
     *
     * @param description The description.
     * @see #getDescription()
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets a particular floor in a building.
     *
     * @return The floor.
     * @see #setFloor(String)
     */
    public String getFloor() {
        return floor;
    }

    /**
     * Sets a particular floor in a building.
     *
     * @param floor The floor.
     * @see #getFloor()
     */
    public void setFloor(String floor) {
        this.floor = floor;
    }

    /**
     * Gets the latitude in decimal degrees North.
     *
     * @return The latitude.
     * @see #setLatitude(Double)
     */
    public Double getLatitude() {
        return latitude;
    }

    /**
     * Sets the latitude in decimal degrees North.
     *
     * @param latitude The latitude.
     * @see #getLatitude()
     */
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    /**
     * Gets a locality within the administrative region, such as a town or city.
     *
     * @return The locality.
     * @see #setLocality(String)
     */
    public String getLocality() {
        return locality;
    }

    /**
     * Sets a locality within the administrative region, such as a town or city.
     *
     * @param locality The locality.
     * @see #getLocality()
     */
    public void setLocality(String locality) {
        this.locality = locality;
    }

    /**
     * Gets the longitude in decimal degrees East.
     *
     * @return The longitude.
     * @see #setLongitude(Double)
     */
    public Double getLongitude() {
        return longitude;
    }

    /**
     * Sets the longitude in decimal degrees East.
     *
     * @param longitude The longitude.
     * @see #getLongitude()
     */
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    /**
     * Gets a code used for postal delivery.
     *
     * @return The postal code.
     * @see #setPostalCode(String)
     */
    public String getPostalCode() {
        return postalCode;
    }

    /**
     * Sets a code used for postal delivery.
     *
     * @param postalCode The postal code.
     * @see #getPostalCode()
     */
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    /**
     * Gets an administrative region of the nation, such as a state or province.
     *
     * @return The region.
     * @see #setRegion(String)
     */
    public String getRegion() {
        return region;
    }

    /**
     * Sets an administrative region of the nation, such as a state or province.
     *
     * @param region The region.
     * @see #getRegion()
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * Gets a particular room in a building.
     *
     * @return The room.
     * @see #setRoom(String)
     */
    public String getRoom() {
        return room;
    }

    /**
     * Sets a particular room in a building.
     *
     * @param room The room.
     * @see #getRoom()
     */
    public void setRoom(String room) {
        this.room = room;
    }

    /**
     * Gets the speed at which the entity is moving, in meters per second.
     *
     * @return The speed.
     * @see #setSpeed(Double)
     */
    public Double getSpeed() {
        return speed;
    }

    /**
     * Sets the speed at which the entity is moving, in meters per second.
     *
     * @param speed The speed.
     * @see #getSpeed()
     */
    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    /**
     * Gets a thoroughfare within the locality, or a crossing of two thoroughfares.
     *
     * @return The street.
     * @see #setStreet(String)
     */
    public String getStreet() {
        return street;
    }

    /**
     * Sets a thoroughfare within the locality, or a crossing of two thoroughfares.
     *
     * @param street The street.
     * @see #setStreet(String)
     */
    public void setStreet(String street) {
        this.street = street;
    }

    /**
     * Gets a catch-all element that captures any other information about the location.
     *
     * @return The text.
     * @see #setText(String)
     */
    public String getText() {
        return text;
    }

    /**
     * Sets a catch-all element that captures any other information about the location.
     *
     * @param text The text.
     * @see #getText()
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Gets the UTC timestamp specifying the moment when the reading was taken.
     *
     * @return The timestamp.
     * @see #setTimestamp(java.util.Date)
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the UTC timestamp specifying the moment when the reading was taken.
     *
     * @param timestamp The timestamp.
     * @see #getTimestamp()
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Gets a URI or URL pointing to information about the location.
     *
     * @return The URI.
     * @see #setUri(java.net.URI)
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Sets a URI or URL pointing to information about the location.
     *
     * @param uri The URI.
     * @see #getUri()
     */
    public void setUri(URI uri) {
        this.uri = uri;
    }

    /**
     * Gets the the natural language of location data.
     *
     * @return The language.
     * @see #setLanguage(String)
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Sets the natural language of location data.
     *
     * @param language The language.
     * @see #getLanguage()
     */
    public void setLanguage(String language) {
        this.language = language;
    }
}
