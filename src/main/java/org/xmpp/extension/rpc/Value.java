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

package org.xmpp.extension.rpc;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "value")
public final class Value {

    @XmlElements(value = {
            @XmlElement(name = "i4", type = Integer.class),
            @XmlElement(name = "int", type = Integer.class),
            @XmlElement(name = "string", type = String.class),
            @XmlElement(name = "double", type = Double.class),
            @XmlElement(name = "base64", type = byte[].class),
            @XmlElement(name = "boolean", type = NumericBoolean.class),
            @XmlElement(name = "dateTime.iso8601", type = Date.class),
            @XmlElement(name = "array", type = ArrayType.class),
            @XmlElement(name = "struct", type = StructType.class)
    })
    private Object value;

    private Value() {
    }

    public Value(Integer integer) {
        this.value = integer;
    }

    public Value(String string) {
        this.value = string;
    }

    public Value(Double d) {
        this.value = d;
    }

    public Value(byte[] bytes) {
        this.value = bytes;
    }

    public Value(Boolean b) {
        this.value = new NumericBoolean(b);
    }

    public Value(Date date) {
        this.value = date;
    }

    public Value(List<Value> list) {
        if (list != null) {
            ArrayType arrayType = new ArrayType();
            for (Value value : list) {
                arrayType.values.add(value);
            }
            this.value = arrayType;
        }
    }

    public Value(Map<String, Value> map) {
        if (map != null) {
            StructType structType = new StructType();
            for (Map.Entry<String, Value> entry : map.entrySet()) {
                structType.values.add(new StructType.MemberType(entry.getKey(), entry.getValue()));
            }
            this.value = structType;
        }
    }

    public Integer getAsInteger() {
        return value instanceof Integer ? (Integer) value : null;
    }

    public Double getAsDouble() {
        return value instanceof Double ? (Double) value : null;
    }

    public String getAsString() {
        return value instanceof String ? (String) value : null;
    }

    public byte[] getAsByteArray() {
        return value instanceof byte[] ? (byte[]) value : null;
    }

    public Boolean getAsBoolean() {
        return value instanceof NumericBoolean ? ((NumericBoolean) value).getAsBoolean() : null;
    }

    public Date getAsDate() {
        return value instanceof Date ? (Date) value : null;
    }

    public List<Value> getAsArray() {
        if (value instanceof ArrayType) {
            ArrayType arrayType = (ArrayType) value;
            List<Value> result = new ArrayList<>();
            if (arrayType.values != null) {
                for (Value value : arrayType.values) {
                    result.add(value);
                }
            }
            return result;
        }
        return null;
    }

    public Map<String, Value> getAsMap() {
        if (value instanceof StructType) {
            StructType structType = (StructType) value;
            Map<String, Value> result = new HashMap<>();
            if (structType.values != null) {
                for (StructType.MemberType member : structType.values) {
                    result.put(member.name, member.value);
                }
            }
            return result;
        }
        return null;
    }
}