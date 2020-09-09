package rocks.xmpp.core;

import java.util.Locale;

/**
 * @author Christian Schudt
 */
public interface LanguageElement {

    /**
     * Gets the language, i.e. the value of the 'xml:lang' attribute.
     *
     * @return The language.
     */
    Locale getLanguage();
}
