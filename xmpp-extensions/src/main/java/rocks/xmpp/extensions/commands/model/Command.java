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

package rocks.xmpp.extensions.commands.model;

import rocks.xmpp.extensions.data.model.DataForm;

import javax.xml.bind.annotation.*;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "command")
@XmlSeeAlso({Command.MalformedAction.class, Command.BadAction.class, Command.BadLocale.class, Command.BadPayload.class, Command.BadSessionId.class, Command.SessionExpired.class})
public final class Command {

    public static final String NAMESPACE = "http://jabber.org/protocol/commands";

    @XmlAttribute(name = "action")
    private Action action;

    @XmlAttribute(name = "node")
    private String node;

    @XmlAttribute(name = "sessionid")
    private String sessionId;

    @XmlAttribute(name = "status")
    private Status status;

    @XmlElement(name = "actions")
    private Actions actions;

    @XmlElementRef
    private DataForm dataForm;

    public Command() {
    }

    public Command(String node, Action action) {
        this.node = node;
        this.action = action;
    }

    public Command(String node, Action action, String sessionId, DataForm dataForm) {
        this.node = node;
        this.action = action;
        this.sessionId = sessionId;
        this.dataForm = dataForm;
    }

    public DataForm getDataForm() {
        return dataForm;
    }

    public Action getAction() {
        return action;
    }

    public String getNode() {
        return node;
    }

    public Status getStatus() {
        return status;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Actions getActions() {
        return actions;
    }

    public enum Action {
        /**
         * The command should be canceled.
         */
        @XmlEnumValue("cancel")
        CANCEL,
        /**
         * The command should be completed (if possible).
         */
        @XmlEnumValue("complete")
        COMPLETE,
        /**
         * The command should be executed or continue to be executed. This is the default value.
         */
        @XmlEnumValue("execute")
        EXECUTE,
        /**
         * The command should progress to the next stage of execution.
         */
        @XmlEnumValue("next")
        NEXT,
        /**
         * The command should be digress to the previous stage of execution.
         */
        @XmlEnumValue("prev")
        PREV
    }

    public enum Status {

        /**
         * The command has been canceled. The command session has ended.
         */
        @XmlEnumValue("canceled")
        CANCELED,
        /**
         * The command has completed. The command session has ended.
         */
        @XmlEnumValue("completed")
        COMPLETED,
        /**
         * The command is being executed.
         */
        @XmlEnumValue("executing")
        EXECUTING
    }

    public static final class Actions {

        @XmlElement(name = "prev")
        private String prev;

        @XmlElement(name = "next")
        private String next;

        @XmlElement(name = "complete")
        private String complete;

        @XmlAttribute(name = "execute")
        private Action action;

        /**
         * Gets the default action.
         *
         * @return The default action.
         */
        public Action getAction() {
            return action;
        }
    }

    /**
     * The responding JID does not understand the specified action.
     */
    @XmlRootElement(name = "malformed-action")
    public static final class MalformedAction {
    }

    /**
     * The responding JID cannot accept the specified action.
     */
    @XmlRootElement(name = "bad-action")
    public static final class BadAction {
    }

    /**
     * The responding JID cannot accept the specified language/locale.
     */
    @XmlRootElement(name = "bad-locale")
    public static final class BadLocale {
    }

    /**
     * The responding JID cannot accept the specified payload (e.g. the data form did not provide one or more required fields).
     */
    @XmlRootElement(name = "bad-payload")
    public static final class BadPayload {
    }

    /**
     * The responding JID cannot accept the specified sessionid.
     */
    @XmlRootElement(name = "bad-sessionid")
    public static final class BadSessionId {
    }

    /**
     * The requesting JID specified a sessionid that is no longer active (either because it was completed, canceled, or timed out).
     */
    @XmlRootElement(name = "session-expired")
    public static final class SessionExpired {
    }
}


