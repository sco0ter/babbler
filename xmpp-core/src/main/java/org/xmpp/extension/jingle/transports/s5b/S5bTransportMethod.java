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

package org.xmpp.extension.jingle.transports.s5b;

import org.xmpp.Jid;
import org.xmpp.extension.jingle.transports.TransportMethod;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "transport")
public class S5bTransportMethod extends TransportMethod {

    @XmlElement(name = "candidate")
    private final List<Candidate> candidates = new ArrayList<>();

    @XmlAttribute(name = "dstaddr")
    private String dstAddr;

    @XmlAttribute(name = "mode")
    private Mode mode = Mode.TCP;

    @XmlAttribute(name = "sid")
    private String sessionId;

    public String getDstAddr() {
        return dstAddr;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Mode getMode() {
        return mode;
    }

    public List<Candidate> getCandidates() {
        return candidates;
    }

    public enum Mode {
        @XmlEnumValue("tcp")
        TCP,
        @XmlEnumValue("udp")
        UDP
    }

    public static final class Candidate implements Comparable<Candidate> {

        @XmlAttribute(name = "cid")
        private String cid;

        @XmlAttribute(name = "host")
        private String host;

        @XmlAttribute(name = "jid")
        private Jid jid;

        @XmlAttribute(name = "port")
        private int port;

        @XmlAttribute(name = "priority")
        private int priority;

        @XmlAttribute(name = "type")
        private Type type = Type.DIRECT;

        private Candidate() {
        }

        public Candidate(String cid, String host, Jid jid, int localPriority) {
            this.cid = cid;
            this.host = host;
            this.jid = jid;
            this.priority = calculatePriority(localPriority);
        }

        public Candidate(String cid, String host, Jid jid, int localPriority, Type type, int port) {
            this.cid = cid;
            this.host = host;
            this.jid = jid;
            this.type = type;
            this.port = port;
            this.priority = calculatePriority(localPriority);
        }

        /**
         * Calculates the priority.
         *
         * @param localPreference The local priority.
         * @return The priority.
         */
        private int calculatePriority(int localPreference) {
            return 65536 * (type == null ? Type.DIRECT.getPreferenceValue() : type.getPreferenceValue()) + localPreference;
        }

        public String getCid() {
            return cid;
        }

        public String getHost() {
            return host;
        }

        public Jid getJid() {
            return jid;
        }

        public int getPort() {
            return port;
        }

        public int getPriority() {
            return priority;
        }

        public Type getType() {
            return type;
        }

        @Override
        public int compareTo(Candidate o) {
            return Integer.compare(o.priority, priority);
        }


        public enum Type {
            /**
             * Direct connection using the given interface.
             */
            @XmlEnumValue("assisted")
            ASSISTED(120),
            /**
             * Direct connection using the given interface.
             */
            @XmlEnumValue("direct")
            DIRECT(126),
            /**
             * SOCKS5 Relay.
             */
            @XmlEnumValue("proxy")
            PROXY(10),
            /**
             * Tunnel protocols such as Teredo.
             */
            @XmlEnumValue("tunnel")
            TUNNEL(110);

            private final int preferenceValue;

            private Type(int preferenceValue) {
                this.preferenceValue = preferenceValue;
            }

            /**
             * The preference value.
             *
             * @return The preference value.
             */
            public int getPreferenceValue() {
                return preferenceValue;
            }
        }
    }

    private static final class CandidateUsed {
        @XmlAttribute(name = "cid")
        private String cid;
    }

    private static final class Activated {
        @XmlAttribute(name = "cid")
        private String cid;
    }
}
