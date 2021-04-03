/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2021 Christian Schudt
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

package rocks.xmpp.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for the {@link ByteBufferOutputStream} class.
 *
 * @author Christian Schudt
 */
public class ByteBufferOutputStreamTest {

    @Test
    public void testWrite() {
        try (ByteBufferOutputStream byteBufferOutputStream = new ByteBufferOutputStream(4, false)) {
            byteBufferOutputStream.write('a');
            ByteBuffer byteBuffer = byteBufferOutputStream.getBuffer();

            // Prepare for reading
            byteBuffer = byteBuffer.flip();
            Assert.assertEquals(byteBuffer.capacity(), 4);
            Assert.assertEquals(byteBuffer.position(), 0);
            Assert.assertEquals(byteBuffer.limit(), 1);
            Assert.assertEquals(byteBuffer.get(), 'a');
        }
    }

    @Test
    public void testWriteByteArray() throws IOException {
        try (ByteBufferOutputStream byteBufferOutputStream = new ByteBufferOutputStream(4, false)) {
            byteBufferOutputStream.write("Hello World".getBytes(StandardCharsets.UTF_8));
            ByteBuffer byteBuffer = byteBufferOutputStream.getBuffer();

            // Prepare for reading
            byteBuffer = byteBuffer.flip();
            Assert.assertEquals(byteBuffer.capacity(), 11);
            Assert.assertEquals(byteBuffer.position(), 0);
            Assert.assertEquals(byteBuffer.limit(), 11);
            byte[] dest = new byte[11];
            byteBuffer.get(dest);
            Assert.assertEquals(new String(dest, StandardCharsets.UTF_8), "Hello World");
        }
    }

    @Test
    public void testWriteByteArrayWithOffset() {
        try (ByteBufferOutputStream byteBufferOutputStream = new ByteBufferOutputStream(4, false)) {
            byteBufferOutputStream.write("Hello World".getBytes(StandardCharsets.UTF_8), 6, 5);
            ByteBuffer byteBuffer = byteBufferOutputStream.getBuffer();

            // Prepare for reading
            byteBuffer = byteBuffer.flip();
            Assert.assertEquals(byteBuffer.capacity(), 8);
            Assert.assertEquals(byteBuffer.position(), 0);
            Assert.assertEquals(byteBuffer.limit(), 5);
            byte[] dest = new byte[5];
            byteBuffer.get(dest);
            Assert.assertEquals(new String(dest, StandardCharsets.UTF_8), "World");
        }
    }
}
