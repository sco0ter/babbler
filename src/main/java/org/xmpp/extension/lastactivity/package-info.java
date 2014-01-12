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

/**
 * Contains classes for <a href="http://xmpp.org/extensions/xep-0012.html">XEP-0012: Last Activity</a> and <a href="http://xmpp.org/extensions/xep-0256.html">XEP-0256: Last Activity in Presence</a>.
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/extensions/xep-0012.html#intro">1. Introduction</a></cite></p>
 * <p>It is often helpful to know the time of the last activity associated with a entity. The canonical usage is to discover when a disconnected user last accessed its server. The 'jabber:iq:last' namespace provides a method for retrieving that information. The 'jabber:iq:last' namespace can also be used to discover or publicize when a connected user was last active on the server (i.e., the user's idle time) or to query servers and components about their current uptime.</p>
 * </blockquote>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSchema(namespace = "jabber:iq:last", elementFormDefault = XmlNsForm.QUALIFIED) package org.xmpp.extension.lastactivity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;