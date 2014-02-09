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

package org.xmpp.extension.stream.filetransfer;

import org.xmpp.Connection;
import org.xmpp.extension.dataforms.DataForm;
import org.xmpp.extension.featurenegotiation.FeatureNegotiation;
import org.xmpp.extension.stream.ibb.InBandBytestreamManager;
import org.xmpp.extension.stream.initiation.StreamInitiation;
import org.xmpp.stanza.IQ;
import org.xmpp.stanza.Stanza;

import java.util.EventObject;

/**
 * @author Christian Schudt
 */
public final class FileTransferRequestEvent extends EventObject {

    private final FileTransfer fileTransfer;

    private final Connection connection;

    private final IQ iq;

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    FileTransferRequestEvent(Object source, Connection connection, IQ iq, FileTransfer fileTransfer) {
        super(source);
        this.connection = connection;
        this.iq = iq;
        this.fileTransfer = fileTransfer;
    }

    public FileTransfer getFileTransfer() {
        return fileTransfer;
    }

    public void accept() {
        DataForm dataForm = new DataForm(DataForm.Type.SUBMIT);
        DataForm.Field field = new DataForm.Field(DataForm.Field.Type.TEXT_SINGLE, "stream-method");
        field.getValues().add(InBandBytestreamManager.NAMESPACE);
        dataForm.getFields().add(field);
        FeatureNegotiation featureNegotiation = new FeatureNegotiation(dataForm);
        StreamInitiation streamInitiation = new StreamInitiation(featureNegotiation);
        IQ result = iq.createResult();
        result.setExtension(streamInitiation);
        connection.send(result);
    }

    public void reject() {
        connection.send(iq.createError(new Stanza.Error(new Stanza.Error.Forbidden())));
    }
}
