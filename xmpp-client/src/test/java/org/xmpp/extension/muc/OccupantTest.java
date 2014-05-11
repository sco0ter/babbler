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

package org.xmpp.extension.muc;

import org.testng.annotations.Test;
import org.xmpp.Jid;
import org.xmpp.extension.muc.user.MucUser;
import org.xmpp.stanza.client.Presence;

/**
 * @author Christian Schudt
 */
public class OccupantTest {

    @Test
    public void testComparable() {
        Presence presence1 = new Presence();
        presence1.setFrom(Jid.valueOf("room@conference/firstwitch"));
        presence1.getExtensions().add(MucUser.withItem(Affiliation.OWNER, Role.MODERATOR));

        Presence presence2 = new Presence();
        presence2.setFrom(Jid.valueOf("room@conference/secondwitch"));
        presence2.getExtensions().add(MucUser.withItem(Affiliation.OWNER, Role.MODERATOR));

        Presence presence3 = new Presence();
        presence3.setFrom(Jid.valueOf("room@conference/aaa"));
        presence3.getExtensions().add(MucUser.withItem(Affiliation.ADMIN, Role.MODERATOR));

        Presence presence4 = new Presence();
        presence4.setFrom(Jid.valueOf("room@conference/bbb"));
        presence4.getExtensions().add(MucUser.withItem(Affiliation.MEMBER, Role.PARTICIPANT));

        Presence presence5 = new Presence();
        presence5.setFrom(Jid.valueOf("room@conference/bbb"));
        presence5.getExtensions().add(MucUser.withItem(Affiliation.OUTCAST, Role.PARTICIPANT));

        Presence presence6 = new Presence();
        presence6.setFrom(Jid.valueOf("room@conference/bbb"));
        presence6.getExtensions().add(MucUser.withItem(Affiliation.NONE, Role.PARTICIPANT));

        // TODO
    }
}
