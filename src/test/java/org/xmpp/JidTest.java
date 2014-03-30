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
        Jid jid = Jid.valueOf("node\u046A34-23?\u0498@domain/user@conference.com/nick");
        Assert.assertEquals(jid.getDomain(), "domain");
        Assert.assertEquals(jid.getLocal(), "node\u046B34-23?\u0499");
        Assert.assertEquals(jid.getResource(), "user@conference.com/nick");
    }

    @Test
    public void testJidEscaping() {
        Jid jid = Jid.valueOf("d'artagnan@musketeers.lit");
        Assert.assertEquals(jid.toString(), "d'artagnan@musketeers.lit");
        Assert.assertEquals(jid.toEscapedString(), "d\\27artagnan@musketeers.lit");

        Jid jid2 = Jid.valueOf("d\\27artagnan@musketeers.lit");
        Assert.assertEquals(jid2.toString(), "d\\27artagnan@musketeers.lit");
        Assert.assertEquals(jid2.toEscapedString(), "d\\5c27artagnan@musketeers.lit");

        Jid jid3 = Jid.valueOf("treville\\40musketeers.lit@smtp.gascon.fr");
        Assert.assertEquals(jid3.getLocal(), "treville\\40musketeers.lit");
        Assert.assertEquals(jid3.toString(), "treville\\40musketeers.lit@smtp.gascon.fr");
        Assert.assertEquals(jid3.toEscapedString(), "treville\\5c40musketeers.lit@smtp.gascon.fr");

        Jid jid4 = Jid.valueOf("\"& '/:<>@\\@domain/resource");
        Assert.assertEquals(jid4.toEscapedString(), "\\22\\26\\20\\27\\2f\\3a\\3c\\3e\\40\\@domain/resource");
        Assert.assertEquals(jid4.toString(), "\"& '/:<>@\\@domain/resource");

        Jid jid5 = Jid.valueOf("treville\\5cmusketeers.lit@smtp.gascon.fr");
        Assert.assertEquals(jid5.toEscapedString(), "treville\\5c5cmusketeers.lit@smtp.gascon.fr");
    }

    @Test
    public void testJidEscapingExceptions() {
        Jid jid = Jid.valueOf("\\2plus\\2is\\4@domain");
        Assert.assertEquals(jid.toString(), "\\2plus\\2is\\4@domain");
        Assert.assertEquals(jid.toEscapedString(), "\\2plus\\2is\\4@domain");

        Jid jid2 = Jid.valueOf("foo\\bar@domain");
        Assert.assertEquals(jid2.toString(), "foo\\bar@domain");
        Assert.assertEquals(jid2.toEscapedString(), "foo\\bar@domain");

        Jid jid3 = Jid.valueOf("foob\\41r@domain");
        Assert.assertEquals(jid3.toString(), "foob\\41r@domain");
        Assert.assertEquals(jid3.toEscapedString(), "foob\\41r@domain");

        Jid jid4 = Jid.valueOf("c:\\5commas@example.com");
        Assert.assertEquals(jid4.toString(), "c:\\5commas@example.com");
        Assert.assertEquals(jid4.toEscapedString(), "c\\3a\\5c5commas@example.com");
    }

    @Test
    public void testJidEscapingExamples() {
        Jid jid1 = Jid.valueOf("space cadet@example.com");
        Assert.assertEquals(jid1.toEscapedString(), "space\\20cadet@example.com");
        Assert.assertEquals(jid1.toString(), "space cadet@example.com");

        Jid jid2 = Jid.valueOf("call me \"ishmael\"@example.com");
        Assert.assertEquals(jid2.toEscapedString(), "call\\20me\\20\\22ishmael\\22@example.com");
        Assert.assertEquals(jid2.toString(), "call me \"ishmael\"@example.com");

        Jid jid3 = Jid.valueOf("at&t guy@example.com");
        Assert.assertEquals(jid3.toEscapedString(), "at\\26t\\20guy@example.com");
        Assert.assertEquals(jid3.toString(), "at&t guy@example.com");

        Jid jid4 = Jid.valueOf("d'artagnan@example.com");
        Assert.assertEquals(jid4.toEscapedString(), "d\\27artagnan@example.com");
        Assert.assertEquals(jid4.toString(), "d'artagnan@example.com");

        Jid jid5 = Jid.valueOf("/.fanboy@example.com");
        Assert.assertEquals(jid5.toEscapedString(), "\\2f.fanboy@example.com");
        Assert.assertEquals(jid5.toString(), "/.fanboy@example.com");

        Jid jid6 = Jid.valueOf("::foo::@example.com");
        Assert.assertEquals(jid6.toEscapedString(), "\\3a\\3afoo\\3a\\3a@example.com");
        Assert.assertEquals(jid6.toString(), "::foo::@example.com");

        Jid jid7 = Jid.valueOf("<foo>@example.com");
        Assert.assertEquals(jid7.toEscapedString(), "\\3cfoo\\3e@example.com");
        Assert.assertEquals(jid7.toString(), "<foo>@example.com");

        Jid jid8 = Jid.valueOf("user@host@example.com");
        Assert.assertEquals(jid8.toEscapedString(), "user\\40host@example.com");
        Assert.assertEquals(jid8.toString(), "user@host@example.com");

        Jid jid9 = Jid.valueOf("c:\\net@example.com");
        Assert.assertEquals(jid9.toEscapedString(), "c\\3a\\net@example.com");
        Assert.assertEquals(jid9.toString(), "c:\\net@example.com");

        Jid jid10 = Jid.valueOf("c:\\\\net@example.com");
        Assert.assertEquals(jid10.toEscapedString(), "c\\3a\\\\net@example.com");
        Assert.assertEquals(jid10.toString(), "c:\\\\net@example.com");

        Jid jid11 = Jid.valueOf("c:\\cool stuff@example.com");
        Assert.assertEquals(jid11.toEscapedString(), "c\\3a\\cool\\20stuff@example.com");
        Assert.assertEquals(jid11.toString(), "c:\\cool stuff@example.com");

        Jid jid12 = Jid.valueOf("c:\\5commas@example.com");
        Assert.assertEquals(jid12.toEscapedString(), "c\\3a\\5c5commas@example.com");
        Assert.assertEquals(jid12.toString(), "c:\\5commas@example.com");

        Jid jid13 = Jid.valueOf("\\3and\\2is\\5cool@example.com");
        Assert.assertEquals(jid13.toEscapedString(), "\\5c3and\\2is\\5c5cool@example.com");
        Assert.assertEquals(jid13.toString(), "\\3and\\2is\\5cool@example.com");
    }

    @Test
    public void testComplexUnescape() {
        Jid jid14 = Jid.valueOf("\\5c3and\\2is\\5c5cool@example.com", true);
        Assert.assertEquals(jid14.toEscapedString(), "\\5c3and\\2is\\5c5cool@example.com");
        Assert.assertEquals(jid14.toString(), "\\3and\\2is\\5cool@example.com");
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
        Jid jid = Jid.valueOf("name@mail.de@chat.facebook.com/LPfS9dVP", true);
        Assert.assertEquals(jid.getLocal(), "name@mail.de");
        Assert.assertEquals(jid.getDomain(), "chat.facebook.com");
        Assert.assertEquals(jid.getResource(), "LPfS9dVP");
        Assert.assertEquals(jid.toEscapedString(), "name\\40mail.de@chat.facebook.com/LPfS9dVP");
    }

    @Test
    public void testWithResource() {
        Jid jid = Jid.valueOf("test@domain");
        Jid withReource = jid.withResource("resource");
        Assert.assertEquals(withReource, Jid.valueOf("test@domain/resource"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testWithIllegalChars() {
        Jid jid = Jid.valueOf("test\u001Ftest@domain");
    }

    @Test
    public void testAsciiControlCharacters() {
        String[] chars = new String[]{"\u0000", "\u001F", "\u007F", "\uD83F\uDFFE"};
        int failed = 0;
        for (String aChar : chars) {
            try {
                Jid.prepare(aChar, true);
            } catch (IllegalArgumentException e) {
                failed++;
            }
        }
        Assert.assertEquals(failed, chars.length);
    }

    @Test
    public void testSurrogateCodes() {
        String[] chars = new String[]{"\uD800", "\uDFFF"};
        int failed = 0;
        for (String aChar : chars) {
            try {
                Jid.prepare(aChar, true);
            } catch (IllegalArgumentException e) {
                failed++;
            }
        }
        Assert.assertEquals(failed, chars.length);
    }

    @Test
    public void testNonAsciiControlCharacters() {
        String[] chars = new String[]{"\u0080", "\u06DD", "\u070F", "\u180E", "\u2028", "\u2029", "\u2061", "\u2062", "\u2063", "\u206A", "\u206B", "\u206C", "\u206D", "\u206E", "\u206F", "\uFFF9", "\uFFFA", "\uFFFB", "\uD834\uDD73", "\uD834\uDD7A"};
        int failed = 0;
        int i = 0;
        for (String aChar : chars) {

            try {
                Jid.prepare(aChar, true);
                System.out.println(i);
            } catch (IllegalArgumentException e) {
                failed++;
            }
            i++;
        }
        Assert.assertEquals(failed, chars.length);
    }

    @Test
    public void testPrivateUseCharacters() {
        String[] chars = new String[]{"\uE000", "\uF8FF", "\uDB80\uDC00", "\uDBC0\uDC00"};
        int failed = 0;
        for (String aChar : chars) {
            try {
                Jid.prepare(aChar, true);
            } catch (IllegalArgumentException e) {
                failed++;
            }
        }
        Assert.assertEquals(failed, chars.length);
    }

    @Test
    public void testNonCharacterCodePoints() {
        String[] chars = new String[]{"\uFDD0", "\uFDEF"};
        int failed = 0;
        for (String aChar : chars) {
            try {
                Jid.prepare(aChar, true);
            } catch (IllegalArgumentException e) {
                failed++;
            }
        }
        Assert.assertEquals(failed, chars.length);
    }

    @Test
    public void testNodePrep() {
        // Some examples from http://tools.ietf.org/html/rfc3454#appendix-B.2
        String s = "\u0149@domain";
        Assert.assertEquals(Jid.valueOf(s).getLocal(), "\u02BC\u006E");

        String s1 = "ß@domain";
        Assert.assertEquals(Jid.valueOf(s1).getLocal(), "ss");

        String s2 = "\u03B0@domain";
        Assert.assertEquals(Jid.valueOf(s2).getLocal(), "\u03C5\u0308\u0301");

        String s3 = "\u01E0@domain";
        Assert.assertEquals(Jid.valueOf(s3).getLocal(), "\u01E1");

        String s4 = "\u0226@domain";
        Assert.assertEquals(Jid.valueOf(s4).getLocal(), "\u0227");

        String s5 = "\u03D2@domain";
        Assert.assertEquals(Jid.valueOf(s5).getLocal(), "\u03C5");

        String s6 = "\u0480@domain";
        Assert.assertEquals(Jid.valueOf(s6).getLocal(), "\u0481");

        String s7 = "\u0587@domain";
        Assert.assertEquals(Jid.valueOf(s7).getLocal(), "\u0565\u0582");

        String s8 = "\u1F52@domain";
        Assert.assertEquals(Jid.valueOf(s8).getLocal(), "\u03C5\u0313\u0300");

        String s9 = "UPPERCASE@domain";
        Assert.assertEquals(Jid.valueOf(s9).getLocal(), "uppercase");
    }

    @Test
    public void shouldMapToNothing() {
        // http://tools.ietf.org/html/rfc3454#appendix-B.1
        String s = "s\u00AD\u034F\u1806\u180B\u180C\u180D\u200B\u200C\u200D\u2060\uFE00\uFE01\uFE0F\uFEFFs";
        Assert.assertEquals(Jid.prepare(s, true), "ss");
    }

    @Test
    public void shouldCaseFold() {
        // Some examples from http://tools.ietf.org/html/rfc3454#appendix-B.2
        String s = "\u0149";
        Assert.assertEquals(Jid.prepare(s, true), "\u02BC\u006E");

        String s1 = "ß";
        Assert.assertEquals(Jid.prepare(s1, true), "ss");

        String s2 = "\u03B0";
        Assert.assertEquals(Jid.prepare(s2, true), "\u03C5\u0308\u0301");

        String s3 = "\u01E0";
        Assert.assertEquals(Jid.prepare(s3, true), "\u01E1");

        String s4 = "\u0226";
        Assert.assertEquals(Jid.prepare(s4, true), "\u0227");

        String s5 = "\u03D2";
        Assert.assertEquals(Jid.prepare(s5, true), "\u03C5");

        String s6 = "\u0480";
        Assert.assertEquals(Jid.prepare(s6, true), "\u0481");

        String s7 = "\u0587";
        Assert.assertEquals(Jid.prepare(s7, true), "\u0565\u0582");

        String s8 = "\u1F52";
        Assert.assertEquals(Jid.prepare(s8, true), "\u03C5\u0313\u0300");
    }

    //@Test
    public void testPerformance() {


        long start = System.currentTimeMillis();

        for (int i = 0; i < 100000; i++) {
            Jid.prepare("testὒNode", false);
        }

        System.out.println(System.currentTimeMillis() - start);
    }
}
