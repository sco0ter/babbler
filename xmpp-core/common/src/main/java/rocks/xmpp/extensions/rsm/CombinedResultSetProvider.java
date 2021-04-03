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

package rocks.xmpp.extensions.rsm;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import rocks.xmpp.extensions.rsm.model.ResultSetItem;

/**
 * Combines multiple result set providers into one.
 *
 * @param <T> The result set item.
 */
final class CombinedResultSetProvider<T extends ResultSetItem> implements ResultSetProvider<T> {

    private final Iterable<ResultSetProvider<T>> resultSetProviders;

    CombinedResultSetProvider(final Iterable<ResultSetProvider<T>> resultSetProviders) {
        this.resultSetProviders = resultSetProviders;
    }

    @Override
    public final List<T> getItems() {
        return StreamSupport.stream(resultSetProviders.spliterator(), false)
                .flatMap(resultSetProvider -> resultSetProvider.getItems().stream())
                .collect(Collectors.toList());
    }

    @Override
    public final int getItemCount() {
        return StreamSupport.stream(resultSetProviders.spliterator(), false)
                .flatMapToInt(resultSetProvider -> IntStream.of(resultSetProvider.getItemCount()))
                .sum();
    }

    @Override
    public final List<T> getItems(final int index, final int maxSize) {
        final List<T> items = new ArrayList<>();
        int currentCount = 0;
        int i = index;
        for (ResultSetProvider<T> resultSetProvider : resultSetProviders) {
            final int count = resultSetProvider.getItemCount();
            final int size = items.size();
            currentCount += count;
            if (index < currentCount) {
                if (maxSize > size) {
                    items.addAll(resultSetProvider.getItems(i, maxSize - size));
                }
                if (items.size() >= maxSize) {
                    break;
                }
                // For all subsequent providers, the index will be 0.
                i = 0;
            } else {
                i = index - currentCount;
            }
        }
        return items;
    }

    @Override
    public final List<T> getItemsAfter(final String itemId, final int maxSize) {
        return getItems(indexOf(itemId) + 1, maxSize);
    }

    @Override
    public final List<T> getItemsBefore(final String itemId, final int maxSize) {
        return getItems(indexOf(itemId) - maxSize, maxSize);
    }

    @Override
    public final int indexOf(final String itemId) {
        int count = 0;
        for (ResultSetProvider<T> resultSetProvider : resultSetProviders) {
            int index = resultSetProvider.indexOf(itemId);
            if (index > -1) {
                return index + count;
            }
            count += resultSetProvider.getItemCount();
        }
        return -1;
    }
}
