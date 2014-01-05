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

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christian Schudt
 */
public class JidTest {

    @Test
    public void testJidDomainOnly() {
        Jid jid = new Jid("domain");
        Assert.assertEquals("domain", jid.toString());
    }

    @Test
    public void testJidNodeAndDomain() {
        Jid jid = new Jid("node", "domain");
        Assert.assertEquals("node@domain", jid.toString());
    }

    @Test
    public void testJidFull() {
        Jid jid = new Jid("node", "domain", "resource");
        Assert.assertEquals("node@domain/resource", jid.toString());
    }

    @Test
    public void testJidParseDomain() {
        Jid jid = Jid.fromString("domain");
        Assert.assertEquals("domain", jid.toString());
    }

    @Test
    public void testJidParseNode() {
        Jid jid = Jid.fromString("node@domain");
        Assert.assertEquals("node@domain", jid.toString());
    }

    @Test
    public void testJidParseFull() {
        Jid jid = Jid.fromString("node@domain/resource");
        Assert.assertEquals("node@domain/resource", jid.toString());
    }

    @Test
    public void testBareJid() {
        Jid jid = Jid.fromString("node@domain/resource");
        Assert.assertEquals("node@domain", jid.toBareJid().toString());
    }

    @Test
    public void testJidParseDomainAndResource() {
        Jid jid = Jid.fromString("domain/resource");
        Assert.assertEquals("domain/resource", jid.toString());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testJidIncomplete1() {
        Jid.fromString("@domain");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testJidIncomplete2() {
        Jid.fromString("domain/");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testJidLongerThan1023() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1024; i++) {
            sb.append("d");
        }
        Jid.fromString(sb.toString());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testJidNull() {
        Jid.fromString(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testJidEmpty() {
        Jid.fromString("");
    }

    @Test
    public void testJidTrim() {
        Jid jid = Jid.fromString(" node@domain/resource ");
        Assert.assertEquals("node@domain/resource", jid.toString());
    }

    @Test
    public void testJidEquals() {
        Jid jid1 = Jid.fromString("node@domain/resource");
        Jid jid2 = Jid.fromString("node@domain/resource");

        Assert.assertEquals(jid1, jid2);

        Jid jid3 = Jid.fromString("node@domain/test");

        Assert.assertNotEquals(jid1, jid3);

        Jid jid4 = Jid.fromString("node@domain");

        Assert.assertNotEquals(jid1, jid4);
    }

    @Test
    public void testIsBareJid() {
        Jid jid = Jid.fromString("node@domain");
        Assert.assertTrue(jid.isBareJid());
        Assert.assertFalse(jid.isFullJid());
    }

    @Test
    public void testIsFullJid() {
        Jid jid = Jid.fromString("node@domain/resource");
        Assert.assertTrue(jid.isFullJid());
        Assert.assertFalse(jid.isBareJid());
    }

    @Test
    public void testGetter() {
        Jid jid = Jid.fromString("node@domain/resource");
        Assert.assertEquals(jid.getDomain(), "domain");
        Assert.assertEquals(jid.getLocal(), "node");
        Assert.assertEquals(jid.getResource(), "resource");
    }

    @Test
    public void testJidEscaping() {
        Jid jid = Jid.fromString("d'artagnan@musketeers.lit");
        Assert.assertEquals(jid.toString(), "d'artagnan@musketeers.lit");
        Assert.assertEquals(jid.toEscapedString(), "d\\27artagnan@musketeers.lit");

        Jid jid2 = Jid.fromString("d\\27artagnan@musketeers.lit");
        Assert.assertEquals(jid, jid2);

        Jid jid3 = Jid.fromString("treville\\40musketeers.lit@smtp.gascon.fr");
        Assert.assertEquals(jid3.getLocal(), "treville@musketeers.lit");
        Assert.assertEquals(jid3.toString(), "treville@musketeers.lit@smtp.gascon.fr");
        Assert.assertEquals(jid3.toEscapedString(), "treville\\40musketeers.lit@smtp.gascon.fr");

        Jid jid4 = Jid.fromString("\"&'/:<>@\\@domain/resource");
        Assert.assertEquals(jid4.toEscapedString(), "\\22\\26\\27\\2f\\3a\\3c\\3e\\40\\5c@domain/resource");
        Assert.assertEquals(jid4.toString(), "\"&'/:<>@\\@domain/resource");
    }
}
