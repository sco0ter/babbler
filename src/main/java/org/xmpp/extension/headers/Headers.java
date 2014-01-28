package org.xmpp.extension.headers;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "headers")
public final class Headers {

    @XmlElement(name = "header")
    private List<Header> headers = new ArrayList<>();

    public Headers(List<Header> headers) {
        this.headers = headers;
    }

    /**
     * Gets the headers.
     *
     * @return The headers.
     */
    public List<Header> getHeaders() {
        return headers;
    }
}
