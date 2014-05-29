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

package org.xmpp.extension.httpbind;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christian Schudt
 */
public class BoshConnectionTest {

    @Test
    public void testInsertionOrder() {

        BoshConnection boshConnection = new BoshConnection("", 5280);

        Body body1 = new Body();
        boshConnection.unacknowledgedRequests.put(1L, body1);

        Body body2 = new Body();
        boshConnection.unacknowledgedRequests.put(2L, body2);

        Body body3 = new Body();
        boshConnection.unacknowledgedRequests.put(3L, body3);

        Body body4 = new Body();
        boshConnection.unacknowledgedRequests.put(4L, body4);

        Body body5 = new Body();
        boshConnection.unacknowledgedRequests.put(5L, body5);

        int i = 0;
        for (Body body : boshConnection.unacknowledgedRequests.values()) {
            switch (i) {
                case 0:
                    Assert.assertEquals(body, body1);
                    break;
                case 1:
                    Assert.assertEquals(body, body2);
                    break;
                case 2:
                    Assert.assertEquals(body, body3);
                    break;
                case 3:
                    Assert.assertEquals(body, body4);
                    break;
                case 4:
                    Assert.assertEquals(body, body5);
                    break;
            }
            i++;
        }
    }
}
