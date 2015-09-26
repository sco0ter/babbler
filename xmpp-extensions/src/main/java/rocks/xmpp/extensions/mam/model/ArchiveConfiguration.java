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

package rocks.xmpp.extensions.mam.model;

/**
 * @author Christian Schudt
 */
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

import rocks.xmpp.addr.Jid;
import rocks.xmpp.extensions.data.model.DataForm;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Collection;

/**
 * Represents a standardized {@link rocks.xmpp.extensions.data.model.DataForm} with form type {@code http://jabber.org/protocol/muc#roomconfig}, which can be used to configure a MUC room.
 * <h3>Usage</h3>
 * To wrap an existing {@link rocks.xmpp.extensions.data.model.DataForm} to retrieve standard data from it, use:
 * <pre>
 * {@code
 * RoomConfiguration roomConfiguration = new RoomConfiguration(dataForm);
 * }
 * </pre>
 * To build a form:
 * <pre>
 * {@code
 * RoomConfiguration roomConfiguration = RoomConfiguration.builder()
 *     .maxHistoryMessages(4)
 *     .rolesThatMaySendPrivateMessages(Arrays.asList(Role.MODERATOR, Role.PARTICIPANT))
 *     .invitesAllowed(true)
 *     .changeSubjectAllowed(true)
 *     .loggingEnabled(true)
 *     .rolesThatMayRetrieveMemberList(Collections.singleton(Role.PARTICIPANT))
 *     .language("en")
 *     .pubSubNode(URI.create("xmpp:pubsub.shakespeare.lit?;node=princely_musings"))
 *     .maxUsers(30)
 *     .membersOnly(true)
 *     .moderated(true)
 *     .passwordProtected(true)
 *     .persistent(true)
 *     .rolesForWhichPresenceIsBroadcast(Arrays.asList(Role.MODERATOR, Role.PARTICIPANT))
 *     .publicRoom(true)
 *     .administrators(Arrays.asList(Jid.of("admin1"), Jid.of("admin2")))
 *     .description("description")
 *     .name("name")
 *     .owners(Arrays.asList(Jid.of("owner1"), Jid.of("owner2")))
 *     .password("pass")
 *     .rolesThatMayDiscoverRealJids(EnumSet.of(Role.MODERATOR))
 *     .build();
 * }
 * </pre>
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0045.html#createroom-reserved">10.1.3 Creating a Reserved Room</a>
 * @see <a href="http://xmpp.org/extensions/xep-0045.html#registrar-formtype-owner">15.5.3 muc#roomconfig FORM_TYPE</a>
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
         * @see <a href="http://www.xmpp.org/extensions/xep-0313.html#filter-jid">4.1.1 Filtering by JID</a>
         */
        public final Builder with(Jid with) {
            this.with = with;
            return this;
        }

        /**
         * @param start The start date.
         * @return The builder.
         * @see <a href="http://www.xmpp.org/extensions/xep-0313.html#filter-time">4.1.2 Filtering by time received</a>
         */
        public final Builder start(Instant start) {
            this.start = start;
            return this;
        }

        /**
         * @param end The end date.
         * @return The builder.
         * @see <a href="http://www.xmpp.org/extensions/xep-0313.html#filter-time">4.1.2 Filtering by time received</a>
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


