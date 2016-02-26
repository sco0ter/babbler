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

package rocks.xmpp.util.cache;

import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * A simple concurrent implementation of a least-recently-used cache.
 * <p>
 * This cache is keeps a maximal number of items in memory and removes the least-recently-used item, when new items are added.
 *
 * @param <K> The key.
 * @param <V> The value.
 * @author Christian Schudt
 * @see <a href="http://javadecodedquestions.blogspot.de/2013/02/java-cache-static-data-loading.html">http://javadecodedquestions.blogspot.de/2013/02/java-cache-static-data-loading.html</a>
 * @see <a href="http://stackoverflow.com/a/22891780">http://stackoverflow.com/a/22891780</a>
 */
public final class LruCache<K, V> implements Map<K, V> {
    private final int maxEntries;

    private final Map<K, V> map;

    private final Queue<K> queue;

    public LruCache(int maxEntries) {
        this.maxEntries = maxEntries;
        this.map = new ConcurrentHashMap<>(maxEntries);
        this.queue = new ConcurrentLinkedDeque<>();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        // Remove the key from the queue and re-add it to the tail. It is now the most recently used key.
        if (queue.remove(key)) {
            queue.offer((K) key);
        }
        return map.get(key);
    }

    @Override
    public V put(final K key, final V value) {
        // Put the new key/value in the map.
        V oldValue = map.put(key, value);
        if (oldValue != null) {
            // If the key already existed, remove it from the queue and re-add it, to make it the most recently used key.
            if (queue.remove(key)) {
                queue.offer(key);
            }
        } else {
            queue.offer(key);
        }

        while (queue.size() > maxEntries) {
            K oldestKey = queue.poll();
            if (null != oldestKey) {
                map.remove(oldestKey);
            }
        }
        return oldValue;
    }

    @Override
    public V remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<V> values() {
        return map.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return map.entrySet();
    }
}