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

package org.xmpp.extension.muc;

import org.xmpp.Jid;
import org.xmpp.XmppException;
import org.xmpp.extension.data.DataForm;
import org.xmpp.stanza.Message;
import org.xmpp.stanza.Presence;

/**
 * @author Christian Schudt
 */
public interface MucRoom {

    void join(String nick) throws XmppException;

    void join(String nick, String password) throws XmppException;

    void join(String nick, History history) throws XmppException;

    void join(String nick, String password, History history) throws XmppException;

    void addSubjectChangeListener(SubjectChangeListener subjectChangeListener);

    void removeSubjectChangeListener(SubjectChangeListener subjectChangeListener);

    void changeSubject(String subject);

    void sendMessage(String message);

    void sendMessage(Message message);

    void sendPrivateMessage(String message, String nick);

    void sendPrivateMessage(Message message, String nick);

    void changeNickname(String newNickname) throws XmppException;

    void changeAvailabilityStatus(Presence.Show show, String status);

    void invite(Jid invitee, String reason);

    DataForm getRegistrationForm() throws XmppException;

    void submitRegistrationForm() throws XmppException;

    void requestVoice();

    void exit();

    void exit(String message);

    void kickOccupant(String nickname, String reason) throws XmppException;

    void banUser(Jid user, String reason) throws XmppException;
}
