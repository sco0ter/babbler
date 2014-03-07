package org.xmpp.extension.muc;

import javax.xml.bind.annotation.XmlAttribute;
import java.util.Date;

/**
 * @author Christian Schudt
 */
public final class History {

    @XmlAttribute(name = "maxchars")
    private Integer maxChars;

    @XmlAttribute(name = "maxstanzas")
    private Integer maxStanzas;

    @XmlAttribute(name = "seconds")
    private Integer seconds;

    @XmlAttribute(name = "since")
    private Date since;

    public static History forMaxChars(int maxChars) {
        History history = new History();
        history.maxChars = maxChars;
        return history;
    }

    public static History forMaxStanzas(int maxStanzas) {
        History history = new History();
        history.maxStanzas = maxStanzas;
        return history;
    }

    public static History forSeconds(int seconds) {
        History history = new History();
        history.seconds = seconds;
        return history;
    }

    public static History since(Date date) {
        History history = new History();
        history.since = date;
        return history;
    }

    public static History none() {
        History history = new History();
        history.maxChars = 0;
        return history;
    }
}
