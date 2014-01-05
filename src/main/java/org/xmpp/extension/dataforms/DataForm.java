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

package org.xmpp.extension.dataforms;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of <a href="http://xmpp.org/extensions/xep-0004.html">XEP-0004: Data Forms</a>.
 *
 * @author Christian Schudt
 */
@XmlRootElement(name = "x")
@XmlAccessorType(XmlAccessType.FIELD)
public final class DataForm {

    @XmlAttribute(name = "type")
    private Type type;

    @XmlElement(name = "instructions")
    private List<String> instructions = new ArrayList<>();

    @XmlElement(name = "title")
    private String title;

    @XmlElement(name = "field")
    private List<Field> fields = new ArrayList<>();

    @XmlElement(name = "item")
    private List<Item> items = new ArrayList<>();

    @XmlElementWrapper(name = "reported")
    @XmlElement(name = "field")
    private List<Field> reportedFields = new ArrayList<>();

    /**
     * Gets the title of the form.
     * <blockquote>
     * <p>The OPTIONAL {@code <title/>} and {@code <instructions/>} elements enable the form-processing entity to label the form as a whole and specify natural-language instructions to be followed by the form-submitting entity. The XML character data for these elements SHOULD NOT contain newlines (the \n and \r characters), and any handling of newlines (e.g., presentation in a user interface) is unspecified herein; however, multiple instances of the {@code <instructions/>} element MAY be included.</p>
     * </blockquote>
     *
     * @return The title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the form.
     *
     * @param title The title.
     * @see #getTitle()
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the fields of the form.
     *
     * @return The fields.
     */
    public List<Field> getFields() {
        return fields;
    }

    /**
     * Gets the instructions of the form.
     * <blockquote>
     * <p>The OPTIONAL {@code <title/>} and {@code <instructions/>} elements enable the form-processing entity to label the form as a whole and specify natural-language instructions to be followed by the form-submitting entity. The XML character data for these elements SHOULD NOT contain newlines (the \n and \r characters), and any handling of newlines (e.g., presentation in a user interface) is unspecified herein; however, multiple instances of the {@code <instructions/>} element MAY be included.</p>
     * </blockquote>
     *
     * @return The instructions.
     */
    public List<String> getInstructions() {
        return instructions;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }



    public List<Field> getReportedFields() {
        return reportedFields;
    }

    public List<Item> getItems() {
        return items;
    }

    /**
     *
     */
    @XmlEnum
    @XmlType(name = "form-type")
    public enum Type {
        /**
         * The form-submitting entity has cancelled submission of data to the form-processing entity.
         */
        @XmlEnumValue(value = "cancel")
        CANCEL,
        /**
         * The form-processing entity is asking the form-submitting entity to complete a form.
         */
        @XmlEnumValue(value = "form")
        FORM,
        /**
         * The form-processing entity is returning data (e.g., search results) to the form-submitting entity, or the data is a generic data set.
         */
        @XmlEnumValue(value = "result")
        RESULT,
        /**
         * The form-submitting entity is submitting data to the form-processing entity. The submission MAY include fields that were not provided in the empty form, but the form-processing entity MUST ignore any fields that it does not understand.
         */
        @XmlEnumValue(value = "submit")
        SUBMIT
    }

    @XmlRootElement(name = "field")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Field {

        @XmlElement(name = "desc")
        private String description;

        @XmlElement(name = "required")
        private String required;

        @XmlElement(name = "value")
        private List<String> values = new ArrayList<>();

        @XmlElement(name = "option")
        private List<Option> options = new ArrayList<>();

        @XmlAttribute(name = "label")
        private String label;

        @XmlAttribute(name = "type")
        private Type type;

        @XmlAttribute(name = "var")
        private String var;

        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        public String getVar() {
            return var;
        }

        public void setVar(String var) {
            this.var = var;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public List<Option> getOptions() {
            return options;
        }

        public List<String> getValues() {
            return values;
        }

        @XmlEnum
        @XmlType(name = "field-type")
        public static enum Type {
            /**
             * The field enables an entity to gather or provide an either-or choice between two options. The default value is "false".
             */
            @XmlEnumValue(value = "boolean")
            BOOLEAN,
            /**
             * The field is intended for data description (e.g., human-readable text such as "section" headers) rather than data gathering or provision. The {@code <value/>} child SHOULD NOT contain newlines (the \n and \r characters); instead an application SHOULD generate multiple fixed fields, each with one {@code <value/>} child.
             */
            @XmlEnumValue(value = "fixed")
            FIXED,
            /**
             * The field is not shown to the form-submitting entity, but instead is returned with the form. The form-submitting entity SHOULD NOT modify the value of a hidden field, but MAY do so if such behavior is defined for the "using protocol".
             */
            @XmlEnumValue(value = "hidden")
            HIDDEN,
            /**
             * The field enables an entity to gather or provide multiple Jabber IDs. Each provided JID SHOULD be unique (as determined by comparison that includes application of the Nodeprep, Nameprep, and Resourceprep profiles of Stringprep as specified in XMPP Core), and duplicate JIDs MUST be ignored.
             */
            @XmlEnumValue(value = "jid-multi")
            JID_MULTI,
            /**
             * The field enables an entity to gather or provide a single Jabber ID.
             */
            @XmlEnumValue(value = "jid-single")
            JID_SINGLE,
            /**
             * The field enables an entity to gather or provide one or more options from among many. A form-submitting entity chooses one or more items from among the options presented by the form-processing entity and MUST NOT insert new options. The form-submitting entity MUST NOT modify the order of items as received from the form-processing entity, since the order of items MAY be significant.
             */
            @XmlEnumValue(value = "list-multi")
            LIST_MULTI,
            /**
             * The field enables an entity to gather or provide one option from among many. A form-submitting entity chooses one item from among the options presented by the form-processing entity and MUST NOT insert new options.
             */
            @XmlEnumValue(value = "list-single")
            LIST_SINGLE,
            /**
             * The field enables an entity to gather or provide multiple lines of text.
             */
            @XmlEnumValue(value = "text-multi")
            TEXT_MULTI,
            /**
             * The field enables an entity to gather or provide a single line or word of text, which shall be obscured in an interface (e.g., with multiple instances of the asterisk character).
             */
            @XmlEnumValue(value = "text-private")
            TEXT_PRIVATE,
            /**
             * The field enables an entity to gather or provide a single line or word of text, which may be shown in an interface. This field type is the default and MUST be assumed if a form-submitting entity receives a field type it does not understand.
             */
            @XmlEnumValue(value = "text-single")
            TEXT_SINGLE
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Item {
        @XmlElement(name = "field")
        private List<Field> fields = new ArrayList<>();

        public List<Field> getFields() {
            return fields;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Option {
        @XmlAttribute(name = "label")
        private String label;

        @XmlElement(name = "value")
        private String value;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
