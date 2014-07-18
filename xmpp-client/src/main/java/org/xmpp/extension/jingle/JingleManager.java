package org.xmpp.extension.jingle;

import org.xmpp.XmppSession;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.stanza.IQEvent;
import org.xmpp.stanza.IQListener;
import org.xmpp.stanza.client.IQ;

/**
 * @author Christian Schudt
 */
public final class JingleManager extends ExtensionManager {
    private JingleManager(final XmppSession xmppSession) {
        super(xmppSession);

        xmppSession.addIQListener(new IQListener() {
            @Override
            public void handle(IQEvent e) {
                IQ iq = e.getIQ();
                if (e.isIncoming() && isEnabled() && !e.isConsumed() && iq.getType() == IQ.Type.SET) {
                    Jingle jingle = iq.getExtension(Jingle.class);
                    if (jingle != null) {
                        if (jingle.getAction() == Jingle.Action.SESSION_INITIATE) {
                            for (Jingle.Content content : jingle.getContents()) {

                            }
                            xmppSession.send(iq.createResult());
                        }
                    }
                }
            }
        });
    }
}
