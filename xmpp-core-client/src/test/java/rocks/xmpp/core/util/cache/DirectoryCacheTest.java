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

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @author Christian Schudt
 */
public class DirectoryCacheTest {

    private static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        if (!file.delete()) {
                            return false;
                        }
                    }
                }
            }
        }
        return (path.delete());
    }

    @Test
    public void testDirectoryCache() throws IOException {

        // Create a temp cache directory.
        File file = Files.createTempDirectory(new File(".").toPath(), "cache_test").toFile();
        DirectoryCache cache = new DirectoryCache(file);
        try {
            byte[] data = new byte[]{1, 2, 3};
            Assert.assertEquals(cache.size(), 0);
            Assert.assertFalse(cache.containsKey("1"));
            Assert.assertTrue(cache.isEmpty());
            Assert.assertNull(cache.put("1", data));
            Assert.assertNotNull(cache.put("1", data));
            byte[] loaded = cache.get("1");
            Assert.assertEquals(loaded, data);
            Assert.assertEquals(cache.size(), 1);
            Assert.assertTrue(cache.containsKey("1"));
            Assert.assertFalse(cache.isEmpty());
            Assert.assertNull(cache.remove("0"));
            Assert.assertEquals(cache.remove("1"), new byte[]{1, 2, 3});
            Assert.assertTrue(cache.isEmpty());

            cache.put("1", data);
            cache.put("2", data);
            cache.put("3", data);

            Assert.assertEquals(cache.size(), 3);
            cache.clear();
            Assert.assertTrue(cache.isEmpty());

        } finally {
            deleteDirectory(file);
        }
    }
}
