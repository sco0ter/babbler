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

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * The implementation of the {@code <command/>} element in the {@code http://jabber.org/protocol/commands} namespace.
 *
 * @author Christian Schudt
 */
@XmlRootElement(name = "command")
@XmlSeeAlso({Command.MalformedAction.class, Command.BadAction.class, Command.BadLocale.class, Command.BadPayload.class, Command.BadSessionId.class, Command.SessionExpired.class})
public final class Command {

    /**
     * http://jabber.org/protocol/commands
     */
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

    @XmlElement(name = "note")
    private List<Note> notes = new ArrayList<>();

    @XmlAnyElement(lax = true)
    private Object payload;

    private Command() {
    }

    /**
     * Creates a command for simple execution.
     *
     * @param node   The node.
     * @param action The action. If null, {@link Action#EXECUTE} is implied.
     */
    public Command(String node, Action action) {
        this.node = node;
        this.action = action;
    }

    /**
     * Creates a command.
     *
     * @param node      The node.
     * @param action    The action. If null, {@link Action#EXECUTE} is implied.
     * @param sessionId The session id.
     * @param payload   The payload.
     */
    public Command(String node, Action action, String sessionId, Object payload) {
        this.node = node;
        this.action = action;
        this.sessionId = sessionId;
        this.payload = payload;
    }

    /**
     * Gets the command payload.
     *
     * @return The command payload.
     * @see <a href="http://xmpp.org/extensions/xep-0050.html#impl-payloads">3.5 Command Payloads</a>
     */
    public Object getPayload() {
        return payload;
    }

    /**
     * Gets the action, which is used by the requester.
     *
     * @return The action.
     */
    public Action getAction() {
        return action;
    }

    /**
     * Gets the node.
     *
     * @return The node.
     * @see <a href="http://xmpp.org/extensions/xep-0050.html#impl-nodes">3.2 Command Nodes</a>
     */
    public String getNode() {
        return node;
    }

    /**
     * Gets the status of the command.
     *
     * @return The status.
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Gets the session id.
     *
     * @return The session id.
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Gets the possible command actions, which are provided by the responder.
     *
     * @return The command actions.
     * @see <a href="http://xmpp.org/extensions/xep-0050.html#impl-actions">3.4 Command Actions</a>
     */
    public Collection<Action> getActions() {
        if (status == Status.COMPLETED) {
            return Collections.emptyList();
        }
        List<Action> possibleActions = new ArrayList<>();
        // 1. The action "cancel" is always allowed.
        possibleActions.add(Action.CANCEL);

        // 2. If there is no <actions/> element, the user-agent can use a single-stage dialog or view.
        // The action "execute" is equivalent to the action "complete".
        // 3. If there is an <actions/> element, the user-agent usually uses a multi-stage dialog or view, such as a wizard.
        // The action "execute" is always allowed, and is equivalent to the action "next".
        possibleActions.add(Action.EXECUTE);

        if (actions == null) {
            possibleActions.add(Action.COMPLETE);
        } else {
            // The "prev" action is typically the "back" or "previous" button or option in a wizard. If <prev/> is not contained by the <actions/>, it is disabled.
            if (actions.prev != null) {
                possibleActions.add(Action.PREV);
            }
            // The "next" action is typically the "next" button or option in a wizard. If <next/> is not contained by the <actions/>, it is disabled.
            if (actions.next != null) {
                possibleActions.add(Action.NEXT);
            }
            // The "complete" action is typically the "finish" or "done" button or option in a wizard. If <complete/> is not contained by the <actions/>, it is disabled.
            if (actions.complete != null) {
                possibleActions.add(Action.COMPLETE);
            }
        }
        return Collections.unmodifiableList(possibleActions);
    }

    /**
     * Gets the default action or null, if there is no default action.
     *
     * @return The default action.
     */
    public Action getDefaultAction() {
        return actions != null ? actions.defaultAction : null;
    }

    /**
     * Represents a command action.
     */
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

    /**
     * Represents the status of the command.
     */
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

    /**
     * The actions element.
     */
    private static final class Actions {

        @XmlElement(name = "prev")
        private String prev;

        @XmlElement(name = "next")
        private String next;

        @XmlElement(name = "complete")
        private String complete;

        @XmlAttribute(name = "execute")
        private Action defaultAction;
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

    /**
     * Represents a note associated with a command.
     *
     * @see <a href="http://xmpp.org/extensions/xep-0050.html#desc-note">4.3 &lt;note/&gt; Element</a>
     */
    public static final class Note {

        @XmlValue
        private String value;

        @XmlAttribute(name = "type")
        private Type type;

        private Note() {
        }

        /**
         * Creates a note.
         *
         * @param type  The note type.
         * @param value The actual note value.
         */
        public Note(Type type, String value) {
            this.type = type;
            this.value = value;
        }

        /**
         * Gets the note type.
         *
         * @return The type.
         */
        public Type getType() {
            return type;
        }

        /**
         * Gets the note value.
         *
         * @return The note value.
         */
        public String getValue() {
            return value;
        }

        /**
         * Represents the note type.
         */
        public enum Type {
            /**
             * The note indicates an error. The text should indicate the reason for the error.
             */
            @XmlEnumValue(value = "error")
            ERROR,
            /**
             * The note is informational only. This is not really an exceptional condition.
             */
            @XmlEnumValue(value = "info")
            INFO,
            /**
             * The note indicates a warning. Possibly due to illogical (yet valid) data.
             */
            @XmlEnumValue(value = "warn")
            WARN
        }
    }
}


