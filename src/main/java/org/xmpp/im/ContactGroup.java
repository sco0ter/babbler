/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Christian Schudt
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

package org.xmpp.im;

import java.util.*;

/**
 * Represents a contact group in the user's roster.
 * A group consists of contacts and may contain nested sub-groups.
 *
 * @author Christian Schudt
 */
public final class ContactGroup implements Comparable<ContactGroup> {

    private final Set<Contact> contacts;

    private final Set<ContactGroup> groups;

    private final ContactGroup parentGroup;

    private final String name;

    private final String fullName;

    ContactGroup(String name, String fullName, ContactGroup parentGroup) {
        this.name = name;
        this.fullName = fullName;
        this.contacts = new TreeSet<>();
        this.groups = new TreeSet<>();
        this.parentGroup = parentGroup;
    }

    /**
     * Gets the name of the group.
     *
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the full name of the group.
     *
     * @return The full name.
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Gets the nested sub-groups of this group.
     *
     * @return The nested groups.
     */
    public Collection<ContactGroup> getGroups() {
        return groups;
    }

    /**
     * Gets the contacts in this group.
     *
     * @return The contacts.
     */
    public Collection<Contact> getContacts() {
        return contacts;
    }

    @Override
    public int compareTo(ContactGroup o) {
        if (this == o) {
            return 0;
        }

        if (o != null) {
            int result;
            if (fullName != null) {
                if (o.fullName != null) {
                    result = fullName.compareToIgnoreCase(o.fullName);
                } else {
                    result = -1;
                }
            } else {
                if (o.fullName != null) {
                    result = 1;
                } else {
                    result = 0;
                }
            }
            return result;
        } else {
            return -1;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ContactGroup)) {
            return false;
        }
        ContactGroup other = (ContactGroup) o;

        return fullName == null ? other.fullName == null : fullName.equals(other.fullName);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + ((fullName == null) ? 0 : fullName.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return name;
    }
}
