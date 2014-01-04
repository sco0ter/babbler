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

package org.xmpp.extension.servicediscovery;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Christian Schudt
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public final class Feature implements Comparable<Feature> {

    /**
     * Each {@code <feature/>} element MUST possess a 'var' attribute whose value is a protocol namespace or other feature offered by the entity.
     */
    @XmlAttribute(name = "var")
    private String var;

    private Feature() {
    }

    public Feature(String var) {
        this.var = var;
    }

    public String getVar() {
        return var;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Feature)) {
            return false;
        }
        Feature other = (Feature) o;
        return var == null ? other.var == null : var.equals(other.var);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + ((var == null) ? 0 : var.hashCode());
        return result;

    }

    @Override
    public int compareTo(Feature o) {
        int result;
        if (o == null) {
            result = 1;
        } else {
            if (getVar() == null && o.getVar() == null) {
                result = 0;
            } else if (getVar() == null) {
                result = -1;
            } else if (getVar() == null) {
                result = 1;
            } else {
                result = getVar().compareTo(o.getVar());
            }
        }
        return result;
    }
}
