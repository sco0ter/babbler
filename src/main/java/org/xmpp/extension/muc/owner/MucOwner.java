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

package org.xmpp.extension.muc.owner;

import org.xmpp.Jid;
import org.xmpp.extension.data.DataForm;
import org.xmpp.extension.muc.Destroy;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The implementation of the {@code <query/>} element in the muc#owner namespace.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0045.html#schemas-owner">18.4 http://jabber.org/protocol/muc#owner</a>
 */
@XmlRootElement(name = "query")
public final class MucOwner {

    @XmlElementRef
    private DataForm dataForm;

    @XmlElement(name = "destroy")
    private MucOwnerDestroy destroy;

    /**
     * Creates an empty query element.
     */
    public MucOwner() {
    }

    /**
     * Creates a query element with a configuration form.
     *
     * @param dataForm The configuration form.
     */
    public MucOwner(DataForm dataForm) {
        this.dataForm = dataForm;
    }

    /**
     * Creates a query element with a {@code <destroy/>} element.
     *
     * @param destroy The destroy element.
     */
    public MucOwner(Destroy destroy) {
        this.destroy = new MucOwnerDestroy(destroy);
    }

    /**
     * Gets the configuration form.
     *
     * @return The configuration form.
     */
    public DataForm getConfigurationForm() {
        return dataForm;
    }

    static final class MucOwnerDestroy implements Destroy {
        @XmlElement(name = "password")
        private String password;

        @XmlElement(name = "reason")
        private String reason;

        @XmlAttribute(name = "jid")
        private Jid jid;

        private MucOwnerDestroy() {
        }

        private MucOwnerDestroy(Destroy destroy) {
            this.jid = destroy.getJid();
            this.reason = destroy.getReason();
        }

        @Override
        public Jid getJid() {
            return jid;
        }

        @Override
        public String getReason() {
            return reason;
        }
    }
}
