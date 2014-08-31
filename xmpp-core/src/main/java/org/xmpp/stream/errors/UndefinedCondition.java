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

package org.xmpp.stream.errors;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * The implementation of the {@code <bad-format/>} stream error.
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#streams-error-conditions-undefined-condition">4.9.3.21.  undefined-condition</a></cite></p>
 * <p>The error condition is not one of those defined by the other conditions in this list; this error condition SHOULD NOT be used except in conjunction with an application-specific condition.</p>
 * </blockquote>
 */
@XmlRootElement(name = "undefined-condition")
public final class UndefinedCondition extends Condition {
}