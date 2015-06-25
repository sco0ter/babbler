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

package rocks.xmpp.util;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.EventObject;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class with static factory methods.
 *
 * @author Christian Schudt
 */
public final class XmppUtils {

    private static final Logger logger = Logger.getLogger(XmppUtils.class.getName());

    private XmppUtils() {
    }

    /**
     * Creates a {@link javax.xml.stream.XMLStreamWriter} instance, which writes XML without namespace prefixes.
     * <p><b>Sample usage:</b></p>
     * <pre>
     * {@code
     * Writer writer = new StringWriter();
     *
     * XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newFactory().createXMLStreamWriter(writer);
     * XMLStreamWriter xmppStreamWriter = XmppUtils.createXmppStreamWriter(xmlStreamWriter, true);
     *
     * JAXBContext jaxbContext = JAXBContext.newInstance(Message.class, Sent.class);
     * Marshaller marshaller = jaxbContext.createMarshaller();
     * marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
     *
     * Message forwardedMessage = new Message(Jid.valueOf("romeo@example.net"), Message.Type.CHAT, "Hi!!");
     *
     * Message message = new Message(Jid.valueOf("juliet@example.net"));
     * message.getExtensions().add(new Sent(new Forwarded(forwardedMessage)));
     *
     * marshaller.marshal(message, xmppStreamWriter);
     *
     * System.out.println(writer.toString());
     * }
     * </pre>
     * The output of this is:
     * <pre>
     * {@code
     * <message to="juliet@example.net">
     *     <sent xmlns="urn:xmpp:carbons:2">
     *         <forwarded xmlns="urn:xmpp:forward:0">
     *             <message xmlns="jabber:client" to="romeo@example.net" type="chat">
     *                 <body>Hi!!</body>
     *             </message>
     *         </forwarded>
     *     </sent>
     * </message>
     * }
     * </pre>
     *
     * @param xmlStreamWriter  The underlying XML stream writer.
     * @param contentNamespace The content namspace, e.g. "jabber:client".
     * @return The prefix-free canonicalization writer.
     * @throws XMLStreamException Thrown by {@link javax.xml.stream.XMLStreamWriter}.
     */
    public static XMLStreamWriter createXmppStreamWriter(XMLStreamWriter xmlStreamWriter, String contentNamespace) throws XMLStreamException {
        return new PrefixFreeCanonicalizationWriter(xmlStreamWriter, contentNamespace);
    }

    /**
     * Creates a {@link javax.xml.stream.XMLStreamWriter} instance, which writes XML without namespace prefixes.
     * <p>
     * The content namespace is "jabber:client".
     *
     * @param xmlStreamWriter The underlying XML stream writer.
     * @return The prefix-free canonicalization writer.
     * @throws XMLStreamException Thrown by {@link javax.xml.stream.XMLStreamWriter}.
     */
    public static XMLStreamWriter createXmppStreamWriter(XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        return createXmppStreamWriter(xmlStreamWriter, "jabber:client");
    }

    /**
     * Creates an branched {@link java.io.InputStream}, which means that everything read by the source stream is written to the target {@link java.io.OutputStream}.
     * <p>This is useful for reading the XMPP stream and writing the inbound XMPP traffic to an {@link java.io.OutputStream}.</p>
     *
     * @param source The source stream.
     * @param target The target stream.
     * @return The branched input stream.
     */
    public static InputStream createBranchedInputStream(InputStream source, OutputStream target) {
        return new BranchedInputStream(source, target);
    }

    /**
     * Creates a branched {@link java.io.OutputStream}, which means that everything written to the original stream is also written to the branched stream.
     * <p>This is useful for writing the outbound XMPP traffic to another stream.</p>
     *
     * @param out    The original stream.
     * @param branch The branched stream.
     * @return The branched output stream.
     */
    public static OutputStream createBranchedOutputStream(OutputStream out, OutputStream branch) {
        return new BranchedOutputStream(out, branch);
    }

    /**
     * Creates a hex encoded SHA-1 hash.
     *
     * @param bytes The data.
     * @return The hash.
     */
    public static String hash(byte[] bytes) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            messageDigest.reset();
            return String.format("%040x", new BigInteger(1, messageDigest.digest(bytes)));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a thread factory which creates named daemon threads.
     *
     * @param threadName The thread name.
     * @return The thread factory.
     */
    public static ThreadFactory createNamedThreadFactory(final String threadName) {
        return r -> {
            Thread thread = new Thread(r, threadName);
            thread.setDaemon(true);
            return thread;
        };
    }

    /**
     * Invokes listeners in a fail-safe way.
     *
     * @param eventListeners The event listeners.
     * @param e              The event object.
     * @param <T>            The event object type.
     */
    public static <T extends EventObject> void notifyEventListeners(Iterable<Consumer<T>> eventListeners, T e) {
        eventListeners.forEach(listener -> {
            try {
                listener.accept(e);
            } catch (Exception ex) {
                logger.log(Level.WARNING, ex.getMessage(), ex);
            }
        });
    }
}
