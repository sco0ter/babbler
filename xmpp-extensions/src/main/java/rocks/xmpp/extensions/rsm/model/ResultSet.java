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

package rocks.xmpp.extensions.rsm.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The implementation of the {@code <set/>} element in the {@code http://jabber.org/protocol/rsm} namespace.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0059.html">XEP-0059: Result Set Management</a>
 * @see <a href="http://xmpp.org/extensions/xep-0059.html#schema">XML Schema</a>
 */
@XmlRootElement(name = "set")
public final class ResultSet {

    @XmlElement(name = "max")
    private Integer max;

    @XmlElement(name = "after")
    private String after;

    @XmlElement(name = "before")
    private String before;

    @XmlElement(name = "count")
    private Integer count;

    @XmlElement(name = "first")
    private First first;

    @XmlElement(name = "index")
    private Integer index;

    @XmlElement(name = "last")
    private String last;

    private ResultSet() {
    }

    /**
     * Gets a result set, which limits the number of items of a result to be returned.
     *
     * @param limit The limit, i.e. the maximum number of items.
     * @return The result set.
     * @see <a href="http://xmpp.org/extensions/xep-0059.html#limit">2.1 Limiting the Number of Items</a>
     */
    public static ResultSet forLimit(int limit) {
        ResultSet resultSet = new ResultSet();
        resultSet.max = limit;
        return resultSet;
    }

    /**
     * Gets a result set, which requests the first page.
     *
     * @param itemCount The item count per page.
     * @return The result set.
     * @see <a href="http://xmpp.org/extensions/xep-0059.html#forwards">2.2 Paging Forwards Through a Result Set</a>
     */
    public static ResultSet forFirstPage(int itemCount) {
        return forLimit(itemCount);
    }

    /**
     * Gets a result set, which requests the next page after a specified item.
     *
     * @param itemCount The item count per page.
     * @param id        The id of the last item of the previous page. This should be the {@link #getLastItem()} ()} of the previous page.
     * @return The result set.
     * @see <a href="http://xmpp.org/extensions/xep-0059.html#forwards">2.2 Paging Forwards Through a Result Set</a>
     */
    public static ResultSet forNextPage(int itemCount, String id) {
        ResultSet resultSet = new ResultSet();
        resultSet.max = itemCount;
        resultSet.after = id;
        return resultSet;
    }

    /**
     * Gets a result set, which requests the previous page before a specified item.
     *
     * @param itemCount The item count per page.
     * @param id        The id of the first item of the next page. This should be the {@link #getFirstItem()} of the next page.
     * @return The result set.
     * @see <a href="http://xmpp.org/extensions/xep-0059.html#backwards">2.3 Paging Backwards Through a Result Set</a>
     */
    public static ResultSet forPreviousPage(int itemCount, String id) {
        ResultSet resultSet = new ResultSet();
        resultSet.max = itemCount;
        resultSet.before = id;
        return resultSet;
    }

    /**
     * Gets a result set, which requests the last page.
     *
     * @param itemCount The item count per page.
     * @return The result set.
     * @see <a href="http://xmpp.org/extensions/xep-0059.html#last">2.5 Requesting the Last Page in a Result Set</a>
     */
    public static ResultSet forLastPage(int itemCount) {
        ResultSet resultSet = new ResultSet();
        resultSet.max = itemCount;
        resultSet.before = "";
        return resultSet;
    }

    /**
     * Gets a result set, which starts at a particular index.
     *
     * @param itemCount The item count per page.
     * @param index     The index to start from.
     * @return The result set.
     * @see <a href="http://xmpp.org/extensions/xep-0059.html#jump">2.6 Retrieving a Page Out of Order</a>
     */
    public static ResultSet forIndex(int itemCount, int index) {
        ResultSet resultSet = new ResultSet();
        resultSet.max = itemCount;
        resultSet.index = index;
        return resultSet;
    }

    /**
     * Gets a result set, which gets the item count of a result without retrieving the items themselves.
     *
     * @return The result set.
     * @see <a href="http://xmpp.org/extensions/xep-0059.html#count">2.7 Getting the Item Count</a>
     */
    public static ResultSet forItemCount() {
        return forLimit(0);
    }

    /**
     * Gets the item count of a result set.
     *
     * @return The item count.
     */
    public Integer getItemCount() {
        return count;
    }

    /**
     * Gets the first item.
     *
     * @return The first item.
     */
    public String getFirstItem() {
        return first != null ? first.value : null;
    }

    /**
     * Gets the first item's index.
     *
     * @return The first item's index.
     */
    public Integer getFirstItemIndex() {
        return first != null ? first.index : null;
    }

    /**
     * Gets the last item.
     *
     * @return The last item.
     */
    public String getLastItem() {
        return last;
    }
}
