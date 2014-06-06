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

package org.xmpp.sasl;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.text.Bidi;

/**
 * @author Christian Schudt
 */
public class ScramBaseTest {

    @Test
    public void testMapToSpace() {
        String input = "\u00A0\u1680\u1680\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200A\u200B\u202F\u205F\u3000";
        String output = SaslPrep.prepare(input);
        Assert.assertEquals(output, "                 ");
    }

    @Test
    public void testMapToNothing() {
        String input = "\u00AD\u034F\u1806\u180B\u180C\u180D\u200B\u200C\u200D\u2060\uFE00\uFE01\uFE02\uFE03\uFE04\uFE05\uFE06";
        String output = SaslPrep.prepare(input);
        Assert.assertEquals(output, " ");
    }

    @Test
    public void testExample1() {
        String input = "I\u00ADX";
        String output = SaslPrep.prepare(input);
        Assert.assertEquals(output, "IX");
    }

    @Test
    public void testExample2() {
        String input = "user";
        String output = SaslPrep.prepare(input);
        Assert.assertEquals(output, "user");
    }

    @Test
    public void testExample3() {
        String input = "USER";
        String output = SaslPrep.prepare(input);
        Assert.assertEquals(output, "USER");
    }

    @Test
    public void testExample4() {
        String input = "\u00AA";
        String output = SaslPrep.prepare(input);
        Assert.assertEquals(output, "a");
    }

    @Test
    public void testExample5() {
        String input = "\u2168";
        String output = SaslPrep.prepare(input);
        Assert.assertEquals(output, "IX");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testExample6() {
        String input = "\u0007";
        SaslPrep.prepare(input);
        Assert.fail("Should have thrown exception");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testExample7() {
        String input = "\u0627\u0031";
        Bidi bidi = new Bidi(input, Bidi.DIRECTION_LEFT_TO_RIGHT);

        boolean test = Bidi.requiresBidi(input.toCharArray(), 0, 2);

        SaslPrep.prepare(input);

        Assert.fail("Should have thrown exception");
    }
}
