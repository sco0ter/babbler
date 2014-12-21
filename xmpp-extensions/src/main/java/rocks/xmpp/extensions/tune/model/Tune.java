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

package rocks.xmpp.extensions.tune.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

/**
 * The implementation of the {@code <tune/>} element in the {@code http://jabber.org/protocol/tune} namespace.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0118.html">XEP-0118: User Tune</a>
 * @see <a href="http://xmpp.org/extensions/xep-0118.html#schema">XML Schema</a>
 */
@XmlRootElement(name = "tune")
public final class Tune {

    /**
     * http://jabber.org/protocol/tune
     */
    public static final String NAMESPACE = "http://jabber.org/protocol/tune";

    @XmlElement(name = "artist")
    private String artist;

    @XmlElement(name = "length")
    private Integer length;

    @XmlElement(name = "rating")
    private Integer rating;

    @XmlElement(name = "source")
    private String source;

    @XmlElement(name = "title")
    private String title;

    @XmlElement(name = "track")
    private String track;

    @XmlElement(name = "uri")
    private URI uri;

    @Deprecated
    public Tune() {
    }

    public Tune(String artist, String title) {
        this.artist = artist;
        this.title = title;
    }

    public Tune(String artist, String title, Integer length, Integer rating, String source, String track, URI uri) {
        this.artist = artist;
        this.title = title;
        this.length = length;
        if (rating != null && (rating < 0 || rating > 10)) {
            throw new IllegalArgumentException("rating must not be greater than 10.");
        }
        this.rating = rating;
        this.source = source;
        this.track = track;
        this.uri = uri;
    }

    /**
     * Gets the artist or performer of the song or piece.
     *
     * @return The artist.
     * @see #setArtist(String)
     */
    public String getArtist() {
        return artist;
    }

    /**
     * Sets the artist or performer of the song or piece.
     *
     * @param artist The artist.
     * @see #getArtist()
     * @deprecated Use constructor.
     */
    @Deprecated
    public void setArtist(String artist) {
        this.artist = artist;
    }

    /**
     * Gets the duration of the song or piece in seconds.
     *
     * @return The duration.
     * @see #setLength(Integer)
     */
    public Integer getLength() {
        return length;
    }

    /**
     * Sets the duration of the song or piece in seconds.
     *
     * @param length The length.
     * @see #getLength()
     * @deprecated Use constructor.
     */
    @Deprecated
    public void setLength(Integer length) {
        this.length = length;
    }

    /**
     * Gets the user's rating of the song or piece, from 1 (lowest) to 10 (highest).
     *
     * @return The rating.
     * @see #setRating(Integer)
     */
    public Integer getRating() {
        return rating;
    }

    /**
     * Sets the user's rating of the song or piece, from 1 (lowest) to 10 (highest).
     *
     * @param rating The rating.
     * @see #getRating()
     * @deprecated Use constructor.
     */
    @Deprecated
    public void setRating(Integer rating) {
        this.rating = rating;
    }

    /**
     * Gets the collection (e.g., album) or other source (e.g., a band website that hosts streams or audio files).
     *
     * @return The source.
     * @see #setSource(String)
     */
    public String getSource() {
        return source;
    }

    /**
     * Sets the collection (e.g., album) or other source (e.g., a band website that hosts streams or audio files).
     *
     * @param source The source.
     * @see #getSource()
     * @deprecated Use constructor.
     */
    @Deprecated
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Gets the title of the song or piece.
     *
     * @return The title.
     * @see #setTitle(String)
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the song or piece.
     *
     * @param title The title.
     * @see #getTitle()
     * @deprecated Use constructor.
     */
    @Deprecated
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets a unique identifier for the tune; e.g., the track number within a collection or the specific URI for the object (e.g., a stream or audio file).
     *
     * @return The track.
     * @see #setTrack(String)
     */
    public String getTrack() {
        return track;
    }

    /**
     * Sets a unique identifier for the tune; e.g., the track number within a collection or the specific URI for the object (e.g., a stream or audio file).
     *
     * @param track The track.
     * @see #getTrack()
     * @deprecated Use constructor.
     */
    @Deprecated
    public void setTrack(String track) {
        this.track = track;
    }

    /**
     * Gets a URI or URL pointing to information about the song, collection, or artist.
     *
     * @return The URI.
     * @see #setUri(java.net.URI)
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Sets a URI or URL pointing to information about the song, collection, or artist.
     *
     * @param uri The URI.
     * @see #setUri(java.net.URI)
     * @deprecated Use constructor.
     */
    @Deprecated
    public void setUri(URI uri) {
        this.uri = uri;
    }
}
