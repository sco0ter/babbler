/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Christian Schudt
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

import javafx.application.Application;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import rocks.xmpp.core.Jid;
import rocks.xmpp.core.session.TcpConnectionConfiguration;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.session.XmppSessionConfiguration;
import rocks.xmpp.core.stanza.model.client.Presence;
import rocks.xmpp.debug.gui.VisualDebugger;
import rocks.xmpp.extensions.filetransfer.FileTransfer;
import rocks.xmpp.extensions.filetransfer.FileTransferManager;
import rocks.xmpp.extensions.filetransfer.FileTransferStatusEvent;
import rocks.xmpp.extensions.filetransfer.FileTransferStatusListener;

import java.io.File;

/**
 * @author Christian Schudt
 */
public class FileTransferSender extends Application {

    private ObjectProperty<XmppSession> xmppSession = new SimpleObjectProperty<>();

    @Override
    public void start(final Stage primaryStage) throws Exception {

        final Task<XmppSession> task = new Task<XmppSession>() {
            @Override
            protected XmppSession call() throws Exception {
                TcpConnectionConfiguration tcpConfiguration = TcpConnectionConfiguration.builder()
                        .port(5222)
                        .secure(false)
                        .build();

                XmppSessionConfiguration configuration = XmppSessionConfiguration.builder()
                        .debugger(VisualDebugger.class)
                        .defaultResponseTimeout(10000)
                        .build();

                XmppSession xmppSession = new XmppSession("localhost", configuration, tcpConfiguration);

                // Connect
                xmppSession.connect();
                // Login
                xmppSession.login("111", "111", "filetransfer");
                // Send initial presence
                xmppSession.send(new Presence());

                return xmppSession;
            }
        };
        task.stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
                switch (newValue) {
                    case SUCCEEDED:
                        xmppSession.set(task.getValue());
                        break;
                    case FAILED:
                        task.getException().printStackTrace();
                        break;
                }
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

        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                final File file = fileChooser.showOpenDialog(primaryStage);

                if (file != null) {
                    final Task<Void> fileTransferTask = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            FileTransferManager fileTransferManager = xmppSession.get().getExtensionManager(FileTransferManager.class);
                            updateMessage("Offering file... waiting for acceptance");
                            FileTransfer fileTransfer = fileTransferManager.offerFile(file, "Hello", new Jid("222", xmppSession.get().getDomain(), "filetransfer"), 10000);
                            fileTransfer.addFileTransferStatusListener(new FileTransferStatusListener() {
                                @Override
                                public void fileTransferStatusChanged(FileTransferStatusEvent e) {
                                    System.out.println(e);
                                    try {
                                        Thread.sleep(100); // For visualization the progress only.
                                    } catch (InterruptedException e1) {
                                        e1.printStackTrace();
                                    }
                                    updateMessage(e.toString());
                                    updateProgress(e.getBytesTransferred(), file.length());
                                }
                            });
                            fileTransfer.transfer();
                            return null;
                        }
                    };

                    fileTransferTask.stateProperty().addListener(new ChangeListener<Worker.State>() {
                        @Override
                        public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
                            switch (newValue) {
                                case FAILED:
                                    Throwable e = fileTransferTask.getException();
                                    e.printStackTrace();
                                    break;
                                case SUCCEEDED:
                                    break;
                            }
                        }
                    });

                    progressBar.progressProperty().bind(fileTransferTask.progressProperty());
                    label.textProperty().bind(fileTransferTask.messageProperty());
                    new Thread(fileTransferTask).start();
                }
            }
        });

        Scene scene = new Scene(new VBox(button, progressBar, label));
        primaryStage.setScene(scene);
        primaryStage.show();
    }

//    private class FileTransferService extends Service<Void> {
//        @Override
//        protected Task<Void> createTask() {
//            return new Task<Void>() {
//                @Override
//                protected Void call() throws Exception {
//                    FileTransferManager fileTransferManager = xmppSession.get().getExtensionManager(FileTransferManager.class);
//                    try {
//                        updateMessage("Offering file... waiting for acceptance");
//                        FileTransfer fileTransfer = fileTransferManager.offerFile(file, "Hello", new Jid("222", xmppSession.get().getDomain(), "filetransfer"), 10000);
//                        fileTransfer.addFileTransferStatusListener(new FileTransferStatusListener() {
//                            @Override
//                            public void fileTransferStatusChanged(FileTransferStatusEvent e) {
//                                System.out.println(e);
//                                try {
//                                    Thread.sleep(100);
//                                } catch (InterruptedException e1) {
//                                    e1.printStackTrace();
//                                }
//                                updateMessage(e.toString());
//                                updateProgress(e.getBytesTransferred(), file.length());
//                            }
//                        });
//                        fileTransfer.transfer();
//                    } catch (FileTransferRejectedException e) {
//                        updateMessage("File transfer offer has been rejected.");
//                    } catch (IOException | XmppException e) {
//                        updateMessage(e.getMessage());
//                    }
//
//                    return null;
//                }
//            };
//        }
//    }
}
