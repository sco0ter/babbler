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
        Jid jid = Jid.valueOf("domain.com");
        Assert.assertEquals("domain.com", jid.toString());
    }

    @Test
    public void testJidFromEscapedString() {
        Jid jid = Jid.valueOf("domain");
        Assert.assertEquals("domain", jid.toString());
        Jid jid2 = Jid.valueOf("domain/resource");
        Assert.assertEquals("domain/resource", jid2.toString());
        Jid jid3 = Jid.valueOf("local@domain");
        Assert.assertEquals("local@domain", jid3.toString());
        Jid jid4 = Jid.valueOf("local@domain/resource");
        Assert.assertEquals("local@domain/resource", jid4.toString());
    }

    @Test
    public void testJidParseNode() {
        Jid jid = Jid.valueOf("node@domain");
        Assert.assertEquals("node@domain", jid.toString());
    }

    @Test
    public void testJidParseFull() {
        Jid jid = Jid.valueOf("node@domain/resource");
        Assert.assertEquals("node@domain/resource", jid.toString());
    }

    @Test
    public void testBareJid() {
        Jid jid = Jid.valueOf("node@domain/resource");
        Assert.assertEquals("node@domain", jid.asBareJid().toString());
    }

    @Test
    public void testJidParseDomainAndResource() {
        Jid jid = Jid.valueOf("domain/resource");
        Assert.assertEquals("domain/resource", jid.toString());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testJidIncomplete1() {
        Jid.valueOf("@domain");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testJidIncomplete2() {
        Jid.valueOf("domain/");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testJidNull() {
        Jid.valueOf(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testJidEmpty() {
        Jid.valueOf("");
    }

    @Test
    public void testJidTrim() {
        Jid jid = Jid.valueOf(" node@domain/resource ");
        Assert.assertEquals("node@domain/resource", jid.toString());
    }

    @Test
    public void testJidEquals() {
        Jid jid1 = Jid.valueOf("node@domain/resource");
        Jid jid2 = Jid.valueOf("node@domain/resource");

        Assert.assertEquals(jid1, jid2);

        Jid jid3 = Jid.valueOf("node@domain/test");

        Assert.assertNotEquals(jid1, jid3);

        Jid jid4 = Jid.valueOf("node@domain");

        Assert.assertNotEquals(jid1, jid4);
    }

    @Test
    public void testIsBareJid() {
        Jid jid = Jid.valueOf("node@domain");
        Assert.assertTrue(jid.isBareJid());
        Assert.assertFalse(jid.isFullJid());
    }

    @Test
    public void testIsFullJid() {
        Jid jid = Jid.valueOf("node@domain/resource");
        Assert.assertTrue(jid.isFullJid());
        Assert.assertFalse(jid.isBareJid());
    }

    @Test
    public void testGetter() {
        Jid jid = Jid.valueOf("node@domain/resource");
        Assert.assertEquals(jid.getDomain(), "domain");
        Assert.assertEquals(jid.getLocal(), "node");
        Assert.assertEquals(jid.getResource(), "resource");
    }

    @Test
    public void testWithJidAsResource() {
        Jid jid = Jid.valueOf("node@domain/user@conference.com/nick");
        Assert.assertEquals(jid.getDomain(), "domain");
        Assert.assertEquals(jid.getLocal(), "node");
        Assert.assertEquals(jid.getResource(), "user@conference.com/nick");
    }

    @Test
    public void testJidEscaping() {
        Jid jid = Jid.valueOf("d'artagnan@musketeers.lit");
        Assert.assertEquals(jid.toString(), "d'artagnan@musketeers.lit");
        Assert.assertEquals(jid.toEscapedString(), "d\\27artagnan@musketeers.lit");

        Jid jid2 = Jid.valueOf("d\\27artagnan@musketeers.lit");
        Assert.assertEquals(jid2.toString(), "d'artagnan@musketeers.lit");
        Assert.assertEquals(jid2.toEscapedString(), "d\\27artagnan@musketeers.lit");
        Assert.assertEquals(jid, jid2);

        Jid jid3 = Jid.valueOf("treville\\40musketeers.lit@smtp.gascon.fr");
        Assert.assertEquals(jid3.getLocal(), "treville@musketeers.lit");
        Assert.assertEquals(jid3.toString(), "treville@musketeers.lit@smtp.gascon.fr");
        Assert.assertEquals(jid3.toEscapedString(), "treville\\40musketeers.lit@smtp.gascon.fr");

        Jid jid4 = Jid.valueOf("\"&'/:<>@\\@domain/resource");
        Assert.assertEquals(jid4.toEscapedString(), "\\22\\26\\27\\2f\\3a\\3c\\3e\\40\\5c@domain/resource");
        Assert.assertEquals(jid4.toString(), "\"&'/:<>@\\@domain/resource");
    }

    @Test
    public void testComplexJid() {
        Jid jid = Jid.valueOf("d'art@a/gnan@musketeers.lit/another/@Jid@test.de");

        Assert.assertEquals(jid.getLocal(), "d'art");
        Assert.assertEquals(jid.getDomain(), "a");
        Assert.assertEquals(jid.getResource(), "gnan@musketeers.lit/another/@Jid@test.de");
    }

    /**
     * The domainpart for every XMPP service MUST be a fully qualified domain name (FQDN; see [DNS]), IPv4 address, IPv6 address, or unqualified hostname (i.e., a text label that is resolvable on a local network).
     */
    @Test
    public void testHostname() {
        Jid jid = Jid.valueOf("typical-hostname33.whatever.co.uk");
        Assert.assertEquals(jid.getDomain(), "typical-hostname33.whatever.co.uk");

        Jid jid2 = Jid.valueOf("conference.server123");
        Assert.assertEquals(jid2.getDomain(), "conference.server123");
    }

    @Test
    public void testIpAddress() {
        Jid jid = Jid.valueOf("127.0.0.1");
        Assert.assertEquals(jid.getDomain(), "127.0.0.1");
    }

    @Test
    public void testDoubleAt() {
        // Facebook uses something like this:
        Jid jid = Jid.valueOf("name@mail.de@chat.facebook.com/LPfS9dVP");
        Assert.assertEquals(jid.getLocal(), "name@mail.de");
        Assert.assertEquals(jid.getDomain(), "chat.facebook.com");
        Assert.assertEquals(jid.getResource(), "LPfS9dVP");
    }

    @Test
    public void testWithResource() {
        Jid jid = Jid.valueOf("test@domain");
        Jid withReource = jid.withResource("resource");
        Assert.assertEquals(withReource, Jid.valueOf("test@domain/resource"));
    }
}
