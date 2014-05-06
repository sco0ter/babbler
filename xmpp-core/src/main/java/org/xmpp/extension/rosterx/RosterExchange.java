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

package org.xmpp.extension.rosterx;

import org.xmpp.Jid;
import org.xmpp.JidAdapter;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "x")
public final class RosterExchange {

    @XmlElement(name = "item")
    private List<Item> items;

    public List<Item> getItems() {
        return items;
    }

    public static class Item {

        @XmlAttribute
        private Action action;

        @XmlJavaTypeAdapter(JidAdapter.class)
        @XmlAttribute
        private Jid jid;

        @XmlAttribute
        private String name;

        @XmlElement(name = "group")
        private List<String> groups = new ArrayList<>();

        private Item() {

        }

        public Item(Jid jid, String name, List<String> groups, Action action) {
            this.jid = jid;
            this.name = name;
            this.groups = groups;
            this.action = action;
        }

        public Jid getJid() {
            return jid;
        }

        public Action getAction() {
            return action;
        }

        public String getName() {
            return name;
        }

        public List<String> getGroups() {
            return groups;
        }

        @XmlEnum
        public enum Action {
            @XmlEnumValue("add")
            ADD,
            @XmlEnumValue("delete")
            DELETE,
            @XmlEnumValue("modify")
            MODIFY
        }
    }

}
