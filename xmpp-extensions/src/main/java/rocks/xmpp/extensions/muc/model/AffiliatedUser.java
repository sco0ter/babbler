/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Christian Schudt
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

package rocks.xmpp.extensions.muc.model;

import rocks.xmpp.core.Jid;
import rocks.xmpp.extensions.muc.model.admin.MucAdmin;

/**
 * @author Christian Schudt
 */
public final class AffiliatedUser {

    private final Affiliation affiliation;

    private final Role role;

    private final Jid jid;

    private final String nick;

    AffiliatedUser(MucAdmin mucAdmin) {

        if (!mucAdmin.getItems().isEmpty()) {
            Item item = mucAdmin.getItems().get(0);
            this.nick = item.getNick();
            this.affiliation = item.getAffiliation();
            this.role = item.getRole();
            this.jid = item.getJid();
        } else {
            this.nick = null;
            this.affiliation = null;
            this.role = null;
            this.jid = null;
        }
    }
}
