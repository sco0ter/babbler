/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Christian Schudt
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

package rocks.xmpp.extensions.data.model;

import rocks.xmpp.core.Jid;
import rocks.xmpp.extensions.data.layout.model.Page;
import rocks.xmpp.extensions.data.mediaelement.model.Media;
import rocks.xmpp.extensions.data.validate.model.Validation;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
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

    /**
     * jabber:x:data
     */
    public static final String NAMESPACE = "jabber:x:data";

    /**
     * The name of the hidden field, which determines the form type, "FORM_TYPE".
     */
    public static final String FORM_TYPE = "FORM_TYPE";

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
     * @param type   The form type.
     * @param fields The fields.
     */
    public DataForm(Type type, Collection<Field> fields) {
        this.type = type;
        if (fields != null) {
            this.fields.addAll(fields);
        }
    }

    public DataForm(Builder<? extends Builder> builder) {
        if (builder.formType != null) {
            this.fields.add(Field.builder().var(FORM_TYPE).value(builder.formType).type(Field.Type.HIDDEN).build());
        }
        this.fields.addAll(builder.fields);
        this.type = builder.type;
        this.title = builder.title;
        if (builder.items != null) {
            this.items.addAll(builder.items);
        }
        if (builder.instructions != null) {
            this.instructions.addAll(builder.instructions);
        }
        if (builder.pages != null) {
            this.pages.addAll(pages);
        }
        if (builder.reportedFields != null && !builder.reportedFields.isEmpty()) {
            this.reportedFields = new ArrayList<>();
            this.reportedFields.addAll(builder.reportedFields);
        }
    }

    public DataForm(Type type, String title, Collection<Field> fields, Collection<Field> reportedFields, Collection<Item> items, Collection<String> instructions, Collection<Page> pages) {
        this.type = type;
        this.title = title;
        if (instructions != null) {
            this.instructions.addAll(instructions);
        }
        if (pages != null) {
            this.pages.addAll(pages);
        }
        if (fields != null) {
            this.fields.addAll(fields);
        }
        if (items != null) {
            this.items.addAll(items);
        }
        if (reportedFields != null && !reportedFields.isEmpty()) {
            this.reportedFields = new ArrayList<>();
            this.reportedFields.addAll(reportedFields);
        }
    }

    /**
     * Parses a value as boolean. Positive values as per XEP-0004 are "1" and "true".
     *
     * @param value The value.
     * @return The parsed boolean value.
     */
    public static boolean parseBoolean(String value) {
        return Boolean.parseBoolean(value) || "1".equals(value);
    }

    /**
     * Gets the value for a specific field.
     *
     * @param var The field name.
     * @return The value or null, if the field does not exist.
     */
    public String findValue(String var) {
        List<String> values = findValues(var);
        return values.isEmpty() ? null : values.get(0);
    }

    /**
     * Gets the values for a specific field.
     *
     * @param var The field name.
     * @return The values.
     */
    public List<String> findValues(String var) {
        Field field = findField(var);
        return field == null ? Collections.<String>emptyList() : field.getValues();
    }

    /**
     * Finds the field and gets the value as boolean.
     *
     * @param var The field name.
     * @return The value as boolean.
     */
    public boolean findValueAsBoolean(String var) {
        return parseBoolean(findValue(var));
    }

    /**
     * Finds the field and gets its value as integer.
     *
     * @param var The field name.
     * @return The value as integer or null, if the field could not be found.
     */
    public Integer findValueAsInteger(String var) {
        Field field = findField(var);
        return field == null ? null : field.getValueAsInteger();
    }

    /**
     * Finds the field and gets its value as date.
     *
     * @param var The field name.
     * @return The value as date or null, if the field could not be found.
     */
    public Date findValueAsDate(String var) {
        Field field = findField(var);
        return field == null ? null : field.getValueAsDate();
    }

    /**
     * Finds the field and gets the value as JID.
     *
     * @param var The field name.
     * @return The value as JID or null, if the field could not be found.
     */
    public Jid findValueAsJid(String var) {
        Field field = findField(var);
        return field == null ? null : field.getValueAsJid();
    }

    /**
     * Finds the field and gets its values as JID list. If the field could not be found, an empty list is returned.
     *
     * @param var The field name.
     * @return The values as JID list.
     */
    public List<Jid> findValuesAsJid(String var) {
        Field field = findField(var);
        return field == null ? Collections.<Jid>emptyList() : field.getValuesAsJid();
    }

    /**
     * Gets the form type of this form, i.e. the value of the "FORM_TYPE" field.
     *
     * @return The form type or null, if there is no form type.
     */
    public String getFormType() {
        for (Field field : getFields()) {
            if (FORM_TYPE.equals(field.getVar()) && !field.getValues().isEmpty()) {
                return field.getValues().get(0);
            }
        }
        return null;
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
     * Gets the fields of the form.
     *
     * @return The fields.
     */
    public List<Field> getFields() {
        return Collections.unmodifiableList(fields);
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
        return Collections.unmodifiableList(instructions);
    }

    /**
     * Gets the type of the form.
     *
     * @return The type.
     */
    public Type getType() {
        return type;
    }

    /**
     * Gets the reported fields, which can be understood as "table headers" describing the data to follow.
     *
     * @return The reported fields.
     */
    public List<Field> getReportedFields() {
        return Collections.unmodifiableList(reportedFields);
    }

    /**
     * Gets the items, which can be understood as "table rows" containing data (if any) that matches the request.
     *
     * @return The items.
     */
    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Gets the layout pages for this data form.
     *
     * @return The pages.
     */
    public List<Page> getPages() {
        return Collections.unmodifiableList(pages);
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
        String ft = getFormType();
        String fto = o != null ? o.getFormType() : null;
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

        @XmlElement(name = "desc")
        private String description;

        @XmlElement(name = "required")
        private String required;

        @XmlElementRef
        private Validation validation;

        @XmlElement(name = "value")
        private List<String> values = new ArrayList<>();

        @XmlElement(name = "option")
        private List<Option> options = new ArrayList<>();

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

        private Field(Builder builder) {
            this.type = builder.type;
            this.description = builder.description;
            this.required = builder.required ? "" : null;
            this.validation = builder.validation;
            this.values.addAll(builder.values);
            this.options.addAll(builder.options);
            this.media = builder.media;
            this.label = builder.label;
            this.var = builder.var;
        }

        /**
         * Creates the builder to build a data form field.
         *
         * @return The builder.
         */
        public static Builder builder() {
            return new Builder();
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
         * Gets a unique identifier for the field in the context of the form.
         *
         * @return The var attribute.
         */
        public String getVar() {
            return var;
        }

        /**
         * Gets the field label.
         *
         * @return The label.
         */
        public String getLabel() {
            return label;
        }

        /**
         * Gets the options if the field type is {@link Field.Type#LIST_SINGLE} or {@link Field.Type#LIST_MULTI}.
         *
         * @return The options.
         */
        public List<Option> getOptions() {
            return Collections.unmodifiableList(options);
        }

        /**
         * Gets the values of the field. Fields of type {@link Field.Type#LIST_MULTI}, {@link Field.Type#JID_MULTI} or {@link Field.Type#TEXT_MULTI} may contain multiple values.
         *
         * @return The values.
         */
        public List<String> getValues() {
            return Collections.unmodifiableList(values);
        }

        /**
         * Gets the value as boolean.
         *
         * @return The value as boolean.
         */
        public boolean getValueAsBoolean() {
            return parseBoolean(values.isEmpty() ? null : values.get(0));
        }

        /**
         * Returns the first value as integer.
         *
         * @return The integer or null, if the values are empty.
         */
        public Integer getValueAsInteger() {
            return values.isEmpty() ? null : Integer.valueOf(values.get(0));
        }

        /**
         * Returns the first value as date.
         *
         * @return The date or null, if the values are empty.
         */
        public Date getValueAsDate() {
            if (values.isEmpty()) {
                return null;
            } else {
                return values.get(0) != null ? DatatypeConverter.parseDateTime(values.get(0)).getTime() : null;
            }
        }

        /**
         * Returns a JID list for the {@link Type#JID_MULTI} field type.
         *
         * @return The JID list.
         */
        public List<Jid> getValuesAsJid() {
            List<Jid> jids = new ArrayList<>();
            for (String value : values) {
                jids.add(Jid.valueOf(value, true));
            }
            return Collections.unmodifiableList(jids);
        }

        /**
         * Returns the first value as JID, e.g. for the {@link Type#JID_SINGLE} field type.
         *
         * @return The JID or null, if the values are empty.
         */
        public Jid getValueAsJid() {
            return values.isEmpty() ? null : Jid.valueOf(values.get(0));
        }

        /**
         * Gets the media element.
         *
         * @return The media element.
         */
        public Media getMedia() {
            return media;
        }

        /**
         * Gets a natural-language description of the field, intended for presentation in a user-agent (e.g., as a "tool-tip", help button, or explanatory text provided near the field).
         *
         * @return The description.
         */
        public String getDescription() {
            return description;
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
         * If the field as required in order for the form to be considered valid.
         *
         * @return True, if the field is required.
         */
        public boolean isRequired() {
            return required != null;
        }

        @Override
        public int compareTo(Field o) {

            if (FORM_TYPE.equals(getVar()) && !FORM_TYPE.equals(o.getVar())) {
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
         * Defines field types.
         * <blockquote>
         * <p><cite><a href="http://xmpp.org/extensions/xep-0004.html#protocol-fieldtypes">3.3 Field Types</a></cite></p>
         * <p>The following field types represent data "types" that are commonly exchanged between Jabber/XMPP entities.</p>
         * </blockquote>
         */
        @XmlType(name = "field-type")
        public enum Type {
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

        /**
         * A builder class to build a data form field.
         */
        public static final class Builder {
            private final List<String> values = new ArrayList<>();

            private final List<Option> options = new ArrayList<>();

            private Type type;

            private String description;

            private boolean required;

            private Media media;

            private Validation validation;

            private String var;

            private String label;

            private Builder() {
            }

            /**
             * Sets the type of the field.
             *
             * @param type The field type.
             * @return The builder.
             */
            public Builder type(Type type) {
                this.type = type;
                return this;
            }

            /**
             * Sets if the field is required.
             *
             * @param required If the field is required.
             * @return The builder.
             */
            public Builder required(boolean required) {
                this.required = required;
                return this;
            }

            /**
             * Sets the description.
             *
             * @param description The description.
             * @return The builder.
             */
            public Builder description(String description) {
                this.description = description;
                return this;
            }

            /**
             * Sets the media element.
             *
             * @param media The media element.
             * @return The builder.
             */
            public Builder media(Media media) {
                this.media = media;
                return this;
            }

            /**
             * Sets the validation.
             *
             * @param validation The validation.
             * @return The builder.
             */
            public Builder validation(Validation validation) {
                this.validation = validation;
                return this;
            }

            /**
             * Sets the label.
             *
             * @param label The label.
             * @return The builder.
             */
            public Builder label(String label) {
                this.label = label;
                return this;
            }

            /**
             * Sets the var attribute.
             *
             * @param var The var attribute.
             * @return The builder.
             */
            public Builder var(String var) {
                this.var = var;
                return this;
            }

            /**
             * Sets the value as string.
             *
             * @param value The value.
             * @return The builder.
             */
            public Builder value(String value) {
                this.values.clear();
                this.values.add(value);
                return type(Type.TEXT_SINGLE);
            }

            /**
             * Sets the value as boolean. This methods sets the field type implicitly to {@link Field.Type#BOOLEAN}.
             *
             * @param value The value.
             * @return The builder.
             */
            public Builder value(boolean value) {
                value(value ? "1" : "0");
                return type(Type.BOOLEAN);
            }

            /**
             * Sets the value as integer. This methods sets the field type implicitly to {@link Field.Type#TEXT_SINGLE}.
             *
             * @param value The value.
             * @return The builder.
             */
            public Builder value(int value) {
                value(String.valueOf(value));
                return type(Type.TEXT_SINGLE);
            }

            /**
             * Sets the value as JID. This methods sets the field type implicitly to {@link Field.Type#JID_SINGLE}.
             *
             * @param value The value.
             * @return The builder.
             */
            public Builder value(Jid value) {
                if (value != null) {
                    value(value.toEscapedString());
                }
                return type(Type.JID_SINGLE);
            }

            /**
             * Sets the value as date. This methods sets the field type implicitly to {@link Field.Type#TEXT_SINGLE}.
             *
             * @param date The value.
             * @return The builder.
             */
            public Builder value(Date date) {
                if (date != null) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    value(DatatypeConverter.printDateTime(calendar));
                }
                return type(Type.TEXT_SINGLE);
            }

            /**
             * Sets the values. This methods sets the field type implicitly to {@link Field.Type#TEXT_MULTI}.
             *
             * @param values The values.
             * @return The builder.
             */
            public Builder values(Collection<String> values) {
                this.values.clear();
                if (values != null) {
                    this.values.addAll(values);
                }
                return type(Type.TEXT_MULTI);
            }

            /**
             * Sets the values from an enum. This methods sets the field type implicitly to {@link Field.Type#LIST_SINGLE}.
             *
             * @param values The values.
             * @return The builder.
             */
            public Builder valuesEnum(Collection<? extends Enum<?>> values) {
                this.values.clear();
                for (Enum<?> enumValue : values) {
                    this.values.add(enumValue.name().toLowerCase());
                }
                return type(Type.LIST_SINGLE);
            }

            /**
             * Sets the values as JIDs. This methods sets the field type implicitly to {@link Field.Type#JID_MULTI}.
             *
             * @param values The values.
             * @return The builder.
             */
            public Builder valuesJid(Collection<Jid> values) {
                this.values.clear();
                if (values != null) {
                    for (Jid value : values) {
                        this.values.add(value.toEscapedString());
                    }
                }
                return type(Type.JID_MULTI);
            }

            /**
             * Sets the options.
             *
             * @param options The options.
             * @return The builder.
             */
            public Builder options(Collection<Option> options) {
                this.options.clear();
                this.options.addAll(options);
                return this;
            }

            /**
             * Builds the field.
             *
             * @return The field.
             */
            public Field build() {
                return new Field(this);
            }
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

    /**
     * An abstract builder to build simple data forms.
     *
     * @param <T> The sub builder.
     */
    public abstract static class Builder<T extends Builder<T>> {
        private Collection<Field> fields;

        private Collection<Item> items;

        private String formType;

        private Type type;

        private String title;

        private Collection<String> instructions;

        private Collection<Page> pages;

        private Collection<Field> reportedFields;

        /**
         * Sets the fields. Fields are appended to the existing fields.
         *
         * @param fields The fields.
         * @return The builder.
         */
        public final T fields(Collection<Field> fields) {
            this.fields = fields;
            return self();
        }

        /**
         * Sets the form type.
         *
         * @param formType The form type.
         * @return The builder.
         */
        public final T formType(String formType) {
            this.formType = formType;
            return self();
        }

        /**
         * Sets the type of the form.
         *
         * @param type The data form type.
         * @return The builder.
         */
        public final T type(Type type) {
            this.type = type;
            return self();
        }

        public final T title(String title) {
            this.title = title;
            return self();
        }

        public final T instructions(Collection<String> instructions) {
            this.instructions = instructions;
            return self();
        }

        public final T pages(Collection<Page> pages) {
            this.pages = pages;
            return self();
        }

        public final T items(Collection<Item> items) {
            this.items = items;
            return self();
        }

        public final T reportedFields(Collection<Field> reportedFields) {
            this.reportedFields = reportedFields;
            return self();
        }

        /**
         * Returns an instance of the concrete builder.
         *
         * @return The concrete builder.
         */
        protected abstract T self();
    }
}
