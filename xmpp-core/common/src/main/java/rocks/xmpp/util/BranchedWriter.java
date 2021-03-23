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

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

final class BranchedWriter extends FilterWriter {

    private final Writer branch;

    public BranchedWriter(final Writer in, final Writer branch) {
        super(in);
        this.branch = branch;
    }

    @Override
    public void write(int c) throws IOException {
        super.write(c);
        branch.write(c);
    }

    @Override
    public void write(char[] cbuf) throws IOException {
        super.write(cbuf);
        branch.write(cbuf);
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        super.write(cbuf, off, len);
        branch.write(cbuf, off, len);
    }

    @Override
    public void write(String str) throws IOException {
        super.write(str);
        branch.write(str);
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        super.write(str, off, len);
        branch.write(str, off, len);
    }
}
