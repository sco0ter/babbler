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

package rocks.xmpp.debug.gui;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import rocks.xmpp.core.session.SessionStatusEvent;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.session.debug.XmppDebugger;
import rocks.xmpp.core.stanza.PresenceEvent;
import rocks.xmpp.core.stanza.model.Presence;
import rocks.xmpp.util.XmppUtils;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * @author Christian Schudt
 */
public final class VisualDebugger implements XmppDebugger {

    static {
        initializeLogging();
    }

    private static final Map<Tab, Consumer<SessionStatusEvent>> CONNECTION_LISTENER_MAP = new HashMap<>();

    private static final Queue<LogRecord> LOG_RECORDS = new ArrayDeque<>();

    private static final Formatter FORMATTER = new LogFormatter();

    private static Stage stage;

    private static TabPane tabPane;

    private static TextArea textArea;

    private static SplitPane root;

    private static volatile boolean platformInitialized;

    final StringProperty title = new SimpleStringProperty();

    private DebugController debugController;

    private ByteArrayOutputStream outputStreamInbound;

    private ByteArrayOutputStream outputStreamOutbound;

    private static void initializeLogging() {

        Handler logHandler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                synchronized (LOG_RECORDS) {
                    int maxLogEntries = 500;
                    if (LOG_RECORDS.size() >= maxLogEntries) {
                        LOG_RECORDS.poll();
                    }
                    LOG_RECORDS.offer(record);
                }
                if (platformInitialized) {
                    Platform.runLater(VisualDebugger::updateTextArea);
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        };
        logHandler.setLevel(Level.FINE);

        final Logger logger = Logger.getLogger("rocks.xmpp");
        logger.addHandler(logHandler);
        logger.setLevel(Level.FINE);
    }

    private static void waitForPlatform() {
        if (!platformInitialized) {
            synchronized (VisualDebugger.class) {
                try {
                    VisualDebugger.class.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private static void updateTextArea() {
        synchronized (LOG_RECORDS) {
            textArea.clear();
            for (LogRecord logRecord : LOG_RECORDS) {
                textArea.appendText(FORMATTER.format(logRecord));
            }
        }
    }

    @Override
    public void initialize(final XmppSession xmppSession) {

        final Consumer<SessionStatusEvent> connectionListener = e -> {
            waitForPlatform();
            Platform.runLater(() -> {
                if (e.getStatus() == XmppSession.Status.CONNECTED && xmppSession.getActiveConnection() != null) {
                    debugController.viewModel.server.set(xmppSession.getActiveConnection().getHostname());
                    debugController.viewModel.port.set(xmppSession.getActiveConnection().getPort());
                    title.set(xmppSession.getDomain());
                }
                if (e.getStatus() == XmppSession.Status.AUTHENTICATED) {
                    title.set(xmppSession.getConnectedResource().toString());
                } else {
                    debugController.viewModel.presence.set(null);
                }
                debugController.viewModel.status.set(e.getStatus());
            });
        };
        xmppSession.addSessionStatusListener(connectionListener);

        final Consumer<PresenceEvent> presenceListener = e -> {
            final Presence presence = e.getPresence();
            if (presence.getTo() == null) {
                waitForPlatform();
                Platform.runLater(() -> debugController.viewModel.presence.set(presence));
            }
        };
        xmppSession.addOutboundPresenceListener(presenceListener);

        SwingUtilities.invokeLater(() -> {
            new JFXPanel(); // this will prepare JavaFX toolkit and environment
            Platform.runLater(() -> {

                try {
                    Font.loadFont(getClass().getResource("Inconsolata.ttf").toExternalForm(), 12);
                    if (stage == null) {
                        root = new SplitPane();
                        root.setDividerPositions(0.8);
                        root.setOrientation(Orientation.VERTICAL);
                        tabPane = new TabPane();
                        textArea = new TextArea();
                        textArea.setEditable(false);
                        root.getItems().addAll(tabPane, textArea);
                        updateTextArea();
                        Scene scene = new Scene(root, 800, 600);
                        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
                        stage = new Stage();
                        stage.setTitle("XMPP Viewer");
                        stage.getIcons().addAll(new Image(getClass().getResource("xmpp.png").toExternalForm()));
                        stage.setOnHidden(event -> {
                            for (Tab tab : tabPane.getTabs()) {
                                xmppSession.removeSessionStatusListener(CONNECTION_LISTENER_MAP.remove(tab));
                            }

                            tabPane.getTabs().clear();
                            stage = null;
                            tabPane = null;
                        });
                        stage.setScene(scene);
                    }

                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("DebugView.fxml"));
                    TabPane debugView = fxmlLoader.load();
                    debugController = fxmlLoader.getController();
                    final Tab tab = new Tab(xmppSession.getDomain());
                    tab.setContent(debugView);
                    tab.textProperty().bind(title);
                    CONNECTION_LISTENER_MAP.put(tab, connectionListener);

                    final AnimationTimer animationTimer = new AnimationTimer() {
                        @Override
                        public void handle(long now) {
                            if (outputStreamInbound != null) {
                                String inbound = outputStreamInbound.toString();
                                if (!inbound.isEmpty()) {
                                    debugController.appendTextInbound(inbound);
                                    outputStreamInbound.reset();
                                }
                            }
                            if (outputStreamOutbound != null) {
                                String outbound = outputStreamOutbound.toString();
                                if (!outbound.isEmpty()) {
                                    debugController.appendTextOutbound(outbound);
                                    outputStreamOutbound.reset();
                                }
                            }
                        }
                    };
                    animationTimer.start();

                    tab.setOnClosed(event -> {
                        xmppSession.removeSessionStatusListener(CONNECTION_LISTENER_MAP.remove(tab));
                        xmppSession.removeOutboundPresenceListener(presenceListener);
                        animationTimer.stop();
                    });

                    tabPane.getTabs().add(tab);
                    stage.show();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                platformInitialized = true;
                synchronized (VisualDebugger.class) {
                    VisualDebugger.class.notifyAll();
                }
            });
        });
    }

    @Override
    public void writeStanza(final String xml, final Object stanza) {
        final String outbound;
        if (outputStreamOutbound != null) {
            outputStreamOutbound.write((int) '\n');
            outbound = outputStreamOutbound.toString();
            outputStreamOutbound.reset();
        } else {
            outbound = "";
        }

        waitForPlatform();
        Platform.runLater(() -> {
            debugController.addStanza(new StanzaEntry(false, xml, stanza));
            if (!outbound.isEmpty()) {
                debugController.appendTextOutbound(outbound);
            }
        });
    }

    @Override
    public void readStanza(final String xml, final Object stanza) {
        final String inbound;
        if (outputStreamInbound != null) {
            outputStreamInbound.write((int) '\n');
            inbound = outputStreamInbound.toString();
            outputStreamInbound.reset();
        } else {
            inbound = "";
        }

        waitForPlatform();
        Platform.runLater(() -> {
            debugController.addStanza(new StanzaEntry(true, xml, stanza));
            if (!inbound.isEmpty()) {
                debugController.appendTextInbound(inbound);
            }
        });
    }

    @Override
    public OutputStream createOutputStream(OutputStream outputStream) {
        outputStreamOutbound = new ByteArrayOutputStream();
        return XmppUtils.createBranchedOutputStream(outputStream, outputStreamOutbound);
    }

    @Override
    public InputStream createInputStream(InputStream inputStream) {
        outputStreamInbound = new ByteArrayOutputStream();
        return XmppUtils.createBranchedInputStream(inputStream, outputStreamInbound);
    }
}
