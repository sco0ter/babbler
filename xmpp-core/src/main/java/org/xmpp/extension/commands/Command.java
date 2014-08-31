package org.xmpp.extension.commands;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * @author Christian Schudt
 */
public final class Command {

    @XmlAttribute(name = "action")
    private Action action;

    private String node;

    private String sessionId;

    @XmlAttribute(name = "status")
    private Status status;


    public enum Action {
        @XmlEnumValue("cancel")
        CANCEL,
        @XmlEnumValue("complete")
        COMPLETE,
        @XmlEnumValue("execute")
        EXECUTE,
        @XmlEnumValue("next")
        NEXT,
        @XmlEnumValue("prev")
        PREV
    }


    public enum Status {

        @XmlEnumValue("canceled")
        CANCELED,
        @XmlEnumValue("completed")
        COMPLETED,
        @XmlEnumValue("executing")
        EXECUTING
    }
}


