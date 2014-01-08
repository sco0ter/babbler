package org.xmpp.extension.attention;

import org.xmpp.Connection;
import org.xmpp.Jid;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.servicediscovery.Feature;
import org.xmpp.stanza.Message;

/**
 * This manager allows to capture another user's attention.
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/extensions/xep-0224.html">XEP-0224: Attention</a></cite></p>
 * <p>This feature is known as 'nudge' or 'buzz' in some non-XMPP IM protocols.</p>
 * </blockquote>
 * <p>If you want to listen for incoming attention requests, listen for incoming messages and check if they have the {@link Attention} extension.
 * </p>
 * <h3>Sample</h3>
 * <pre>
 * <code>
 * connection.addMessageListener(new MessageListener() {
 *     {@literal @}Override
 *     public void handle(MessageEvent e) {
 *         if (e.isIncoming() && e.getMessage().getExtension(Attention.class) != null) {
 *             // Handle attention request.
 *         }
 *     }
 * });
 * </code>
 * </pre>
 * <p>If you use attentions, enable this manager class, in order to register this extension in service discovery:</p>
 * <pre>
 * <code>
 * connection.getExtensionManager(AttentionManager.class).setEnabled(true);
 * </code>
 * </pre>
 *
 * @author Christian Schudt
 */
public final class AttentionManager extends ExtensionManager {

    private static final Feature FEATURE = new Feature("urn:xmpp:attention:0");

    public AttentionManager(Connection connection) {
        super(connection);
    }

    /**
     * Captures the attention of another user.
     *
     * @param jid The user
     */
    public void captureAttention(Jid jid) {
        Message message = new Message(jid, Message.Type.HEADLINE);
        message.getExtensions().add(new Attention());
        connection.send(message);
    }

    @Override
    protected Feature getFeature() {
        return FEATURE;
    }
}
