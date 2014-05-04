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

package org.xmpp.extension.jingle;

import org.xmpp.Jid;
import org.xmpp.extension.jingle.transports.Transport;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "jingle")
public final class Jingle {

    @XmlAttribute(name = "action")
    private Action action;

    @XmlAttribute(name = "initiator")
    private Jid initiator;

    @XmlAttribute(name = "responder")
    private Jid responder;

    @XmlAttribute(name = "sid")
    private String sessionId;

    @XmlElement(name = "reason")
    private Reason reason;

    @XmlElement(name = "content")
    private List<Content> contents = new ArrayList<>();

    public Action getAction() {
        return action;
    }

    public Jid getInitiator() {
        return initiator;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Jid getResponder() {
        return responder;
    }

    public Reason getReason() {
        return reason;
    }

    public List<Content> getContents() {
        return contents;
    }

    public enum Action {
        /**
         * Accept a content-add action received from another party.
         */
        @XmlEnumValue("content-accept")
        CONTENT_ACCEPT,
        /**
         * Add one or more new content definitions to the session.
         */
        @XmlEnumValue("content-add")
        CONTENT_ADD,
        /**
         * Change the directionality of media sending.
         */
        @XmlEnumValue("content-modify")
        CONTENT_MODIFY,
        /**
         * Reject a content-add action received from another party.
         */
        @XmlEnumValue("content-reject")
        CONTENT_REJECT,
        /**
         * Remove one or more content definitions from the session.
         */
        @XmlEnumValue("content-remove")
        CONTENT_REMOVE,
        /**
         * Exchange information about parameters for an application type.
         */
        @XmlEnumValue("description-info")
        DESCRIPTION_INFO,
        /**
         * Send information related to establishment or maintenance of security preconditions.
         */
        @XmlEnumValue("security-info")
        SECURITY_INFO,
        /**
         * Definitively accept a session negotiation.
         */
        @XmlEnumValue("session-accept")
        SESSION_ACCEPT,
        /**
         * Send session-level information, such as a ping or a ringing message.
         */
        @XmlEnumValue("session-info")
        SESSION_INFO,
        /**
         * Request negotiation of a new Jingle session.
         */
        @XmlEnumValue("session-initiate")
        SESSION_INITIATE,
        /**
         * End an existing session.
         */
        @XmlEnumValue("session-terminate")
        SESSION_TERMINATE,
        /**
         * Accept a transport-replace action received from another party.
         */
        @XmlEnumValue("session-terminate")
        TRANSPORT_ACCEPT,
        /**
         * Exchange transport candidates.
         */
        @XmlEnumValue("transport-info")
        TRANSPORT_INFO,
        /**
         * Reject a transport-replace action received from another party.
         */
        @XmlEnumValue("transport-reject")
        TRANSPORT_REJECT,
        /**
         * Redefine a transport method or replace it with a different method.
         */
        @XmlEnumValue("transport-replace")
        TRANSPORT_REPLACE
    }

    public static final class Content {

        @XmlAttribute(name = "creator")
        private Creator creator;

        @XmlAttribute(name = "disposition")
        private String disposition;

        @XmlAttribute(name = "name")
        private String name;

        @XmlElementRef
        private List<Transport> transports = new ArrayList<>();

        public Creator getCreator() {
            return creator;
        }

        public String getName() {
            return name;
        }

        public List<Transport> getTransports() {
            return transports;
        }

        public enum Creator {
            @XmlEnumValue("initiator")
            INITIATOR,
            @XmlEnumValue("responder")
            RESPONDER
        }

        public enum Senders {
            BOTH,
            INITIATOR,
            NONE,
            RESPONDER
        }
    }

    public static final class Reason {

        @XmlElements({
                @XmlElement(name = "alternative-session", type = AlternativeSession.class),
                @XmlElement(name = "busy", type = Busy.class),
                @XmlElement(name = "cancel", type = Cancel.class),
                @XmlElement(name = "connectivity-error", type = ConnectivityError.class),
                @XmlElement(name = "decline", type = Decline.class),
                @XmlElement(name = "expired", type = Expired.class),
                @XmlElement(name = "failed-application", type = FailedApplication.class),
                @XmlElement(name = "failed-transport", type = FailedTransport.class),
                @XmlElement(name = "general-error", type = GeneralError.class),
                @XmlElement(name = "gone", type = Gone.class),
                @XmlElement(name = "incompatible-parameters", type = IncompatibleParameters.class),
                @XmlElement(name = "media-error", type = MediaError.class),
                @XmlElement(name = "security-error", type = SecurityError.class),
                @XmlElement(name = "success", type = Success.class),
                @XmlElement(name = "timeout", type = Timeout.class),
                @XmlElement(name = "unsupported-applications", type = UnsupportedApplications.class),
                @XmlElement(name = "unsupported-transports", type = UnsupportedTransports.class)
        })
        private ReasonType reasonType;

        @XmlElement(name = "text")
        private String text;

        public ReasonType getType() {
            return reasonType;
        }

        public String getText() {
            return text;
        }

        private static abstract class ReasonType {

        }

        public static final class AlternativeSession extends ReasonType {

        }

        public static final class Busy extends ReasonType {

        }

        public static final class Cancel extends ReasonType {

        }

        public static final class ConnectivityError extends ReasonType {

        }

        public static final class Decline extends ReasonType {

        }

        public static final class Expired extends ReasonType {

        }

        public static final class FailedApplication extends ReasonType {

        }

        public static final class FailedTransport extends ReasonType {

        }

        public static final class GeneralError extends ReasonType {

        }

        public static final class Gone extends ReasonType {

        }

        public static final class IncompatibleParameters extends ReasonType {

        }

        public static final class MediaError extends ReasonType {

        }

        public static final class SecurityError extends ReasonType {

        }

        public static final class Success extends ReasonType {

        }

        public static final class Timeout extends ReasonType {

        }

        public static final class UnsupportedApplications extends ReasonType {

        }

        public static final class UnsupportedTransports extends ReasonType {

        }
    }
}
