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

package rocks.xmpp.extensions.commands;

import rocks.xmpp.core.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.model.client.IQ;
import rocks.xmpp.extensions.commands.model.Command;
import rocks.xmpp.extensions.data.model.DataForm;

/**
 * @author Christian Schudt
 */
public class CommandSession {

    private final XmppSession xmppSession;

    private final String sessionId;

    private final String node;

    private final Jid to;

    private volatile Command currentCommand;

    public CommandSession(Jid to, XmppSession xmppSession, Command command) {
        this.to = to;
        this.xmppSession = xmppSession;
        this.node = command.getNode();
        this.sessionId = command.getSessionId();
    }

    public void execute() throws XmppException {
        executeAction(Command.Action.EXECUTE, null);
    }

    public void execute(DataForm dataForm) throws XmppException {
        executeAction(Command.Action.EXECUTE, dataForm);
    }

    /**
     * Cancels the command.
     *
     * @throws XmppException
     */
    public void cancel() throws XmppException {
        executeAction(Command.Action.CANCEL, null);
    }

    /**
     * Completes the command.
     */
    public void complete(DataForm dataForm) throws XmppException {
        executeAction(Command.Action.COMPLETE, dataForm);
    }

    /**
     * Executes the previous command.
     *
     * @throws XmppException
     */
    public void previous() throws XmppException {
        executeAction(Command.Action.PREV, null);
    }

    /**
     * Executes the next command.
     *
     * @throws XmppException
     */
    public void next(DataForm dataForm) throws XmppException {
        executeAction(Command.Action.NEXT, dataForm);
    }


    private void executeAction(Command.Action action, DataForm dataForm) throws XmppException {
        IQ result = xmppSession.query(new IQ(to, IQ.Type.SET, new Command(node, action, sessionId, dataForm)));
        currentCommand = result.getExtension(Command.class);
    }
}
