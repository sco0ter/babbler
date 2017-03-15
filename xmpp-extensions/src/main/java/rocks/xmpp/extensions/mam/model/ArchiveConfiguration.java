/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 Christian Schudt
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

package rocks.xmpp.extensions.mam.model;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.extensions.data.model.DataForm;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Collection;

/**
 * Represents a standardized {@link rocks.xmpp.extensions.data.model.DataForm} with form type {@code urn:xmpp:mam:1}, which can be used to query a message archive.
 * <h3>Usage</h3>
 * <pre>
 * {@code
 * ArchiveConfiguration configuration = ArchiveConfiguration.builder()
 *      .with(Jid.of("juliet@capulet.lit"))
 *      .start(start)
 *      .end(end)
 *      .build();
 * }
 * </pre>
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0313.html#filter">4.1 Filtering results</a>
 */
public final class ArchiveConfiguration {

    private static final String FORM_TYPE = MessageArchive.NAMESPACE;

    private static final String START = "start";

    private static final String END = "end";

    private static final String WITH = "with";

    private final DataForm dataForm;

    public ArchiveConfiguration(DataForm dataForm) {
        this.dataForm = dataForm;
    }

    public static Builder builder() {
        return new Builder();
    }

    public final Jid getWith() {
        return dataForm.findValueAsJid(WITH);
    }

    public final Instant getStart() {
        return dataForm.findValueAsInstant(START);
    }

    public final Instant getEnd() {
        return dataForm.findValueAsInstant(END);
    }

    /**
     * Gets the underlying form.
     *
     * @return The underlying data form.
     */
    public DataForm getDataForm() {
        return dataForm;
    }

    /**
     * A builder to build a room configuration. The form is of type {@link rocks.xmpp.extensions.data.model.DataForm.Type#SUBMIT} by default.
     */
    public static final class Builder extends DataForm.Builder<Builder> {

        private Jid with;

        private Instant start;

        private Instant end;

        private Builder() {
        }

        /**
         * @param with The JID against which to match messages.
         * @return The builder.
         * @see <a href="http://xmpp.org/extensions/xep-0313.html#filter-jid">4.1.1 Filtering by JID</a>
         */
        public final Builder with(Jid with) {
            this.with = with;
            return this;
        }

        /**
         * If specified, a server MUST only return messages whose timestamp is equal to or later than the given timestamp.
         * If omitted, the server SHOULD assume the value of 'start' to be equal to the date/time of the earliest message stored in the archive.
         *
         * @param start The start date.
         * @return The builder.
         * @see <a href="http://xmpp.org/extensions/xep-0313.html#filter-time">4.1.2 Filtering by time received</a>
         */
        public final Builder start(Instant start) {
            this.start = start;
            return this;
        }

        /**
         * The 'end' field is used to exclude from the results messages after a certain point in time.
         * If omitted, the server SHOULD assume the value of 'end' to be equal to the date/time of the most recent message stored in the archive.
         *
         * @param end The end date.
         * @return The builder.
         * @see <a href="http://xmpp.org/extensions/xep-0313.html#filter-time">4.1.2 Filtering by time received</a>
         */
        public final Builder end(Instant end) {
            this.end = end;
            return this;
        }

        @Override
        protected final Builder self() {
            return this;
        }

        /**
         * Builds the room configuration.
         *
         * @return The room configuration.
         */
        public final ArchiveConfiguration build() {
            Collection<DataForm.Field> fields = new ArrayDeque<>();
            if (with != null) {
                fields.add(DataForm.Field.builder().var(WITH).value(with).type(DataForm.Field.Type.JID_SINGLE).build());
            }
            if (start != null) {
                fields.add(DataForm.Field.builder().var(START).value(start).type(DataForm.Field.Type.TEXT_SINGLE).build());
            }
            if (end != null) {
                fields.add(DataForm.Field.builder().var(END).value(end).type(DataForm.Field.Type.TEXT_SINGLE).build());
            }
            fields(fields).formType(FORM_TYPE).type(DataForm.Type.SUBMIT);
            return new ArchiveConfiguration(new DataForm(this));
        }
    }
}


