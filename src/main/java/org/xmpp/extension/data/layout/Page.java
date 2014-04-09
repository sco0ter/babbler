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

package org.xmpp.extension.data.layout;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * A page is the top-level layout container for data forms. It may contain sections, which partition the page into smaller parts.
 * <p>
 * Each page in a data form contains references to a field in the data form, in order to know which fields should be displayed on this page.
 * </p>
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0141.html#paging">3.1 Paging Fields</a>
 * @see Section
 * @see FieldReference
 * @see org.xmpp.extension.data.DataForm#getPages()
 */
@XmlRootElement(name = "page")
public final class Page {

    @XmlAttribute(name = "label")
    private String label;

    @XmlElement(name = "text")
    private List<String> text;

    @XmlElement(name = "fieldref")
    private List<FieldReference> fieldReferences;

    @XmlElement(name = "section")
    private List<Section> sections;

    @XmlElement(name = "reportedref")
    private FieldReference reportedReference;

    private Page() {
    }

    /**
     * Creates a page.
     *
     * @param label           The label.
     * @param fieldReferences The field references.
     */
    public Page(String label, List<FieldReference> fieldReferences) {
        this.label = label;
        this.fieldReferences = fieldReferences;
    }

    /**
     * Creates a page.
     *
     * @param label           The label.
     * @param fieldReferences The field references.
     * @param text            The text.
     */
    public Page(String label, List<FieldReference> fieldReferences, List<String> text) {
        this.label = label;
        this.text = text;
        this.fieldReferences = fieldReferences;
    }

    /**
     * Creates a page.
     *
     * @param label             The label.
     * @param fieldReferences   The field references.
     * @param text              The text.
     * @param reportedReference The reference to a reported field.
     */
    public Page(String label, List<FieldReference> fieldReferences, List<String> text, FieldReference reportedReference) {
        this.label = label;
        this.text = text;
        this.fieldReferences = fieldReferences;
        this.reportedReference = reportedReference;
    }

    /**
     * Gets additional information for the page.
     *
     * @return Additional information.
     */
    public List<String> getText() {
        return text;
    }

    /**
     * Gets the field references. These are the fields, which appear on this page.
     *
     * @return The field references.
     */
    public List<FieldReference> getFieldReferences() {
        return fieldReferences;
    }

    /**
     * Gets the sections for this page.
     *
     * @return The sections.
     */
    public List<Section> getSections() {
        return sections;
    }

    /**
     * Gets the reported field reference.
     *
     * @return The reported field reference.
     */
    public FieldReference getReportedReference() {
        return reportedReference;
    }
}
