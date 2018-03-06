/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2019 Christian Schudt
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

package rocks.xmpp.im.roster.server;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.im.roster.model.RosterItem;
import rocks.xmpp.im.roster.server.spi.RosterItemProvider;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;
import javax.interceptor.Interceptor;
import java.util.List;

/**
 * @author Christian Schudt
 */
@Priority(Interceptor.Priority.LIBRARY_BEFORE)
@Dependent
public class DefaultRosterItemProvider implements RosterItemProvider {

    @Override
    public void create(String username, RosterItem rosterItem) {

    }

    @Override
    public void update(String username, RosterItem rosterItem) {

    }

    @Override
    public RosterItem delete(String username, Jid jid) {
        return null;
    }

    @Override
    public RosterItem get(long id) {
        return null;
    }

    @Override
    public RosterItem get(String username, Jid jid) {
        return null;
    }

    @Override
    public List<? extends RosterItem> getRosterItems(String username) {
        return null;
    }

    @Override
    public long getItemCount(String username) {
        return 0;
    }
}
