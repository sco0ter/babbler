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

package org.xmpp.extension.avatar;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.BaseTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @author Christian Schudt
 */
public class AvatarCacheTest extends BaseTest {

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

        File file = Files.createTempDirectory(new File(".").toPath(), "avatars_test").toFile();
        DirectoryAvatarCache avatarCache = new DirectoryAvatarCache(file);
        try {
            byte[] data = new byte[]{1, 2, 3};
            Assert.assertNull(avatarCache.put("1", data));
            Assert.assertNotNull(avatarCache.put("1", data));
            byte[] loaded = avatarCache.get("1");
            Assert.assertEquals(loaded, data);
        } finally {
            deleteDirectory(file);
        }
    }
}
