package org.xmpp.extension.commands;

import org.xmpp.Jid;
import org.xmpp.XmppException;
import org.xmpp.XmppSession;
import org.xmpp.extension.data.DataForm;
import org.xmpp.stanza.client.IQ;

/**
 * @author Christian Schudt
 */
public class CommandSession {

    private final XmppSession xmppSession;

    private final String sessionId;

    private final String node;

    private volatile Command currentCommand;

    private final Jid to;

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
