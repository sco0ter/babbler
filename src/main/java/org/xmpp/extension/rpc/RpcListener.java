package org.xmpp.extension.rpc;

import java.util.EventListener;

/**
 * @author Christian Schudt
 */
public interface RpcListener extends EventListener {

    void processRpc(RpcEvent e);

}
