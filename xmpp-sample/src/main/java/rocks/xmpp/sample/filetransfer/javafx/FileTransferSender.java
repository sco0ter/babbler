/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Christian Schudt
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

package rocks.xmpp.sample.filetransfer.javafx;

import java.io.File;
import java.time.Duration;

import javafx.application.Application;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.net.ChannelEncryption;
import rocks.xmpp.core.net.client.TcpConnectionConfiguration;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.session.XmppSessionConfiguration;
import rocks.xmpp.debug.gui.VisualDebugger;
import rocks.xmpp.extensions.filetransfer.FileTransfer;
import rocks.xmpp.extensions.filetransfer.FileTransferManager;

/**
 * @author Christian Schudt
 */
public class FileTransferSender extends Application {

    private final ObjectProperty<XmppSession> xmppSession = new SimpleObjectProperty<>();

    @Override
    public void start(final Stage primaryStage) {

        final Task<XmppSession> task = new Task<XmppSession>() {
            @Override
            protected XmppSession call() throws Exception {
                TcpConnectionConfiguration tcpConfiguration = TcpConnectionConfiguration.builder()
                        .port(5222)
                        .channelEncryption(ChannelEncryption.DISABLED)
                        .build();

                XmppSessionConfiguration configuration = XmppSessionConfiguration.builder()
                        .debugger(VisualDebugger.class)
                        .defaultResponseTimeout(Duration.ofSeconds(10))
                        .build();

                XmppClient xmppSession = XmppClient.create("localhost", configuration, tcpConfiguration);

                // Connect
                xmppSession.connect();
                // Login
                xmppSession.login("222", "222", "filetransfer");

                return xmppSession;
            }
        };
        task.stateProperty().addListener((observable, oldValue, newValue) -> {
            switch (newValue) {
                case SUCCEEDED:
                    xmppSession.set(task.getValue());
                    break;
                case FAILED:
                    task.getException().printStackTrace();
                    break;
                default:
                    break;
            }
        });

        // Login
        new Thread(task).start();

        Button button = new Button("Send file");
        button.disableProperty().bind(new BooleanBinding() {
            {
                super.bind(xmppSession);
            }

            @Override
            protected boolean computeValue() {
                return xmppSession.get() == null;
            }
        });

        final ProgressBar progressBar = new ProgressBar();
        final Label label = new Label();

        button.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            final File file = fileChooser.showOpenDialog(primaryStage);

            if (file != null) {
                final Task<Void> fileTransferTask = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        FileTransferManager fileTransferManager =
                                xmppSession.get().getManager(FileTransferManager.class);
                        updateMessage("Offering file... waiting for acceptance");
                        FileTransfer fileTransfer = fileTransferManager.offerFile(file, "Hello",
                                Jid.of("111", xmppSession.get().getDomain().toString(), "filetransfer"),
                                Duration.ofSeconds(10)).get();
                        fileTransfer.addFileTransferStatusListener(e -> {
                            System.out.println(e);
                            try {
                                Thread.sleep(100); // For visualization the progress only.
                            } catch (InterruptedException e1) {
                                Thread.currentThread().interrupt();
                            }
                            updateMessage(e.toString());
                            updateProgress(e.getBytesTransferred(), file.length());
                        });
                        fileTransfer.transfer();
                        return null;
                    }
                };

                fileTransferTask.stateProperty().addListener((observable, oldValue, newValue) -> {
                    switch (newValue) {
                        case FAILED:
                            Throwable e = fileTransferTask.getException();
                            e.printStackTrace();
                            break;
                        case SUCCEEDED:
                            break;
                        default:
                            break;
                    }
                });

                progressBar.progressProperty().bind(fileTransferTask.progressProperty());
                label.textProperty().bind(fileTransferTask.messageProperty());
                new Thread(fileTransferTask).start();
            }
        });

        Scene scene = new Scene(new VBox(button, progressBar, label));
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
