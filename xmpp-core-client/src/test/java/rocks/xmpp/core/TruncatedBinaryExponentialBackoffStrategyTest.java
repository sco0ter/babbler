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
import rocks.xmpp.core.session.ReconnectionStrategy;

import java.time.Duration;

/**
 * @author Christian Schudt
 */
public class TruncatedBinaryExponentialBackoffStrategyTest {

    @Test
    public void test() {
        ReconnectionStrategy truncatedBinaryExponentialBackoffStrategy = ReconnectionStrategy.truncatedBinaryExponentialBackoffStrategy(60, 4);
        Duration first = truncatedBinaryExponentialBackoffStrategy.getNextReconnectionAttempt(0, null);

        Assert.assertTrue(first.getSeconds() >= 0 && first.getSeconds() < 60);
        Duration second = truncatedBinaryExponentialBackoffStrategy.getNextReconnectionAttempt(1, null);
        Assert.assertTrue(second.getSeconds() >= 0 && second.getSeconds() < 180);
        Duration third = truncatedBinaryExponentialBackoffStrategy.getNextReconnectionAttempt(2, null);
        Assert.assertTrue(third.getSeconds() >= 0 && third.getSeconds() < 420);
        Duration fourth = truncatedBinaryExponentialBackoffStrategy.getNextReconnectionAttempt(3, null);
        Assert.assertTrue(fourth.getSeconds() >= 0 && fourth.getSeconds() < 900);
        Duration fifth = truncatedBinaryExponentialBackoffStrategy.getNextReconnectionAttempt(4, null);
        Assert.assertTrue(fifth.getSeconds() >= 0 && fifth.getSeconds() < 1860);
        Duration sixth = truncatedBinaryExponentialBackoffStrategy.getNextReconnectionAttempt(5, null);
        Assert.assertTrue(sixth.getSeconds() >= 0 && sixth.getSeconds() < 1860);
        Duration seventh = truncatedBinaryExponentialBackoffStrategy.getNextReconnectionAttempt(6, null);
        Assert.assertTrue(seventh.getSeconds() >= 0 && seventh.getSeconds() < 1860);
    }
}
