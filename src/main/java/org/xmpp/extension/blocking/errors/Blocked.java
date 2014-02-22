package org.xmpp.extension.blocking.errors;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * The implementation of the {@code <blocked/>} error.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0191.html#block">3.3 User Blocks Contact</a>
 */
@XmlRootElement(name = "blocked")
public final class Blocked {
}
