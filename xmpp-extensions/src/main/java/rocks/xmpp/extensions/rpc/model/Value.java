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

package rocks.xmpp.extensions.rpc.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The value type, which is used by XML-RPC.
 *
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
    private final Object value;

    private Value() {
        this.value = null;
    }

    /**
     * Creates an integer value.
     *
     * @param integer The integer value.
     */
    public Value(Integer integer) {
        this.value = integer;
    }

    /**
     * Creates a string value.
     *
     * @param string The string value.
     */
    public Value(String string) {
        this.value = string;
    }

    /**
     * Creates a double value.
     *
     * @param d The double value.
     */
    public Value(Double d) {
        this.value = d;
    }

    /**
     * Creates a binary (base64) value.
     *
     * @param bytes The binary value.
     */
    public Value(byte[] bytes) {
        this.value = bytes;
    }

    /**
     * Creates a boolean value.
     *
     * @param b The boolean value.
     */
    public Value(Boolean b) {
        this.value = new NumericBoolean(b);
    }

    /**
     * Creates a date value.
     *
     * @param date The date value.
     */
    public Value(Date date) {
        this.value = date;
    }

    /**
     * Creates an array type value.
     *
     * @param list The array type value.
     */
    public Value(List<Value> list) {
        if (list != null) {
            ArrayType arrayType = new ArrayType();
            arrayType.values.addAll(list.stream().collect(Collectors.toList()));
            this.value = arrayType;
        } else {
            this.value = null;
        }
    }

    /**
     * Creates a struct type value.
     *
     * @param map The struct type value.
     */
    public Value(Map<String, Value> map) {
        if (map != null) {
            StructType structType = new StructType();
            structType.values.addAll(map.entrySet().stream().map(entry -> new StructType.MemberType(entry.getKey(), entry.getValue())).collect(Collectors.toList()));
            this.value = structType;
        } else {
            this.value = null;
        }
    }

    /**
     * Gets the value as integer or null.
     *
     * @return The integer or null.
     */
    public final Integer getAsInteger() {
        return value instanceof Integer ? (Integer) value : null;
    }

    /**
     * Gets the value as double or null.
     *
     * @return The double or null.
     */
    public final Double getAsDouble() {
        return value instanceof Double ? (Double) value : null;
    }

    /**
     * Gets the value as string or null.
     *
     * @return The string or null.
     */
    public final String getAsString() {
        return value instanceof String ? (String) value : null;
    }

    /**
     * Gets the value as byte array or null.
     *
     * @return The byte array or null.
     */
    public final byte[] getAsByteArray() {
        return value instanceof byte[] ? (byte[]) value : null;
    }

    /**
     * Gets the value as boolean or null.
     *
     * @return The boolean or null.
     */
    public final Boolean getAsBoolean() {
        return value instanceof NumericBoolean ? ((NumericBoolean) value).getAsBoolean() : null;
    }

    /**
     * Gets the value as date or null.
     *
     * @return The date or null.
     */
    public final Date getAsDate() {
        return value instanceof Date ? (Date) value : null;
    }

    /**
     * Gets the value as array or null.
     *
     * @return The array or null.
     */
    public final List<Value> getAsArray() {
        if (value instanceof ArrayType) {
            ArrayType arrayType = (ArrayType) value;
            return arrayType.values.stream().collect(Collectors.toList());
        }
        return null;
    }

    /**
     * Gets the value as map or null.
     *
     * @return The map or null.
     */
    public final Map<String, Value> getAsMap() {
        if (value instanceof StructType) {
            StructType structType = (StructType) value;
            Map<String, Value> result = new HashMap<>();
            for (StructType.MemberType member : structType.values) {
                result.put(member.name, member.value);
            }
            return result;
        }
        return null;
    }
}