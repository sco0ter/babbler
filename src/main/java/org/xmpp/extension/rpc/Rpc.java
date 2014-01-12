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

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "query")
@XmlAccessorType(XmlAccessType.FIELD)
public final class Rpc {

    @XmlElement(name = "methodCall")
    private MethodCall methodCall;

    @XmlElement(name = "methodResponse")
    private String methodResponse;

    public MethodCall getMethodCall() {
        return methodCall;
    }

    public static class MethodCall {

        @XmlElement(name = "methodName")
        private String methodName;

        @XmlElementWrapper(name = "params")
        @XmlElement(name = "param")
        private List<Parameter> parameters;

        public String getMethodName() {
            return methodName;
        }

        public List<Parameter> getParameters() {
            return parameters;
        }

        public static class Parameter {

            @XmlElement(name = "value")
            private Value value;

            public Value getValue() {
                return value;
            }
        }

        public static class Value {
            @XmlElements(value = {
                    @XmlElement(name = "i4", type = Integer.class),
                    @XmlElement(name = "int", type = Integer.class),
                    @XmlElement(name = "string", type = String.class),
                    @XmlElement(name = "double", type = Double.class),
                    @XmlElement(name = "base64", type = byte[].class),
                    @XmlElement(name = "boolean", type = Boolean.class),
                    @XmlElement(name = "dateTime.iso8601", type = Date.class),
                    @XmlElement(name = "array", type = ArrayList.class),
                    @XmlElement(name = "struct", type = ArrayList.class),
            })
            private Object value;
        }
    }
}
