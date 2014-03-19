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
 * The implementation of the {@code <remote-connection-failed/>} stream error.
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#streams-error-conditions-remote-connection-failed">4.9.3.15.  remote-connection-failed</a></cite></p>
 * <p>The server is unable to properly connect to a remote entity that is needed for authentication or authorization (e.g., in certain scenarios related to Server Dialback [XEP-0220]); this condition is not to be used when the cause of the error is within the administrative domain of the XMPP service provider, in which case the {@code <internal-server-error/>} condition is more appropriate.</p>
 * </blockquote>
 */
@XmlRootElement(name = "remote-connection-failed")
public final class RemoteConnectionFailed extends Condition {
}