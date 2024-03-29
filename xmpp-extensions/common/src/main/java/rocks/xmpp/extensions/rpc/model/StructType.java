/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Christian Schudt
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Christian Schudt
 */
final class StructType {

    final List<MemberType> member = new ArrayList<>();

    @Override
    public final boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof StructType)) {
            return false;
        }
        StructType other = (StructType) o;

        return Objects.equals(member, other.member);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(member);
    }

    @Override
    public final String toString() {
        return '{' + member.stream().map(MemberType::toString).collect(Collectors.joining(", ")) + '}';
    }

    static final class MemberType {

        final String name;

        final Value value;

        private MemberType() {
            this(null, null);
        }

        MemberType(String name, Value value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public final boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof MemberType)) {
                return false;
            }
            MemberType other = (MemberType) o;

            return Objects.equals(name, other.name)
                    && Objects.equals(value, other.value);
        }

        @Override
        public final int hashCode() {
            return Objects.hash(name, value);
        }

        @Override
        public final String toString() {
            return '\'' + name + "': " + value;
        }
    }
}
