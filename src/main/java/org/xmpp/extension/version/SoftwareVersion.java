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

package org.xmpp.extension.version;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The implementation of the software version protocol extension.
 *
 * @author Christian Schudt
 */
@XmlRootElement(name = "query")
public final class SoftwareVersion {

    @XmlElement
    private String name;

    @XmlElement
    private String version;

    @XmlElement
    private String os;

    SoftwareVersion() {
    }

    /**
     * Creates a software version instance.
     * The operating system will be determined automatically.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/extensions/xep-0092.html#security">5. Security Considerations</a></cite></p>
     * <p>Revealing the application's underlying operating system may open the user or system to attacks directed against that operating system; therefore, an application MUST provide a way for a human user or administrator to disable sharing of information about the operating system.</p>
     * </blockquote>
     * If you want to hide OS information, use {@link #SoftwareVersion(String, String, String)} and pass null for the os parameter.
     *
     * @param name    The software's name.
     * @param version The software's version.
     */
    public SoftwareVersion(String name, String version) {
        this(name, version, System.getProperty("os.name"));
    }

    /**
     * Creates a software version instance.
     *
     * @param name    The software's name.
     * @param version The software's version.
     * @param os      The operating system.
     */
    public SoftwareVersion(String name, String version, String os) {
        this.name = name;
        this.version = version;
        this.os = os;
    }

    /**
     * Gets the operating system.
     *
     * @return The operating system.
     */
    public String getOs() {
        return os;
    }

    /**
     * Gets the version.
     *
     * @return The version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets the software's name.
     *
     * @return The software's name.
     */
    public String getName() {
        return name;
    }
}
