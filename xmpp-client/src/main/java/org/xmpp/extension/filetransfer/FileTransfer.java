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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * A class for managing a single file transfer.
 * It allows to monitor
 *
 * @author Christian Schudt
 */
public final class FileTransfer {

    private final long length;

    private final InputStream inputStream;

    private final OutputStream outputStream;

    private final Set<FileTransferStatusListener> fileTransferStatusListeners = new CopyOnWriteArraySet<>();

    private volatile Status status;

    private volatile Exception exception;

    private volatile long bytesTransferred;

    public FileTransfer(InputStream inputStream, OutputStream outputStream, long length) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.length = length;
    }

    /**
     * Adds a file transfer status listener, which allows to listen for file transfer status changes.
     *
     * @param fileTransferStatusListener The listener.
     * @see #removeFileTransferStatusListener(FileTransferStatusListener)
     */
    public void addFileTransferStatusListener(FileTransferStatusListener fileTransferStatusListener) {
        fileTransferStatusListeners.add(fileTransferStatusListener);
    }

    /**
     * Removes a previously added file transfer status listener.
     *
     * @param fileTransferStatusListener The listener.
     * @see #addFileTransferStatusListener(FileTransferStatusListener)
     */
    public void removeFileTransferStatusListener(FileTransferStatusListener fileTransferStatusListener) {
        fileTransferStatusListeners.remove(fileTransferStatusListener);
    }

    /**
     * Gets the status of the file transfer.
     *
     * @return The status.
     */
    public Status getStatus() {
        return status;
    }

    private void updateStatus(Status status) {
        if (this.status != status) {
            this.status = status;
            for (FileTransferStatusListener fileTransferStatusListener : fileTransferStatusListeners) {
                try {
                    fileTransferStatusListener.fileTransferStatusChanged(new FileTransferStatusEvent(this, status));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public boolean isDone() {
        return status != Status.IN_PROGRESS;
    }

    /**
     * Gets the transferred bytes.
     *
     * @return The transferred bytes.
     */
    public long getBytesTransferred() {
        return bytesTransferred;
    }

    /**
     * Gets the progress of the file transfer.
     *
     * @return A value between 0 and 1 indicating the progress or -1 if the progress is unknown.
     */
    public double getProgress() {
        if (length != 0) {
            return (double) getBytesTransferred() / length;
        }
        return -1;
    }

    /**
     * Transfers the file in its own thread.
     */
    public void transfer() {

        Thread thread = new Thread() {
            @Override
            public void run() {
                byte[] buffer = new byte[1024];
                int len;
                bytesTransferred = 0;

                updateStatus(Status.IN_PROGRESS);

                try {
                    while ((len = inputStream.read(buffer)) > -1 && status != Status.CANCELED) {
                        outputStream.write(buffer, 0, len);
                        bytesTransferred += len;
                    }

                    if (bytesTransferred != length) {
                        updateStatus(Status.FAILED);
                    }
                } catch (IOException e) {
                    exception = e;
                    updateStatus(Status.FAILED);
                } finally {
                    // Close the stream
                    try {
                        inputStream.close();
                        outputStream.close();
                    } catch (IOException e) {
                        exception = e;
                        updateStatus(Status.FAILED);
                    } finally {
                        if (status == Status.IN_PROGRESS) {
                            updateStatus(Status.COMPLETED);
                        }
                    }
                }
            }
        };
        thread.start();
    }

    public void cancel() {
        updateStatus(Status.CANCELED);
    }

    /**
     * Gets the exception if the status is {@link Status#FAILED}
     *
     * @return The exception or null.
     */
    public Exception getException() {
        return exception;
    }

    public enum Status {
        CANCELED,
        COMPLETED,
        FAILED,
        IN_PROGRESS
    }
}
