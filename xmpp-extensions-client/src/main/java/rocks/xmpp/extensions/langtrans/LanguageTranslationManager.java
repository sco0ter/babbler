package rocks.xmpp.extensions.langtrans;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.Manager;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.info.InfoNode;
import rocks.xmpp.extensions.disco.model.items.Item;
import rocks.xmpp.extensions.disco.model.items.ItemNode;
import rocks.xmpp.extensions.langtrans.model.LanguageTranslation;
import rocks.xmpp.extensions.langtrans.model.items.LanguageSupport;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Manages XMPP language translation protocol.
 * <p>
 * It lets you discover language translation providers and ask them to translate a text.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0171.html">XEP-0171: Language Translation</a>
 */
public final class LanguageTranslationManager extends Manager {

    private final ServiceDiscoveryManager serviceDiscoveryManager;

    private LanguageTranslationManager(XmppSession xmppSession) {
        super(xmppSession);
        serviceDiscoveryManager = xmppSession.getManager(ServiceDiscoveryManager.class);
    }

    /**
     * Discovers the language provider on a server.
     *
     * @param server The server address.
     * @return The list of translation providers.
     * @throws rocks.xmpp.core.stanza.StanzaException      If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0171.html#disco">4.2 Discovering Translation Providers</a>
     */
    public List<Item> discoverTranslationProviders(Jid server) throws XmppException {
        ItemNode itemNode = serviceDiscoveryManager.discoverItems(server);
        List<Item> items = new ArrayList<>();
        for (Item item : itemNode.getItems()) {
            InfoNode infoNode = serviceDiscoveryManager.discoverInformation(item.getJid());
            if (infoNode.getFeatures().contains(LanguageTranslation.NAMESPACE)) {
                items.add(item);
            }
        }
        return items;
    }

    /**
     * Discovers language support for a translation provider.
     *
     * @param translationProvider The translation provider.
     * @return The list of supported languages with details.
     * @throws rocks.xmpp.core.stanza.StanzaException      If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0171.html#disco-lang">4.2.3 Discovering Language Support</a>
     */
    public List<LanguageSupport.Item> discoverLanguageSupport(Jid translationProvider) throws XmppException {
        return xmppSession.query(new IQ(translationProvider, IQ.Type.GET, new LanguageSupport())).getExtension(LanguageSupport.class).getItems();
    }

    /**
     * Translates a text by sending a query to a translation provider.
     *
     * @param translationProvider The translation provider.
     * @param text                The text to be translated.
     * @param sourceLanguage      The source language.
     * @param destinationLanguage The destination language.
     * @return The translations.
     * @throws rocks.xmpp.core.stanza.StanzaException      If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0171.html#request">4.3 Requesting a Translation from a Service</a>
     */
    public List<LanguageTranslation.Translation> translate(Jid translationProvider, String text, String sourceLanguage, String... destinationLanguage) throws XmppException {
        Collection<LanguageTranslation.Translation> translations = new ArrayDeque<>();
        for (String dl : destinationLanguage) {
            translations.add(LanguageTranslation.Translation.ofDestinationLanguage(dl));
        }
        IQ result = xmppSession.query(new IQ(translationProvider, IQ.Type.GET, new LanguageTranslation(text, sourceLanguage, translations)));
        LanguageTranslation languageTranslation = result.getExtension(LanguageTranslation.class);
        return languageTranslation != null ? languageTranslation.getTranslations() : Collections.emptyList();
    }
}
