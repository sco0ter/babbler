package rocks.xmpp.extensions.langtrans.model.items;

import rocks.xmpp.addr.Jid;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The implementation of the the {@code <query/>} element in the {@code urn:xmpp:langtrans:items} namespace.
 *
 * @author Christian Schudt
 */
@XmlRootElement(name = "query")
public final class LanguageSupport {

    @XmlElement
    private final List<Item> item = new ArrayList<>();

    /**
     * Creates an empty query element.
     */
    public LanguageSupport() {
    }

    /**
     * Creates an query element with items.
     *
     * @param items The items.
     */
    public LanguageSupport(Collection<Item> items) {
        this.item.addAll(items);
    }

    /**
     * Gets the items, which hold language translation support details.
     *
     * @return The items.
     */
    public final List<Item> getItems() {
        return Collections.unmodifiableList(item);
    }

    /**
     * The implementation of the the {@code <item/>} element in the {@code urn:xmpp:langtrans:items} namespace.
     */
    public static final class Item {

        @XmlAttribute(name = "source_lang")
        private final String sourceLanguage;

        @XmlAttribute(name = "destination_lang")
        private final String destinationLanguage;

        @XmlAttribute
        private final Jid jid;

        @XmlAttribute
        private final String engine;

        @XmlAttribute
        private final Boolean pivotable;

        @XmlAttribute
        private final String dictionary;

        private Item() {
            this.sourceLanguage = null;
            this.jid = null;
            this.destinationLanguage = null;
            this.engine = null;
            this.pivotable = null;
            this.dictionary = null;
        }

        public Item(String sourceLanguage, Jid jid) {
            this(sourceLanguage, jid, null, null, null, null);
        }

        public Item(String sourceLanguage, Jid jid, String destinationLanguage, String engine, Boolean pivotable, String dictionary) {
            this.sourceLanguage = Objects.requireNonNull(sourceLanguage);
            this.jid = Objects.requireNonNull(jid);
            this.destinationLanguage = destinationLanguage;
            this.engine = engine;
            this.pivotable = pivotable;
            this.dictionary = dictionary;
        }

        /**
         * Gets the source language.
         *
         * @return The source language.
         */
        public final String getSourceLanguage() {
            return sourceLanguage;
        }

        /**
         * Gets the JID of the translation service.
         *
         * @return The JID.
         */
        public final Jid getJid() {
            return jid;
        }

        /**
         * Gets the destination language.
         *
         * @return The destination language.
         */
        public final String getDestinationLanguage() {
            return destinationLanguage;
        }

        /**
         * Gets the translation engine.
         *
         * @return The translation engine.
         */
        public final String getEngine() {
            return engine;
        }

        /**
         * Pivoting is the process of using one or more intermediate languages to translate from a given source language to a specific destination language.
         *
         * @return If a text is pivotable.
         */
        public final boolean isPivotable() {
            return pivotable != null && pivotable;
        }

        /**
         * In order to enhance the accuracy of translation engines most support the concept of mission specific dictionaries.
         *
         * @return The dictionary.
         */
        public final String getDictionary() {
            return dictionary;
        }
    }
}
