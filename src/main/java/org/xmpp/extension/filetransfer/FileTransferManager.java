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

package org.xmpp.extension.filetransfer;

import org.xmpp.Connection;
import org.xmpp.Jid;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.dataforms.DataForm;
import org.xmpp.extension.featurenegotiation.FeatureNegotiation;
import org.xmpp.extension.si.StreamInitiation;
import org.xmpp.stanza.IQ;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.Collection;

/**
 * @author Christian Schudt
 */
public final class FileTransferManager extends ExtensionManager {
    protected FileTransferManager(Connection connection) {
        super(connection);
    }

    public void sendFile(File file, Jid jid) {

        if (file == null) {
            throw new IllegalArgumentException("file must not be null.");
        }
        if (jid == null) {
            throw new IllegalArgumentException("jid must not be null.");
        }

        if (!file.exists()) {
            throw new IllegalArgumentException(new NoSuchFileException(file.getName()));
        }

        FileTransfer fileTransfer = new FileTransfer(file.getName(), file.length());

        DataForm dataForm = new DataForm(DataForm.Type.FORM);
        DataForm.Field field = new DataForm.Field(DataForm.Field.Type.LIST_SINGLE, "stream-method");
        field.getOptions().add(new DataForm.Option("http://jabber.org/protocol/ibb"));
        dataForm.getFields().add(field);

        FeatureNegotiation featureNegotiation = new FeatureNegotiation(dataForm);

        String mimeType;

        try {
            mimeType = Files.probeContentType(file.toPath());
        } catch (IOException e) {
            mimeType = null;
        }
        StreamInitiation streamInitiation = new StreamInitiation(FileTransfer.PROFILE, mimeType, fileTransfer, featureNegotiation);

        IQ iq = new IQ(jid, IQ.Type.SET, streamInitiation);

        connection.send(iq);
    }

    @Override
    protected Collection<String> getFeatureNamespaces() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
