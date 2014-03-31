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

import org.xmpp.Jid;

/**
 * A factory class, which allows to create implementations of the {@link Item} and {@link Destroy} interfaces.
 *
 * @author Christian Schudt
 */
public class MucElementFactory {

    private MucElementFactory() {
    }

    public static Destroy createDestroy(Jid jid, String reason) {
        return new MucDestroy(jid, reason);
    }

    public static Item createItem(Affiliation affiliation) {
        return new MucItem(affiliation);
    }

    public static Item createItem(Affiliation affiliation, Jid jid) {
        return new MucItem(affiliation, jid);
    }

    public static Item createItem(Affiliation affiliation, Jid jid, String reason) {
        return new MucItem(affiliation, jid, reason);
    }

    public static Item createItem(Affiliation affiliation, Jid jid, String reason, String nick) {
        return new MucItem(affiliation, jid, reason, nick);
    }

    public static Item createItem(Role role, String nick) {
        return new MucItem(role, nick);
    }

    public static Item createItem(Role role, String nick, String reason) {
        return new MucItem(role, nick, reason);
    }

    static final class MucDestroy implements Destroy {

        private String reason;

        private Jid jid;

        private MucDestroy() {
        }

        public MucDestroy(Jid jid, String reason) {
            this.jid = jid;
            this.reason = reason;
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

    static final class MucItem implements Item {

        private Actor actor;

        private String reason;

        private Affiliation affiliation;

        private Jid jid;

        private String nick;

        private Role role;

        private MucItem() {
        }

        MucItem(Affiliation affiliation) {
            this(affiliation, null, null);
        }

        MucItem(Affiliation affiliation, Jid jid) {
            this(affiliation, jid, null);
        }

        MucItem(Affiliation affiliation, Jid jid, String reason) {
            this(affiliation, jid, reason, null);
        }

        MucItem(Affiliation affiliation, Jid jid, String reason, String nick) {
            this.affiliation = affiliation;
            this.jid = jid;
            this.reason = reason;
            this.nick = nick;
        }

        MucItem(Role role, String nick) {
            this(role, nick, null);
        }

        MucItem(Role role, String nick, String reason) {
            this.role = role;
            this.nick = nick;
            this.reason = reason;
        }

        @Override
        public String getNick() {
            return nick;
        }

        @Override
        public Role getRole() {
            return role;
        }

        @Override
        public Jid getJid() {
            return jid;
        }

        @Override
        public Affiliation getAffiliation() {
            return affiliation;
        }

        @Override
        public String getReason() {
            return reason;
        }

        @Override
        public Actor getActor() {
            return actor;
        }
        /*
        static final class MucActor implements Actor {
            private Jid jid;

            private String nick;

            private MucActor() {
            }

            MucActor(Actor actor) {
                this.jid = actor.getJid();
                this.nick = actor.getNick();
            }

            @Override
            public String getNick() {
                return nick;
            }

            @Override
            public Jid getJid() {
                return jid;
            }
        }  */
    }
}
