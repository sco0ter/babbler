/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Christian Schudt
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

package rocks.xmpp.core;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.session.TruncatedBinaryExponentialBackoffStrategy;

/**
 * @author Christian Schudt
 */
public class TruncatedBinaryExponentialBackoffStrategyTest {

    @Test
    public void test() {
        TruncatedBinaryExponentialBackoffStrategy truncatedBinaryExponentialBackoffStrategy = new TruncatedBinaryExponentialBackoffStrategy(60, 4);
        int first = truncatedBinaryExponentialBackoffStrategy.getNextReconnectionAttempt(0);

        Assert.assertTrue(first >= 0 && first < 60);
        int second = truncatedBinaryExponentialBackoffStrategy.getNextReconnectionAttempt(1);
        Assert.assertTrue(second >= 0 && second < 180);
        int third = truncatedBinaryExponentialBackoffStrategy.getNextReconnectionAttempt(2);
        Assert.assertTrue(third >= 0 && third < 420);
        int fourth = truncatedBinaryExponentialBackoffStrategy.getNextReconnectionAttempt(3);
        Assert.assertTrue(fourth >= 0 && fourth < 900);
        int fifth = truncatedBinaryExponentialBackoffStrategy.getNextReconnectionAttempt(4);
        Assert.assertTrue(fifth >= 0 && fifth < 1860);
        int sixth = truncatedBinaryExponentialBackoffStrategy.getNextReconnectionAttempt(5);
        Assert.assertTrue(sixth >= 0 && sixth < 1860);
        int seventh = truncatedBinaryExponentialBackoffStrategy.getNextReconnectionAttempt(6);
        Assert.assertTrue(seventh >= 0 && seventh < 1860);
    }
}
