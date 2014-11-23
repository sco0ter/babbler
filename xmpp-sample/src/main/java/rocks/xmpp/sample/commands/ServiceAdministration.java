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

package rocks.xmpp.sample.commands;

import javafx.application.Application;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.TcpConnectionConfiguration;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.session.XmppSessionConfiguration;
import rocks.xmpp.debug.gui.VisualDebugger;
import rocks.xmpp.extensions.commands.AdHocCommand;
import rocks.xmpp.extensions.commands.AdHocCommandsManager;
import rocks.xmpp.extensions.commands.CommandSession;
import rocks.xmpp.extensions.commands.model.Command;
import rocks.xmpp.extensions.data.model.DataForm;

import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Christian Schudt
 */
public class ServiceAdministration extends Application {

    private final ObservableList<AdHocCommand> commands = FXCollections.observableArrayList();

    private XmppSession xmppSession;

    @Override
    public void start(Stage primaryStage) throws Exception {

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        final Task<Collection<AdHocCommand>> task = new Task<Collection<AdHocCommand>>() {
            @Override
            protected Collection<AdHocCommand> call() throws Exception {
                TcpConnectionConfiguration connectionConfiguration = TcpConnectionConfiguration.builder()
                        .secure(false)
                        .build();
                XmppSessionConfiguration sessionConfiguration = XmppSessionConfiguration.builder()
                        .debugger(VisualDebugger.class)
                        .build();

                xmppSession = new XmppSession("localhost", sessionConfiguration, connectionConfiguration);
                xmppSession.connect();
                xmppSession.login("admin", "admin");
                AdHocCommandsManager adHocCommandsManager = xmppSession.getExtensionManager(AdHocCommandsManager.class);
                return adHocCommandsManager.getCommands();
            }
        };
        task.stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
                switch (newValue) {
                    case SUCCEEDED:
                        commands.addAll(task.getValue());
                        break;
                    case FAILED:
                        task.getException().printStackTrace();
                        break;
                }
            }
        });
        executorService.execute(task);

        final ComboBox<AdHocCommand> comboBox = new ComboBox<>();
        SortedList<AdHocCommand> sortedList = new SortedList<>(commands);
        sortedList.setComparator(new Comparator<AdHocCommand>() {
            @Override
            public int compare(AdHocCommand o1, AdHocCommand o2) {
                if (o1 != null) {
                    if (o2 != null) {
                        if (o1.getName() != null) {
                            return o1.getName().compareTo(o2.getName());
                        } else {
                            return -1;
                        }
                    } else {
                        return -1;
                    }
                }
                return 1;
            }
        });
        comboBox.setItems(sortedList);
        comboBox.setCellFactory(new Callback<ListView<AdHocCommand>, ListCell<AdHocCommand>>() {
            @Override
            public ListCell<AdHocCommand> call(ListView<AdHocCommand> param) {
                return new ListCell<AdHocCommand>() {
                    @Override
                    protected void updateItem(AdHocCommand item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setText(item.getName());
                        }
                    }
                };
            }
        });
        comboBox.setButtonCell(comboBox.getCellFactory().call(null));

        final VBox formContainer = new VBox();


        final Button btnExecute = new Button();
        btnExecute.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                AdHocCommand adHocCommand = comboBox.getValue();
                try {
                    CommandSession commandSession = adHocCommand.execute();
                    Command command = commandSession.getCurrentCommand();
                    Object payload = command.getPayload();
                    formContainer.getChildren().clear();
                    if (payload instanceof DataForm) {
                        DataForm dataForm = (DataForm) payload;
                        for (DataForm.Field field : dataForm.getFields()) {
                            VBox formField = new VBox(5);
                            Control control;
                            if (field.getType() != DataForm.Field.Type.HIDDEN) {
                                if (field.getType() != null) {
                                    switch (field.getType()) {
                                        case TEXT_SINGLE:
                                            control = new TextField();
                                            break;
                                        case TEXT_PRIVATE:
                                            control = new PasswordField();
                                            break;
                                        default:
                                            control = new TextField();
                                            break;
                                    }
                                } else {
                                    control = new TextField();
                                }
                                formField.getChildren().add(control);
                                if (field.getLabel() != null) {
                                    formField.getChildren().add(new Label(field.getLabel()));
                                }
                                formContainer.getChildren().add(formField);
                            }
                        }
                    }
                } catch (XmppException e) {
                    e.printStackTrace();
                }
            }
        });
        btnExecute.disableProperty().bind(new BooleanBinding() {
            {
                super.bind(comboBox.valueProperty());
            }

            @Override
            protected boolean computeValue() {
                return comboBox.getValue() == null;
            }
        });

        Scene scene = new Scene(new VBox(comboBox, btnExecute, formContainer));
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
