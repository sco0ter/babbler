/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Christian Schudt
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

package rocks.xmpp.core.stanza.model;

import rocks.xmpp.addr.Jid;

import javax.xml.bind.annotation.XmlTransient;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * @author Christian Schudt
 */
@XmlTransient
abstract class ExtensibleStanza extends Stanza {

    ExtensibleStanza(Jid to, Jid from, String id, Locale language, Collection<?> extensions, StanzaError error) {
        super(to, from, id, language, extensions, error);
    }

    /**
     * Adds an extension.
     *
     * @param extension The extension.
     * @return If the extension was added.
     */
    public final boolean addExtension(Object extension) {
        return getExtensions().add(extension);
    }

    /**
     * Removes all extensions of the given type.
     *
     * @param clazz The extension class.
     * @return If the extension could be removed.
     */
    public final boolean removeExtension(Class<?> clazz) {
        return getExtensions().removeIf(extension -> clazz.isAssignableFrom(extension.getClass()));
    }

    /**
     * Gets all extensions.
     *
     * @return The extensions.
     */
    public final List<Object> getExtensions() {
        return extensions;
    }
}