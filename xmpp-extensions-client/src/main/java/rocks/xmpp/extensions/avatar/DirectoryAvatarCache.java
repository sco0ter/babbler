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

package rocks.xmpp.extensions.avatar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author Christian Schudt
 */
final class DirectoryAvatarCache implements Map<String, byte[]> {

    private final File cacheDirectory;

    public DirectoryAvatarCache(File cacheDirectory) {
        this.cacheDirectory = cacheDirectory;
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsKey(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] get(final Object key) {
        if (key != null && !key.toString().isEmpty()) {
            File dir = new File(cacheDirectory, ".");
            File[] files = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith(key.toString());
                }
            });
            if (files != null && files.length > 0) {
                File file = files[0];
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
    public byte[] put(String hash, byte[] imageData) {
        boolean exists = cacheDirectory.exists();
        if (!exists) {
            exists = cacheDirectory.mkdir();
        }
        File file = new File(cacheDirectory, hash + ".avatar");
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
        if (exists) {
            try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                fileOutputStream.write(imageData);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    @Override
    public byte[] remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends String, ? extends byte[]> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> keySet() {
        throw new UnsupportedOperationException();
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
