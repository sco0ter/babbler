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

package org.xmpp.extension.activity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "activity")
@XmlSeeAlso({Category.class})
public final class Activity {

    @XmlElements({
            @XmlElement(name = "doing_chores", type = Category.DoingChores.class),
            @XmlElement(name = "drinking", type = Category.Drinking.class),
            @XmlElement(name = "eating", type = Category.Eating.class),
            @XmlElement(name = "exercising", type = Category.Exercising.class),
            @XmlElement(name = "grooming", type = Category.Grooming.class),
            @XmlElement(name = "having_appointment", type = Category.HavingAppointment.class),
            @XmlElement(name = "inactive", type = Category.Inactive.class),
            @XmlElement(name = "relaxing", type = Category.Relaxing.class),
            @XmlElement(name = "talking", type = Category.Talking.class),
            @XmlElement(name = "traveling", type = Category.Traveling.class),
            @XmlElement(name = "undefined", type = Category.Undefined.class),
            @XmlElement(name = "working", type = Category.Working.class)
    })
    private Category category;

    @XmlElement(name = "text")
    private String text;

    public Activity() {

    }

    public String getText() {
        return text;
    }

    public Category getCategory() {
        return category;
    }
}
