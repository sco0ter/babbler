package org.xmpp.extension.ibb;

import java.util.EventListener;

/**
 * @author Christian Schudt
 */
public interface IbbListener extends EventListener {

    void streamRequested(IbbEvent e);
}
