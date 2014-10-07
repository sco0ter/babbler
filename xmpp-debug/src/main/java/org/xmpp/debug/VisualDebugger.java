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

package org.xmpp.debug;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.embed.swing.JFXPanel;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.xmpp.ConnectionEvent;
import org.xmpp.ConnectionListener;
import org.xmpp.XmppSession;
import org.xmpp.XmppUtils;
import org.xmpp.stanza.PresenceEvent;
import org.xmpp.stanza.PresenceListener;
import org.xmpp.stanza.client.Presence;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Christian Schudt
 */
public final class VisualDebugger implements XmppDebugger {

    private static final Map<Tab, ConnectionListener> connectionListenerMap = new HashMap<>();

    private static Stage stage;

    private static TabPane tabPane;

    final StringProperty title = new SimpleStringProperty();

    private DebugController debugController;

    private volatile boolean platformInitialized;

    private ByteArrayOutputStream outputStreamIncoming;

    private ByteArrayOutputStream outputStreamOutgoing;

    @Override
    public void initialize(final XmppSession xmppSession) {
        final ConnectionListener connectionListener = new ConnectionListener() {
            @Override
            public void statusChanged(final ConnectionEvent e) {
                waitForPlatform();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (e.getStatus() == XmppSession.Status.CONNECTED && xmppSession.getUsedConnection() != null) {
                            debugController.viewModel.server.set(xmppSession.getUsedConnection().getHostname());
                            debugController.viewModel.port.set(xmppSession.getUsedConnection().getPort());
                            title.set(xmppSession.getDomain());
                        }
                        if (e.getStatus() == XmppSession.Status.AUTHENTICATED) {
                            title.set(xmppSession.getConnectedResource().toString());
                        } else {
                            debugController.viewModel.presence.set(null);
                        }
                        debugController.viewModel.status.set(e.getStatus());
                    }
                });
            }
        };
        xmppSession.addConnectionListener(connectionListener);

        final PresenceListener presenceListener = new PresenceListener() {
            @Override
            public void handle(PresenceEvent e) {
                if (!e.isIncoming()) {
                    final Presence presence = e.getPresence();
                    if (presence.getTo() == null) {
                        waitForPlatform();
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                debugController.viewModel.presence.set(presence);
                            }
                        });
                    }
                }
            }
        };
        xmppSession.addPresenceListener(presenceListener);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new JFXPanel(); // this will prepare JavaFX toolkit and environment
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Font.loadFont(getClass().getResource("Inconsolata.ttf").toExternalForm(), 12);
                            if (stage == null) {
                                tabPane = new TabPane();
                                Scene scene = new Scene(tabPane, 800, 600);
                                scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
                                stage = new Stage();
                                stage.setTitle("XMPP Viewer");
                                stage.getIcons().addAll(new Image(getClass().getResource("xmpp.png").toExternalForm()));
                                stage.setOnHidden(new EventHandler<WindowEvent>() {
                                    @Override
                                    public void handle(WindowEvent event) {
                                        for (Tab tab : tabPane.getTabs()) {
                                            xmppSession.removeConnectionListener(connectionListenerMap.remove(tab));
                                        }

                                        tabPane.getTabs().clear();
                                        stage = null;
                                        tabPane = null;
                                    }
                                });
                                stage.setScene(scene);
                            }

                            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("DebugView.fxml"));
                            TabPane debugView = fxmlLoader.load();
                            debugController = fxmlLoader.getController();
                            final Tab tab = new Tab(xmppSession.getDomain());
                            tab.setContent(debugView);
                            tab.textProperty().bind(title);
                            connectionListenerMap.put(tab, connectionListener);

                            final AnimationTimer animationTimer = new AnimationTimer() {
                                @Override
                                public void handle(long now) {
                                    if (outputStreamIncoming != null) {
                                        String incoming = outputStreamIncoming.toString();
                                        if (!incoming.isEmpty()) {
                                            debugController.appendTextIncoming(incoming);
                                            outputStreamIncoming.reset();
                                        }
                                    }
                                    if (outputStreamOutgoing != null) {
                                        String outgoing = outputStreamOutgoing.toString();
                                        if (!outgoing.isEmpty()) {
                                            debugController.appendTextOutgoing(outgoing);
                                            outputStreamOutgoing.reset();
                                        }
                                    }
                                }
                            };
                            animationTimer.start();

                            tab.setOnClosed(new EventHandler<Event>() {
                                @Override
                                public void handle(Event event) {
                                    xmppSession.removeConnectionListener(connectionListenerMap.remove(tab));
                                    xmppSession.removePresenceListener(presenceListener);
                                    animationTimer.stop();
                                }
                            });

                            tabPane.getTabs().add(tab);
                            stage.show();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        platformInitialized = true;
                        synchronized (VisualDebugger.this) {
                            VisualDebugger.this.notifyAll();
                        }
                    }
                });
            }
        });
    }

    private void waitForPlatform() {
        if (!platformInitialized) {
            synchronized (VisualDebugger.this) {
                try {
                    VisualDebugger.this.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Override
    public void writeStanza(final String xml, final Object stanza) {
        final String outgoing;
        if (outputStreamOutgoing != null) {
            outputStreamOutgoing.write((int) '\n');
            outgoing = outputStreamOutgoing.toString();
            outputStreamOutgoing.reset();
        } else {
            outgoing = "";
        }

        waitForPlatform();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                debugController.addStanza(new StanzaEntry(false, xml, stanza));
                if (!outgoing.isEmpty()) {
                    debugController.appendTextOutgoing(outgoing);
                }
            }
        });
    }

    @Override
    public void readStanza(final String xml, final Object stanza) {
        final String incoming;
        if (outputStreamIncoming != null) {
            outputStreamIncoming.write((int) '\n');
            incoming = outputStreamIncoming.toString();
            outputStreamIncoming.reset();
        } else {
            incoming = "";
        }

        waitForPlatform();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                debugController.addStanza(new StanzaEntry(true, xml, stanza));
                if (!incoming.isEmpty()) {
                    debugController.appendTextIncoming(incoming);
                }
            }
        });
    }

    @Override
    public OutputStream createOutputStream(OutputStream outputStream) {
        outputStreamOutgoing = new ByteArrayOutputStream();
        return XmppUtils.createBranchedOutputStream(outputStream, outputStreamOutgoing);
    }

    @Override
    public InputStream createInputStream(InputStream inputStream) {
        outputStreamIncoming = new ByteArrayOutputStream();
        return XmppUtils.createBranchedInputStream(inputStream, outputStreamIncoming);
    }
}
