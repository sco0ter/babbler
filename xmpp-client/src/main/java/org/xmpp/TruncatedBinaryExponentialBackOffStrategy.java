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

package org.xmpp;

import java.util.Random;

/**
 * @author Christian Schudt
 */
final class TruncatedBinaryExponentialBackOffStrategy implements ReconnectionStrategy {

    private static final Random RANDOM = new Random();

    private final int slotTime;

    private final int ceiling;

    /**
     * @param slotTime The slot time (in seconds), usually 60.
     * @param ceiling  The ceiling, i.e. when the time is truncated. E.g. if the ceiling is 5
     */
    public TruncatedBinaryExponentialBackOffStrategy(int slotTime, int ceiling) {
        this.slotTime = slotTime;
        this.ceiling = ceiling;
    }

    @Override
    public int getNextReconnectionAttempt(int attempt) {
        // For the first attempt choose a random number between 0 and 60.
        // For the second attempt choose a random number between 0 and 180.
        // For the third attempt choose a random number between 0 and 420.
        // For the forth attempt choose a random number between 0 and 900.
        // For the fifth attempt choose a random number between 0 and 1860.
        // ==> max wait time: 1860 seconds = 31 minutes. (if ceiling == 4)
        return RANDOM.nextInt((int) (Math.pow(2, Math.min(attempt, ceiling) + 1) - 1) * slotTime);
    }
}
