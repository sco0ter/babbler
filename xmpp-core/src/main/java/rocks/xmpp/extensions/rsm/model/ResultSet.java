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

package rocks.xmpp.extensions.rsm.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A result set usually consists of a list of items and additional result set information (e.g. to mark first and last items).
 * <p>
 * This class is immutable.
 *
 * @author Christian Schudt
 */
public final class ResultSet<T extends ResultSetItem> {

    private final List<T> items = new ArrayList<>();

    private final ResultSetManagement resultSetManagement;

    public ResultSet(Collection<T> items, ResultSetManagement resultSetManagement) {
        if (items != null) {
            this.items.addAll(items);
        }
        this.resultSetManagement = resultSetManagement;
    }

    /**
     * Gets the items.
     *
     * @return The items.
     */
    public final List<T> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Gets the result set info.
     *
     * @return The result set info.
     */
    public final ResultSetManagement getResultSetManagement() {
        return resultSetManagement;
    }
}
