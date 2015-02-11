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

import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.session.XmppSessionConfiguration;
import rocks.xmpp.extensions.httpbind.BoshConnectionConfiguration;

/**
 * @author Christian Schudt
 */
public class BoshTest {

    public static void main(String args[]) {

        XmppSessionConfiguration configuration = XmppSessionConfiguration.builder()
                //.debugger(VisualDebugger.class)
                .build();
        XmppSessionConfiguration.setDefault(configuration);

        BoshConnectionConfiguration boshConnectionConfiguration = BoshConnectionConfiguration.builder()
                .hostname("localhost")
                .port(7070)
                .file("/http-bind/")
                //.useKeySequence(true)
                .build();

        long start = System.currentTimeMillis();

        for (int i = 0; i < 500; i++) {
            XmppSession xmppSession = new XmppSession("christihudtsmbp.fritz.box", boshConnectionConfiguration);
            System.out.println(i);
            try {
                xmppSession.connect();
                xmppSession.login("admin", "admin", null);
                //xmppSession.send(new Presence());
                //xmppSession.getRosterManager().requestRoster();
                xmppSession.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println(System.currentTimeMillis() - start);
    }

}
