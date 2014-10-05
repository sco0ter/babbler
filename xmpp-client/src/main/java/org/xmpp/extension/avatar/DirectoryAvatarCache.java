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

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;

/**
 * @author Christian Schudt
 */
public enum DirectoryAvatarCache implements AvatarCache {

    INSTANCE;

    private File cacheDirectory = new File(System.getProperty("user.dir"), "avatars");

    public void setCacheDirectory(File file) {
        this.cacheDirectory = file;
    }

    @Override
    public void store(String hash, byte[] imageData) throws IOException {
        ImageInputStream iis = null;
        iis = ImageIO.createImageInputStream(new ByteArrayInputStream(imageData));
        Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
        if (readers.hasNext()) {
            ImageReader reader = readers.next();
            String format = reader.getFormatName();
            boolean exists = cacheDirectory.exists();
            if (!exists) {
                exists = cacheDirectory.mkdir();
            }
            if (exists) {
                ImageIO.write(ImageIO.read(iis), format, new File(cacheDirectory, hash + "." + format));
            }
        }
    }

    @Override
    public byte[] load(final String hash) throws IOException {
        File dir = new File(cacheDirectory, ".");
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(hash);
            }
        });
        if (files != null && files.length > 0) {
            ByteArrayOutputStream baos = null;
            try {
                BufferedImage originalImage = ImageIO.read(files[0]);
                baos = new ByteArrayOutputStream();
                ImageIO.write(originalImage, "png", baos);
                baos.flush();
                return baos.toByteArray();
            } finally {
                if (baos != null) {
                    baos.close();
                }
            }
        }
        return null;
    }
}
