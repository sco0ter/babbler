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

package rocks.xmpp.core.util.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A simple directory based cache for caching of persistent items like avatars or entity capabilities.
 *
 * @author Christian Schudt
 */
public final class DirectoryCache implements Map<String, byte[]> {

    private final File cacheDirectory;

    public DirectoryCache(File cacheDirectory) {
        this.cacheDirectory = cacheDirectory;
        // Make sure the directory exists.
        cacheDirectory.mkdirs();
    }

    private static void deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
    }

    @Override
    public int size() {
        return cacheDirectory.list().length;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return Arrays.asList(cacheDirectory.list()).contains(key);
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] get(final Object key) {
        if (key != null && !key.toString().isEmpty()) {
            File file = new File(cacheDirectory, key.toString());
            if (file.exists()) {
                try (FileInputStream fileInputStream = new FileInputStream(file)) {
                    byte[] data = new byte[(int) file.length()];
                    if (fileInputStream.read(data, 0, data.length) > -1) {
                        return data;
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }

    @Override
    public byte[] put(String key, byte[] value) {
        File file = new File(cacheDirectory, key);
        byte[] data = get(key);
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return data;
    }

    @Override
    public byte[] remove(Object key) {
        File file = new File(cacheDirectory, key.toString());
        byte[] data = get(key);
        file.delete();
        return data;
    }

    @Override
    public void putAll(Map<? extends String, ? extends byte[]> m) {
        for (Map.Entry<? extends String, ? extends byte[]> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        File[] files = cacheDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
    }

    @Override
    public Set<String> keySet() {
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(cacheDirectory.list())));
    }

    @Override
    public Collection<byte[]> values() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Entry<String, byte[]>> entrySet() {
        throw new UnsupportedOperationException();
    }
}
