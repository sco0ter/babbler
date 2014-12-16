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

package rocks.xmpp.extensions.rsm;

import rocks.xmpp.core.session.ExtensionManager;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.extensions.rsm.model.ResultSet;
import rocks.xmpp.extensions.rsm.model.ResultSetItem;
import rocks.xmpp.extensions.rsm.model.ResultSetManagement;

import java.util.List;

/**
 * @author Christian Schudt
 */
public final class ResultSetManager extends ExtensionManager {
    private ResultSetManager(XmppSession xmppSession) {
        super(xmppSession, ResultSetManagement.NAMESPACE);
        setEnabled(true);
    }

    public static <T extends ResultSetItem> ResultSet<T> createResultSet(ResultSetProvider<T> resultSetProvider, ResultSetManagement resultSet) {

        // If the query has a RSM extension, use it and return a limited result set.
        if (resultSet != null && resultSet.getMaxSize() != null) {
            // If the max size == 0, it means the requester wants to know the total item count only.
            if (resultSet.getMaxSize() != 0) {
                if (resultSet.getAfter() != null) {
                    // 2.2 Paging Forwards Through a Result Set
                    return createResponse(resultSetProvider.getItemsAfter(resultSet.getAfter(), resultSet.getMaxSize()), resultSetProvider);
                } else if (resultSet.getBefore() != null) {
                    if (!resultSet.getBefore().isEmpty()) {
                        // 2.3 Paging Backwards Through a Result Set
                        return createResponse(resultSetProvider.getItemsBefore(resultSet.getBefore(), resultSet.getMaxSize()), resultSetProvider);
                    } else {
                        // 2.5 Requesting the Last Page in a Result Set
                        return createResponse(resultSetProvider.getItems(resultSetProvider.getItemCount() - resultSet.getItemCount(), resultSet.getMaxSize()), resultSetProvider);
                    }
                } else {
                    // 2.6 Retrieving a Page Out of Order
                    int fromIndex = resultSet.getIndex() != null ? resultSet.getIndex() : 0;
                    return createResponse(resultSetProvider.getItems(fromIndex, resultSet.getMaxSize()), resultSetProvider);
                }
            } else {
                // 2.7 Getting the Item Count
                return new ResultSet<>(null, ResultSetManagement.forCount(resultSetProvider.getItemCount()));
            }
        } else {
            // If there's no RSM in the query, return all items, without any result set info.
            return new ResultSet<>(resultSetProvider.getItems(), null);
        }
    }

    private static <T extends ResultSetItem> ResultSet<T> createResponse(List<T> result, final ResultSetProvider<T> resultSetProvider) {
        ResultSetManagement resultSetManagement;
        if (!result.isEmpty()) {
            //  the responding entity MUST include <first/> and <last/> elements that specify the unique ID (UID) for the first and last items in the page.
            resultSetManagement = ResultSetManagement.forCount(resultSetProvider.getItemCount(), resultSetProvider.indexOf(result.get(0).getId()), result.get(0).getId(), result.get(result.size() - 1).getId());
        } else {
            // If there are no items in the page, then the <first/> and <last/> elements MUST NOT be included.
            // Only return the <count/> in this case.
            resultSetManagement = ResultSetManagement.forCount(resultSetProvider.getItemCount());
        }
        return new ResultSet<>(result, resultSetManagement);
    }
}
