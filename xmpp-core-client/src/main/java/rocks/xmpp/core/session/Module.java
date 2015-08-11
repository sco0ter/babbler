package rocks.xmpp.core.session;

import java.util.Collection;

/**
 * A module defines a set of extensions. There's at least a core module containing classes from the core specification.
 * Then there could be modules for different kinds of use cases, each combining multiple extensions, e.g. a BOSH module, an IoT module, a File Transfer module, etc.
 * <p>
 * Modules are loaded by the core using the {@link java.util.ServiceLoader} API.
 *
 * @author Christian Schudt
 */
public interface Module {

    /**
     * Gets the extensions belonging to this module.
     *
     * @return The extensions.
     */
    Collection<Extension> getExtensions();
}
