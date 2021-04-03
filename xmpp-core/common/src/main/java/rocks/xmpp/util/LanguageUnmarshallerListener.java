/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2020 Christian Schudt
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

package rocks.xmpp.util;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import javax.xml.XMLConstants;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * Listens to unmarshal events in order to assign a parent object's {@code xml:lang} attribute to its child elements.
 * This is important because the language of a parent is implicitly inherited to its children.
 * If a child element were detached from it's parent the language information would be lost.
 * <p>
 * This even may lead to misbehavior, e.g. when calculating the Entity Capabilities hash, which takes the language into account.
 *
 * @author Christian Schudt
 */
public final class LanguageUnmarshallerListener extends Unmarshaller.Listener {

    /**
     * The root locale which is used if an unmarshalled element has no xml:lang attribute.
     */
    private final Locale rootLocale;

    /**
     * Maps a parent to its children.
     */
    private final IdentityHashMap<Object, List<Object>> parentToChildren = new IdentityHashMap<>();

    /**
     * Keeps track of the object tree, in order to know everything has been unmarshalled. Push happens before unmarshal, pop happens after unmarshal.
     */
    private final Deque<Object> stack = new ArrayDeque<>();

    public LanguageUnmarshallerListener(final Locale rootLocale) {
        this.rootLocale = rootLocale;
    }

    @Override
    public final void afterUnmarshal(final Object target, final Object parent) {
        super.afterUnmarshal(target, parent);
        Object root = stack.pop();
        if (stack.isEmpty()) {
            // The whole object has been unmarshalled.
            // Now starting from the root object
            assignLocale(root, rootLocale);
        }
    }

    @Override
    public final void beforeUnmarshal(final Object target, final Object parent) {
        super.beforeUnmarshal(target, parent);
        if (!stack.isEmpty()) {
            List<Object> children = parentToChildren.computeIfAbsent(parent, key -> new ArrayList<>());
            children.add(target);
        }
        stack.push(target);
    }

    /**
     * Assigns the parent locale to the object.
     * Then recursively assigns the object's locale to its children and so on.
     *
     * @param object       The object.
     * @param parentLocale The parent locale.
     */
    private void assignLocale(Object object, Locale parentLocale) {

        Locale locale = setLocaleIfNull(object, parentLocale);
        List<Object> children = parentToChildren.remove(object);

        if (children != null) {
            for (Object child : children) {
                assignLocale(child, locale);
            }
        }
    }

    /**
     * Sets the <code>xml:lang</code> attribute of an object (if the attribute is present but not set) with the given locale.
     * <p>
     * This is done via reflection by searching for a field which is JAXB mapped to a <code>xml:lang</code> attribute.
     *
     * @param target       The object where the xml:lang attribute is searched in.
     * @param parentLocale The parent locale, which is set in case that the object has no locale.
     * @return The preset locale (if it was present) or the passed parent locale which has been set.
     */
    private static Locale setLocaleIfNull(Object target, Locale parentLocale) {
        if (target == null) {
            return parentLocale;
        }
        Class<?> current = target.getClass();
        while (current.getSuperclass() != null) {
            try {
                Field field = current.getDeclaredField("lang");
                XmlAttribute xmlAttribute = field.getAnnotation(XmlAttribute.class);

                if (XMLConstants.XML_NS_URI.equals(xmlAttribute.namespace()) && field.getType() == Locale.class) {
                    field.setAccessible(true);
                    Locale presetLocale = (Locale) field.get(target);
                    if (presetLocale != null) {
                        return presetLocale;
                    } else {
                        if (parentLocale != null) {
                            field.set(target, parentLocale);
                            return parentLocale;
                        }
                        break;
                    }
                }
                current = current.getSuperclass();
            } catch (NoSuchFieldException | IllegalAccessException ignore) {
                current = current.getSuperclass();
            }
        }
        return parentLocale;
    }
}
