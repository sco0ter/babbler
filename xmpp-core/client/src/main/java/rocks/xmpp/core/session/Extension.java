/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Christian Schudt
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package rocks.xmpp.core.session;

import rocks.xmpp.core.ExtensionProtocol;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Represents an XMPP protocol extension (XEP).
 * <p>
 * An extension usually consists of:
 * <ul>
 * <li>A namespace, which uniquely identifies the extension, e.g. {@code "urn:xmpp:receipts"}</li>
 * <li>A {@link Manager} class, which is associated with the extension and manages its business logic.</li>
 * <li>A collection of classes which are implementations of the XML schema.</li>
 * <li>A set of features, which are advertised together with the namespace in service discovery, e.g. "urn:xmpp:hash-function-text-names:sha-1"</li>
 * </ul>
 * All of these characteristics are optional:
 * <p>
 * An extension could have an XML schema implementation, but no business logic and therefore no manager class, like XEP-0203: Delayed Delivery.
 * <p>
 * The namespace is optional as well: If an extension has no namespace, it won't be advertised in Service Discovery.
 * <p>
 * Extensions which have business logic (and hence a manager class), can be enabled or disabled.
 * <p>
 * In order to create an extension, use one of its static factory methods.
 * <p>
 * This class overrides {@link #equals(Object)} and {@link #hashCode()}, so that two extensions are considered equal if either their namespace are equal or their manager classes.
 * This allows to disable certain extensions or managers by default.
 *
 * @author Christian Schudt
 * @see <a href="https://xmpp.org/extensions/xep-0030.html">XEP-0030: Service Discovery</a>
 */
public final class Extension implements ExtensionProtocol {

    private final String namespace;

    private final Class<?> manager;

    private final Set<Class<?>> classes;

    private final Set<String> features;

    private final boolean enabled;

    private Extension(String namespace, Class<?> manager, Set<String> features, boolean enabled, Class<?>... classes) {
        this.namespace = namespace;
        this.manager = manager;
        this.classes = new HashSet<>(Arrays.asList(classes));
        this.features = features;
        this.enabled = enabled;
    }

    /**
     * Creates an extension with a set of classes (XML schema implementations) used by this extension.
     * <p>
     * Use it for extensions without business logic and no namespace which needs to be advertised, e.g. for Delayed Delivery.
     *
     * @param classes The classes.
     * @return The extension.
     */
    public static Extension of(Class<?>... classes) {
        if (classes.length == 0) {
            throw new IllegalArgumentException("Classes cannot be empty.");
        }
        for (Class<?> cl : classes) {
            if (Manager.class.isAssignableFrom(cl)) {
                throw new IllegalArgumentException("Manager classes are not allowed to use as JAXB class.");
            }
        }
        return new Extension(null, null, Collections.emptySet(), true, classes);
    }

    /**
     * Creates an extension, which won't get advertised during service discovery and only has a manager class.
     *
     * @param manager The manager class.
     * @param enabled If this manager is enabled.
     * @return The extension.
     */
    public static Extension of(Class<?> manager, boolean enabled) {
        return new Extension(null, Objects.requireNonNull(manager), Collections.emptySet(), enabled);
    }

    /**
     * Creates an extension without business logic, but an XML schema class. If enabled, it will be advertised as a feature in service discovery.
     * Examples are XEP-0020, XEP-0122 or XEP-0141.
     *
     * @param namespace The protocol namespace.
     * @param enabled   If this extension is enabled, i.e. whether it's advertised in service discovery.
     * @param classes   The XML schema classes.
     * @return The extension.
     */
    public static Extension of(String namespace, boolean enabled, Class<?>... classes) {
        return new Extension(Objects.requireNonNull(namespace), null, Collections.emptySet(), enabled, classes);
    }

    /**
     * Creates an extension with a namespace, business logic and XML schema classes.
     * <p>
     * This is the most common extension characteristic.
     *
     * @param namespace The protocol namespace.
     * @param manager   The manager class, which covers the business logic.
     * @param enabled   If this extension is enabled, i.e. whether its manager class is enabled and whether it's advertised in service discovery.
     * @param classes   The XML schema classes.
     * @return The extension.
     */
    public static Extension of(String namespace, Class<?> manager, boolean enabled, Class<?>... classes) {
        return new Extension(Objects.requireNonNull(namespace), manager, Collections.emptySet(), enabled, classes);
    }

    /**
     * Creates an PEP (<a href="https://xmpp.org/extensions/xep-0163.html">Personal Eventing Protocol</a>) extension with a namespace, business logic and XML schema classes.
     *
     * @param namespace The protocol namespace.
     * @param manager   The manager class, which covers the business logic.
     * @param notify    True, if this feature should be advertised along with a "filtered notification", i.e. as "namespace+notify".
     * @param enabled   If this extension is enabled, i.e. whether its manager class is enabled and whether it's advertised in service discovery.
     * @param classes   The XML schema classes.
     * @return The extension.
     */
    public static Extension of(String namespace, Class<? extends Manager> manager, boolean notify, boolean enabled, Class<?>... classes) {
        return new Extension(Objects.requireNonNull(namespace), manager, notify ? Collections.singleton(namespace + "+notify") : Collections.emptySet(), enabled, classes);
    }

    /**
     * Creates an extension which can advertise additional features, such as XEP-0300.
     *
     * @param namespace The protocol namespace.
     * @param manager   The manager class, which covers the business logic.
     * @param features  The features, which are advertised in service discovery.
     * @param enabled   If this extension is enabled, i.e. whether its manager class is enabled and whether it's advertised in service discovery.
     * @param classes   The XML schema classes.
     * @return The extension.
     */
    public static Extension of(String namespace, Class<? extends Manager> manager, Set<String> features, boolean enabled, Class<?>... classes) {
        return new Extension(namespace, manager, features, enabled, classes);
    }

    public static Extension of(ExtensionProtocol extensionProtocol, boolean enabled) {
        return new Extension(null, null, extensionProtocol.getFeatures(), enabled);
    }

    public static Extension of(ExtensionProtocol extensionProtocol, Class<? extends Manager> manager, boolean enabled) {
        return new Extension(extensionProtocol.getFeatures().iterator().next(), manager, extensionProtocol.getFeatures(), enabled);
    }

    /**
     * Gets the protocol namespace.
     *
     * @return The protocol namespace or null.
     */
    public final String getNamespace() {
        return namespace;
    }

    /**
     * Gets the manager class.
     *
     * @return The manager class or null.
     */
    public final Class<?> getManager() {
        return manager;
    }

    /**
     * Gets the collection of classes, which represent the extension's XML schema implementation.
     *
     * @return The classes.
     */
    public final Collection<Class<?>> getClasses() {
        return classes;
    }

    /**
     * Gets the collection of "sub" features, which are associated with the extension.
     *
     * @return The features.
     */
    @Override
    public final Set<String> getFeatures() {
        return features;
    }

    /**
     * Indicates whether the extension is enabled or not.
     *
     * @return True, if the extension is enabled; false if disabled.
     */
    @Override
    public final boolean isEnabled() {
        return enabled;
    }

    @Override
    public final boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Extension)) {
            return false;
        }
        Extension other = (Extension) o;

        if (namespace != null) {
            return namespace.equals(other.namespace);
        }
        if (manager != null) {
            return manager.equals(other.manager);
        }
        return classes.equals(other.classes);
    }

    @Override
    public final int hashCode() {
        if (namespace != null) {
            return Objects.hash(namespace);
        }
        if (manager != null) {
            return Objects.hash(manager);
        }
        return Objects.hash(classes);
    }

    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder();
        if (namespace != null) {
            sb.append(namespace);
        }
        if (manager != null) {
            if (sb.length() > 0) {
                sb.append(" | ");
            }
            sb.append(manager.getCanonicalName());
        }
        if (namespace != null || manager != null) {
            if (sb.length() > 0) {
                sb.append(" | ");
            }
            if (enabled) {
                sb.append("enabled");
            } else {
                sb.append("disabled");
            }
        }
        if (!classes.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(" | ");
            }
            StringJoiner sj = new StringJoiner(", ");
            classes.stream().map(Class::getCanonicalName).forEach(sj::add);
            sb.append(sj);
        }

        return sb.toString();
    }
}
