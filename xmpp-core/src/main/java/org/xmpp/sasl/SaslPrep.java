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

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * @author Christian Schudt
 * @see <a href="http://tools.ietf.org/search/rfc4013">SASLprep: Stringprep Profile for User Names and Passwords</a>
 */
public final class SaslPrep {

    /**
     * C.1.2 Non-ASCII space characters
     */
    private static final Pattern MAP_TO_SPACE = Pattern.compile("([\u00A0\u1680\u2000-\u200B\u202F\u205F\u3000])");

    /**
     * B.1 Commonly mapped to nothing
     */
    private static final Pattern MAP_TO_NOTHING = Pattern.compile("([\u00AD\u034F\u1806\u180B\u180C\u180D\u200B-\u200D\u2060\uFE00-\uFE0F\uFEFF])");

    /**
     * Every character, which is not a letter, number, punctuation, symbol character or marker character.
     */
    private static final Pattern PROHIBITED_CHARACTERS = Pattern.compile("[^\\p{L}\\p{N}\\p{P}\\p{S}\\p{M}]");


    private SaslPrep() {
    }

    /**
     * 2.1. Mapping
     * This profile specifies:
     * -  non-ASCII space characters [StringPrep, C.1.2] that can be
     * mapped to SPACE (U+0020), and
     * -  the "commonly mapped to nothing" characters [StringPrep, B.1]
     * that can be mapped to nothing.
     *
     * @return The mapped string.
     */
    public static String prepare(String input) {
        String mapped = MAP_TO_NOTHING.matcher(MAP_TO_SPACE.matcher(input).replaceAll(" ")).replaceAll("");
        String normalized = Normalizer.normalize(mapped, Normalizer.Form.NFKC);

        if (PROHIBITED_CHARACTERS.matcher(input).find()) {
            throw new IllegalArgumentException();
        }

        return normalized;
    }
}
