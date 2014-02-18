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

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.Product;
import org.xmpp.*;
import org.xmpp.extension.avatar.Avatar;
import org.xmpp.extension.avatar.AvatarChangeEvent;
import org.xmpp.extension.avatar.AvatarChangeListener;
import org.xmpp.extension.avatar.AvatarManager;
import org.xmpp.extension.caps.EntityCapabilitiesManager;
import org.xmpp.extension.disco.ServiceDiscoveryManager;
import org.xmpp.extension.disco.info.InfoNode;
import org.xmpp.extension.disco.items.ItemNode;
import org.xmpp.extension.httpbind.BoshConnection;
import org.xmpp.extension.last.LastActivityManager;
import org.xmpp.extension.last.LastActivityStrategy;
import org.xmpp.extension.ping.PingManager;
import org.xmpp.extension.privatedata.PrivateDataManager;
import org.xmpp.extension.privatedata.annotations.Annotation;
import org.xmpp.extension.pubsub.PubSubManager;
import org.xmpp.extension.receipts.MessageDeliveredEvent;
import org.xmpp.extension.receipts.MessageDeliveredListener;
import org.xmpp.extension.receipts.MessageDeliveryReceiptsManager;
import org.xmpp.extension.search.Search;
import org.xmpp.extension.search.SearchManager;
import org.xmpp.extension.shim.HeaderManager;
import org.xmpp.extension.stream.si.filetransfer.FileTransferEvent;
import org.xmpp.extension.stream.si.filetransfer.FileTransferListener;
import org.xmpp.extension.stream.si.filetransfer.FileTransferManager;
import org.xmpp.extension.time.EntityTime;
import org.xmpp.extension.time.EntityTimeManager;
import org.xmpp.extension.tune.Tune;
import org.xmpp.extension.vcard.VCard;
import org.xmpp.extension.vcard.VCardManager;
import org.xmpp.extension.version.SoftwareVersion;
import org.xmpp.extension.version.SoftwareVersionManager;
import org.xmpp.im.*;
import org.xmpp.stanza.*;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.login.LoginException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.logging.*;

/**
 * @author Christian Schudt
 */
public class JavaFXApp extends Application {

    private Connection connection;

    private Map<Jid, ChatWindow> windows = new HashMap<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage stage) throws Exception {
        stage.setTitle("New");
        VBox vBox = new VBox(10);
        vBox.setPadding(new Insets(20, 20, 20, 20));
        final TextField txtDomain = new TextField();
        txtDomain.setPromptText("XMPP domain");
        final TextField txtServer = new TextField();
        txtServer.setPromptText("Server");
        final TextField txtPort = new TextField();
        txtPort.setPromptText("Port");
        txtPort.setText("5222");
        final TextField txtUser = new TextField();
        txtUser.setPromptText("Username");
        final TextField txtPassword = new PasswordField();
        txtPassword.setPromptText("Password");
        final CheckBox useBosh = new CheckBox();
        useBosh.setText("Use BOSH");

        for (int i = 0; i < 1; i++) {
            Connection connection1 = new TestConnection();
            connection1.getExtensionManager(LastActivityManager.class).setLastActivityStrategy(new LastActivityStrategy() {
                @Override
                public Date getLastActivity() {
                    return null;
                }
            });
            connection1.close();
        }

        ComboBox<Presence.Show> comboBox = new ComboBox<>();
        comboBox.setItems(FXCollections.<Presence.Show>observableList(Arrays.asList(Presence.Show.CHAT, Presence.Show.AWAY, Presence.Show.XA, Presence.Show.DND)));
        comboBox.setCellFactory(new Callback<ListView<Presence.Show>, ListCell<Presence.Show>>() {
            @Override
            public ListCell<Presence.Show> call(ListView<Presence.Show> presenceListView) {
                return new ListCell<Presence.Show>() {
                    @Override
                    protected void updateItem(Presence.Show item, boolean isEmpty) {
                        super.updateItem(item, isEmpty);
                        if (item != null) {
                            setText(item.toString().toLowerCase());
                        }
                    }
                };
            }
        });
        comboBox.setButtonCell(comboBox.getCellFactory().call(null));
        comboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Presence.Show>() {
            @Override
            public void changed(ObservableValue<? extends Presence.Show> observableValue, Presence.Show show, Presence.Show show2) {
                connection.send(new Presence(show2));
            }
        });

        final Map<Roster.Contact, ContactItem> contactMap = new HashMap<>();

        final ObservableList<ContactItem> contactItems = FXCollections.observableArrayList();
        LogManager.getLogManager().reset();
        Logger globalLogger = Logger.getLogger("");

        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.FINE);
        consoleHandler.setFormatter(new LogFormatter());
        globalLogger.addHandler(consoleHandler);

        final Logger logger = Logger.getLogger("org.xmpp");
        //logger.addHandler(consoleHandler);
        logger.setLevel(Level.FINE);

        //LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(Level.OFF);

        Button btnConnect = new Button("Login");
        btnConnect.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                new Thread() {
                    @Override
                    public void run() {

                        if (!useBosh.isSelected()) {

                            XmppContext.getDefault().registerExtension(Product.class);

                            connection = new TcpConnection(txtDomain.getText(), txtServer.getText(), Integer.parseInt(txtPort.getText()));
                        } else {
                            connection = new BoshConnection(txtDomain.getText(), txtServer.getText(), Integer.parseInt(txtPort.getText()));
                        }
                        try {
                            SSLContext sslContext = SSLContext.getInstance("TLS");
                            sslContext.init(null, new TrustManager[]{
                                    new X509TrustManager() {
                                        @Override
                                        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                                        }

                                        @Override
                                        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                                        }

                                        @Override
                                        public X509Certificate[] getAcceptedIssuers() {
                                            return new X509Certificate[0];
                                        }
                                    }
                            }, new SecureRandom());
                            connection.getSecurityManager().setSSLContext(sslContext);
                        } catch (Exception e) {
                            logger.log(Level.SEVERE, e.getMessage(), e);
                        }
                        connection.addConnectionListener(new ConnectionListener() {
                            @Override
                            public void statusChanged(ConnectionEvent e) {
                                logger.info(e.getStatus().toString());
                            }
                        });
                        connection.getExtensionManager(HeaderManager.class).setEnabled(true);
                        connection.getChatManager().addChatSessionListener(new ChatSessionListener() {
                            @Override
                            public void chatSessionCreated(final ChatSessionEvent chatSessionEvent) {
                                final ChatSession chatSession = chatSessionEvent.getChatSession();

                                chatSession.addMessageListener(new MessageListener() {
                                    @Override
                                    public void handle(final MessageEvent e) {
                                        Platform.runLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                Jid chatPartner = chatSession.getChatPartner().toBareJid();
                                                ChatWindow chatWindow = windows.get(chatPartner);
                                                if (chatWindow == null) {
                                                    chatWindow = new ChatWindow(chatPartner, connection);
                                                    windows.put(chatPartner, chatWindow);
                                                }

                                                chatWindow.chatSession = chatSession;
                                                chatWindow.show();
                                                chatWindow.appendMessage(e.getMessage());
                                            }
                                        });
                                    }
                                });
                            }
                        });
                        connection.addMessageListener(new MessageListener() {
                            @Override
                            public void handle(MessageEvent e) {

                            }
                        });
                        connection.getRosterManager().addRosterListener(new RosterListener() {
                            @Override
                            public void rosterChanged(final RosterEvent e) {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (Roster.Contact contact : e.getAddedContacts()) {
                                            ContactItem contactItem1 = new ContactItem(contact);
                                            contactItems.add(contactItem1);
                                            contactMap.put(contact, contactItem1);
                                        }

                                        for (Roster.Contact contact : e.getUpdatedContacts()) {
                                            ContactItem contactItem1 = contactMap.get(contact);
                                            contactItem1.contact.set(contact);
                                        }

                                        for (Roster.Contact contact : e.getRemovedContacts()) {
                                            contactItems.remove(contactMap.remove(contact));
                                        }
                                    }
                                });
                            }
                        });

                        connection.addPresenceListener(new PresenceListener() {
                            @Override
                            public void handle(final PresenceEvent e) {
                                if (e.isIncoming()) {
                                    if (e.getPresence().isAvailable()) {
                                        Platform.runLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                Presence presence = e.getPresence();
                                                Roster.Contact contact = connection.getRosterManager().getContact(presence.getFrom());
                                                if (contact != null) {
                                                    ContactItem contactItem1 = contactMap.get(contact);
                                                    contactItem1.presence.set(presence);
                                                    FXCollections.sort(contactItems);
                                                }
                                            }
                                        });
                                    } else if (e.getPresence().getType() == Presence.Type.SUBSCRIBE) {
                                        connection.getPresenceManager().denySubscription(e.getPresence().getFrom());
                                    }
                                }
                            }
                        });

                        FileTransferManager fileTransferManager = connection.getExtensionManager(FileTransferManager.class);
                        fileTransferManager.addFileTransferListener(new FileTransferListener() {
                            @Override
                            public void fileTransferRequest(FileTransferEvent e) {
                                e.accept();
                            }
                        });

                        AvatarManager avatarManager = connection.getExtensionManager(AvatarManager.class);
                        avatarManager.addAvatarChangeListener(new AvatarChangeListener() {
                            @Override
                            public void avatarChanged(final AvatarChangeEvent e) {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {

                                        Roster.Contact contact = connection.getRosterManager().getContact(e.getContact());
                                        if (contact != null) {
                                            ContactItem contactItem = contactMap.get(contact);
                                            if (contactItem != null) {
                                                contactItem.avatar.set(e.getAvatar().getImageData());
                                            }
                                        }
                                    }
                                });
                            }
                        });

                        MessageDeliveryReceiptsManager messageDeliveryReceiptsManager = connection.getExtensionManager(MessageDeliveryReceiptsManager.class);
                        messageDeliveryReceiptsManager.addMessageDeliveredListener(new MessageDeliveredListener() {
                            @Override
                            public void messageDelivered(MessageDeliveredEvent e) {
                                System.out.println("Message delivered: " + e.getMessageId());
                            }
                        });

                        connection.getExtensionManager(EntityCapabilitiesManager.class).setEnabled(true);

                        SoftwareVersionManager softwareVersionManager = connection.getExtensionManager(SoftwareVersionManager.class);
                        softwareVersionManager.setSoftwareVersion(new SoftwareVersion("Babbler", "0.1"));
                        try {
                            connection.connect();
                            connection.login(txtUser.getText(), txtPassword.getText(), "test");
                            connection.send(new Presence());
                            connection.getRosterManager().requestRoster();
                        } catch (TimeoutException | LoginException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });


        ListView<ContactItem> listView = new ListView<>();

        listView.setCellFactory(new Callback<ListView<ContactItem>, ListCell<ContactItem>>() {
            @Override
            public ListCell<ContactItem> call(ListView<ContactItem> contactListView) {
                final ListCell<ContactItem> listCell = new ListCell<ContactItem>() {
                    @Override
                    protected void updateItem(final ContactItem item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(null);
                        setContextMenu(null);

                        if (item != null) {
                            final Jid user = item.contact.get().getJid();
                            if (item.avatar.get() == null) {
                                final Task<Avatar> task = new Task<Avatar>() {
                                    @Override
                                    protected Avatar call() throws Exception {
                                        AvatarManager avatarManager = connection.getExtensionManager(AvatarManager.class);
                                        try {
                                            return avatarManager.getAvatar(user);
                                        } catch (TimeoutException e) {
                                            e.printStackTrace();
                                        }
                                        return null;
                                    }
                                };
                                task.stateProperty().addListener(new ChangeListener<Worker.State>() {
                                    @Override
                                    public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State state, Worker.State state2) {
                                        switch (state2) {
                                            case SUCCEEDED:
                                                Avatar avatar = task.getValue();
                                                if (avatar != null) {
                                                    item.avatar.set(avatar.getImageData());
                                                }
                                        }
                                    }
                                });
                                new Thread(task).start();
                            }

                            setGraphic(new ContactItemView(item));
                            ContextMenu contextMenu = new ContextMenu();
                            MenuItem lastActivityMenuItem = new MenuItem("Get last activity");
                            lastActivityMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent actionEvent) {
                                    LastActivityManager lastActivityManager = connection.getExtensionManager(LastActivityManager.class);
                                    try {
                                        lastActivityManager.getLastActivity(item.contact.get().getJid());
                                    } catch (StanzaException e) {
                                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                    } catch (TimeoutException e) {
                                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                    }
                                }
                            });
                            MenuItem pingMenuItem = new MenuItem("Ping");
                            pingMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent actionEvent) {
                                    PingManager pingManager = connection.getExtensionManager(PingManager.class);
                                    try {
                                        pingManager.pingServer();
                                    } catch (TimeoutException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            MenuItem searchMenuItem = new MenuItem("Search");
                            searchMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent actionEvent) {
                                    SearchManager searchManager = connection.getExtensionManager(SearchManager.class);
                                    try {
                                        Search search = new Search();
                                        search.setFirst("22*");
                                        searchManager.discoverSearchFields(new Jid("search.dev"));
                                        Search result = searchManager.search(search, new Jid("search.dev"));
                                        for (Search.Item item : result.getItems()) {
                                            System.out.println(item.getJid());
                                        }
                                    } catch (TimeoutException | StanzaException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            MenuItem softwareVersionItem = new MenuItem("Get Software Version");
                            softwareVersionItem.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent actionEvent) {
                                    SoftwareVersionManager softwareVersionManager = connection.getExtensionManager(SoftwareVersionManager.class);
                                    try {
                                        SoftwareVersion softwareVersion = softwareVersionManager.getSoftwareVersion(item.contact.get().getJid());
                                        if (softwareVersion != null)
                                            System.out.println(softwareVersion.getName());
                                    } catch (TimeoutException | StanzaException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            MenuItem serviceDiscoveryMenuItem = new MenuItem("Discover Info");
                            serviceDiscoveryMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent actionEvent) {
                                    ServiceDiscoveryManager serviceDiscoveryManager = connection.getExtensionManager(ServiceDiscoveryManager.class);
                                    try {
                                        Jid jid = new Jid(item.contact.get().getJid().getLocal(), item.contact.get().getJid().getDomain());
                                        InfoNode infoNode = serviceDiscoveryManager.discoverInformation(null);
                                        int i = 0;
                                    } catch (TimeoutException | StanzaException e) {
                                        e.printStackTrace();
                                    }

                                }
                            });
                            MenuItem vCardItem = new MenuItem("Get VCard");
                            vCardItem.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent actionEvent) {
                                    VCardManager vCardManager = connection.getExtensionManager(VCardManager.class);
                                    try {
                                        Jid jid = new Jid(item.contact.get().getJid().getLocal(), item.contact.get().getJid().getDomain());
                                        VCard vCard = vCardManager.getVCard(jid);
                                        int i = 0;
                                    } catch (TimeoutException | StanzaException e) {
                                        e.printStackTrace();
                                    }

                                }
                            });
                            MenuItem storeAnnotationsItems = new MenuItem("Store annotations");
                            storeAnnotationsItems.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent actionEvent) {
                                    PrivateDataManager privateDataManager = connection.getExtensionManager(PrivateDataManager.class);
                                    try {
                                        List<Annotation.Note> notes = new ArrayList<>();
                                        notes.add(new Annotation.Note("Hallo", item.contact.get().getJid()));
                                        privateDataManager.storeData(new Annotation(notes));
                                    } catch (TimeoutException | StanzaException e) {
                                        e.printStackTrace();
                                    }

                                }
                            });
                            MenuItem getAnnotationsItems = new MenuItem("Get annotations");
                            getAnnotationsItems.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent actionEvent) {
                                    PrivateDataManager privateDataManager = connection.getExtensionManager(PrivateDataManager.class);
                                    try {
                                        List<Annotation> annotations = privateDataManager.getData(Annotation.class);
                                        int i = 0;
                                    } catch (TimeoutException | StanzaException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            MenuItem pubSubItem = new MenuItem("PubSub");
                            pubSubItem.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent actionEvent) {
                                    PubSubManager pubSubManager = connection.getExtensionManager(PubSubManager.class);
                                    ServiceDiscoveryManager serviceDiscoveryManager = connection.getExtensionManager(ServiceDiscoveryManager.class);
                                    try {
                                        ItemNode infoNode = serviceDiscoveryManager.discoverItems(null);
                                        int i = 0;
                                    } catch (TimeoutException | StanzaException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            MenuItem pepItem = new MenuItem("PEP");
                            pepItem.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent actionEvent) {
                                    PubSubManager pubSubManager = connection.getExtensionManager(PubSubManager.class);
                                    try {
                                        pubSubManager.publish("http://jabber.org/protocol/tune", new Tune("AC/DC"));
                                    } catch (TimeoutException | StanzaException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            MenuItem sendFile = new MenuItem("Send file");
                            sendFile.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent actionEvent) {
                                    FileTransferManager fileTransferManager = connection.getExtensionManager(FileTransferManager.class);
                                    FileChooser fileChooser = new FileChooser();
                                    File file = fileChooser.showOpenDialog(stage);

                                    try {
                                        fileTransferManager.sendFile(file, connection.getPresenceManager().getPresence(item.contact.get().getJid()).getFrom(), 10000);
                                    } catch (TimeoutException | StanzaException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                            MenuItem timeItem = new MenuItem("Get time");
                            timeItem.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent actionEvent) {
                                    EntityTimeManager entityTimeManager = connection.getExtensionManager(EntityTimeManager.class);

                                    try {
                                        EntityTime entityTime = entityTimeManager.getEntityTime(null);
                                    } catch (TimeoutException | StanzaException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                            contextMenu.getItems().addAll(lastActivityMenuItem, pingMenuItem, searchMenuItem, softwareVersionItem, serviceDiscoveryMenuItem, vCardItem, storeAnnotationsItems, getAnnotationsItems, pubSubItem, pepItem, sendFile, timeItem);
                            setContextMenu(contextMenu);
                        }
                    }
                };
                listCell.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        if (mouseEvent.getClickCount() == 2 && listCell.getItem() != null) {
                            Jid chatPartner = listCell.getItem().contact.get().getJid().toBareJid();
                            ChatWindow chatWindow = windows.get(chatPartner);
                            if (chatWindow == null) {
                                chatWindow = new ChatWindow(chatPartner, connection);
                                windows.put(chatPartner, chatWindow);
                            }
                            chatWindow.show();

                        }
                    }
                });


                return listCell;
            }
        });

        listView.setItems(contactItems);

        Button btnClose = new Button("Close");
        btnClose.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    connection.close();
                    contactItems.clear();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        Button btn = new Button("Misc");
        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                PresenceManager presenceManager = connection.getPresenceManager();
                presenceManager.requestSubscription(Jid.fromString("222@christian-schudts-macbook-pro.fritz.box"), null);
                Message message = new Message(null, Message.Type.CHAT);
                message.setBody("Test");
                connection.send(message);

            }
        });
        Button btnExit = new Button("Exit");
        btnExit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                System.exit(0);
            }
        });
        vBox.getChildren().addAll(txtDomain, txtServer, txtPort, txtUser, txtPassword, useBosh, btnConnect, comboBox, listView, btnClose, btnExit, btn);

        Scene scene = new Scene(vBox);
        stage.setScene(scene);
        stage.show();
    }

    public static class ChatWindow extends Stage {
        private TextArea messages;

        private ChatSession chatSession;

        public ChatWindow(final Jid chatPartner, final Connection connection) {
            messages = new TextArea();
            messages.setDisable(true);
            final TextArea textArea = new TextArea();
            Button btnSend = new Button("Send");
            btnSend.setDefaultButton(true);
            btnSend.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    if (chatSession == null) {
                        chatSession = connection.getChatManager().newChatSession(chatPartner);
                    }
                    Message message = new Message(chatSession.getChatPartner(), Message.Type.CHAT, textArea.getText());
                    message.setId(UUID.randomUUID().toString());
                    chatSession.send(message);
                    textArea.clear();
                }
            });
            VBox chatBox = new VBox(10);
            chatBox.setPadding(new Insets(10, 10, 10, 10));
            chatBox.getChildren().addAll(messages, textArea, btnSend);
            Scene scene = new Scene(chatBox);
            setScene(scene);
        }

        public void appendMessage(Message message) {
            messages.appendText(message.getBody() + "\n");
        }
    }

    public static class ContactItemView extends HBox {

        private ImageView imageView = new ImageView();

        private Label lblName = new Label();

        private Circle circle = new Circle(8);

        public ContactItemView(final ContactItem contactItem) {
            setSpacing(10);
            setPadding(new Insets(5, 5, 5, 5));
            imageView.imageProperty().bind(new ObjectBinding<Image>() {
                {
                    super.bind(contactItem.avatar);
                }

                @Override
                protected Image computeValue() {
                    if (contactItem.avatar.get() != null) {
                        try {
                            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(contactItem.avatar.get()));
                            return SwingFXUtils.toFXImage(bufferedImage, null);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return null;
                }
            });
            lblName.textProperty().bind(new StringBinding() {
                {
                    super.bind(contactItem.contact);
                }

                @Override
                protected String computeValue() {
                    return contactItem.contact.get().getName();
                }
            });
            circle.fillProperty().bind(new ObjectBinding<Paint>() {
                {
                    super.bind(contactItem.presence);
                }

                @Override
                protected Paint computeValue() {
                    Presence presence = contactItem.presence.get();
                    if (presence != null && presence.isAvailable()) {
                        return new Color(0.52, 0.84, 0.27, 1);
                    }
                    return Color.LIGHTGREY;
                }
            });
            getChildren().add(imageView);
            getChildren().add(circle);
            getChildren().add(lblName);
        }
    }

    public static final class ContactItem implements Comparable<ContactItem> {
        private final ObjectProperty<Roster.Contact> contact;

        private final ObjectProperty<Presence> presence;

        private final ObjectProperty<byte[]> avatar;

        public ContactItem(Roster.Contact contact) {
            this.contact = new SimpleObjectProperty<>(contact);
            this.presence = new SimpleObjectProperty<>();
            this.avatar = new SimpleObjectProperty<>();
        }

        @Override
        public int compareTo(ContactItem o) {
            if (presence.get() != null && presence.get().getType() != Presence.Type.UNAVAILABLE && o.presence.get() == null) {
                return -1;
            }
            if (presence.get() == null && o.presence.get() != null && o.presence.get().getType() != Presence.Type.UNAVAILABLE) {
                return 1;
            }
            int result = 0;
            if (presence.get() != null && o.presence.get() != null) {
                result = presence.get().compareTo(o.presence.get());
            }
            if (result == 0) {
                result = contact.get().compareTo(o.contact.get());
            }
            return result;
        }
    }
}
