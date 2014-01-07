package org.xmpp.extension.rosterx;

import org.xmpp.Jid;
import org.xmpp.util.JidAdapter;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "x")
@XmlAccessorType(XmlAccessType.FIELD)
public final class RosterExchange {

    @XmlElement(name = "item")
    private List<Item> items;

    public List<Item> getItems() {
        return items;
    }

    public static class Item {

        @XmlAttribute(name = "action")
        private Action action;

        @XmlJavaTypeAdapter(JidAdapter.class)
        @XmlAttribute(name = "jid")
        private Jid jid;

        @XmlAttribute(name = "name")
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
