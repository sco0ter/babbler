/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2020 Christian Schudt
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

package rocks.xmpp.extensions.softwareinfo;

/**
 * A common interface for the different but similar software information protocols <a href="https://xmpp.org/extensions/xep-0232.html">XEP-0232: Software Information</a>
 * and <a href="https://xmpp.org/extensions/xep-0092.html">XEP-0092: Software Version</a>.
 */
public interface SoftwareInfo {

    /**
     * The operating system on which the XMPP software is running.
     *
     * @return The operating system.
     */
    String getOs();

    /**
     * Gets the XMPP software running at the entity.
     *
     * @return The XMPP software running at the entity.
     */
    String getSoftware();

    /**
     * Gets the XMPP software version.
     *
     * @return The XMPP software version.
     */
    String getSoftwareVersion();
}
