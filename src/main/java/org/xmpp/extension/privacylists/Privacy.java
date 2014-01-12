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

package org.xmpp.extension.privacylists;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "query")
public final class Privacy {

    @XmlJavaTypeAdapter(ActiveNameAdapter.class)
    @XmlElement(name = "active")
    private String activeName;

    @XmlJavaTypeAdapter(DefaultNameAdapter.class)
    @XmlElement(name = "default")
    private String defaultName;

    @XmlElement(name = "list")
    private List<PrivacyList> privacyLists = new ArrayList<>();

    public Privacy() {

    }

    public Privacy(PrivacyList privacyList) {
        privacyLists.add(privacyList);
    }

    public String getActiveName() {
        return activeName;
    }

    public void setActiveName(String activeName) {
        this.activeName = activeName;
    }

    public String getDefaultName() {
        return defaultName;
    }

    public void setDefaultName(String defaultName) {
        this.defaultName = defaultName;
    }

    public List<PrivacyList> getPrivacyLists() {
        return privacyLists;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class PrivacyList {
        @XmlAttribute(name = "name")
        private String name;

        @XmlElement(name = "item")
        private List<Item> items = new ArrayList<>();

        private PrivacyList() {

        }

        public PrivacyList(String name) {
            this.name = name;
        }

        public List<Item> getItems() {
            return items;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        public static class Item {
            @XmlAttribute(name = "order")
            private int order;

            @XmlAttribute(name = "value")
            private String value;

            @XmlAttribute(name = "type")
            private Type type;

            @XmlAttribute(name = "action")
            private Action action;

            public Type getType() {
                return type;
            }

            public void setType(Type type) {
                this.type = type;
            }

            public Action getAction() {
                return action;
            }

            public void setAction(Action action) {
                this.action = action;
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }

            public int getOrder() {
                return order;
            }

            public void setOrder(int order) {
                this.order = order;
            }

            @XmlEnum
            public enum Type {
                /**
                 *
                 */
                @XmlEnumValue("group")
                GROUP,
                /**
                 *
                 */
                @XmlEnumValue("jid")
                JID,
                /**
                 *
                 */
                @XmlEnumValue("subscription")
                SUBSCRIPTION
            }

            @XmlEnum
            public enum Action {
                /**
                 *
                 */
                @XmlEnumValue("allow")
                ALLOW,
                /**
                 *
                 */
                @XmlEnumValue("deny")
                DENY
            }

            public static class Message {

            }

            public static class IQ {

            }

            public static class PresenceIn {

            }

            public static class PresenceOut {

            }
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    private static class Active {
        @XmlAttribute(name = "name")
        private String name;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    private static class Default {
        @XmlAttribute(name = "name")
        private String name;
    }

    private static class ActiveNameAdapter extends XmlAdapter<Active, String> {

        @Override
        public String unmarshal(Active v) throws Exception {
            if (v != null) {
                return v.name;
            }
            return null;
        }

        @Override
        public Active marshal(String v) throws Exception {
            if (v != null) {
                Active active = new Active();
                active.name = v;
                return active;
            }
            return null;
        }
    }

    private static class DefaultNameAdapter extends XmlAdapter<Default, String> {

        @Override
        public String unmarshal(Default v) throws Exception {
            if (v != null) {
                return v.name;
            }
            return null;
        }

        @Override
        public Default marshal(String v) throws Exception {
            if (v != null) {
                Default def = new Default();
                def.name = v;
                return def;
            }
            return null;
        }
    }
}
