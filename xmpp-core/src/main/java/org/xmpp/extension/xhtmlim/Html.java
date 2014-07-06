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

package org.xmpp.extension.xhtmlim;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "html")
public final class Html {
    static final String NAMESPACE = "http://jabber.org/protocol/xhtml-im";

    @XmlElement(name = "body", namespace = "http://www.w3.org/1999/xhtml")
    private Object body;

    private Html() {
    }

    /**
     * Creates an empty HTML document with an empty body.
     * Use this constructor if you want to create your body element manually with DOM operations.
     *
     * @param document The document.
     * @see #getBody()
     */
    public Html(Document document) {
        this.body = document.createElement("body");
    }

    /**
     * Creates an HTML document from a string source (body).
     *
     * @param xhtmlContent The body element as string, e.g. {@code <p>Hi</p>}.
     * @throws SAXException If the input string could not be parsed.
     */
    public Html(String xhtmlContent) throws SAXException {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader("<body>" + xhtmlContent + "</body>")));
            this.body = document.getDocumentElement();
        } catch (ParserConfigurationException | IOException e) {
            // Should never occur with this setup, so don't let the API user deal with it.
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the body of this HTML document. Use this to append child nodes.
     *
     * @return The body.
     */
    public Element getBody() {
        return (Element) body;
    }

    public String getContent() {
        if (body != null) {
            try {
                TransformerFactory transFactory = TransformerFactory.newInstance();
                Transformer transformer = transFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

                StringWriter writer = new StringWriter();
                NodeList nodeList = ((Element) body).getChildNodes();
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    transformer.transform(new DOMSource(node), new StreamResult(writer));
                }
                return writer.toString();
            } catch (TransformerException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
