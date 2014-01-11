package org.xmpp.extension.ibb;

import java.util.EventObject;

/**
 * @author Christian Schudt
 */
public final class IbbEvent extends EventObject {

    public IbbEvent(Object source) {
        super(source);
    }

    public void reject() {

    }
}
