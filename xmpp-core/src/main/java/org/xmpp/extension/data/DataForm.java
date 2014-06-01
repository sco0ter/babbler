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

package org.xmpp.extension.data;

import org.xmpp.extension.data.layout.Page;
import org.xmpp.extension.data.media.Media;
import org.xmpp.extension.data.validate.Validation;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The implementation of the {@code <x/>} element in the {@code jabber:x:data} namespace, which represents data forms.
 * <blockquote>
 * <p>This specification defines an XMPP protocol extension for data forms that can be used in workflows such as service configuration as well as for application-specific data description and reporting. The protocol includes lightweight semantics for forms processing (such as request, response, submit, and cancel), defines several common field types (boolean, list options with single or multiple choice, text with single line or multiple lines, single or multiple JabberIDs, hidden fields, etc.), provides extensibility for future data types, and can be embedded in a wide range of applications. The protocol is not intended to provide complete forms-processing functionality as is provided in the W3C XForms technology, but instead provides a basic subset of such functionality for use by XMPP entities.</p>
 * </blockquote>
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0004.html">XEP-0004: Data Forms</a>
 * @see <a href="http://xmpp.org/extensions/xep-0004.html#schema">XML Schema</a>
 */
@XmlRootElement(name = "x")
public final class DataForm implements Comparable<DataForm> {
    @XmlElement
    private final List<String> instructions = new ArrayList<>();

    @XmlElementRef
    private final List<Page> pages = new ArrayList<>();

    @XmlElement(name = "field")
    private final List<Field> fields = new ArrayList<>();

    @XmlElement(name = "item")
    private final List<Item> items = new ArrayList<>();

    @XmlAttribute
    private Type type;

    @XmlElement
    private String title;

    @XmlElementWrapper(name = "reported")
    @XmlElement(name = "field")
    private List<Field> reportedFields;

    private DataForm() {
    }

    /**
     * Creates a data form.
     *
     * @param type The form type.
     */
    public DataForm(Type type) {
        this.type = type;
    }

    /**
     * Creates a data form.
     *
     * @param type  The form type.
     * @param title The form title.
     */
    public DataForm(Type type, String title) {
        this.type = type;
        this.title = title;
    }

    /**
     * Creates a data form.
     *
     * @param type         The form type.
     * @param title        The form title.
     * @param instructions The instructions.
     */
    public DataForm(Type type, String title, String... instructions) {
        this.type = type;
        this.title = title;
        this.instructions.addAll(Arrays.asList(instructions));
    }

    private static String getFormType(DataForm dataForm) {
        for (Field field : dataForm.getFields()) {
            if ("FORM_TYPE".equals(field.getVar()) && !field.getValues().isEmpty()) {
                return field.getValues().get(0);
            }
        }
        return null;
    }

    public static boolean parseBoolean(String value) {
        return Boolean.parseBoolean(value) || "1".equals(value);
    }

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

    /**
     * Gets the type of the form.
     *
     * @return The type.
     * @see #setType(org.xmpp.extension.data.DataForm.Type)
     */
    public Type getType() {
        return type;
    }

    /**
     * Sets the form type.
     *
     * @param type The form type.
     * @see #getType()
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * Gets the reported fields, which can be understood as "table headers" describing the data to follow.
     *
     * @return The reported fields.
     */
    public List<Field> getReportedFields() {
        return reportedFields;
    }

    /**
     * Gets the items, which can be understood as "table rows" containing data (if any) that matches the request.
     *
     * @return The items.
     */
    public List<Item> getItems() {
        return items;
    }

    /**
     * Finds a field by its name ('ver' attribute).
     *
     * @param name The name.
     * @return The field or null if the field could be found.
     */
    public Field findField(String name) {
        if (name != null) {
            for (Field field : fields) {
                if (name.equals(field.getVar())) {
                    return field;
                }
            }
        }
        return null;
    }

    /**
     * Compares this data form with another data form.
     * Data forms which have a "FORM_TYPE" field are are listed first in a collection.
     *
     * @param o The other data form.
     * @return The comparison result.
     */
    @Override
    public int compareTo(DataForm o) {
        String ft = getFormType(this);
        String fto = getFormType(o);
        if (ft == null && fto == null) {
            return 0;
        } else if (ft == null) {
            return 1;
        } else if (fto == null) {
            return -1;
        } else {
            return ft.compareTo(fto);
        }
    }

    public List<Page> getPages() {
        return pages;
    }

    /**
     * The form type.
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

    /**
     * A form field.
     *
     * @see <a href="http://xmpp.org/extensions/xep-0004.html#protocol-field">3.2 The Field Element</a>
     */
    @XmlRootElement(name = "field")
    public static final class Field implements Comparable<Field> {

        @XmlElement(name = "option")
        private final List<Option> options = new ArrayList<>();

        @XmlElement(name = "desc")
        private String description;

        @XmlElement(name = "required")
        private String required;

        @XmlElementRef
        private Validation validation;

        @XmlElement(name = "value")
        private List<String> values = new ArrayList<>();

        @XmlElementRef
        private Media media;

        @XmlAttribute(name = "label")
        private String label;

        @XmlAttribute(name = "type")
        private Type type;

        @XmlAttribute(name = "var")
        private String var;

        private Field() {
        }

        /**
         * Creates a field.
         *
         * @param type The field type.
         */
        public Field(Type type) {
            this.type = type;
        }

        /**
         * Creates a field.
         *
         * @param type   The field type.
         * @param var    The unique identifier for the field.
         * @param values The values.
         */
        public Field(Type type, String var, String... values) {
            this.type = type;
            this.var = var;
            this.values = new ArrayList<>(Arrays.asList(values));
        }

        /**
         * Gets the field type.
         *
         * @return The field type.
         */
        public Type getType() {
            return type;
        }

        /**
         * Sets the field type.
         *
         * @param type The type.
         */
        public void setType(Type type) {
            this.type = type;
        }

        /**
         * Gets a unique identifier for the field in the context of the form.
         *
         * @return The var attribute.
         * @see #setVar(String)
         */
        public String getVar() {
            return var;
        }

        /**
         * Sets a unique identifier for the field in the context of the form.
         *
         * @param var The var attribute.
         * @see #getVar()
         */
        public void setVar(String var) {
            this.var = var;
        }

        /**
         * Gets the field label.
         *
         * @return The label.
         * @see #setLabel(String)
         */
        public String getLabel() {
            return label;
        }

        /**
         * Sets the field label.
         *
         * @param label The label.
         * @see #getLabel()
         */
        public void setLabel(String label) {
            this.label = label;
        }

        /**
         * Gets the options if the field type is {@link Field.Type#LIST_SINGLE} or {@link Field.Type#LIST_MULTI}.
         *
         * @return The options.
         */
        public List<Option> getOptions() {
            return options;
        }

        /**
         * Gets the values of the field. Fields of type {@link Field.Type#LIST_MULTI}, {@link Field.Type#JID_MULTI} or {@link Field.Type#TEXT_MULTI} may contain multiple values.
         *
         * @return The values.
         */
        public List<String> getValues() {
            return values;
        }

        /**
         * Gets the media element.
         *
         * @return The media element.
         * @see #setMedia(org.xmpp.extension.data.media.Media)
         */
        public Media getMedia() {
            return media;
        }

        /**
         * Sets a media element.
         *
         * @param media The media element.
         * @see #getMedia()
         */
        public void setMedia(Media media) {
            this.media = media;
        }

        /**
         * Gets a natural-language description of the field, intended for presentation in a user-agent (e.g., as a "tool-tip", help button, or explanatory text provided near the field).
         *
         * @return The description.
         * @see #setDescription(String)
         */
        public String getDescription() {
            return description;
        }

        /**
         * Sets a description of the field.
         *
         * @param description The description.
         * @see #getDescription()
         */
        public void setDescription(String description) {
            this.description = description;
        }

        /**
         * If the field as required in order for the form to be considered valid.
         *
         * @return True, if the field is required.
         * @see #setRequired(boolean)
         */
        public boolean isRequired() {
            return required != null;
        }

        /**
         * Sets the field as required.
         *
         * @param required If the field is required.
         * @see #isRequired()
         */
        public void setRequired(boolean required) {
            this.required = required ? "" : null;
        }

        @Override
        public int compareTo(Field o) {

            if ("FORM_TYPE".equals(getVar()) && !"FORM_TYPE".equals(o.getVar())) {
                return -1;
            }

            if (getVar() == null && o.getVar() == null) {
                return 0;
            } else if (getVar() == null) {
                return -1;
            } else if (o.getVar() == null) {
                return 1;
            } else {
                return getVar().compareTo(o.getVar());
            }
        }

        /**
         * Gets the validation for this field.
         *
         * @return The validation.
         * @see <a href="http://xmpp.org/extensions/xep-0122.html">XEP-0122: Data Forms Validation</a>
         */
        public Validation getValidation() {
            return validation;
        }

        /**
         * Defines field types.
         * <blockquote>
         * <p><cite><a href="http://xmpp.org/extensions/xep-0004.html#protocol-fieldtypes">3.3 Field Types</a></cite></p>
         * <p>The following field types represent data "types" that are commonly exchanged between Jabber/XMPP entities.</p>
         * </blockquote>
         */
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

    /**
     * An item which can be understood as a table row. The fields can be understood as table cells.
     */
    public static final class Item {
        @XmlElement(name = "field")
        private final List<Field> fields = new ArrayList<>();

        /**
         * Gets the fields.
         *
         * @return The fields.
         */
        public List<Field> getFields() {
            return fields;
        }
    }

    /**
     * Defines an option in a field of type {@link DataForm.Field.Type#LIST_SINGLE} or {@link DataForm.Field.Type#LIST_MULTI}.
     */
    public static final class Option {

        @XmlAttribute(name = "label")
        private String label;

        @XmlElement(name = "value")
        private String value;

        private Option() {
        }

        /**
         * Creates an option.
         *
         * @param value The option value.
         */
        public Option(String value) {
            this.value = value;
        }

        /**
         * Creates an option.
         *
         * @param value The option value.
         * @param label A human-readable name for the option.
         */
        public Option(String value, String label) {
            this.value = value;
            this.label = label;
        }

        /**
         * Gets the label.
         *
         * @return The label.
         */
        public String getLabel() {
            return label;
        }

        /**
         * Gets the value of the option.
         *
         * @return The option.
         */
        public String getValue() {
            return value;
        }
    }
}
