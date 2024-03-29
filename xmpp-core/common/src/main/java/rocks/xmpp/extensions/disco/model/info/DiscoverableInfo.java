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

package rocks.xmpp.extensions.disco.model.info;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import rocks.xmpp.extensions.data.model.DataForm;

/**
 * Information which can be discovered via Service Discovery.
 *
 * <p>It includes, an entity's identities, features and extensions.</p>
 *
 * @author Christian Schudt
 */
public interface DiscoverableInfo {

    /**
     * Gets the identities.
     *
     * @return The identities.
     */
    default Set<Identity> getIdentities() {
        return Collections.emptySet();
    }

    /**
     * Gets the features.
     *
     * @return The features.
     */
    Set<String> getFeatures();

    /**
     * Gets the service discovery extensions as described in <a href="https://xmpp.org/extensions/xep-0128.html">XEP-0128:
     * Service Discovery Extensions</a>
     *
     * @return The service discovery extensions.
     */
    default List<DataForm> getExtensions() {
        return Collections.emptyList();
    }
}
